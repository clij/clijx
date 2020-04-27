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

public class InteractiveThresholding extends PlugInTool {


    private ClearCLBuffer input;
    private ClearCLBuffer output;
    private ClearCLBuffer temp1;
    private ClearCLBuffer temp2;
    private ImagePlus imp;
    private Integer startX = null;
    private Integer startY = null;

    private Float lowerThreshold = 0f;
    private Float upperThreshold = 0f;

    private CLIJx clijx;


    @Override
    public void run(String arg) {
        System.out.println("init");
        IJ.showStatus("Thresholding: CLIJx initializing...");
        this.imp = IJ.getImage();
        refresh();
        this.imp.killRoi();
        this.imp = null;
        IJ.showStatus("Thresholding: CLIJx Ready.");
        super.run(arg);
    }

    public void mousePressed(ImagePlus imp, MouseEvent e) {
        if (this.imp != imp) {
            clijx = CLIJx.getInstance();
            this.imp = imp;
            imp.killRoi();
            input = clijx.pushCurrentSlice(imp);

            temp1 = clijx.create(input.getDimensions(), clijx.Float);
            temp2 = clijx.create(input.getDimensions(), clijx.Float);
            output = clijx.create(input.getDimensions(), clijx.UnsignedByte);


            startX = imp.getWindow().getCanvas().offScreenX(e.getX());
            startY = imp.getWindow().getCanvas().offScreenY(e.getY());


            lowerThreshold = imp.getProcessor().getf(startX, startY);
            upperThreshold = imp.getProcessor().getf(startX, startY);

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
                this.imp.killRoi();
                this.imp = null;

                clijx.release(input);
                clijx.release(temp1);
                clijx.release(temp2);
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

                    lowerThreshold = (float)stats.min;
                    upperThreshold = (float)stats.max;
                    refresh();
                }
            }
        }
        e.consume();
    }

    private void refresh() {
        try {
            clijx.activateSizeIndependentKernelCompilation();
            clijx.smallerOrEqualConstant(input, temp1, upperThreshold);

            clijx.activateSizeIndependentKernelCompilation();
            clijx.greaterOrEqualConstant(input, temp2, lowerThreshold);

            clijx.activateSizeIndependentKernelCompilation();
            clijx.binaryAnd(temp1, temp2, output);

            Roi roi = clijx.pullAsROI(output);
            imp.setRoi(roi);

        } catch (Exception e) {
            System.out.println(e.getStackTrace().toString().getBytes());
        }
    }

    public static void main(String... args) {
        new ImageJ();

        Toolbar.addPlugInTool(new InteractiveThresholding());
        ImagePlus imp = IJ.openImage("C:\\structure\\data\\covid-chestxray-dataset\\images\\1-s2.0-S0929664620300449-gr3_lrg-d.jpg");
        //"../clij2-tests/src/test/resources/blobs.tif");
        imp.show();



    }

    @Override
    public String getToolName() {
        return "Blur and threshold";
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
                /*0*/	 "                " +
                /*1*/	 "      #         " +
                /*2*/	 "                " +
                /*3*/	 "      #         " +
                /*4*/	 "#               " +
                /*5*/	 "#     #         " +
                /*6*/	 "#           ##  " +
                /*7*/	 "##    #    #### " +
                /*8*/	 "##         #### " +
                /*9*/	 "###   #    #####" +
                /*A*/	 "#####     ######" +
                /*B*/	 "################" +
                /*C*/	 "                " +
                /*D*/	 "      #         " +
                /*E*/	 "                " +
                /*F*/	 "                " ;
    }
}
