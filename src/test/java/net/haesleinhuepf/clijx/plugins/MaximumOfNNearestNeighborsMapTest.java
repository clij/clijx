package net.haesleinhuepf.clijx.plugins;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clijx.CLIJx;

public class MaximumOfNNearestNeighborsMapTest {
    public static void main(String... args) {
        ImagePlus t = IJ.openImage("src/test/resources/tissue.tif");
        ImagePlus m = IJ.openImage("src/test/resources/measurements.tif");

        CLIJx clijx = CLIJx.getInstance();
        ClearCLBuffer tissue = clijx.push(t);
        ClearCLBuffer measurement = clijx.push(m);

        ClearCLBuffer result = clijx.create(measurement);

        new ImageJ();
        clijx.maximumOfNNearestNeighborsMap(measurement, tissue, result, 1);

        clijx.show(result, "Result");
    }
}
