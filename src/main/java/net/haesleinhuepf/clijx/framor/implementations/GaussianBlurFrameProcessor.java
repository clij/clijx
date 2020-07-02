package net.haesleinhuepf.clijx.framor.implementations;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij2.CLIJ2;
import net.haesleinhuepf.clijx.framor.AbstractFrameProcessor;
import net.haesleinhuepf.clijx.framor.FrameProcessor;
import net.haesleinhuepf.clijx.framor.Framor;

public class GaussianBlurFrameProcessor extends AbstractFrameProcessor implements PlugInFilter {
    private Float sigmaX = 1f;
    private Float sigmaY = 1f;
    private Float sigmaZ = 1f;

    public GaussianBlurFrameProcessor() {}
    public GaussianBlurFrameProcessor(Float sigmaX, Float sigmaY, Float sigmaZ) {
        this.sigmaX = sigmaX;
        this.sigmaY = sigmaY;
        this.sigmaZ = sigmaZ;
    }

    @Override
    public ImagePlus process(ImagePlus imp) {
        CLIJ2 clij2 = getCLIJ2();
        ClearCLBuffer input = clij2.push(imp);
        ClearCLBuffer output = clij2.create(input);
        if (imp.getNSlices() > 1) {
            clij2.gaussianBlur(input, output, sigmaX, sigmaY);
        } else {
            clij2.gaussianBlur(input, output, sigmaX, sigmaY, sigmaZ);
        }
        ImagePlus result = clij2.pull(output);
        input.close();
        output.close();

        return result;
    }

    @Override
    public FrameProcessor duplicate() {
        GaussianBlurFrameProcessor frameProcessor = new GaussianBlurFrameProcessor(sigmaX, sigmaY, sigmaZ);
        frameProcessor.setCLIJ2(getCLIJ2());
        return frameProcessor;
    }

    @Override
    public long getMemoryNeedInBytes(ImagePlus imp) {
        return imp.getBitDepth() / 8 * imp.getWidth() * imp.getHeight() * imp.getNSlices() * 2;
    }

    @Override
    public int setup(String arg, ImagePlus imp) {
        return DOES_ALL;
    }

    @Override
    public void run(ImageProcessor ip) {
        GenericDialog gd = new GenericDialog("Gaussian blur (CLIJxf)");
        gd.addNumericField("Sigma x", sigmaX);
        gd.addNumericField("Sigma y", sigmaY);
        gd.addNumericField("Sigma z", sigmaZ);
        gd.showDialog();
        if (gd.wasCanceled()) {
            return;
        }
        sigmaX = (float)gd.getNextNumber();
        sigmaY = (float)gd.getNextNumber();
        sigmaZ = (float)gd.getNextNumber();

        new Framor(IJ.getImage(), new GaussianBlurFrameProcessor(sigmaX, sigmaY, sigmaZ)).getResult().show();
    }
}
