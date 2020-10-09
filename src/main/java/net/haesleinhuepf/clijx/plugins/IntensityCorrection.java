package net.haesleinhuepf.clijx.plugins;

import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.macro.CLIJMacroPlugin;
import net.haesleinhuepf.clij.macro.CLIJOpenCLProcessor;
import net.haesleinhuepf.clij.macro.documentation.OffersDocumentation;
import net.haesleinhuepf.clij2.AbstractCLIJ2Plugin;
import net.haesleinhuepf.clij2.CLIJ2;
import net.haesleinhuepf.clij2.utilities.HasClassifiedInputOutput;
import net.haesleinhuepf.clij2.utilities.IsCategorized;
import org.scijava.plugin.Plugin;

@Plugin(type = CLIJMacroPlugin.class, name = "CLIJx_intensityCorrection")
public class IntensityCorrection extends AbstractCLIJ2Plugin implements CLIJMacroPlugin, CLIJOpenCLProcessor, OffersDocumentation, IsCategorized, HasClassifiedInputOutput {
    @Override
    public String getInputType() {
        return "Image";
    }

    @Override
    public String getOutputType() {
        return "Image";
    }

    @Override
    public String getParameterHelpText() {
        return "Image input, ByRef Image destination, Number reference_mean_intensity";
    }

    @Override
    public Object[] getDefaultValues() {
        return new Object[]{null, null, 1000};
    }

    @Override
    public boolean executeCL() {
        return intensityCorrection(getCLIJ2(), (ClearCLBuffer) args[0], (ClearCLBuffer) args[1], asFloat(args[2]));
    }

    public static boolean intensityCorrection(CLIJ2 clij2, ClearCLBuffer pushed, ClearCLBuffer result,
                                          Float reference_mean_intensity) {

        System.out.println("reference_mean_intensity = " + reference_mean_intensity);

        double mean_intensity = clij2.meanOfAllPixels(pushed);

        clij2.multiplyImageAndScalar(pushed, result, reference_mean_intensity / mean_intensity);

        return true;
    }

    @Override
    public String getDescription() {
        return "Determines the mean intensity of the image stack and multiplies it with a factor so that the mean intensity becomes equal to a given value.";
    }

    @Override
    public String getAvailableForDimensions() {
        return "2D, 3D";
    }

    @Override
    public String getCategories() {
        return "Filter";
    }
}
