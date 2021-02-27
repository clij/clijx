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

@Plugin(type = CLIJMacroPlugin.class, name = "CLIJx_drawTouchPortionMeshBetweenTouchingLabels")
public class DrawTouchPortionMeshBetweenTouchingLabels extends AbstractCLIJ2Plugin implements CLIJMacroPlugin, CLIJOpenCLProcessor, OffersDocumentation, IsCategorized, HasClassifiedInputOutput {
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
        return drawTouchPortionMeshBetweenTouchingLabels (getCLIJ2(), (ClearCLBuffer) args[0], (ClearCLBuffer) args[1]);
    }

    public static boolean drawTouchPortionMeshBetweenTouchingLabels(CLIJ2 clij2, ClearCLBuffer labels, ClearCLBuffer result) {
        int number_of_labels = (int)clij2.maximumOfAllPixels(labels);
        ClearCLBuffer touch_matrix = clij2.create(number_of_labels + 1, number_of_labels + 1);
        clij2.generateTouchMatrix(labels, touch_matrix);

        ClearCLBuffer touch_count_matrix = clij2.create(touch_matrix);
        ClearCLBuffer touch_count_matrix1 = clij2.create(touch_matrix);
        clij2.generateTouchCountMatrix(labels, touch_count_matrix1);
        clij2.touchMatrixToAdjacencyMatrix(touch_count_matrix1, touch_count_matrix);
        clij2.setWhereXequalsY(touch_count_matrix, 0);
        touch_count_matrix1.close();

        ClearCLBuffer vector = clij2.create(touch_count_matrix.getWidth(), 1);
        clij2.sumYProjection(touch_count_matrix, vector);

        ClearCLBuffer touch_portion_matrix = clij2.create(touch_matrix);
        clij2.divideImages(touch_count_matrix, vector, touch_portion_matrix);
        touch_count_matrix.close();
        vector.close();

        ClearCLBuffer touch_portion_touch_matrix = clij2.create(touch_matrix);
        clij2.multiplyImages(touch_matrix, touch_portion_matrix, touch_portion_touch_matrix);
        touch_matrix.close();
        touch_portion_matrix.close();

        ClearCLBuffer pointlist = clij2.create(number_of_labels, labels.getDimension());
        clij2.centroidsOfLabels(labels, pointlist);

        clij2.set(result, 0);
        clij2.touchMatrixToMesh(pointlist, touch_portion_touch_matrix, result);

        pointlist.close();
        touch_portion_touch_matrix.close();

        return true;
    }

    @Override
    public ClearCLBuffer createOutputBufferFromSource(ClearCLBuffer input)
    {
        return getCLIJ2().create(input.getDimensions(), NativeTypeEnum.Float);
    }

    @Override
    public String getDescription() {
        return "Starting from a label map, draw lines between touching neighbors resulting in a mesh.\n\n" +
                "The end points of the lines correspond to the centroids of the labels. The intensity of the lines \n" +
                "corresponds to the touch portion between these labels.";
    }

    @Override
    public String getAvailableForDimensions() {
        return "2D, 3D";
    }


    @Override
    public String getCategories() {
        return "Measurement, Graph, Label";
    }
}
