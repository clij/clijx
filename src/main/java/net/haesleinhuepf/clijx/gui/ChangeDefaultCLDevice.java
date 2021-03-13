package net.haesleinhuepf.clijx.gui;

import fiji.util.gui.GenericDialogPlus;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import net.haesleinhuepf.clij.CLIJ;
import net.haesleinhuepf.clij.macro.AbstractCLIJPlugin;

import java.util.ArrayList;

public class ChangeDefaultCLDevice implements PlugIn {
    @Override
    public void run(String arg) {
        CLIJ clij = CLIJ.getInstance();

        GenericDialog gd = new GenericDialog("Change CL Device");

        ArrayList<String> deviceList = CLIJ.getAvailableDeviceNames();
        if (clij == null) {
            clij = CLIJ.getInstance();
        }
        String[] deviceArray = new String[deviceList.size()];
        deviceList.toArray(deviceArray);
        gd.addChoice("CL_Device", deviceArray, clij.getClearCLContext().getDevice().getName());
        gd.addMessage("Note: If you are using the CLIJ-assistant, close all windows before switching the CL-device.");

        gd.showDialog();
        if (gd.wasCanceled()) {
            return;
        }

        CLIJ.getInstance(gd.getNextChoice());


    }
}
