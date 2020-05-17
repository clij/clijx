package net.haesleinhuepf.clijx.gui.panel;

import ij.IJ;
import ij.ImageJ;
import ij.ImageListener;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.NewImage;
import ij.gui.Toolbar;
import ij.plugin.Duplicator;
import ij.plugin.PlugIn;
import net.haesleinhuepf.clij.CLIJ;
import net.haesleinhuepf.clijx.CLIJx;
import net.haesleinhuepf.clijx.gui.InteractiveWindowPosition;

import java.util.ArrayList;

public class CLIJxPanel implements PlugIn, ImageListener {

    CLIJx clijx;


    //CLIJx clijxSecondary;

    @Override
    public void run(String s) {
        clijx = CLIJx.getInstance();
        //clijxSecondary = clijx;
        clijx.setWaitForKernelFinish(true);

        GenericDialog gd = new GenericDialog("SPIMcat viewer");
        //gd.addMessage("Running on GPU: " + clijx.getGPUName());

        ArrayList<String> gpuNameList = CLIJ.getAvailableDeviceNames();
        String[] gpuChoice = new String[gpuNameList.size()];
        gpuNameList.toArray(gpuChoice);

        gd.addChoice("Primary GPU", gpuChoice, clijx.getGPUName());
        //gd.addChoice("Secondary GPU", gpuChoice, clijx.getGPUName());

        gd.addNumericField("Width", 500, 0);
        gd.addNumericField("Height", 1000, 0);
        gd.showDialog();
        if (gd.wasCanceled()) {
            return;
        }


        int primaryGPU = gd.getNextChoiceIndex();
        //int secondaryGPU = gd.getNextChoiceIndex();
        //if (primaryGPU == secondaryGPU) {
        //    if (clijx.getGPUName().compareTo(gpuNameList.get(primaryGPU)) != 0) {
                clijx = new CLIJx(new CLIJ(primaryGPU));
        //    }
//            clijxSecondary = clijx;
//        } else {
//            if (clijx.getGPUName().compareTo(gpuNameList.get(primaryGPU)) != 0) {
//                clijx = new CLIJx(new CLIJ(primaryGPU));
//            }
//            if (clijxSecondary.getGPUName().compareTo(gpuNameList.get(secondaryGPU)) != 0) {
//                clijxSecondary = new CLIJx(new CLIJ(secondaryGPU));
//            }
//        }


        int width = (int) gd.getNextNumber();
        int height = (int) gd.getNextNumber();

        Panel panel = Panel.getInstance();
        panel.setWidth(width);
        panel.setHeight(height);
        panel.setCLIJx(clijx);
        //(width, height, clijx, clijxSecondary).show();
        panel.show();

        Toolbar.addPlugInTool(new InteractiveWindowPosition());

        ImagePlus.addImageListener(this);
    }


    public static void main(String[] args) {
        new ImageJ();
        ImagePlus imp = IJ.openImage("C:/structure/data/blobs.tif");

        imp.setRoi(10, 10, 100, 100);
        new Duplicator().run(imp).show();

        imp.setRoi(50, 50, 100, 100);
        new Duplicator().run(imp).show();

        NewImage.createFloatImage("A", 100, 100, 1, NewImage.FILL_NOISE).show();
        NewImage.createFloatImage("B", 100, 100, 1, NewImage.FILL_RANDOM).show();
        NewImage.createFloatImage("C", 100, 100, 1, NewImage.FILL_NOISE).show();

        new CLIJxPanel().run("");
    }

    public static boolean isPanel(ImagePlus imp) {
        return Panel.getInstance().isPanel(imp);
    }

    public static void mouseDown(int x, int y) {
        Panel.getInstance().mouseDown(x, y);
    }
    public static void mouseMove(int x, int y) {
        Panel.getInstance().mouseMove(x, y);
    }
    public static void mouseUp(int x, int y) {
        Panel.getInstance().mouseUp(x, y);
    }

    public static void considerTaking(ImagePlus imp) {
        Panel.getInstance().considerTaking(imp);
    }


    @Override
    public void imageOpened(ImagePlus imp) {

    }

    @Override
    public void imageClosed(ImagePlus imp) {
        if (isPanel(imp)) {
            Panel.getInstance().destroy();
        }
    }

    int formerWidth = 0;
    int formerHeight = 0;

    @Override
    public void imageUpdated(ImagePlus imp) {
        if (isPanel(imp)) {
            if (formerHeight != 0 && formerWidth != 0 && formerWidth != imp.getWindow().getWidth() && formerHeight != imp.getWindow().getHeight()) {
                Panel.getInstance().changeSize(imp.getWindow().getWidth() - 20, imp.getWindow().getHeight() - 20);
            }

            formerWidth = imp.getWindow().getWidth();
            formerHeight = imp.getWindow().getHeight();
        }
    }

}
