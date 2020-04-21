package net.haesleinhuepf.clijx.plugins;

import ij.*;
import ij.gui.GenericDialog;
import ij.gui.Toolbar;
import ij.measure.Calibration;
import ij.plugin.filter.PlugInFilter;
import ij.plugin.frame.Recorder;
import ij.plugin.tool.PlugInTool;
import ij.process.ImageProcessor;
import ij.process.LUT;
import net.haesleinhuepf.clij.CLIJ;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.macro.AbstractCLIJPlugin;
import net.haesleinhuepf.clij.macro.CLIJMacroPlugin;
import net.haesleinhuepf.clij.macro.CLIJOpenCLProcessor;
import net.haesleinhuepf.clij.macro.documentation.OffersDocumentation;
import net.haesleinhuepf.clijx.CLIJx;
import net.haesleinhuepf.clijx.gui.Utilities;
import net.haesleinhuepf.clijx.utilities.AbstractCLIJxPlugin;
import net.imglib2.realtransform.AffineTransform3D;
import org.scijava.plugin.Plugin;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Show3DWithTipTilt
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 *         April 2020
 */
@Plugin(type = CLIJMacroPlugin.class, name = "CLIJx_show3DWithTipTilt")
public class Show3DWithTipTilt extends AbstractCLIJxPlugin implements CLIJMacroPlugin, CLIJOpenCLProcessor, OffersDocumentation, PlugInFilter, ImageListener {


    @Override
    public boolean executeCL() {

        return showWithTipTilt(getCLIJx(), (ClearCLBuffer) args[0], (String)args[1], asBoolean(args[2]));
    }

    @Override
    public String getParameterHelpText() {
        return "Image input, String title, Boolean synchronised_windows";
    }

    @Override
    public String getDescription() {
        return "Visualises a single 3D image stack in a named interactive 2D viewer window. \n\nThe user can change tip and tilt" +
                " of the view perspective.";
    }

    @Override
    public String getAvailableForDimensions() {
        return "2D";
    }

    CLIJx clijx;

    ClearCLBuffer myBuffer = null;
    ClearCLBuffer transformed = null;
    ClearCLBuffer myMaxProjection = null;

    double old_angleX = -1;
    double old_angleY = -1;
    double angleX = 0;
    double angleY = 0;
    double old_translateX = -1;
    double old_translateY = -1;
    double translateX = 0;
    double translateY = 0;

    double zoom = 1.0;

    float scale1X = 1.0f;
    float scale1Y = 1.0f;
    float scale1Z = 1.0f;

    String projection = "Max";

    private String viewerName;

    private boolean synchronise_windows = false;

    @Override
    public int setup(String arg, ImagePlus imp) {
        return PlugInFilter.DOES_ALL;
    }

    public static boolean showWithTipTilt(CLIJx clijx, ClearCLBuffer input, String viewerName, Boolean synchronise_windows) {
        return showWithTipTilt(clijx, input, viewerName, synchronise_windows, 1.0, "Max");
    }

    private static HashMap<String, Show3DWithTipTilt> viewers = new HashMap<>();
    public static boolean showWithTipTilt(CLIJx clijx, ClearCLBuffer input, String viewerName, Boolean synchronise_windows, Double zoom, String projection) {

        Show3DWithTipTilt viewer = viewers.get(viewerName);
        if (viewer == null) {
            viewer = new Show3DWithTipTilt();
            viewers.put(viewerName, viewer);
        } else {
            synchronized (viewer) {
                viewer.myBuffer.close();
                viewer.myBuffer = null;
            }
        }
        viewer.synchronise_windows = synchronise_windows;
        viewer.show(clijx, input, viewerName, zoom, projection);

        return true;
    }

    private void show(CLIJx clijx, ClearCLBuffer input, String viewerName, Double zoom, String projection)
    {
        Recorder.setCommand(null);
        this.viewerName = viewerName;
        ImagePlus imp = clijx.pull(input); //IJ.getImage();
        this.zoom = zoom;
        this.projection = projection;

        Calibration calib = imp.getCalibration();
        scale1X = (float) (calib.pixelWidth * zoom);
        scale1Y = (float) (calib.pixelHeight * zoom);
        scale1Z = (float) (calib.pixelDepth * zoom);

        this.clijx = clijx;
        my_source = imp;

        refresh();
        ImagePlus.removeImageListener(this);
        ImagePlus.addImageListener(this);
    }

    private ImagePlus my_display = null;
    private ImagePlus my_source = null;

    private void refresh() {
        refresh(true);
    }

    private void refresh(boolean callingMySelf) {
        synchronized (this) {
            int min_z = 0;
            int max_z = my_source.getNSlices() - 1;
            System.out.println("AngleX " + angleX);
            System.out.println("Old angle X " + old_angleX);

            if (
                    old_angleX != angleX ||
                    old_angleY != angleY ||
                    old_translateX != translateX ||
                    old_translateY != translateY
            ) {
                if (myBuffer == null) {
                    myBuffer = clijx.pushCurrentZStack(my_source);
                }

                // System.out.println(myBuffer);
                //System.out.println("Angle: " + angleX + "/" + angleY);
                //transformed = clijx.create(new long[]{myBuffer.getWidth(), myBuffer.getHeight(), (long)(myBuffer.getDepth() * 1.5)}, myBuffer.getNativeType());
                if (transformed == null) {
                    transformed = clijx.create(new long[]{
                            (long) (myBuffer.getWidth() * scale1X),
                            (long) (myBuffer.getHeight() * scale1Y),
                            (long) (myBuffer.getDepth() * scale1Z)}, myBuffer.getNativeType());
                }
                if (myMaxProjection == null) {
                    myMaxProjection = clijx.create(new long[]{transformed.getWidth(), transformed.getHeight()}, transformed.getNativeType());
                }
                AffineTransform3D at = new AffineTransform3D();
                at.scale(scale1X, scale1Y, scale1Z);
                at.translate(translateX, translateY, 0);
                at.translate(-transformed.getWidth() / 2, -transformed.getHeight() / 2, 0);
                at.rotate(1, angleX / 180.0 * Math.PI);
                at.rotate(0, angleY / 180.0 * Math.PI);
                at.translate(transformed.getWidth() / 2, transformed.getHeight() / 2, 0);

                //# Execute operation on GPU
                clijx.affineTransform3D(myBuffer, transformed, at);


                if (projection.compareTo("Max") == 0) {
                    clijx.maximumZProjectionBounded(transformed, myMaxProjection, min_z, max_z);
                } else if (projection.compareTo("Min") == 0) {
                    clijx.minimumZProjectionBounded(transformed, myMaxProjection, min_z, max_z);
                } else if (projection.compareTo("Mean") == 0) {
                    clijx.meanZProjectionBounded(transformed, myMaxProjection, min_z, max_z);
                }

                LUT lut = null;
                double displayMin = 0;
                double displayMax = 0;
                ImagePlus viewer = WindowManager.getImage(viewerName);
                if (viewer != null) {
                    ImageProcessor ip = viewer.getProcessor();
                    lut = ip.getLut();
                    displayMin = viewer.getDisplayRangeMin();
                    displayMax = viewer.getDisplayRangeMax();
                }

                my_display = clijx.showGrey(myMaxProjection, viewerName);

                if(viewer != null) {
                    viewer.getProcessor().setLut(lut);
                    viewer.setDisplayRange(displayMin, displayMax);
                } else {
                    Toolbar.addPlugInTool(new TipTiltMouseHandler());
                    my_display.getWindow().getCanvas().disablePopupMenu(true);
                }
                /*else {
                    System.out.println("installing viewer capabilities");
                    while (my_display.getWindow().getCanvas().getMouseListeners().length > 0) {
                        my_display.getWindow().getCanvas().removeMouseListener(my_display.getWindow().getCanvas().getMouseListeners()[0]);
                    }
                    my_display.getWindow().getCanvas().addMouseMotionListener(new MouseAdapter() {
                        @Override
                        public void mouseDragged(MouseEvent e) {
                            double deltaX = e.getX() - mouseStartX;
                            double deltaY = e.getY() - mouseStartY;

                            angleY = angleStartY - deltaX / 5;
                            angleX = angleStartX + deltaY / 5;

                            refresh();
                            //System.out.println("Refreshing...");
                        }
                    });
                    my_display.getWindow().getCanvas().addMouseListener(new MouseAdapter() {
                        @Override
                        public void mousePressed(MouseEvent e) {
                            angleStartX = angleX;
                            angleStartY = angleY;
                            mouseStartX = e.getX();
                            mouseStartY = e.getY();
                        }
                    });
                    //if (projection.compareTo("Mean") != 0) {
                    //    my_display.setDisplayRange(my_source.getDisplayRangeMin(), my_source.getDisplayRangeMax());
                    //}
                }*/
                my_display.updateAndDraw();
            }

            old_angleY = angleY;
            old_angleX = angleX;
            old_translateY = translateY;
            old_translateX = translateX;

        }
        if (synchronise_windows && callingMySelf) {
            for (String key : viewers.keySet()) {
                Show3DWithTipTilt viewer = viewers.get(key);
                if (viewer.synchronise_windows) {
                    viewer.angleX = angleX;
                    viewer.angleY = angleY;
                    viewer.translateX = translateX;
                    viewer.translateY = translateY;
                    viewer.refresh(false);
                }
            }
        }
    }

    private class TipTiltMouseHandler extends PlugInTool {

        public TipTiltMouseHandler(){}

        double mouseStartX;
        double mouseStartY;
        double angleStartX;
        double angleStartY;
        double translationStartX;
        double translationStartY;

        @Override
        public void mousePressed(ImagePlus imp, MouseEvent e) {
            for (Show3DWithTipTilt viewer : viewers.values()) {
                if (imp == viewer.my_display) {
                    angleStartX = angleX;
                    angleStartY = angleY;
                    translationStartX = translateX;
                    translationStartY = translateY;
                    mouseStartX = e.getX();
                    mouseStartY = e.getY();
                    //System.out.println("Start Refreshing...");
                    return;
                }
            }
        }

        @Override
        public void mouseDragged(ImagePlus imp, MouseEvent e) {
            for (Show3DWithTipTilt viewer : viewers.values()) {
                if (imp == viewer.my_display) {

                    double deltaX = e.getX() - mouseStartX;
                    double deltaY = e.getY() - mouseStartY;

                    if (!SwingUtilities.isLeftMouseButton(e)) {
                        translateX = translationStartX - deltaX;
                        translateY = translationStartY - deltaY;
                    } else {
                        angleX = angleStartX - deltaX / 5;
                        angleY = angleStartY + deltaY / 5;
                    }
                    refresh();
                    //System.out.println("Refreshing...");
                    return;
                }
            }
        }

        @Override
        public String getToolName() {
            return "CLIJx Tip/tilt";
        }

        @Override
        public String getToolIcon()
        {
            return Utilities.generateIconCodeString(
                    getToolIconString()
            );
        }

        public String getToolIconString()
        {
            return
                    //        0123456789ABCDEF
                    /*0*/	 "####  ####      " +
                    /*1*/	 "##      ##      " +
                    /*2*/	 "# #    # #      " +
                    /*3*/	 "#  #  #  #      " +
                    /*4*/	 "    ##          " +
                    /*5*/	 "    ##          " +
                    /*6*/	 "#  #  #  #      " +
                    /*7*/	 "# #    # #      " +
                    /*8*/	 "##      ##  ####" +
                    /*9*/	 "####  ####  ##  " +
                    /*A*/	 "            # # " +
                    /*B*/	 "            #  #" +
                    /*C*/	 "        ####   #" +
                    /*D*/	 "        ##     #" +
                    /*E*/	 "        # #   # " +
                    /*F*/	 "        #  ###  " ;
        }
    }


    private void finish() {
        my_source = null;
        my_display = null;
        viewers.remove(viewerName);

        //ImagePlus.removeImageListener(this);
        clijx.release(myBuffer);
        clijx.release(myMaxProjection);
        clijx.release(transformed);

        clijx = null;
    }

    @Override
    public void imageOpened(ImagePlus imp) {

    }

    @Override
    public void imageClosed(ImagePlus imp) {
        if (imp == my_source || imp == my_display) {
            finish();
        }
    }

    @Override
    public void imageUpdated(ImagePlus imp) {
        if (imp == my_source) {
            refresh();
        }
    }

    public static void main(String[] args) {
        new ImageJ();
        ImagePlus imp = IJ.openImage("C:/structure/data/t1-head.tif");

        CLIJx clijx = CLIJx.getInstance();
        ClearCLBuffer buffer = clijx.push(imp);

        Show3DWithTipTilt.showWithTipTilt(clijx, buffer, "viewer", true);
    }
}
