package net.haesleinhuepf.clijx.gui.stickyfilters.implementations;

import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clijx.CLIJx;
import net.haesleinhuepf.clijx.gui.stickyfilters.AbstractStickyFilter;

public class NumberOfTouchingNeighbors extends AbstractStickyFilter {
    protected void computUnaryOperation(CLIJx clijx, ClearCLBuffer input, ClearCLBuffer output) {
        int number_of_labels = (int) clijx.maximumOfAllPixels(input);
        ClearCLBuffer touch_matrix = clijx.create(new long[]{number_of_labels + 1, number_of_labels + 1}, clijx.UnsignedByte);
        clijx.generateTouchMatrix(input, touch_matrix);
        ClearCLBuffer touch_count_vector = clijx.create(new long[]{number_of_labels + 1, 1, 1}, clijx.UnsignedByte);
        clijx.countTouchingNeighbors(touch_matrix, touch_count_vector);
        clijx.replaceIntensities(input, touch_count_vector, output);
        touch_matrix.close();
        touch_count_vector.close();
    }
}
