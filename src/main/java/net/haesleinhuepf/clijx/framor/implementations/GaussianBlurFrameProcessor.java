package net.haesleinhuepf.clijx.framor.implementations;

import ij.ImagePlus;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij2.CLIJ2;
import net.haesleinhuepf.clijx.framor.FrameProcessor;

public class GaussianBlurFrameProcessor implements FrameProcessor {
    private final Float sigmaX;
    private final Float sigmaY;
    private final Float sigmaZ;
    private CLIJ2 clij2;
    private int frame;

    public GaussianBlurFrameProcessor(Float sigmaX, Float sigmaY, Float sigmaZ) {
        this.sigmaX = sigmaX;
        this.sigmaY = sigmaY;
        this.sigmaZ = sigmaZ;
    }

    @Override
    public void setCLIJ2(CLIJ2 clij2) {
        this.clij2 = clij2;
    }

    @Override
    public CLIJ2 getCLIJ2() {
        return clij2;
    }

    @Override
    public ImagePlus process(ImagePlus imp) {
        ClearCLBuffer input = clij2.pushCurrentZStack(imp);
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
    public void setFrame(int frame) {
        this.frame = frame;
    }

    @Override
    public int getFrame() {
        return frame;
    }
}
