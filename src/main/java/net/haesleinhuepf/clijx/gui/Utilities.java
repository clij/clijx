package net.haesleinhuepf.clijx.gui;

import ij.IJ;
import ij.gui.Toolbar;

public class Utilities {

    public static boolean ignoreEvent = false;

    public static void installTools() {
        String tool = IJ.getToolName();
        ignoreEvent = true;
        //Toolbar.removeMacroTools();


        Toolbar.addPlugInTool(new ContinuousWebcamAcquisition());
        Toolbar.addPlugInTool(new InteractiveBrightnessContrast());
        //Toolbar.addPlugInTool(new InteractiveZoom());
        Toolbar.addPlugInTool(new InteractiveWindowPosition());
        //Toolbar.addPlugInTool(new InteractiveWand());
        //Toolbar.addPlugInTool(new InteractiveRotation());
        //Toolbar.addPlugInTool(new InteractiveThresholding());
        //Toolbar.addPlugInTool(new InteractiveBlurAndThreshold());
        //Toolbar.addPlugInTool(new InteractiveTopMaxAndThreshold());
        //Toolbar.addPlugInTool(new InteractiveDifferenceOfGaussian());
        //Toolbar.addPlugInTool(new InteractiveSpotDetection());

        ignoreEvent = false;

        IJ.setTool(tool);
    }


    public static String generateIconCodeString(String icon)
    {
        String[] positions = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f", "0"};

        String result = "C000";
        int x = 0;
        int y = 0;

        char empty = new String(" ").charAt(0);
        //DebugHelper.print(new Object(), "len: " + icon.length());
        for (int i = 0; i < icon.length(); i++)
        {
            //DebugHelper.print(new Object(), "|" + icon.charAt(i) + " == " + empty + "|");
            if (icon.charAt(i) != empty)
            {
                result = result.concat("D" + positions[x] + positions[y]);
            }

            x++;
            if (x > 15)
            {
                x = 0;
                y++;
            }
        }
        //DebugHelper.print(new Object(), result);
        return result;
    }
}
