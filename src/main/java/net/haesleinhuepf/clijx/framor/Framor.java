package net.haesleinhuepf.clijx.framor;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.NewImage;
import ij.plugin.HyperStackConverter;
import net.haesleinhuepf.clij.CLIJ;
import net.haesleinhuepf.clij2.AbstractCLIJ2Plugin;
import net.haesleinhuepf.clij2.CLIJ2;
import net.haesleinhuepf.clijx.framor.implementations.GaussianBlurFrameProcessor;
import org.apache.commons.math3.distribution.HypergeometricDistribution;
import org.jocl.CL;

import java.util.ArrayList;
import java.util.HashMap;

public class Framor {
    private ImagePlus input;
    private FrameProcessor frameProcessor;
    CLIJ2[] clij2s = null;
    int max_num_clijs_per_device = 2;
    public static boolean auto_contrast = true;

    public static boolean multi_gpu_support = false;

    public Framor(ImagePlus input, FrameProcessor frameProcessor) {
        this.input = input;
        this.frameProcessor = frameProcessor;

        if (multi_gpu_support) {
            ArrayList names = CLIJ.getAvailableDeviceNames();
            long memoryNeed = frameProcessor.getMemoryNeedInBytes(input);
            ArrayList<CLIJ2> clij2list = new ArrayList<>();
            for (int i = 0; i < names.size(); i++) {
                long availableMemory = new CLIJ(i).getGPUMemoryInBytes();
                int num_clijs_per_device = 0;
                while (availableMemory > memoryNeed && num_clijs_per_device < max_num_clijs_per_device) {
                    clij2list.add(new CLIJ2(new CLIJ(i)));
                    availableMemory -= memoryNeed;
                    num_clijs_per_device++;
                }
            }

            clij2s = new CLIJ2[clij2list.size()];
            clij2list.toArray(clij2s);

            if (clij2s.length == 0) {
                throw new IllegalArgumentException("No GPU found with enough memory (> " + memoryNeed + " bytes).");
            }
            System.out.println("Available GPUs with enough memory:");

        } else {
            clij2s = new CLIJ2[1];
            clij2s[0] = CLIJ2.getInstance();
        }
        for (int i = 0; i < clij2s.length; i++) {
            System.out.println(" * " + clij2s[i].getGPUName());
        }
    }

    private synchronized ImagePlus extractFrame(ImagePlus imp, int frame_channel) {
        int frame = frame_channel % input.getNFrames();
        int channel = frame_channel / input.getNFrames();

        ImageStack stack = new ImageStack();
        imp.setC(channel + 1);
        imp.setT(frame + 1);
        for (int z = 0; z < imp.getNSlices(); z++ ){
            imp.setZ(z + 1);
            stack.addSlice(imp.getProcessor());
        }
        ImagePlus extracted = new ImagePlus("Stack", stack);
        extracted.setCalibration(imp.getCalibration());
        return extracted;
    }

    public ImagePlus getResult() {
        int framesProcessed = 0;
        ArrayList<ExecutorOnFrame> executors = new ArrayList<>();
        HashMap<Integer, ImagePlus> processedFrames = new HashMap<>();
        for (int i = 0; i < input.getNFrames() * input.getNChannels() && i < clij2s.length; i++) {
            FrameProcessor newProcessor = frameProcessor.duplicate();
            newProcessor.setCLIJ2(clij2s[i]);
            ExecutorOnFrame executor = new ExecutorOnFrame(extractFrame(input, i), i, newProcessor);
            new Thread(executor).start();
            executors.add(executor);
            framesProcessed++;
        }

        while(processedFrames.size() < input.getNFrames() * input.getNChannels() ) {
            for (int i = 0; i < executors.size(); i++) {
                ExecutorOnFrame executor = executors.get(i);
                if (executor != null) {
                    ImagePlus result = executor.getOutput();
                    if (result != null) {
                        processedFrames.put(executor.getFrame(), result);
                        if (framesProcessed < input.getNFrames() * input.getNChannels()) {
                            executor = new ExecutorOnFrame(extractFrame(input, framesProcessed), framesProcessed, executor.getProcessor());
                            new Thread(executor).start();
                            executors.set(i, executor);
                            framesProcessed++;
                        } else {
                            executors.set(i, null);
                        }
                    }
                }
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
        int nSlices = processedFrames.get(0).getNSlices();

        ImageStack result = new ImageStack();
        for (int f = 0; f < input.getNFrames(); f++) {
            for (int z = 0; z < nSlices; z++) {
                for (int c = 0; c < input.getNChannels(); c++) {
                    ImagePlus frame_imp = processedFrames.get(f + c * input.getNFrames());

                    frame_imp.setZ(z + 1);
                    result.addSlice(frame_imp.getProcessor());
                }
            }
        }
        ImagePlus output = new ImagePlus(frameProcessor.getClass().getSimpleName() + "_" + input.getTitle(), result);
        if (output.getNSlices() > 1) {
            output = HyperStackConverter.toHyperStack(output, input.getNChannels(), output.getNSlices() / input.getNFrames() / input.getNChannels(), input.getNFrames());
        }

        if (auto_contrast) {
            IJ.run(output, "Enhance Contrast", "saturated=0.35");
        }
        return output;
    }

    public static void main(String... args) {
        new ImageJ();

        ImagePlus input = IJ.openImage("src/test/resources/stack.tif");
                //NewImage.createFloatImage("temp", 100, 100, 100, NewImage.FILL_RANDOM);
        //input = HyperStackConverter.toHyperStack(input, 1, 10, 10);

        FrameProcessor frameProcessor = new GaussianBlurFrameProcessor(0f, 0f, 0f);

        ImagePlus result = new Framor(input, frameProcessor).getResult();
        result.show();
    }
}
