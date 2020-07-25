package net.haesleinhuepf.clijx.plugins;

import ij.measure.ResultsTable;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.macro.CLIJMacroPlugin;
import net.haesleinhuepf.clij.macro.CLIJOpenCLProcessor;
import net.haesleinhuepf.clij.macro.documentation.OffersDocumentation;
import net.haesleinhuepf.clij2.AbstractCLIJ2Plugin;
import net.haesleinhuepf.clij2.CLIJ2;
import net.haesleinhuepf.clij2.plugins.StatisticsOfLabelledPixels;
import net.haesleinhuepf.clij2.utilities.IsCategorized;
import org.scijava.plugin.Plugin;

@Plugin(type = CLIJMacroPlugin.class, name = "CLIJx_labelStandardDeviationIntensityMap")
public class LabelStandardDeviationIntensityMap extends AbstractCLIJ2Plugin implements CLIJMacroPlugin, CLIJOpenCLProcessor, OffersDocumentation, IsCategorized {

    @Override
    public String getParameterHelpText() {
        return "Image input, ByRef Image destination";
    }

    @Override
    public boolean executeCL() {
        return labelStandardDeviationIntensityMap(getCLIJ2(), (ClearCLBuffer) args[0], (ClearCLBuffer) args[1]);
    }

    public static boolean labelStandardDeviationIntensityMap(CLIJ2 clij2, ClearCLBuffer pushed, ClearCLBuffer result) {
        int number_of_labels = (int)clij2.maximumOfAllPixels(pushed);
        ClearCLBuffer size_array = clij2.create(number_of_labels + 1,1, 1);

        ResultsTable table = new ResultsTable();
        clij2.statisticsOfBackgroundAndLabelledPixels(pushed, pushed, table);

        clij2.pushResultsTableColumn(size_array, table, StatisticsOfLabelledPixels.STATISTICS_ENTRY.STANDARD_DEVIATION_INTENSITY.toString());

        clij2.replaceIntensities(pushed, size_array, result);
        size_array.close();

        return true;
    }

    @Override
    public String getDescription() {
        return "Takes a label map, determines the standard deviation of the intensity per label and replaces every label with the that number.\n\nThis results in a parametric image expressing standard deviation of object intensity.";
    }

    @Override
    public String getAvailableForDimensions() {
        return "2D, 3D";
    }

    @Override
    public String getCategories() {
        return "Measurements";
    }
}
