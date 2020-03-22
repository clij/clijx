package net.haesleinhuepf.clijx.gui;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.Line;
import ij.gui.Roi;
import ij.gui.Toolbar;
import ij.measure.Measurements;
import ij.plugin.Selection;
import ij.plugin.tool.PlugInTool;
import ij.process.ImageStatistics;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clijx.CLIJx;

import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class InteractiveWand extends PlugInTool {


    private ClearCLBuffer input;
    private ClearCLBuffer output;
    private ClearCLBuffer temp;
    private ImagePlus imp;
    private Integer startX = null;
    private Integer startY = null;

    private Float threshold = 0f;

    private CLIJx clijx;

    @Override
    public void run(String arg) {
        IJ.showStatus("Wand: CLIJx initializing...");
        this.imp = IJ.getImage();
        threshold = (float)imp.getStatistics(Measurements.MEAN).mean;
        refresh();
        this.imp.killRoi();
        this.imp = null;
        IJ.showStatus("Wand: CLIJx Ready.");
        super.run(arg);
    }

    public void mousePressed(ImagePlus imp, MouseEvent e) {
        if (this.imp != imp) {
            clijx = CLIJx.getInstance();
            this.imp = imp;
            imp.killRoi();
            input = clijx.pushCurrentSlice(imp);

            temp = clijx.create(input.getDimensions(), clijx.UnsignedByte);
            output = clijx.create(input.getDimensions(), clijx.UnsignedByte);

            startX = imp.getWindow().getCanvas().offScreenX(e.getX());
            startY = imp.getWindow().getCanvas().offScreenY(e.getY());

            threshold = imp.getProcessor().getf(startX, startY);

            refresh();
        }
        e.consume();
    }

    public void mouseReleased(ImagePlus imp, MouseEvent e) {
        if (this.imp == imp) {
            synchronized (this)
            {
                startX = null;
                startY = null;
                this.imp = null;

                clijx.release(input);
                clijx.release(temp);
                clijx.release(output);
            }
        }
        e.consume();
    }

    public void mouseDragged(ImagePlus imp, MouseEvent e)
    {
        if (this.imp == imp) {
            synchronized (this) {
                if (startX != null && startY != null) {

                    int x = imp.getWindow().getCanvas().offScreenX(e.getX());
                    int y = imp.getWindow().getCanvas().offScreenY(e.getY());

                    Roi lineRoi = new Line(startX, startY, x, y);
                    lineRoi = Selection.lineToArea(lineRoi);
                    imp.setRoi(lineRoi);
                    ImageStatistics stats = imp.getStatistics(ImageStatistics.MIN_MAX);

                    threshold = (float)stats.max;



                    refresh();
                }
            }
        }
        e.consume();
    }

    private void refresh() {
        //System.out.println("ch " + threshold);

        try {
            clijx.activateSizeIndependentKernelCompilation();
            clijx.smallerConstant(input, temp, threshold);

            clijx.activateSizeIndependentKernelCompilation();
            clijx.drawBox(temp, startX, startY, 0, 2, 2, 1, 2);

            clijx.activateSizeIndependentKernelCompilation();
            clijx.floodFillDiamond(temp, output, 1, 2);

            clijx.activateSizeIndependentKernelCompilation();
            clijx.equalConstant(output, temp, 2);

            clijx.activateSizeIndependentKernelCompilation();
            clijx.binaryFillHoles(temp, output);

            Roi roi = clijx.pullAsROI(output);
            imp.setRoi(roi);

        } catch (Exception e) {
            System.out.println(e.getStackTrace().toString().getBytes());
        }
    }

    public static void main(String... args) {
        new ImageJ();

        Toolbar.addPlugInTool(new InteractiveWand());
        ImagePlus imp = IJ.openImage("C:\\structure\\data\\covid-chestxray-dataset\\images\\1-s2.0-S0929664620300449-gr3_lrg-d.jpg");
                //"../clij2-tests/src/test/resources/blobs.tif");
        imp.show();
    }

    @Override
    public String getToolName() {
        return "Interactive Wand";
    }

    @Override
    public String getToolIcon()
    {
        return Utilities.generateIconCodeString(
                getToolIconString()
        );

    }

    public static String getToolIconString()
    {
        return
                //        0123456789ABCDEF
                /*0*/	 "  # # # # #     " +
                /*1*/	 "            #   " +
                /*2*/	 "#               " +
                /*3*/	 "            #   " +
                /*4*/	 "#               " +
                /*5*/	 "  # # # # #     " +
                /*6*/	 "      ##        " +
                /*7*/	 "      ###       " +
                /*8*/	 "      ####      " +
                /*9*/	 "      #####     " +
                /*A*/	 "      ######    " +
                /*B*/	 "      #######   " +
                /*C*/	 "      #####     " +
                /*D*/	 "      ## ###    " +
                /*E*/	 "      #   ###   " +
                /*F*/	 "           ##   " ;
    }
}
