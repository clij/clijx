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

public class MaximumZProjectionFrameProcessor extends AbstractFrameProcessor implements PlugInFilter {


    public MaximumZProjectionFrameProcessor() {}

    @Override
    public ImagePlus process(ImagePlus imp) {
        CLIJ2 clij2 = getCLIJ2();
        ClearCLBuffer input = clij2.push(imp);
        ClearCLBuffer output = clij2.create(new long[]{input.getWidth(), input.getHeight()}, input.getNativeType());
        if (imp.getNSlices() > 1) {
            clij2.maximumZProjection(input, output);
        } else {
            clij2.maximumZProjection(input, output);
        }
        ImagePlus result = clij2.pull(output);
        input.close();
        output.close();

        return result;
    }

    @Override
    public FrameProcessor duplicate() {
        MaximumZProjectionFrameProcessor frameProcessor = new MaximumZProjectionFrameProcessor();
        frameProcessor.setCLIJ2(getCLIJ2());
        return frameProcessor;
    }

    @Override
    public int setup(String arg, ImagePlus imp) {
        return DOES_ALL;
    }

    @Override
    public void run(ImageProcessor ip) {
        new Framor(IJ.getImage(), new MaximumZProjectionFrameProcessor()).getResult().show();
    }

    @Override
    public long getMemoryNeedInBytes(ImagePlus imp) {
        return imp.getBitDepth() / 8 * imp.getWidth() * imp.getHeight() * imp.getNSlices() + imp.getBitDepth() / 8 * imp.getWidth() * imp.getHeight();
    }


    public static void main(String[] args) {
        new ImageJ();
        ImagePlus imp = IJ.openImage("C:/structure/data/Lund_001457.tif");
        imp.show();

        new Framor(imp, new MaximumZProjectionFrameProcessor()).getResult().show();
    }
}
