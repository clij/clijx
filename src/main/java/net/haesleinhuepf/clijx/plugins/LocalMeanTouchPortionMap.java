package net.haesleinhuepf.clijx.plugins;

import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.haesleinhuepf.clij.macro.CLIJMacroPlugin;
import net.haesleinhuepf.clij.macro.CLIJOpenCLProcessor;
import net.haesleinhuepf.clij.macro.documentation.OffersDocumentation;
import net.haesleinhuepf.clij2.AbstractCLIJ2Plugin;
import net.haesleinhuepf.clij2.CLIJ2;
import net.haesleinhuepf.clij2.utilities.HasClassifiedInputOutput;
import net.haesleinhuepf.clij2.utilities.IsCategorized;
import org.scijava.plugin.Plugin;

@Plugin(type = CLIJMacroPlugin.class, name = "CLIJx_localMeanTouchPortionMap")
public class LocalMeanTouchPortionMap extends AbstractCLIJ2Plugin implements CLIJMacroPlugin, CLIJOpenCLProcessor, OffersDocumentation, IsCategorized, HasClassifiedInputOutput {
    @Override
    public String getInputType() {
        return "Label Image";
    }

    @Override
    public String getOutputType() {
        return "Image";
    }

    @Override
    public String getParameterHelpText() {
        return "Image input, ByRef Image destination";
    }

    @Override
    public boolean executeCL() {
        return localMeanTouchPortionMap(getCLIJ2(), (ClearCLBuffer) args[0], (ClearCLBuffer) args[1]);
    }

    public static boolean localMeanTouchPortionMap(CLIJ2 clij2, ClearCLBuffer pushed, ClearCLBuffer result) {
        int number_of_labels = (int)clij2.maximumOfAllPixels(pushed);
        ClearCLBuffer touch_matrix = clij2.create(number_of_labels + 1, number_of_labels + 1);
        clij2.generateTouchMatrix(pushed, touch_matrix);

        ClearCLBuffer pointlist = clij2.create(number_of_labels, pushed.getDimension());
        clij2.centroidsOfLabels(pushed, pointlist);

        ClearCLBuffer distance_matrix = clij2.create(number_of_labels + 1, number_of_labels + 1);
        clij2.generateDistanceMatrix(pointlist, pointlist, distance_matrix);
        pointlist.close();

        ClearCLBuffer touch_count_matrix = clij2.create(distance_matrix);
        clij2.generateTouchCountMatrix(pushed, touch_count_matrix);

        //ClearCLBuffer edge_image = clij2.create(label_map);
        //clij2.detectLabelEdges(label_map, edge_image);
        //ResultsTable table = new ResultsTable();
        //clij2.statisticsOfBackgroundAndLabelledPixels(edge_image, label_map, table);

        ClearCLBuffer sum_vector = clij2.create(touch_count_matrix.getWidth(), 1);
        clij2.sumYProjection(touch_count_matrix, sum_vector);

        ClearCLBuffer count_vector = clij2.create(touch_count_matrix.getWidth(), 1);
        clij2.countTouchingNeighbors(touch_matrix, count_vector);

        ClearCLBuffer average_vector = clij2.create(touch_count_matrix.getWidth(), 1);
        clij2.divideImages(count_vector, sum_vector, average_vector);

        //ClearCLBuffer vector = clij2.create(touch_count_matrix.getWidth(), 1);
        //clij2.power(average_vector, vector, -1);

        // ignore measurement for background
        clij2.setColumn(average_vector, 0, 0);

        clij2.replaceIntensities(pushed, average_vector, result);
        touch_count_matrix.close();
        //vector.close();
        sum_vector.close();
        count_vector.close();
        average_vector.close();
        touch_matrix.close();
        distance_matrix.close();

        return true;
    }

    @Override
    public ClearCLBuffer createOutputBufferFromSource(ClearCLBuffer input)
    {
        return getCLIJ2().create(input.getDimensions(), NativeTypeEnum.Float);
    }

    @Override
    public String getDescription() {
        return "Takes a label map, determines which labels touch and how much, relatively taking the whole outline of \n" +
                "each label into account, and determines for every label with the mean of this value and replaces the \n" +
                "label index with that value.\n\n";
    }

    @Override
    public String getAvailableForDimensions() {
        return "2D, 3D";
    }


    @Override
    public String getCategories() {
        return "Visualisation, Graph, Label, Measurements";
    }
}
