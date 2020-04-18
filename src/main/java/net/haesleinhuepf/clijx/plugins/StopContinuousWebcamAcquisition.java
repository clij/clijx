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

@Plugin(type = CLIJMacroPlugin.class, name = "CLIJx_stopContinuousWebcamAcquisition")
public class StopContinuousWebcamAcquisition extends AbstractCLIJxPlugin implements CLIJMacroPlugin, CLIJOpenCLProcessor, OffersDocumentation {

    static Object lock = new Object();

    @Override
    public boolean executeCL() {
        return stopContinuousWebcamAcquisition(getCLIJx(), asInteger(args[0]));
    }

    public static boolean stopContinuousWebcamAcquisition(CLIJx clijx, Integer cameraIndex) {
        synchronized (lock) {
            // init cam
            Webcam cam = Webcam.getWebcams().get(cameraIndex);
            cam.close();

        }
        return true;
    }


    @Override
    public String getParameterHelpText() {
        return "Number cameraDeviceIndex";
    }

    @Override
    public String getDescription() {
        return "Stops continous acquistion from a webcam.";
    }

    @Override
    public String getAvailableForDimensions() {
        return "3D";
    }
}
