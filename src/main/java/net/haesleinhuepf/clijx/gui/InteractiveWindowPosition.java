package net.haesleinhuepf.clijx.gui;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.Toolbar;
import ij.plugin.tool.PlugInTool;
import net.haesleinhuepf.clijx.gui.panel.CLIJxPanel;
import net.haesleinhuepf.clijx.gui.stickyfilters.AbstractStickyFilter;
import net.haesleinhuepf.clijx.gui.stickyfilters.StickyImagePlus;

import java.awt.event.MouseEvent;


public class InteractiveWindowPosition extends PlugInTool {

    Integer startX = null;
    Integer startY = null;

    Integer windowStartX = null;
    Integer windowStartY = null;


    @Override
    public void mousePressed(ImagePlus imp, MouseEvent e) {
        if(CLIJxPanel.isPanel(imp)) {
            CLIJxPanel.mouseDown(
                    imp.getWindow().getCanvas().offScreenX(e.getX()),
                    imp.getWindow().getCanvas().offScreenY(e.getY())
            );
            return;
        }
        //super.mousePressed(imp, e);
        startX = e.getXOnScreen();
        startY = e.getYOnScreen();

        windowStartX = imp.getWindow().getX();
        windowStartY = imp.getWindow().getY();
    }

    @Override
    public void mouseReleased(ImagePlus imp, MouseEvent e) {
        if (CLIJxPanel.isPanel(imp)) {
            CLIJxPanel.mouseUp(
                    imp.getWindow().getCanvas().offScreenX(e.getX()),
                    imp.getWindow().getCanvas().offScreenY(e.getY())
            );
            return;
        } else if (imp instanceof StickyImagePlus) {
            AbstractStickyFilter.handleCoordinates((StickyImagePlus) imp);
        } else {
            CLIJxPanel.considerTaking(imp);
        }
        //super.mouseReleased(imp, e);

        startX = null;
        startY = null;

        windowStartX = null;
        windowStartY = null;
    }

    @Override
    public void mouseDragged(ImagePlus imp, MouseEvent e) {
        if(CLIJxPanel.isPanel(imp)) {
            CLIJxPanel.mouseMove(
                    imp.getWindow().getCanvas().offScreenX(e.getX()),
                    imp.getWindow().getCanvas().offScreenY(e.getY())
            );
            return;
        }
        //super.mouseDragged(imp, e);
        if (startX != null && startY != null & windowStartX != null & windowStartY != null) {
            imp.getWindow().setLocation(
                    windowStartX - startX + e.getXOnScreen(),
                    windowStartY - startY + e.getYOnScreen());
        }
    }


    public static void main(String[] args) {
        new ImageJ();
        IJ.openImage("src/test/resources/blobs.tif").show();
        IJ.openImage("src/test/resources/blobs.tif").show();


        Toolbar.addPlugInTool(new InteractiveWindowPosition());
        //new InteractiveWindowPosition().run("");

    }


    @Override
    public String getToolName() {
        return "Window Position";
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
                /*0*/	 "       ##       " +
                /*1*/	 "      ####      " +
                /*2*/	 "     ######     " +
                /*3*/	 "       ##       " +
                /*4*/	 "   #   ##   #   " +
                /*5*/	 "  ##   ##   ##  " +
                /*6*/	 " ###   ##   ### " +
                /*7*/	 "################" +
                /*8*/	 "################" +
                /*9*/	 " ###   ##   ### " +
                /*A*/	 "  ##   ##   ##  " +
                /*B*/	 "   #   ##   #   " +
                /*C*/	 "       ##       " +
                /*D*/	 "     ######     " +
                /*E*/	 "      ####      " +
                /*F*/	 "       ##       " ;
    }


}
