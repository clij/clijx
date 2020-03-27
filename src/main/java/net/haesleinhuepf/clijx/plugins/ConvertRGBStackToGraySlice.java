package net.haesleinhuepf.clijx.plugins;

import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.haesleinhuepf.clij.kernels.Kernels;
import net.haesleinhuepf.clij.macro.AbstractCLIJPlugin;
import net.haesleinhuepf.clij.macro.CLIJMacroPlugin;
import net.haesleinhuepf.clij.macro.CLIJOpenCLProcessor;
import net.haesleinhuepf.clij.macro.documentation.OffersDocumentation;
import net.haesleinhuepf.clijx.CLIJx;
import net.haesleinhuepf.clijx.utilities.AbstractCLIJxPlugin;
import org.scijava.plugin.Plugin;

/**
 * Author: @haesleinhuepf
 * 12 2018
 */
@Plugin(type = CLIJMacroPlugin.class, name = "CLIJx_convertRGBStackToGraySlice")
public class ConvertRGBStackToGraySlice extends AbstractCLIJxPlugin implements CLIJMacroPlugin, CLIJOpenCLProcessor, OffersDocumentation {

    private final static float rFactor = 0.299f;
    private final static float gFactor = 0.587f;
    private final static float bFactor = 0.114f;

    @Override
    public boolean executeCL() {
        ClearCLBuffer input = (ClearCLBuffer) args[0];
        ClearCLBuffer output = (ClearCLBuffer) args[1];

        return convertRGBStackToGraySlice(getCLIJx(), input, output);
    }

    public static boolean convertRGBStackToGraySlice(CLIJx clijx, ClearCLBuffer input, ClearCLBuffer output) {
        ClearCLBuffer temp = clijx.create(input.getDimensions(), NativeTypeEnum.Float);

        clijx.multiplyImageStackWithScalars(input, temp, new float[]{rFactor, gFactor, bFactor});
        clijx.sumZProjection(temp, output);
        clijx.release(temp);

        return false;
    }

    @Override
    public ClearCLBuffer createOutputBufferFromSource(ClearCLBuffer input) {
        return getCLIJx().create(new long[]{input.getWidth(), input.getHeight()}, input.getNativeType());
    }

    @Override
    public String getParameterHelpText() {
        return "Image stack_source, ByRef Image slice_destination";
    }


    @Override
    public String getDescription() {
        return "Converts a three channel image (stack with three slices) to a single channel image (2D image) " +
                "by multiplying with factors " + rFactor + ", " + gFactor + ", " + bFactor + ".";
    }

    @Override
    public String getAvailableForDimensions() {
        return "3D -> 2D";
    }
}
