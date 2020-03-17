package net.haesleinhuepf.clijx.plugins;

import com.github.sarxos.webcam.Webcam;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.haesleinhuepf.clij.macro.CLIJMacroPlugin;
import net.haesleinhuepf.clij.macro.CLIJOpenCLProcessor;
import net.haesleinhuepf.clij.macro.documentation.OffersDocumentation;
import net.haesleinhuepf.clijx.CLIJx;
import net.haesleinhuepf.clijx.utilities.AbstractCLIJxPlugin;
import org.scijava.plugin.Plugin;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Author: @haesleinhuepf
 *         March 2020
 */

@Plugin(type = CLIJMacroPlugin.class, name = "CLIJx_startContinuousWebcamAcquisition")
public class StartContinuousWebcamAcquisition extends AbstractCLIJxPlugin implements CLIJMacroPlugin, CLIJOpenCLProcessor, OffersDocumentation {

    static Object lock = new Object();

    @Override
    public boolean executeCL() {
        return StartContinuousWebcamAcquisition(getCLIJx(), asInteger(args[0]), asInteger(args[1]), asInteger(args[2]));
    }

    public static boolean StartContinuousWebcamAcquisition(CLIJx clijx, Integer cameraIndex, Integer imageWidth, Integer imageHeight) {
        synchronized (lock) {
            // init cam
            Webcam cam = Webcam.getWebcams().get(cameraIndex);

            // acquire image
            if (cam.isOpen()) {
                cam.close();
            }
            cam.setViewSize(new Dimension(imageWidth, imageHeight));
            cam.open();

        }
        return true;
    }


    @Override
    public String getParameterHelpText() {
        return "Number cameraDeviceIndex, Number imageWidth, Number imageHeight";
    }

    @Override
    public String getDescription() {
        return "Acquires an image (in fact an RGB image stack with three slices) of given size using a webcam. It uses the webcam-capture library by Bartosz Firyn." +
                "https://github.com/sarxos/webcam-capture";
    }

    @Override
    public String getAvailableForDimensions() {
        return "3D";
    }
}
