package net.haesleinhuepf.clijx.gui;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.ImageWindow;
import ij.gui.Toolbar;
import ij.plugin.tool.PlugInTool;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;


public class InteractiveZoom extends PlugInTool {

    Integer startX = null;
    Integer startY = null;

    Double startMagnification = null;
    Integer startWidth = null;
    Integer startHeight = null;



    @Override
    public void mousePressed(ImagePlus imp, MouseEvent e) {
        //super.mousePressed(imp, e);
        startX = e.getXOnScreen();
        startY = e.getYOnScreen();

        startWidth = imp.getWindow().getWidth();
        startHeight = imp.getWindow().getHeight();

        startMagnification = imp.getWindow().getCanvas().getMagnification();
    }

    @Override
    public void mouseReleased(ImagePlus imp, MouseEvent e) {
        //super.mouseReleased(imp, e);

        startX = null;
        startY = null;

        startMagnification = null;
    }

    @Override
    public void mouseDragged(ImagePlus imp, MouseEvent e) {
        //super.mouseDragged(imp, e);
        if (startX != null && startY != null & startMagnification != null) {

            double newMagnification = startMagnification - (double)(startX - e.getXOnScreen() + startY - e.getYOnScreen()) / 100.0;

            imp.getWindow().getCanvas().setMagnification(newMagnification);

            int newWidth = (int) (startWidth * newMagnification / startMagnification);
            int newHeight = (int) (startHeight * newMagnification / startMagnification);

            int maxWidth = Toolkit.getDefaultToolkit().getScreenSize().width - imp.getWindow().getX();
            int maxHeight = Toolkit.getDefaultToolkit().getScreenSize().height - imp.getWindow().getY();
            if (newWidth > maxWidth) {
                newWidth = maxWidth;
            }
            if (newHeight > maxHeight) {
                newHeight = maxHeight;
            }

            imp.getWindow().setSize(newWidth, newHeight);
            //imp.getWindow().pack();
            imp.updateAndRepaintWindow();
        }
    }

    ImagePlus lastClickedImp = null;
    long lastClickedTime = 0;

    @Override
    public void mouseClicked(ImagePlus imp, MouseEvent e) {
        if (lastClickedImp == imp && System.currentTimeMillis() - lastClickedTime < 1000) {
            imp.getWindow().getCanvas().zoom100Percent();
        }

        lastClickedImp = imp;
        lastClickedTime = System.currentTimeMillis();
    }

    public static void main(String[] args) {
        new ImageJ();
        //IJ.openImage("src/test/resources/blobs.tif").show();
        //IJ.openImage("src/test/resources/blobs.tif").show();


        Toolbar.addPlugInTool(new InteractiveZoom());
        //new InteractiveWindowPosition().run("");

    }


    @Override
    public String getToolName() {
        return "Zoom";
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
                //        0123456789ABCDEF
                /*0*/	 "        #####   " +
                /*1*/	 "       #     #  " +
                /*2*/	 "      #       # " +
                /*3*/	 "     #         #" +
                /*4*/	 "     #         #" +
                /*5*/	 "     #         #" +
                /*6*/	 "     #         #" +
                /*7*/	 "     #         #" +
                /*8*/	 "     #        # " +
                /*9*/	 "    # #      #  " +
                /*A*/	 "   #   ######   " +
                /*B*/	 "  #   #         " +
                /*C*/	 " #   #          " +
                /*D*/	 "#   #           " +
                /*E*/	 " # #            " +
                /*F*/	 "  #             " ;
    }


}
