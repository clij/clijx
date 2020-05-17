package net.haesleinhuepf.clijx.gui;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.Toolbar;
import ij.plugin.tool.PlugInTool;

import java.awt.event.MouseEvent;


public class InteractiveBrightnessContrast extends PlugInTool {

    Integer startX = null;
    Integer startY = null;

    Double window = null;
    Double center = null;


    @Override
    public void mousePressed(ImagePlus imp, MouseEvent e) {
        //super.mousePressed(imp, e);
        startX = e.getXOnScreen();
        startY = e.getYOnScreen();

        window = imp.getDisplayRangeMax() - imp.getDisplayRangeMin();
        center = (imp.getDisplayRangeMax() + imp.getDisplayRangeMin()) / 2;
    }

    @Override
    public void mouseReleased(ImagePlus imp, MouseEvent e) {
        //super.mouseReleased(imp, e);

        startX = null;
        startY = null;

        window = null;
        center = null;
    }

    @Override
    public void mouseDragged(ImagePlus imp, MouseEvent e) {
        //super.mouseDragged(imp, e);
        if (startX != null && startY != null & window != null & center != null) {

            double newCenter = center - startX + e.getXOnScreen();
            double newWidth = window - startY + e.getYOnScreen();
            if (newWidth < 0) {
                newWidth = window;
            }

            double newMin = newCenter - newWidth / 2;
            double newMax = newCenter + newWidth / 2;
            imp.setDisplayRange(newMin, newMax);
            imp.updateAndDraw();


            //imp.getWindow().setLocation(
            //        window - startX + e.getXOnScreen(),
            //        center - startY + e.getYOnScreen());
        }
    }


    public static void main(String[] args) {
        new ImageJ();
        IJ.openImage("src/test/resources/blobs.tif").show();
        IJ.openImage("src/test/resources/blobs.tif").show();


        Toolbar.addPlugInTool(new InteractiveBrightnessContrast());
        //new InteractiveWindowPosition().run("");

    }

    @Override
    public String getToolName() {
        return "Brightness contrast";
    }

    @Override
    public String 	getToolIcon()
    {
        return Utilities.generateIconCodeString(
                getToolIconString()
        );

    }

    public static String getToolIconString()
    {
        return
                //0123456789ABCDEF
                /*0*/	 "### #   #       " +
                /*1*/	 "## #  #   #   # " +
                /*2*/	 "### #   #       " +
                /*3*/	 "## #  #   #     " +
                /*4*/	 "### #   #       " +
                /*5*/	 "## #  #   #   # " +
                /*6*/	 "### #   #       " +
                /*7*/	 "## #  #   #     " +
                /*8*/	 "### #   #       " +
                /*9*/	 "## #  #   #   # " +
                /*A*/	 "### #   #       " +
                /*B*/	 "## #  #   #     " +
                /*C*/	 "### #   #       " +
                /*D*/	 "## #  #   #   # " +
                /*E*/	 "### #   #       " +
                /*F*/	 "## #  #   #     " ;
    }

}
