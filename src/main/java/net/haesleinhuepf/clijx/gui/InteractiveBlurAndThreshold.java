package net.haesleinhuepf.clijx.gui;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.gui.Toolbar;
import ij.plugin.tool.PlugInTool;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clijx.CLIJx;

import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class InteractiveBlurAndThreshold extends PlugInTool {


    private ClearCLBuffer input;
    private ClearCLBuffer output;
    private ClearCLBuffer temp;
    private ImagePlus imp;
    private Integer startX = null;
    private Integer startY = null;

    private Float sigma = 2f;
    private Float threshold = 0f;

    private Float startSigma;
    private Float startThreshold;
    private CLIJx clijx;

    public void mousePressed(ImagePlus imp, MouseEvent e) {
        if (this.imp != imp) {
            clijx = CLIJx.getInstance();
            this.imp = imp;
            imp.killRoi();
            input = clijx.pushCurrentSlice(imp);

            threshold = (float)clijx.meanOfAllPixels(input);

            temp = clijx.create(input.getDimensions(), clijx.Float);
            output = clijx.create(input.getDimensions(), clijx.UnsignedByte);

            startX = e.getX();
            startY = e.getY();
            startThreshold = threshold;
            startSigma = sigma;

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

                clijx.release(input);
                clijx.release(temp);
                clijx.release(output);
                temp = null;
                input = null;
                output = null;

                ClearCLBuffer in = clijx.push(imp);
                ClearCLBuffer tm = clijx.create(in.getDimensions(), clijx.Float);
                ClearCLBuffer ou = clijx.create(in.getDimensions(), clijx.UnsignedByte);

                if (in.getDimension()  > 2) {
                    clijx.blur(in, tm, sigma, sigma, 0);
                } else {
                    clijx.blur(in, tm, sigma, sigma);
                }
                clijx.threshold(tm, ou, threshold);

                clijx.show(ou, imp.getTitle() + " blur" + sigma + " thr" + threshold);
                this.imp = null;
            }
        }
        e.consume();
    }

    public void mouseDragged(ImagePlus imp, MouseEvent e)
    {
        if (this.imp == imp) {
            synchronized (this) {
                if (startX != null && startY != null) {
                    threshold = startThreshold - (startX - e.getX());
                    sigma = startSigma - (startY - e.getY()) * 0.1f;
                    if (sigma < 0) {
                        sigma = 0f;
                    }
                    refresh();
                }
            }
        }
        e.consume();
    }

    private void refresh() {
        System.out.println("ch " + threshold);

        try {
            clijx.blur(input, temp, sigma, sigma);
            clijx.threshold(temp, output, threshold);
            //clijx.binaryEdgeDetection(temp, output);
            Roi roi = clijx.pullAsROI(output);
            imp.setRoi(roi);
            //clijx.showRGB(output, input, output, title);
        } catch (Exception e) {
            try {
                Files.write(Paths.get("C:/structure/temp/log.txt"), e.getStackTrace().toString().getBytes());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void main(String... args) {
        new ImageJ();

        Toolbar.addPlugInTool(new InteractiveBlurAndThreshold());
        ImagePlus imp = IJ.openImage("src/test/resources/blobs.tif");
        imp.show();



    }

    @Override
    public String getToolName() {
        return "Blur and threshold";
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
