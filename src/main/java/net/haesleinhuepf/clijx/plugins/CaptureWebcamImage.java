package net.haesleinhuepf.clijx.plugins;

import com.github.sarxos.webcam.Webcam;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.plugin.Converter;
import ij.process.ColorProcessor;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.haesleinhuepf.clij.macro.CLIJMacroPlugin;
import net.haesleinhuepf.clij.macro.CLIJOpenCLProcessor;
import net.haesleinhuepf.clij.macro.documentation.OffersDocumentation;
import net.haesleinhuepf.clij2.plugins.Absolute;
import net.haesleinhuepf.clijx.CLIJx;
import net.haesleinhuepf.clijx.utilities.AbstractCLIJxPlugin;
import org.apache.commons.io.input.ReaderInputStream;
import org.scijava.plugin.Plugin;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

/**
 * Author: @haesleinhuepf
 *         March 2020
 */

@Plugin(type = CLIJMacroPlugin.class, name = "CLIJx_captureWebcamImage")
public class CaptureWebcamImage extends AbstractCLIJxPlugin implements CLIJMacroPlugin, CLIJOpenCLProcessor, OffersDocumentation {

    static Object lock = new Object();

    @Override
    public boolean executeCL() {
        return captureWebcamImage(getCLIJx(), (ClearCLBuffer) args[0], asInteger(args[1]), asInteger(args[2]), asInteger(args[3]));
    }

    public static boolean captureWebcamImage(CLIJx clijx, ClearCLBuffer input_output, Integer cameraIndex, Integer imageWidth, Integer imageHeight) {
        synchronized (lock) {
            // init cam
            Webcam cam = Webcam.getWebcams().get(cameraIndex);

            boolean camWasOpen = cam.isOpen();

            if (cam.getViewSize().getWidth() != imageWidth || cam.getViewSize().getHeight() != imageHeight) {
                if (cam.isOpen()) {
                    cam.close();
                }
                cam.setViewSize(new Dimension(imageWidth, imageHeight));
            }


            // acquire image
            if (!cam.isOpen()) {
                cam.open();
            }
            BufferedImage image = cam.getImage();
            if (!camWasOpen) {
                IJ.log("Was not open; closing");
                cam.close();
            }

            // convert to imageplus
            ImagePlus imp = new ImagePlus("test", image);
            IJ.run(imp, "RGB Stack", "");

            // convert to clij
            ClearCLBuffer buffer = clijx.push(imp);
            clijx.paste(buffer, input_output, 0, 0, 0);
            clijx.release(buffer);
        }
        return true;
    }

    public static void main(String... args) {
        new ImageJ();
        CLIJx clijx = CLIJx.getInstance();

        int w = 640;
        int h = 480;

        ClearCLBuffer input = clijx.create(w, h, 3);
        ClearCLBuffer grey = clijx.create(w, h);

        CaptureWebcamImage.captureWebcamImage(clijx, input, 0, w, h);

        ConvertRGBStackToGraySlice.convertRGBStackToGraySlice(clijx, input, grey);

        clijx.show(input, "input");
        clijx.show(grey, "grey");
    }

    @Override
    public ClearCLBuffer createOutputBufferFromSource(ClearCLBuffer input) {
        // RGB images of given size
        return getCLIJx().create(new long[]{asInteger(args[2]), asInteger(args[3]), 3}, NativeTypeEnum.UnsignedByte);
    }

    @Override
    public String getParameterHelpText() {
        return "ByRef Image destination, Number cameraDeviceIndex, Number imageWidth, Number imageHeight";
    }

    @Override
    public String getDescription() {
        return "Acquires an image (in fact an RGB image stack with three slices) of given size using a webcam. \n\nIt uses the webcam-capture library by Bartosz Firyn." +
                "https://github.com/sarxos/webcam-capture";
    }

    @Override
    public String getAvailableForDimensions() {
        return "3D";
    }
}
