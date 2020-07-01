package net.haesleinhuepf.clijx.framor;

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

import java.util.ArrayList;
import java.util.HashMap;

public class Framor {
    private ImagePlus input;
    private FrameProcessor frameProcessor;
    CLIJ2[] clij2s = null;
    int numClijsPerDevice = 2;

    public Framor(ImagePlus input, FrameProcessor frameProcessor) {
        this.input = input;
        this.frameProcessor = frameProcessor;

        ArrayList names = CLIJ.getAvailableDeviceNames();
        clij2s = new CLIJ2[names.size() * numClijsPerDevice];
        for (int i = 0; i < names.size(); i++) {
            for (int j = 0; j < numClijsPerDevice; j++) {
                clij2s[i * numClijsPerDevice + j] = new CLIJ2(new CLIJ(i));
            }
        }
     }

    private synchronized ImagePlus extractFrame(ImagePlus imp, int frame_channel) {
        int frame = frame_channel / input.getNChannels();
        int channel = frame_channel % input.getNChannels();

        ImageStack stack = new ImageStack();
        imp.setC(channel + 1);
        imp.setT(frame + 1);
        for (int z = 0; z < imp.getNSlices(); z++ ){
            imp.setZ(z + 1);
            stack.addSlice(imp.getProcessor());
        }
        return new ImagePlus("Stack", stack);
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

        ImageStack result = new ImageStack();
        for (int c = 0; c < input.getNChannels(); c++) {
            for (int f = 0; f < input.getNFrames(); f++) {
                ImagePlus frame_imp = processedFrames.get(f + c * input.getNFrames());
                for (int z = 0; z < input.getNSlices(); z++) {
                    frame_imp.setZ(z + 1);
                    result.addSlice(frame_imp.getProcessor());
                }
            }
        }
        ImagePlus output = new ImagePlus(frameProcessor.getClass().getSimpleName() + "_" + input.getTitle(), result);

        return HyperStackConverter.toHyperStack(output, input.getNChannels(), output.getNSlices() / input.getNFrames() / input.getNChannels(), input.getNFrames());
    }

    public static void main(String... args) {
        new ImageJ();

        ImagePlus input = NewImage.createFloatImage("temp", 100, 100, 100, NewImage.FILL_RANDOM);
        input = HyperStackConverter.toHyperStack(input, 1, 10, 10);

        FrameProcessor frameProcessor = new GaussianBlurFrameProcessor(5f, 5f, 5f);

        ImagePlus result = new Framor(input, frameProcessor).getResult();
        result.show();
    }
}
