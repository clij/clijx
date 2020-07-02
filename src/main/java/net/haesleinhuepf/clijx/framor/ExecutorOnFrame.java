package net.haesleinhuepf.clijx.framor;

import ij.ImagePlus;

public class ExecutorOnFrame implements Runnable {
    private final ImagePlus input;
    private ImagePlus output = null;
    private int frame;
    private final FrameProcessor frameProcessor;

    public ExecutorOnFrame(ImagePlus input, int frame, FrameProcessor frameProcessor) {
        this.input = input;
        this.frame = frame;
        this.frameProcessor = frameProcessor;
    }

    @Override
    public void run() {
        System.out.println("Processing frame " + frame + " on " + frameProcessor.getCLIJ2() + " " + frameProcessor.getCLIJ2().getGPUName());
        output = frameProcessor.process(input);
    }

    public ImagePlus getOutput() {
        return output;
    }

    public int getFrame() {
        return frame;
    }

    public FrameProcessor getProcessor() {
        return frameProcessor;
    }
}
