package net.haesleinhuepf.clijx.plugins;


import net.haesleinhuepf.clij.CLIJ;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.macro.AbstractCLIJPlugin;
import net.haesleinhuepf.clij.macro.CLIJMacroPlugin;
import net.haesleinhuepf.clij.macro.CLIJOpenCLProcessor;
import net.haesleinhuepf.clij.macro.documentation.OffersDocumentation;
import net.haesleinhuepf.clij2.AbstractCLIJ2Plugin;
import net.haesleinhuepf.clij2.CLIJ2;
import net.haesleinhuepf.clij2.utilities.HasClassifiedInputOutput;
import net.haesleinhuepf.clij2.utilities.IsCategorized;
import org.scijava.plugin.Plugin;

@Plugin(type = CLIJMacroPlugin.class, name = "CLIJx_laplacianOfGaussian3D")
public class LaplacianOfGaussian3D extends AbstractCLIJ2Plugin implements CLIJMacroPlugin, CLIJOpenCLProcessor, OffersDocumentation, IsCategorized, HasClassifiedInputOutput {
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
        return "Image input, ByRef Image destination, Number sigma_x, Number sigma_y, Number sigma_z";
    }

    @Override
    public boolean executeCL() {

        boolean result = laplacianOfGaussian3D(getCLIJ2(), (ClearCLBuffer) (args[0]),  (ClearCLBuffer) (args[1]), asFloat(args[2]), asFloat(args[3]), asFloat(args[4]));
        return result;
    }

    public static boolean laplacianOfGaussian3D(CLIJ2 clij2, ClearCLBuffer input, ClearCLBuffer output, Float sigma_x, Float sigma_y, Float sigma_z) {

        ClearCLBuffer temp1 = clij2.create(input);

        clij2.gaussianBlur3D(input, temp1, sigma_x, sigma_y, sigma_z);

        clij2.laplaceBox(temp1, output);

        temp1.close();
        return true;
    }

    @Override
    public String getDescription() {
        return "Determined the Laplacian of Gaussian of a give input image with a given sigma.\n\n" +
                "It is recommended to apply this operation to images of type Float (32 bit) as results might be negative.";
    }

    @Override
    public String getAvailableForDimensions() {
        return "3D";
    }

    @Override
    public String getCategories() {
        return "Filter,Background";
    }
}
