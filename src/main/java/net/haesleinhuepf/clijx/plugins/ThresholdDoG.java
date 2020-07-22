package net.haesleinhuepf.clijx.plugins;

import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.haesleinhuepf.clij.macro.CLIJMacroPlugin;
import net.haesleinhuepf.clij.macro.CLIJOpenCLProcessor;
import net.haesleinhuepf.clij.macro.documentation.OffersDocumentation;
import net.haesleinhuepf.clij2.AbstractCLIJ2Plugin;
import net.haesleinhuepf.clij2.CLIJ2;
import org.scijava.plugin.Plugin;

@Plugin(type = CLIJMacroPlugin.class, name = "CLIJx_thresholdDoG")
public class ThresholdDoG extends AbstractCLIJ2Plugin implements CLIJMacroPlugin, CLIJOpenCLProcessor, OffersDocumentation {


    @Override
    public Object[] getDefaultValues() {
        return new Object[]{null, null, 2, 10, 100, true};
    }

    @Override
    public String getParameterHelpText() {
        return "Image input, ByRef Image destination, Number sigma1, Number sigma2, Number threshold, Boolean above_threshold";
    }

    @Override
    public boolean executeCL() {
        return localDoGThreshold(getCLIJ2(), (ClearCLBuffer) args[0], (ClearCLBuffer) args[1], asFloat(args[2]), asFloat(args[3]), asFloat(args[4]), asBoolean(args[5]));
    }

    public static boolean localDoGThreshold(CLIJ2 clijx, ClearCLBuffer pushed, ClearCLBuffer result, Float sigma1, Float sigma2, Float threshold, Boolean above_threshold) {
        ClearCLBuffer temp = clijx.create(pushed.getDimensions(), NativeTypeEnum.Float);
        clijx.differenceOfGaussian(pushed, temp, sigma1, sigma1, sigma1, sigma2, sigma2, sigma2);

        if (above_threshold) {
            clijx.greaterConstant(temp, result, threshold);
        } else {
            clijx.smallerOrEqualConstant(temp, result, threshold);
        }
        temp.close();

        return true;
    }

    @Override
    public String getDescription() {
        return "Applies a Difference-of-Gaussian filter to an image and thresholds it with given sigma and threshold values.";
    }

    @Override
    public String getAvailableForDimensions() {
        return "2D, 3D";
    }
}
