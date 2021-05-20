package net.haesleinhuepf.clijx.plugins;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clijx.CLIJx;
import net.haesleinhuepf.clijx.clij2wrappers.EuclideanDistanceFromLabelCentroidMap;

public class EuclideanDistanceFromLabelCentroidTest {
    public static void main(String[] args) {
        new ImageJ();

        ImagePlus imp = IJ.openImage("src/test/resources/blobs.tif");

        CLIJx clijx = CLIJx.getInstance();

        ClearCLBuffer input = clijx.push(imp);
        ClearCLBuffer binary = clijx.create(input);
        ClearCLBuffer labels = clijx.create(input);
        ClearCLBuffer edcm = clijx.create(input.getDimensions(), input.getNativeType());

        clijx.thresholdOtsu(input, binary);
        clijx.connectedComponentsLabelingBox(binary, labels);

        EuclideanDistanceFromLabelCentroidMap.euclideanDistanceFromLabelCentroidMap(clijx, labels, edcm);

        clijx.show(edcm, "edcm");


    }

}
