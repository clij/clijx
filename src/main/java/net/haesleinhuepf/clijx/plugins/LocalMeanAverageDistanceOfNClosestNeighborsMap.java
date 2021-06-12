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

@Deprecated
@Plugin(type = CLIJMacroPlugin.class, name = "CLIJx_localMeanAverageDistanceOfNClosestNeighborsMap")
public class LocalMeanAverageDistanceOfNClosestNeighborsMap extends AbstractCLIJ2Plugin implements CLIJMacroPlugin, CLIJOpenCLProcessor, OffersDocumentation, IsCategorized, HasClassifiedInputOutput {
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
        return "Image input, ByRef Image destination, Number n";
    }

    @Override
    public boolean executeCL() {
        return localMeanAverageDistanceOfNClosestNeighborsMap(getCLIJ2(), (ClearCLBuffer) args[0], (ClearCLBuffer) args[1], asInteger(args[2]));
    }

    @Deprecated
    public static boolean localMeanAverageDistanceOfNClosestNeighborsMap(CLIJ2 clij2, ClearCLBuffer pushed, ClearCLBuffer result, Integer n) {
        int number_of_labels = (int)clij2.maximumOfAllPixels(pushed);
        ClearCLBuffer touch_matrix = clij2.create(number_of_labels + 1, number_of_labels + 1);
        clij2.generateTouchMatrix(pushed, touch_matrix);


        ClearCLBuffer pointlist = clij2.create(number_of_labels, pushed.getDimension());
        clij2.centroidsOfLabels(pushed, pointlist);

        ClearCLBuffer distance_matrix = clij2.create(number_of_labels + 1, number_of_labels + 1);
        clij2.generateDistanceMatrix(pointlist, pointlist, distance_matrix);

        ClearCLBuffer distance_vector = clij2.create(number_of_labels + 1, 1, 1);
        clij2.averageDistanceOfNClosestPoints(distance_matrix, distance_vector, n);
        distance_matrix.close();
        pointlist.close();


        ClearCLBuffer mean_vector = clij2.create(number_of_labels, 1, 1);
        clij2.meanOfTouchingNeighbors(distance_vector, touch_matrix, mean_vector);
        distance_vector.close();
        touch_matrix.close();

        // ignore measurement for background
        clij2.setColumn(mean_vector, 0, 0);

        clij2.replaceIntensities(pushed, mean_vector, result);
        mean_vector.close();

        return true;
    }

    @Override
    public ClearCLBuffer createOutputBufferFromSource(ClearCLBuffer input)
    {
        return getCLIJ2().create(input.getDimensions(), NativeTypeEnum.Float);
    }

    @Override
    public String getDescription() {
        return "Deprecated: Takes a label map, determines distances between all centroids, the mean distance of the n closest points for every point\n" +
                " and replaces every label with the mean distance of touching labels.";    }

    @Override
    public String getAvailableForDimensions() {
        return "2D, 3D";
    }

    @Override
    public String getCategories() {
        return "Visualisation, Graph, Label, Measurements";
    }
}
