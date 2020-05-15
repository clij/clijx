package net.haesleinhuepf.clijx.gui;

import ij.*;
import ij.gui.GenericDialog;
import ij.gui.NewImage;
import ij.gui.Overlay;
import ij.gui.TextRoi;
import ij.measure.Calibration;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.clearcl.ClearCLImage;
import net.haesleinhuepf.clij.clearcl.enums.ImageChannelDataType;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.haesleinhuepf.clij2.utilities.CLIJUtilities;
import net.haesleinhuepf.clijx.CLIJx;
import net.haesleinhuepf.clij2.plugins.DepthColorProjection;
import net.imglib2.realtransform.AffineTransform3D;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * InteractiveSubstackMaximumZProjection
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 *         April 2019
 */
public class InteractiveSubstackMaximumZProjection implements PlugInFilter, ImageListener {

    CLIJx clijx;
    ClearCLBuffer myBuffer;
    ClearCLImage converted;
    ClearCLImage transformed;
    ClearCLBuffer myMaxProjection;
    int old_max_z = -1;
    double old_angleX = -1;
    double old_angleY = -1;
    double angleX = 0;
    double angleY = 0;

    double angleStartX = 0;
    double angleStartY = 0;

    int mouseStartX = 0;
    int mouseStartY = 0;

    double zoom = 1.0;

    float scale1X = 1.0f;
    float scale1Y = 1.0f;
    float scale1Z = 1.0f;

    String projection = "Max";

    int slice_thickness = 50;

    String depthLUT = "Fire";

    ClearCLImage substack = null;
    ClearCLBuffer lut;

    @Override
    public int setup(String arg, ImagePlus imp) {
        return PlugInFilter.DOES_ALL;
    }

    @Override
    public void run(ImageProcessor ip) {
        ImagePlus imp = IJ.getImage();
        if (imp == null) {
            return;
        }

        GenericDialog gd = new GenericDialog("Interactive Substack Z Projection");
        gd.addNumericField("Zoom (the smaller, the faster)", zoom, 2);
        gd.addChoice("Projection", new String[]{"Max", "Min", "Mean", "Depth"}, projection);
        gd.addNumericField("Thickness of projected slice", slice_thickness, 0);
        gd.addStringField("Depth lookup table", depthLUT);
        gd.showDialog();
        if (gd.wasCanceled()) {
            return;
        }
        zoom = gd.getNextNumber();
        projection = gd.getNextChoice();
        slice_thickness = (int) gd.getNextNumber();
        depthLUT = gd.getNextString();
        clijx = CLIJx.getInstance();

        if (projection.equals("Depth")) {
            ImagePlus lut = NewImage.createByteImage("lut", 256, 1, 1, NewImage.FILL_RAMP);
            IJ.run(lut, depthLUT, "");
            ///lut.show();

            //System.out.println("A " + lut.getNChannels());
            new ImageConverter(lut).convertToRGB();
            //System.out.println("B " + lut.getNChannels());
            new ImageConverter(lut).convertToRGBStack();
            //System.out.println("C " + lut.getNChannels());

            this.lut = clijx.push(lut);
        }

        Calibration calib = imp.getCalibration();
        scale1X = (float) (calib.pixelWidth * zoom);
        scale1Y = (float) (calib.pixelHeight * zoom);
        scale1Z = (float) (calib.pixelDepth * zoom);

        my_source = imp;
        myBuffer = clijx.push(imp);
        converted = clijx.create(myBuffer.getDimensions(), CLIJUtilities.nativeToChannelType(myBuffer.getNativeType()));

        clijx.copy(myBuffer, converted);

        //transformed = clijx.create(new long[]{myBuffer.getWidth(), myBuffer.getHeight(), (long)(myBuffer.getDepth() * 1.5)}, myBuffer.getNativeType());
        transformed = clijx.create(new long[]{
                (long) (myBuffer.getWidth() * scale1X),
                (long) (myBuffer.getHeight() * scale1Y),
                (long) (myBuffer.getDepth() * scale1Z)}, ImageChannelDataType.Float);
        if (projection.equals("Depth")) {
            myMaxProjection = clijx.create(new long[]{transformed.getWidth(), transformed.getHeight(), 3}, NativeTypeEnum.UnsignedByte);
        } else {
            myMaxProjection = clijx.create(new long[]{transformed.getWidth(), transformed.getHeight()}, NativeTypeEnum.Float);
        }
        refresh();
        ImagePlus.addImageListener(this);
    }

    private ImagePlus my_display = null;
    private ImagePlus my_source = null;
    private void refresh() {
        synchronized (this) {
            int min_z = (int) Math.max(my_source.getZ() * my_source.getCalibration().pixelDepth * zoom - slice_thickness / 2, 0);
            int max_z = (int) Math.min(my_source.getZ() * my_source.getCalibration().pixelDepth  * zoom + slice_thickness / 2, my_source.getNSlices() * my_source.getCalibration().pixelDepth  * zoom - 1);
            if (old_max_z != max_z || old_angleX != angleX || old_angleY != angleY) {
                String window_title = my_source.getTitle() + " Interactive Maximum Z projection";

                if (old_angleX != angleX || old_angleY != angleY) {
                    AffineTransform3D at = new AffineTransform3D();
                    at.scale(scale1X, scale1Y, scale1Z);
                    at.translate(-transformed.getWidth() / 2, -transformed.getHeight() / 2, 0);
                    at.rotate(0, angleX / 180.0 * Math.PI);
                    at.rotate(1, angleY / 180.0 * Math.PI);
                    at.translate(transformed.getWidth() / 2, transformed.getHeight() / 2, 0);

                    at = at.inverse();

                    //# Execute operation on GPU
                    clijx.affineTransform3D(converted, transformed, at);
                    //clijx.show(transformed, "transformed");

                }

                if (projection.compareTo("Max") == 0) {
                    clijx.maximumZProjectionBounded(transformed, myMaxProjection, min_z, max_z);
                } else if (projection.compareTo("Min") == 0) {
                    clijx.minimumZProjectionBounded(transformed, myMaxProjection, min_z, max_z);
                } else if (projection.compareTo("Mean") == 0) {
                    clijx.meanZProjectionBounded(transformed, myMaxProjection, min_z, max_z);
                } else {
                    if (substack == null) {
                        substack = clijx.create(new long[]{ transformed.getWidth(), transformed.getHeight(), max_z - min_z + 1}, CLIJUtilities.nativeToChannelType(transformed.getNativeType()));
                    }
                    clijx.crop(transformed, substack, 0, 0, min_z);
                    //clijx.show(substack, "substack");

                    //System.out.println("a " + my_source.getProcessor().get(256, 256));
                    //System.out.println("b " + my_source.getProcessor().getPixel(256, 256));
                    //System.out.println("c " + my_source.getProcessor().getf(256, 256));
                    //System.out.println("c " + my_source.getProcessor().get(256, 256));



                    DepthColorProjection.depthColorProjection(clijx, substack, lut, myMaxProjection, (float)my_source.getDisplayRangeMin(), (float)my_source.getDisplayRangeMax());
                }
                //clijx.showGrey(myMaxProjection, window_title);

                if (my_display == null) {
                    my_display = clijx.pull(myMaxProjection);
                    //System.out.println("Z " + myMaxProjection.getDimensions()[2]);
                    if (projection.equals("Depth")) {
                        new ImageConverter(my_display).convertRGBStackToRGB();
                    }

                    my_display.show();
                    my_display.setTitle(window_title);
                    my_display.getWindow().getCanvas().disablePopupMenu(false);
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

                } else {
                    ImagePlus imp = clijx.pull(myMaxProjection);
                    if (projection.equals("Depth")) {
                        new ImageConverter(imp).convertRGBStackToRGB();
                    }
                    my_display.setProcessor(imp.getProcessor());
                }

                if (my_display != null) {
                    if (!projection.equals("Depth")) {
                        my_display.setDisplayRange(my_source.getDisplayRangeMin(), my_source.getDisplayRangeMax());
                        //System.out.println(my_source.getDisplayRangeMin());
                        //System.out.println(my_source.getDisplayRangeMax());
                        my_display.updateAndDraw();
                    }
                    Overlay overlay = new Overlay();
                    addText(overlay, "", 10, 10, Color.red);
                    my_display.setOverlay(overlay);

                }
                old_max_z = max_z;
                old_angleY = angleY;
                old_angleX = angleX;
            }
        }
    }

    private void addText(Overlay overlay, String text, int x, int y, Color color) {
        TextRoi roi = new TextRoi(x, y, text, new Font("Arial", 0, 16));
        roi.setColor(color);
        overlay.add(roi);

    }

    private void finish() {
        my_source = null;
        my_display = null;

        ImagePlus.removeImageListener(this);
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

        ImagePlus imp = IJ.openImage("C:/structure/data/2018-02-14-17-26-57-76-Akanksha_nGFP_001111.raw.tif");
        IJ.run(imp, "32-bit", "");
        imp.setZ(100);
        imp.show();

        new InteractiveSubstackMaximumZProjection().run(null);
    }
}
