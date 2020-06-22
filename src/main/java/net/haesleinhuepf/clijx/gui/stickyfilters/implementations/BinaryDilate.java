package net.haesleinhuepf.clijx.gui.stickyfilters.implementations;

import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clijx.CLIJx;
import net.haesleinhuepf.clijx.gui.stickyfilters.AbstractStickyFilter;

public class BinaryDilate extends AbstractStickyFilter {
    protected void computUnaryOperation(CLIJx clijx, ClearCLBuffer input, ClearCLBuffer output) {
        clijx.erodeBox(input, output);
    }
}
