package net.haesleinhuepf.clijx.gui;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.gui.Toolbar;
import ij.plugin.tool.PlugInTool;
import ij.process.ImageProcessor;
import ij.process.LUT;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clijx.CLIJx;

import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class InteractiveRotation extends PlugInTool {


    private ClearCLBuffer input;
    private ClearCLBuffer output;
    private ImagePlus imp;
    private Integer startX = null;
    private Integer startY = null;

    private Float angle = 0f;

    private Float startAngle;
    private CLIJx clijx;

    ImageProcessor backup = null;

    public void mousePressed(ImagePlus imp, MouseEvent e) {
        if (this.imp != imp) {
            backup = imp.getProcessor();
            clijx = CLIJx.getInstance();
            this.imp = imp;
            imp.killRoi();
            input = clijx.pushCurrentSlice(imp);

            output = clijx.create(input);

            startX = e.getX();
            startY = e.getY();
            startAngle = angle;

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
                clijx.release(output);
                input = null;
                output = null;
                LUT lut = backup.getLut();
                this.imp.setProcessor(backup);
                backup = null;

                ClearCLBuffer in = clijx.push(imp);
                ClearCLBuffer ou = clijx.create(in);

                if (in.getDimension()  > 2) {
                    clijx.rotate3D(in, ou, 0, 0, angle, true);
                } else {
                    clijx.rotate2D(in, ou, angle, true);
                }

                clijx.show(ou, imp.getTitle() + " rot" + angle);
                IJ.getImage().setLut(lut);

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
                    angle = startAngle - (startX - e.getX()) - (startY - e.getY()) * 0.1f;
                    refresh();
                }
            }
        }
        e.consume();
    }

    private void refresh() {
        System.out.println("ang " + angle);

        try {
            clijx.rotate2D(input, output, angle, true);

            ImagePlus result = clijx.pull(output);
            result.getProcessor().setLut(backup.getLut());
            imp.setProcessor(result.getProcessor());
            //clijx.binaryEdgeDetection(temp, output);
            //Roi roi = clijx.pullAsROI(output);
            //imp.setRoi(roi);
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

        Toolbar.addPlugInTool(new InteractiveRotation());
        ImagePlus imp = IJ.openImage("src/test/resources/blobs.tif");
        imp.show();
    }

    @Override
    public String getToolName() {
        return "Rotate";
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
                /*1*/	 "                " +
                /*2*/	 "       ####     " +
                /*3*/	 "     ##    ##   " +
                /*4*/	 " #  #        #  " +
                /*5*/	 " # #          # " +
                /*6*/	 " ##           # " +
                /*7*/	 " ####          #" +
                /*8*/	 "               #" +
                /*9*/	 "               #" +
                /*A*/	 "   #           #" +
                /*B*/	 "   #          # " +
                /*C*/	 "    #        #  " +
                /*D*/	 "     ##    ##   " +
                /*E*/	 "       ####     " +
                /*F*/	 "                " +
                /*0*/	 "                " ;

    }
}
