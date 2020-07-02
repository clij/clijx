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

public class TopHatFrameProcessor extends AbstractFrameProcessor implements PlugInFilter {
    private Integer radiusX = 10;
    private Integer radiusY = 10;
    private Integer radiusZ = 10;

    public TopHatFrameProcessor(){}
    public TopHatFrameProcessor(Integer radiusX, Integer radiusY, Integer radiusZ) {
        this.radiusX = radiusX;
        this.radiusY = radiusY;
        this.radiusZ = radiusZ;
    }


    @Override
    public ImagePlus process(ImagePlus imp) {
        CLIJ2 clij2 = getCLIJ2();
        ClearCLBuffer input = clij2.push(imp);
        ClearCLBuffer output = clij2.create(input);
        if (imp.getNSlices() > 1) {
            clij2.topHatBox(input, output, radiusX, radiusY, 0);
        } else {
            clij2.topHatBox(input, output, radiusX, radiusY, radiusZ);
        }
        ImagePlus result = clij2.pull(output);
        input.close();
        output.close();

        return result;
    }

    @Override
    public FrameProcessor duplicate() {
        TopHatFrameProcessor frameProcessor = new TopHatFrameProcessor(radiusX, radiusY, radiusZ);
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
        GenericDialog gd = new GenericDialog("Top-hat background subtraction (CLIJxf)");
        gd.addNumericField("Sigma x", radiusX);
        gd.addNumericField("Sigma y", radiusY);
        gd.addNumericField("Sigma z", radiusZ);
        gd.showDialog();
        if (gd.wasCanceled()) {
            return;
        }
        radiusX = (int)gd.getNextNumber();
        radiusY = (int)gd.getNextNumber();
        radiusZ = (int)gd.getNextNumber();

        new Framor(IJ.getImage(), new TopHatFrameProcessor(radiusX, radiusY, radiusZ)).getResult().show();
    }
}
