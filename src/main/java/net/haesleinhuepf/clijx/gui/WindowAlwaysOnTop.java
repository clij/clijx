package net.haesleinhuepf.clijx.gui;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.PlugIn;

public class WindowAlwaysOnTop implements PlugIn {

    @Override
    public void run(String arg) {
        ImagePlus imp = IJ.getImage();
        imp.getWindow().setAlwaysOnTop(!imp.getWindow().isAlwaysOnTop());
    }
}
