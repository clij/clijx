package net.haesleinhuepf.clijx.plugins;

import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.haesleinhuepf.clij.macro.CLIJMacroPlugin;
import net.haesleinhuepf.clij.macro.CLIJOpenCLProcessor;
import net.haesleinhuepf.clij.macro.documentation.OffersDocumentation;
import net.haesleinhuepf.clij2.AbstractCLIJ2Plugin;
import net.haesleinhuepf.clij2.CLIJ2;
import net.haesleinhuepf.clij2.utilities.HasClassifiedInputOutput;
import net.haesleinhuepf.clij2.utilities.IsCategorized;
import org.scijava.plugin.Plugin;

@Deprecated
@Plugin(type = CLIJMacroPlugin.class, name = "CLIJx_detectAndLabelMaximaAboveThreshold")
public class DetectAndLabelMaximaAboveThreshold extends AbstractCLIJ2Plugin implements CLIJMacroPlugin, CLIJOpenCLProcessor, OffersDocumentation, IsCategorized, HasClassifiedInputOutput {
    @Override
    public String getInputType() {
        return "Image";
    }

    @Override
    public String getOutputType() {
        return "Label Image";
    }

    @Override
    public String getParameterHelpText() {
        return "Image input, ByRef Image destination, Number sigma_x, Numer sigma_y, Number sigma_z, Number minimum_intensity, Boolean invert";
    }

    @Override
    public boolean executeCL() {
        return detectAndLabelMaximaAboveThreshold(getCLIJ2(), (ClearCLBuffer) args[0], (ClearCLBuffer) args[1], asFloat(args[2]), asFloat(args[3]), asFloat(args[4]), asFloat(args[5]), asBoolean(args[6]));
    }

    public static boolean detectAndLabelMaximaAboveThreshold(CLIJ2 clijx, ClearCLBuffer pushed, ClearCLBuffer result, Float sigma_x, Float sigma_y, Float sigma_z, Float intensity_threshold, Boolean invert) {
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

        ClearCLBuffer thresholded = clijx.create(maxima);
        clijx.greaterConstant(blurred, thresholded, intensity_threshold);

        ClearCLBuffer selected_maxima = blurred; // reuse memory
        clijx.binaryAnd(maxima, thresholded, selected_maxima);

        maxima.close();
        thresholded.close();

        clijx.set(result, 0);
        clijx.connectedComponentsLabelingBox(selected_maxima, result);
        blurred.close();

        return true;
    }

    @Override
    public String getDescription() {
        return "Determines maximum regions in a Gaussian blurred version of the original image and excludes found pixels below a given intensity in the blurred image.\n\n" +
                "The regions do not not necessarily have to be single pixels. \n" +
                "It is also possible to invert the image before determining the maxima.";
    }

    @Override
    public String getAvailableForDimensions() {
        return "2D, 3D";
    }

    @Override
    public String getCategories() {
        return "Label, Detection";
    }
}
