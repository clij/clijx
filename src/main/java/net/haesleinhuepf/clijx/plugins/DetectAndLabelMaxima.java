package net.haesleinhuepf.clijx.plugins;

import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.haesleinhuepf.clij.macro.CLIJMacroPlugin;
import net.haesleinhuepf.clij.macro.CLIJOpenCLProcessor;
import net.haesleinhuepf.clij.macro.documentation.OffersDocumentation;
import net.haesleinhuepf.clij2.AbstractCLIJ2Plugin;
import net.haesleinhuepf.clij2.CLIJ2;
import org.scijava.plugin.Plugin;

@Plugin(type = CLIJMacroPlugin.class, name = "CLIJx_detectAndLabelMaxima")
public class DetectAndLabelMaxima extends AbstractCLIJ2Plugin implements CLIJMacroPlugin, CLIJOpenCLProcessor, OffersDocumentation {

    @Override
    public String getParameterHelpText() {
        return "Image input, ByRef Image destination, Number sigma_x, Numer sigma_y, Number sigma_z, Boolean invert";
    }

    @Override
    public boolean executeCL() {
        return detectAndLabelMaxima(getCLIJ2(), (ClearCLBuffer) args[0], (ClearCLBuffer) args[1], asFloat(args[2]), asFloat(args[3]), asFloat(args[4]), asBoolean(args[5]));
    }

    public static boolean detectAndLabelMaxima(CLIJ2 clijx, ClearCLBuffer pushed, ClearCLBuffer result, Float sigma_x, Float sigma_y, Float sigma_z, Boolean invert) {
        ClearCLBuffer blurred = clijx.create(pushed.getDimensions(), NativeTypeEnum.Float);
        clijx.gaussianBlur3D(pushed, blurred, sigma_x, sigma_y, sigma_z);

        if (invert) {
            ClearCLBuffer inverted = clijx.create(blurred);
            clijx.invert(blurred, inverted);
            blurred.close();
            blurred = inverted;
        }

        ClearCLBuffer maxima = clijx.create(blurred.getDimensions(), NativeTypeEnum.UnsignedByte);
        FindMaximaPlateaus.findMaximaPlateaus(clijx, blurred, maxima);
        blurred.close();

        clijx.connectedComponentsLabelingBox(maxima, result);

        return true;
    }

    @Override
    public String getDescription() {
        return "Determines maximum regions in a Gaussian blurred version of the oriignal image.\n\n" +
                "The regions do not not necessarily have to be single pixels. \n" +
                "It is also possible to invert the image before determining the maxima.";
    }

    @Override
    public String getAvailableForDimensions() {
        return "2D, 3D";
    }
}
