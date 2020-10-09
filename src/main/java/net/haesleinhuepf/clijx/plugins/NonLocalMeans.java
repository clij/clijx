package net.haesleinhuepf.clijx.plugins;


import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import net.haesleinhuepf.clij.CLIJ;
import net.haesleinhuepf.clij.clearcl.ClearCL;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.macro.AbstractCLIJPlugin;
import net.haesleinhuepf.clij.macro.CLIJMacroPlugin;
import net.haesleinhuepf.clij.macro.CLIJOpenCLProcessor;
import net.haesleinhuepf.clij.macro.documentation.OffersDocumentation;
import net.haesleinhuepf.clij2.AbstractCLIJ2Plugin;
import net.haesleinhuepf.clij2.CLIJ2;
import net.haesleinhuepf.clij2.utilities.HasAuthor;
import net.haesleinhuepf.clij2.utilities.HasClassifiedInputOutput;
import net.haesleinhuepf.clij2.utilities.IsCategorized;
import net.haesleinhuepf.clijx.utilities.AbstractCLIJxPlugin;
import org.scijava.plugin.Plugin;

import java.util.HashMap;

@Plugin(type = CLIJMacroPlugin.class, name = "CLIJx_nonLocalMeans")
public class NonLocalMeans extends AbstractCLIJxPlugin implements CLIJMacroPlugin, CLIJOpenCLProcessor, OffersDocumentation, HasAuthor, IsCategorized, HasClassifiedInputOutput {
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
        return "Image input, ByRef Image destination, Number radiusX, Number radiusY, Number radiusZ, Number sigma";
    }

    @Override
    public boolean executeCL() {
        boolean result = getCLIJx().nonLocalMeans((ClearCLBuffer) (args[0]), (ClearCLBuffer) (args[1]), asInteger(args[2]), asInteger(args[3]), asInteger(args[4]), asFloat(args[5]));
        return result;
    }

    public static boolean nonLocalMeans(CLIJ2 clij2, ClearCLBuffer input, ClearCLBuffer output, Integer radiusX, Integer radiusY, Integer radiusZ, Float sigma) {
        ClearCLBuffer local_mean = clij2.create(output);
        clij2.mean3DBox(input, local_mean, radiusX, radiusY, radiusZ);

        //clij2.show(local_mean, "local_mean");

        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("input", input);
        parameters.put("local_mean", local_mean);
        parameters.put("output", output);
        parameters.put("radiusX", radiusX);
        parameters.put("radiusY", radiusY);
        if (input.getDimension() > 2) {
            parameters.put("radiusZ", radiusZ);
        }
        parameters.put("sigma", sigma);

        clij2.execute(NonLocalMeans.class, "non_local_means_" + input.getDimension() + "d_x.cl", "non_local_means_" + input.getDimension() + "d", output.getDimensions(), output.getDimensions(), parameters);

        local_mean.close();
        return true;
    }

    @Override
    public String getDescription() {
        return "Applies a non-local means filter using a box neighborhood with a Gaussian weight specified with sigma to the input image.";
    }

    @Override
    public String getAvailableForDimensions() {
        return "2D, 3D";
    }

    @Override
    public String getAuthorName() {
        return "Robert Haase, based on work by Loic A. Royer";
    }

    public static void main(String[] args) {
        CLIJ2 clij2 = CLIJ2.getInstance();
        new ImageJ();

        ImagePlus imp = IJ.openImage("C:/structure/data/t1-head.tif");
        IJ.run(imp, "32-bit", "");

        ClearCLBuffer buff = clij2.push(imp);
        ClearCLBuffer res = clij2.create(buff);

        nonLocalMeans(clij2, buff, res, 2, 2, 2, 10.0f);

        clij2.show(res, "res");

    }

    @Override
    public String getCategories() {
        return "Background, Filter";
    }
}
