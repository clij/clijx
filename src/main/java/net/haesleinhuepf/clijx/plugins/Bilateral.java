package net.haesleinhuepf.clijx.plugins;


import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.macro.CLIJMacroPlugin;
import net.haesleinhuepf.clij.macro.CLIJOpenCLProcessor;
import net.haesleinhuepf.clij.macro.documentation.OffersDocumentation;
import net.haesleinhuepf.clij2.AbstractCLIJ2Plugin;
import net.haesleinhuepf.clij2.CLIJ2;
import net.haesleinhuepf.clij2.utilities.HasAuthor;
import net.haesleinhuepf.clij2.utilities.IsCategorized;
import net.haesleinhuepf.clijx.utilities.AbstractCLIJxPlugin;
import org.scijava.plugin.Plugin;

import java.util.HashMap;

@Deprecated
@Plugin(type = CLIJMacroPlugin.class, name = "CLIJx_bilateral")
public class Bilateral extends AbstractCLIJxPlugin implements CLIJMacroPlugin, CLIJOpenCLProcessor, OffersDocumentation, HasAuthor, IsCategorized {

    @Override
    public String getParameterHelpText() {
        return "Image input, ByRef Image destination, Number radiusX, Number radiusY, Number radiusZ, Number sigma_intensity, Number sigma_space";
    }

    @Override
    public boolean executeCL() {
        boolean result = getCLIJx().bilateral((ClearCLBuffer) (args[0]), (ClearCLBuffer) (args[1]), asInteger(args[2]), asInteger(args[3]), asInteger(args[4]), asFloat(args[5]), asFloat(args[6]));
        return result;
    }

    @Deprecated
    public static boolean bilateral(CLIJ2 clij2, ClearCLBuffer input, ClearCLBuffer output, Integer radiusX, Integer radiusY, Integer radiusZ, Float sigma_intensity, Float sigma_space) {
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("input", input);
        parameters.put("output", output);
        parameters.put("radiusX", radiusX);
        parameters.put("radiusY", radiusY);
        if (input.getDimension() > 2) {
            parameters.put("radiusZ", radiusZ);
        }
        parameters.put("sigma_intensity", sigma_intensity);
        parameters.put("sigma_space", sigma_space);

        clij2.execute(Bilateral.class, "bilateral_" + input.getDimension() + "d_x.cl", "bilateral_" + input.getDimension() + "d", output.getDimensions(), output.getDimensions(), parameters);

        return true;
    }

    @Override
    public String getDescription() {
        return "Applies a bilateral filter using a box neighborhood with sigma weights for space and intensity to the input image.\n\n" +
                "Deprecated: Use SimpleITK bilateral() instead.";
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
        //ImagePlus imp = IJ.openImage("C:/structure/data/blobs.tif");
        IJ.run(imp, "32-bit", "");

        ClearCLBuffer buff = clij2.push(imp);
        ClearCLBuffer res = clij2.create(buff);

        bilateral(clij2, buff, res, 2, 2, 2, 10.0f, 10.0f);

        clij2.show(res, "res");

    }

    @Override
    public String getCategories() {
        return "Noise, Filter";
    }
}
