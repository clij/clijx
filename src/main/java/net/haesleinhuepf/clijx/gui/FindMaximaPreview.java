package net.haesleinhuepf.clijx.gui;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.Roi;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import net.haesleinhuepf.clij.clearcl.ClearCL;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.haesleinhuepf.clij2.CLIJ2;
import net.haesleinhuepf.clijx.CLIJx;
import net.haesleinhuepf.clij2.plugins.Pull2DPointListAsRoi;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;

public class FindMaximaPreview implements PlugInFilter {
    double sigma = 3;
    int noise_threshold = 5;
    boolean invert = false;

    ImagePlus imp;
    CLIJx clijx;
    private TextField previewTextField = null;

    @Override
    public int setup(String arg, ImagePlus imp) {
        return DOES_ALL;
    }

    @Override
    public void run(ImageProcessor ip) {
        clijx = CLIJx.getInstance();
        imp = IJ.getImage();

        //if (true) return;

        GenericDialog gd = new GenericDialog("Find Maxima Preview");
        gd.addNumericField("Gaussian blur sigma", sigma);
        TextField tf1 = (TextField) gd.getNumericFields().get(0);
        tf1.addTextListener(new TextListener() {
            @Override
            public void textValueChanged(TextEvent e) {
                System.out.println("Text1 changed: " + tf1.getText());
                try
                {
                    double value = Integer.parseInt(tf1.getText());
                    if (value >= 0) {
                        sigma = value;
                        preview();
                    }
                } catch (Exception ex) {
                    System.out.println("Exception: " + ex);
                }
            }
        });

        gd.addNumericField("Tolerance", noise_threshold);
        TextField tf2 = (TextField) gd.getNumericFields().get(1);
        tf2.addTextListener(new TextListener() {
            @Override
            public void textValueChanged(TextEvent e) {
                System.out.println("Text2 changed: " + tf2.getText());
                try
                {
                    int value = Integer.parseInt(tf2.getText());
                    if (value > 0) {
                        noise_threshold = value;
                        preview();
                    }
                } catch (Exception ex) {
                    System.out.println("Exception: " + ex);
                }
            }
        });

        gd.addCheckbox("Invert before detecting maxima", false);
        Checkbox cb = (Checkbox) gd.getCheckboxes().get(0);
        cb.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                System.out.println("Text changed: " + tf2.getText());
                invert = cb.getState();
                preview();
            }
        });

        gd.addNumericField("Numer of found objects", 0);
        previewTextField = (TextField) gd.getNumericFields().get(2);
        previewTextField.setEditable(false);

        preview();

        //gd.setModal(false);
        gd.showDialog();

        imp.killRoi();

        ClearCLBuffer input = clijx.push(imp);
        if (!(input.getNativeType() == NativeTypeEnum.Float)) {
            ClearCLBuffer temp1 = clijx.create(input.getDimensions(), NativeTypeEnum.Float);
            clijx.copy(input, temp1);
            input.close();
            input = temp1;
        }
        ClearCLBuffer temp = clijx.create(input.getDimensions(), NativeTypeEnum.Float);
        ClearCLBuffer output = clijx.create(input);

        if (sigma > 0) {
            clijx.gaussianBlur(input, temp, sigma, sigma);
            clijx.copy(temp, input);
        }

        if (invert) {
            double maximum = clijx.maximumOfAllPixels(input);
            clijx.subtractImageFromScalar(input, temp, maximum);
            clijx.copy(temp, input);
        }

        clijx.findMaxima(input, output, noise_threshold);

        int number_of_objects = (int) clijx.maximumOfAllPixels(output);
        System.out.println("Number of points: " + number_of_objects );
        ClearCLBuffer pointlist = clijx.create(number_of_objects + 1, input.getDimension());
        clijx.centroidsOfLabels(output, pointlist);

        ClearCLBuffer centroidsAndValues = clijx.create(pointlist.getWidth(), pointlist.getHeight() + 1);
        clijx.setRampX(centroidsAndValues);
        clijx.paste(pointlist, centroidsAndValues, 0, 1);

        clijx.set(output, 0);
        clijx.writeValuesToPositions(pointlist, output);

        clijx.show(output, "Maxima in " + imp.getTitle());

        pointlist.close();
        centroidsAndValues.close();

        input.close();
        temp.close();
        output.close();


    }

    private void preview() {
        System.out.println("Preview: sigma = " + sigma + " tolerance = " + noise_threshold);
        imp.killRoi();

        ClearCLBuffer input = clijx.pushCurrentSlice(imp);
        if (!(input.getNativeType() == NativeTypeEnum.Float)) {
            ClearCLBuffer temp1 = clijx.create(input.getDimensions(), NativeTypeEnum.Float);
            clijx.copy(input, temp1);
            input.close();
            input = temp1;
        }
        ClearCLBuffer temp = clijx.create(input.getDimensions(), NativeTypeEnum.Float);
        ClearCLBuffer output = clijx.create(input);

        if (sigma > 0) {
            clijx.gaussianBlur(input, temp, sigma, sigma);
            clijx.copy(temp, input);
        }

        if (invert) {
            double maximum = clijx.maximumOfAllPixels(input);
            clijx.subtractImageFromScalar(input, temp, maximum);
            clijx.copy(temp, input);
        }

        clijx.findMaxima(input, output, noise_threshold);
        //clijx.maximum2DSphere(output, temp, 3, 3);

        int number_of_objects = (int) clijx.maximumOfAllPixels(output);
        System.out.println("Number of points: " + number_of_objects );
        if (previewTextField != null) {
            previewTextField.setText("" + number_of_objects);
        }
        ClearCLBuffer pointlist = clijx.create(number_of_objects + 1, 2);
        clijx.centroidsOfLabels(output, pointlist);

        Roi roi = Pull2DPointListAsRoi.pull2DPointListAsRoi(clijx, pointlist);
        pointlist.close();

        imp.setRoi(roi);


        //clijx.multiplyImageAndScalar(temp, outp, 255);

        //clijx.showRGB(input, output, input, "Find Maxima Preview");
        //clijx.show(output, "output");
        input.close();
        temp.close();
        output.close();
        System.out.println("Preview done");
    }

    public static void main(String[] args) {
        new ImageJ();
        CLIJ2 clij2 = CLIJ2.getInstance("HD");
        System.out.println(clij2.getGPUName());

        IJ.openImage("src/test/resources/blobs.tif").show();
        new FindMaximaPreview().run(null);
    }
}
