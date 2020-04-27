package net.haesleinhuepf.clijx.gui;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.Overlay;
import ij.gui.PointRoi;
import ij.gui.Roi;
import ij.gui.Toolbar;
import ij.plugin.tool.PlugInTool;
import ij.process.ImageProcessor;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clijx.CLIJx;

import java.awt.event.MouseEvent;

public class InteractiveSpotDetection extends PlugInTool {


    private ClearCLBuffer input;
    private ClearCLBuffer output;
    private ClearCLBuffer temp1;
    private ClearCLBuffer temp2;
    private ImagePlus imp;
    private Integer startX = null;
    private Integer startY = null;

    private double sigma2 = 0f;
    private double sigma1 = 0f;

    private CLIJx clijx;

    ImageProcessor backup = null;


    @Override
    public void run(String arg) {
        System.out.println("init");
        IJ.showStatus("DoG: CLIJx initializing...");
        this.imp = IJ.getImage();
        refresh();
        this.imp.killRoi();
        this.imp = null;
        IJ.showStatus("DoG: CLIJx Ready.");
        super.run(arg);
    }

    public void mousePressed(ImagePlus imp, MouseEvent e) {
        if (this.imp != imp) {
            clijx = CLIJx.getInstance();
            this.imp = imp;
            backup = imp.getProcessor();
            imp.killRoi();
            input = clijx.pushCurrentSlice(imp);

            temp1 = clijx.create(input.getDimensions(), clijx.Float);
            temp2 = clijx.create(input.getDimensions(), clijx.Float);
            output = clijx.create(input.getDimensions(), clijx.Float);



            startX = imp.getWindow().getCanvas().offScreenX(e.getX());
            startY = imp.getWindow().getCanvas().offScreenY(e.getY());

            Overlay overlay = new Overlay();
            overlay.add(new PointRoi(startX, startY));
            imp.setOverlay(overlay);


            sigma2 = imp.getProcessor().getf(startX, startY);
            sigma1 = imp.getProcessor().getf(startX, startY);

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
                this.imp.setOverlay(null);
                this.imp = null;

                ImagePlus result = clijx.pull(output);
                result.setTitle("Spot detection DoG s1=" + sigma1 + " s1=" + sigma2);
                result.show();

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

                    sigma1 = (startX - x) / 10.0;
                    sigma2 = (startY - y) / 10.0;
                    refresh();
                }
            }
        }
        e.consume();
    }

    private void refresh() {
        try {
            clijx.activateSizeIndependentKernelCompilation();
            clijx.gaussianBlur3D(input, temp1, sigma1, sigma1, 0);

            clijx.activateSizeIndependentKernelCompilation();
            clijx.gaussianBlur3D(input, output, sigma2, sigma2, 0);

            clijx.activateSizeIndependentKernelCompilation();
            clijx.subtractImages(temp1, output, temp2);

            if (sigma1 < 0) {
                clijx.detectMaximaBox(temp2, output, 1);
            } else {
                clijx.detectMinimaBox(temp2, output, 1);
            }


            Roi roi = clijx.pullAsROI(output);
            imp.setRoi(roi);
            //imp.setProcessor(clijx.pull(output).getProcessor());

            IJ.run(imp,"Enhance Contrast", "saturated=0.35");
            imp.updateAndDraw();

        } catch (Exception e) {
            System.out.println(e.getStackTrace().toString().getBytes());
        }
    }

    public static void main(String... args) {
        new ImageJ();

        Toolbar.addPlugInTool(new InteractiveSpotDetection());
        ImagePlus imp = IJ.openImage("C:\\structure\\data\\covid-chestxray-dataset\\images\\1-s2.0-S0929664620300449-gr3_lrg-d.jpg");
        //"../clij2-tests/src/test/resources/blobs.tif");
        imp.show();



    }

    @Override
    public String getToolName() {
        return "Spot detection";
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
                /*0*/	 "     #          " +
                /*1*/	 " #         #    " +
                /*2*/	 "    #      #   #" +
                /*3*/	 "       #        " +
                /*4*/	 "  #  #      #   " +
                /*5*/	 "       #        " +
                /*6*/	 " #   #      #   " +
                /*7*/	 "   #   #        " +
                /*8*/	 "                " +
                /*9*/	 "                " +
                /*A*/	 "       ##       " +
                /*B*/	 "      #  #      " +
                /*C*/	 "      #  #      " +
                /*D*/	 "###   #  #   ###" +
                /*E*/	 "   # #    # #   " +
                /*F*/	 "    #      #    " ;
    }
}
