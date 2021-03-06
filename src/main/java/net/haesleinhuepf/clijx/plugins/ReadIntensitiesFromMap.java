package net.haesleinhuepf.clijx.plugins;


import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.clearcl.interfaces.ClearCLImageInterface;
import net.haesleinhuepf.clij.macro.CLIJMacroPlugin;
import net.haesleinhuepf.clij.macro.CLIJOpenCLProcessor;
import net.haesleinhuepf.clij.macro.documentation.OffersDocumentation;
import net.haesleinhuepf.clij2.AbstractCLIJ2Plugin;
import net.haesleinhuepf.clij2.CLIJ2;
import net.haesleinhuepf.clij2.plugins.ReadValuesFromMap;
import net.haesleinhuepf.clij2.utilities.HasClassifiedInputOutput;
import net.haesleinhuepf.clij2.utilities.IsCategorized;
import org.scijava.plugin.Plugin;

@Deprecated
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
        return "Image labels, Image map_image, ByRef Image values_destination";
    }

    @Override
    public boolean executeCL() {
        boolean result = readIntensitiesFromMap(getCLIJ2(), (ClearCLBuffer) (args[0]),(ClearCLBuffer) (args[1]), (ClearCLBuffer) (args[2]));
        return result;
    }

    public static boolean readIntensitiesFromMap(CLIJ2 clij2, ClearCLImageInterface labels, ClearCLImageInterface map_image, ClearCLImageInterface values_destination) {
        System.out.println("Deprecation warning: readIntensitiesFromMap is deprecated. Use readValuesFromMap instead.");
        return ReadValuesFromMap.readValuesFromMap(clij2, labels, map_image, values_destination);
    }

    @Override
    public String getDescription() {
        return "Takes a label image and an parametric image and reads parametric values from the labels positions.\n\n" +
                "The read intensity valus are stored in a new vector.\n" +
                "\n" +
                "Note: This will only work if all labels have number of voxels == 1 or if all pixels in each label have the same value.\n" +
                "\n" +
                "DEPRECATED: Use ReadValuesFromMap instead";
    }

    @Override
    public String getAvailableForDimensions() {
        return "2D, 3D";
    }

    @Override
    public String getCategories() {
        return "Graph, Labels, Measurement";
    }

    @Override
    public ClearCLBuffer createOutputBufferFromSource(ClearCLBuffer input) {
        int number_of_labels = (int) getCLIJ2().maximumOfAllPixels((ClearCLBuffer)args[0]);
        return getCLIJ2().create(number_of_labels + 1, 1, 1);
    }
}
