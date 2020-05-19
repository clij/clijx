package net.haesleinhuepf.clijx.plugins;


import net.haesleinhuepf.clij.CLIJ;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.macro.AbstractCLIJPlugin;
import net.haesleinhuepf.clij.macro.CLIJMacroPlugin;
import net.haesleinhuepf.clij.macro.CLIJOpenCLProcessor;
import net.haesleinhuepf.clij.macro.documentation.OffersDocumentation;
import net.haesleinhuepf.clij2.AbstractCLIJ2Plugin;
import net.haesleinhuepf.clij2.CLIJ2;
import net.haesleinhuepf.clij2.utilities.HasAuthor;
import org.scijava.plugin.Plugin;

import java.util.HashMap;

//
// Plugins>ImageJ on GPU (CLIJx)>Filter,               "Non-local mean on GPU (experimental)",                                  net.haesleinhuepf.clijx.plugins.NonLocalMean

// @Plugin(type = CLIJMacroPlugin.class, name = "CLIJx_nonLocalMaan")
public class NonLocalMean extends AbstractCLIJ2Plugin implements CLIJMacroPlugin, CLIJOpenCLProcessor, OffersDocumentation, HasAuthor {

    @Override
    public String getParameterHelpText() {
        return "Image input, ByRef Image destination, Number radiusX, Number radiusY, Number radiusZ, Number sigma";
    }

    @Override
    public boolean executeCL() {
        boolean result = nonLocalMean(getCLIJ2(), (ClearCLBuffer) (args[0]), (ClearCLBuffer) (args[1]), asInteger(args[2]), asInteger(args[3]), asInteger(args[4]), asFloat(args[5]));
        return result;
    }

    public static boolean nonLocalMean(CLIJ2 clij2, ClearCLBuffer input, ClearCLBuffer output, Integer radiusX, Integer radiusY, Integer radiusZ, Float sigma) {
        ClearCLBuffer local_mean = clij2.create(output);
        clij2.mean3DBox(input, local_mean, radiusX, radiusY, radiusZ);

        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("input", input);
        parameters.put("local_mean", local_mean);
        parameters.put("output", output);
        parameters.put("radiusX", radiusX);
        parameters.put("radiusY", radiusY);
        parameters.put("radiusZ", radiusZ);
        parameters.put("sigma", sigma);

        clij2.execute(NonLocalMean.class, "non_local_mean_3d_x.cl", "non_local_mean_3d", output.getDimensions(), output.getDimensions(), parameters);

        local_mean.close();
        return true;
    }

    @Override
    public String getDescription() {
        return "Applies a non-local mean filter to the input image";
    }

    @Override
    public String getAvailableForDimensions() {
        return "2D, 3D";
    }

    @Override
    public String getAuthorName() {
        return "Robert Haase, based on work by Loic A. Royer";
    }
}
