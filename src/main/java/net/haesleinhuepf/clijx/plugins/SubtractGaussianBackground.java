package net.haesleinhuepf.clijx.plugins;


import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.clearcl.interfaces.ClearCLImageInterface;
import net.haesleinhuepf.clij.macro.CLIJMacroPlugin;
import net.haesleinhuepf.clij.macro.CLIJOpenCLProcessor;
import net.haesleinhuepf.clij.macro.documentation.OffersDocumentation;
import net.haesleinhuepf.clij2.AbstractCLIJ2Plugin;
import net.haesleinhuepf.clij2.CLIJ2;
import net.haesleinhuepf.clij2.utilities.IsCategorized;
import net.haesleinhuepf.clijx.CLIJx;
import org.scijava.plugin.Plugin;

@Deprecated
@Plugin(type = CLIJMacroPlugin.class, name = "CLIJx_subtractGaussianBackground")
public class SubtractGaussianBackground extends AbstractCLIJ2Plugin implements CLIJMacroPlugin, CLIJOpenCLProcessor, OffersDocumentation, IsCategorized {
    @Override
    public String getParameterHelpText() {
        return "Image input, ByRef Image destination, Number sigmaX, Number sigmaY, Number sigmaZ";
    }

    @Override
    public boolean executeCL() {
        boolean result = subtractGaussianBackground(getCLIJ2(), (ClearCLBuffer) (args[0]), (ClearCLBuffer) (args[1]), asFloat(args[2]), asFloat(args[3]), asFloat(args[4]));
        return result;
    }

    public static boolean subtractGaussianBackground(CLIJ2 clij2, ClearCLImageInterface input, ClearCLImageInterface output, Float sigmaX, Float sigmaY, Float sigmaZ) {

        ClearCLBuffer background = clij2.create(input.getDimensions(), input.getNativeType());

        clij2.gaussianBlur(input, background, sigmaX, sigmaY, sigmaZ);

        clij2.subtractImages(input, background, output);

        clij2.release(background);
        return true;
    }

    @Override
    public String getDescription() {
        return "Applies Gaussian blur to the input image and subtracts the result from the original image.\n\n" +
                "Deprecated: Use differenceOfGaussian() instead.";
    }

    @Override
    public String getAvailableForDimensions() {
        return "2D, 3D";
    }

    @Override
    public String getCategories() {
        return "Filter,Background";
    }
}
