package net.haesleinhuepf.clijx.framor;

import ij.ImagePlus;
import net.haesleinhuepf.clij2.CLIJ2;
import net.imagej.ops.Ops;

public interface FrameProcessor {
    void setCLIJ2(CLIJ2 clij2);
    CLIJ2 getCLIJ2();
    ImagePlus process(ImagePlus input);
    FrameProcessor duplicate();

    void setFrame(int frame);
    int getFrame();

    long getMemoryNeedInBytes(ImagePlus imp);
}
