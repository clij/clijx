package net.haesleinhuepf.clijx.plugins;

import ij.measure.ResultsTable;
import net.haesleinhuepf.clij.clearcl.ClearCL;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.haesleinhuepf.clij.macro.CLIJMacroPlugin;
import net.haesleinhuepf.clij.macro.CLIJOpenCLProcessor;
import net.haesleinhuepf.clij.macro.documentation.OffersDocumentation;
import net.haesleinhuepf.clij2.AbstractCLIJ2Plugin;
import net.haesleinhuepf.clij2.CLIJ2;
import net.haesleinhuepf.clij2.plugins.Clear;
import net.haesleinhuepf.clij2.plugins.ExcludeLabelsOnEdges;
import net.haesleinhuepf.clij2.utilities.HasClassifiedInputOutput;
import net.haesleinhuepf.clij2.utilities.IsCategorized;
import org.scijava.plugin.Plugin;

import java.nio.FloatBuffer;
import java.util.HashMap;


/**
 * Author: @haesleinhuepf
 * January 2021
 */
@Plugin(type = CLIJMacroPlugin.class, name = "CLIJx_flagLabelsOnEdges")
public class FlagLabelsOnEdges extends AbstractCLIJ2Plugin implements CLIJMacroPlugin, CLIJOpenCLProcessor, OffersDocumentation, IsCategorized, HasClassifiedInputOutput {
    @Override
    public String getInputType() {
        return "Label Image";
    }

    @Override
    public String getOutputType() {
        return "Label Image";
    }


    @Override
    public boolean executeCL() {
        ClearCLBuffer label_map_in = (ClearCLBuffer)( args[0]);
        ClearCLBuffer vector = (ClearCLBuffer)( args[1]);

        return flagLabelsOnEdges(getCLIJ2(), label_map_in, vector);
    }

    public static boolean flagLabelsOnEdges(CLIJ2 clij2, ClearCLBuffer label_map_in, ClearCLBuffer flag_vector_out) {
        ResultsTable table = new ResultsTable();
        ClearCLBuffer labels_off_edges = clij2.create(label_map_in);
        clij2.excludeLabelsOnEdges(label_map_in, labels_off_edges);

        clij2.statisticsOfBackgroundAndLabelledPixels(labels_off_edges, label_map_in, table);
        labels_off_edges.close();

        ClearCLBuffer vector = clij2.create((long)clij2.getMaximumOfAllPixels(label_map_in) + 1, 1, 1);
        clij2.pushResultsTableColumn(vector, table, "MEAN_INTENSITY");
        clij2.equalConstant(vector, flag_vector_out, 0);
        vector.close();
        return true;
    }

    @Override
    public String getParameterHelpText() {
        return "Image label_map_input, ByRef Image flag_vector_destination";
    }

    @Override
    public String getDescription() {
        return "Determines which labels in a label map touch the edges of the image (in X, Y and Z if the image is 3D). \n\n" +
                "It results in a vector image with values 1 (touches edges) and 0 (does not touch edge).\n" +
                "The entry in the vector (index 0) corresponds to background, following entries correspond to labels.";
    }

    @Override
    public String getAvailableForDimensions() {
        return "2D, 3D";
    }

    @Override
    public ClearCLBuffer createOutputBufferFromSource(ClearCLBuffer input) {
        return getCLIJ2().create((long)getCLIJ2().getMaximumOfAllPixels(input) + 1, 1, 1);
    }

    @Override
    public String getCategories() {
        return "Label, Filter";
    }
}
