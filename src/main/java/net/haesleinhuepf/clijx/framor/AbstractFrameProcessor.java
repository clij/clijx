package net.haesleinhuepf.clijx.framor;

import net.haesleinhuepf.clij2.CLIJ2;

public abstract class AbstractFrameProcessor implements FrameProcessor {
    private CLIJ2 clij2;
    private int frame;


    @Override
    public void setCLIJ2(CLIJ2 clij2) {
        this.clij2 = clij2;
    }

    @Override
    public CLIJ2 getCLIJ2() {
        return clij2;
    }

    @Override
    public void setFrame(int frame) {
        this.frame = frame;
    }

    @Override
    public int getFrame() {
        return frame;
    }
}
