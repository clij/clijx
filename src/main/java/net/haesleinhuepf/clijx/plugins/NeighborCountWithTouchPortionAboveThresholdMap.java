package net.haesleinhuepf.clijx.plugins;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.plugin.Duplicator;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.haesleinhuepf.clij.macro.CLIJMacroPlugin;
import net.haesleinhuepf.clij.macro.CLIJOpenCLProcessor;
import net.haesleinhuepf.clij.macro.documentation.OffersDocumentation;
import net.haesleinhuepf.clij2.AbstractCLIJ2Plugin;
import net.haesleinhuepf.clij2.CLIJ2;
import net.haesleinhuepf.clij2.utilities.HasClassifiedInputOutput;
import net.haesleinhuepf.clij2.utilities.IsCategorized;
import net.haesleinhuepf.clijx.CLIJx;
import org.scijava.plugin.Plugin;

@Plugin(type = CLIJMacroPlugin.class, name = "CLIJx_neighborCountWithTouchPortionAboveThresholdMap")
public class NeighborCountWithTouchPortionAboveThresholdMap extends AbstractCLIJ2Plugin implements CLIJMacroPlugin, CLIJOpenCLProcessor, OffersDocumentation, IsCategorized, HasClassifiedInputOutput {
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
        return "Image labels, ByRef Image map_destination, Number minimum_touch_portion";
    }

    @Override
    public Object[] getDefaultValues() {
        return new Object[]{null, null, 0.1};
    }

    @Override
    public boolean executeCL() {
        return neighborCountWithTouchPortionAboveThresholdMap (getCLIJ2(), (ClearCLBuffer) args[0], (ClearCLBuffer) args[1], asFloat(args[2]));
    }

    public static boolean neighborCountWithTouchPortionAboveThresholdMap(CLIJ2 clij2, ClearCLBuffer labels, ClearCLBuffer map_destination, Float minimum_touch_portion) {
        int number_of_labels = (int)clij2.maximumOfAllPixels(labels);
        ClearCLBuffer touch_matrix = clij2.create(number_of_labels + 1, number_of_labels + 1);
        //clij2.generateTouchMatrix(labels, touch_matrix);

        ClearCLBuffer touch_count_matrix = clij2.create(touch_matrix);
        clij2.generateTouchCountMatrix(labels, touch_count_matrix);

        //clij2.show(touch_count_matrix, "touch_count_matrix");

        ClearCLBuffer vector = clij2.create(touch_count_matrix.getWidth(), 1);
        ClearCLBuffer vector1 = clij2.create(touch_count_matrix.getWidth(), 1);
        clij2.sumYProjection(touch_count_matrix, vector1);
        clij2.replaceIntensity(vector1, vector, 0, 1);
        //clij2.print(vector);
        vector1.close();

        ClearCLBuffer touch_portion_matrix = clij2.create(touch_matrix);
        clij2.divideImages(touch_count_matrix, vector, touch_portion_matrix);
        touch_count_matrix.close();
        //clij2.show(touch_portion_matrix, "touch_portion_matrix");

        clij2.greaterOrEqualConstant(touch_portion_matrix, touch_matrix, minimum_touch_portion);
        //clij2.setWhereXequalsY(touch_matrix, 0);
        //clij2.setColumn(touch_matrix, 0, 0);
        //clij2.setWhereXgreaterThanY(touch_matrix, 0);

        //clij2.show(touch_matrix, "touch matrix");

        clij2.countTouchingNeighbors(touch_matrix, vector);

        touch_matrix.close();
        touch_portion_matrix.close();

        clij2.replaceIntensities(labels, vector, map_destination);
        vector.close();

        return true;
    }

    public static void main(String[] args) {
        new ImageJ();

        CLIJx clijx = CLIJx.getInstance();
        ImagePlus imp = IJ.openImage("C:/Users/rober/OneDrive/Desktop/tissue_sim.tif");
        imp = new Duplicator().run(imp, 1, 1, 1, 1, 1, 1 );
        ClearCLBuffer input = clijx.push(imp);
        ClearCLBuffer labels = clijx.create(input.getDimensions(), clijx.Float);
        clijx.voronoiLabeling(input, labels);
        clijx.show(labels, "labels");

        ClearCLBuffer map = clijx.create(input.getDimensions(), clijx.Float);
        NeighborCountWithTouchPortionAboveThresholdMap.neighborCountWithTouchPortionAboveThresholdMap(clijx, labels, map, 0.2f);
        clijx.show(map, "map");
    }

    @Override
    public ClearCLBuffer createOutputBufferFromSource(ClearCLBuffer input)
    {
        return getCLIJ2().create(input.getDimensions(), NativeTypeEnum.Float);
    }

    @Override
    public String getDescription() {
        return "Starts from a label map, determines touch portion to neighbors, counts those above a given value (between 0 and 1) and draws a map.\n\n";
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
