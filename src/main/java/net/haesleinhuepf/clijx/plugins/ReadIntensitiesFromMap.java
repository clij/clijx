package net.haesleinhuepf.clijx.plugins;


import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.clearcl.interfaces.ClearCLImageInterface;
import net.haesleinhuepf.clij.macro.CLIJMacroPlugin;
import net.haesleinhuepf.clij.macro.CLIJOpenCLProcessor;
import net.haesleinhuepf.clij.macro.documentation.OffersDocumentation;
import net.haesleinhuepf.clij2.AbstractCLIJ2Plugin;
import net.haesleinhuepf.clij2.CLIJ2;
import net.haesleinhuepf.clij2.plugins.ReplaceIntensities;
import net.haesleinhuepf.clij2.utilities.HasClassifiedInputOutput;
import net.haesleinhuepf.clij2.utilities.IsCategorized;
import org.scijava.plugin.Plugin;

import java.util.HashMap;

@Plugin(type = CLIJMacroPlugin.class, name = "CLIJx_readIntensitiesFromMap")
public class ReadIntensitiesFromMap extends AbstractCLIJ2Plugin implements CLIJMacroPlugin, CLIJOpenCLProcessor, OffersDocumentation, IsCategorized, HasClassifiedInputOutput {
    @Override
    public String getInputType() {
        return "Image, Label Image";
    }

    @Override
    public String getOutputType() {
        return "Vector";
    }


    @Override
    public String getParameterHelpText() {
        return "Image input, Image new_values_vector, ByRef Image destination";
    }

    @Override
    public boolean executeCL() {
        boolean result = readIntensitiesFromMap(getCLIJ2(), (ClearCLBuffer) (args[0]),(ClearCLBuffer) (args[1]), (ClearCLBuffer) (args[2]));
        return result;
    }

    public static boolean readIntensitiesFromMap(CLIJ2 clij2, ClearCLImageInterface labels, ClearCLImageInterface map_image, ClearCLImageInterface values_destination) {
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.clear();
        parameters.put("labels", labels);
        parameters.put("map_image", map_image);
        parameters.put("intensities", values_destination);

        long[] dims = labels.getDimensions();

        clij2.activateSizeIndependentKernelCompilation();
        clij2.execute(ReadIntensitiesFromMap.class, "read_intensities_from_map_x.cl", "read_intensities_from_map", dims, dims, parameters);
        return true;
    }

    @Override
    public String getDescription() {
        return "Takes a label image and an parametric image and reads parametric values from the labels positions.\n\n" +
                "The read intensity valus are stored in a new vector.\n" +
                "\n" +
                "Note: This will only work if all labels have number of voxels == 1 or if all pixels in each label have the same value.\n" +
                "\n" +
                "Parameters\n" +
                "----------\n" +
                "labels\n" +
                "map_image\n" +
                "values_destination";
    }

    @Override
    public String getAvailableForDimensions() {
        return "2D, 3D";
    }

    @Override
    public String getCategories() {
        return "Graph, Labels, Measurement";
    }
}
