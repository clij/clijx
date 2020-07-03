package net.haesleinhuepf.clijx.framor.implementations;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij2.CLIJ2;
import net.haesleinhuepf.clijx.framor.AbstractFrameProcessor;
import net.haesleinhuepf.clijx.framor.FrameProcessor;
import net.haesleinhuepf.clijx.framor.Framor;

public class EqualizeMeanIntensityFrameProcessor extends AbstractFrameProcessor implements PlugInFilter {


    private Float reference_intensity;

    public EqualizeMeanIntensityFrameProcessor() {}
    public EqualizeMeanIntensityFrameProcessor(Float reference_intensity) {
        this.reference_intensity = reference_intensity;
    }

    @Override
    public ImagePlus process(ImagePlus imp) {
        CLIJ2 clij2 = getCLIJ2();
        ClearCLBuffer input = clij2.push(imp);
        ClearCLBuffer output = clij2.create(input);

        float mean_intensity = (float) clij2.meanOfAllPixels(input);
        float factor = reference_intensity / mean_intensity;
        clij2.multiplyImageAndScalar(input, output, factor);
        ImagePlus result = clij2.pull(output);
        input.close();
        output.close();

        return result;
    }

    @Override
    public FrameProcessor duplicate() {
        EqualizeMeanIntensityFrameProcessor frameProcessor = new EqualizeMeanIntensityFrameProcessor(reference_intensity);
        frameProcessor.setCLIJ2(getCLIJ2());
        return frameProcessor;
    }

    @Override
    public int setup(String arg, ImagePlus imp) {
        return DOES_ALL;
    }

    @Override
    public void run(ImageProcessor ip) {
        GenericDialog gd = new GenericDialog("Equalize mean intensity (CLIJxf)");
        gd.addNumericField("Reference_frame (0-indiced)", 0);
        gd.showDialog();
        if (gd.wasCanceled()) {
            return;
        }
        int frame = (int) gd.getNextNumber();
        ImagePlus imp = IJ.getImage();
        imp.setT(frame + 1);

        CLIJ2 clij2 = CLIJ2.getInstance();
        ClearCLBuffer buffer = clij2.pushCurrentZStack(imp);
        float mean_intensity = (float) clij2.meanOfAllPixels(buffer);
        buffer.close();

        new Framor(imp, new EqualizeMeanIntensityFrameProcessor(mean_intensity)).getResult().show();
    }

    @Override
    public long getMemoryNeedInBytes(ImagePlus imp) {
        return imp.getBitDepth() / 8 * imp.getWidth() * imp.getHeight() * imp.getNSlices() + imp.getBitDepth() / 8 * imp.getWidth() * imp.getHeight();
    }


    public static void main(String[] args) {
        new ImageJ();
        ImagePlus imp = IJ.openImage("C:/structure/data/Lund_001457.tif");
        imp.show();

        new Framor(imp, new EqualizeMeanIntensityFrameProcessor()).getResult().show();
    }
}
