package net.haesleinhuepf.clijx.plugins;

import ij.IJ;
import ij.measure.Calibration;
import net.haesleinhuepf.clij.macro.CLIJMacroPlugin;
import net.haesleinhuepf.clij.macro.CLIJOpenCLProcessor;
import net.haesleinhuepf.clij.macro.documentation.OffersDocumentation;
import net.haesleinhuepf.clijx.utilities.AbstractCLIJxPlugin;
import org.scijava.plugin.Plugin;

import java.util.Stack;

/**
 * Author: @haesleinhuepf
 */

@Plugin(type = CLIJMacroPlugin.class, name = "CLIJx_popMetaData")
public class PopMetaData extends AbstractCLIJxPlugin implements CLIJMacroPlugin, CLIJOpenCLProcessor, OffersDocumentation {

    @Override
    public boolean executeCL() {
        IJ.getImage().setCalibration(PushMetaData.metaData.pop());
        return true;
    }

    @Override
    public String getParameterHelpText() {
        return "";
    }

    @Override
    public String getDescription() {
        return "Takes meta data from a stack and assigns it to the current image. The stack implements the Last-In-First-Out (LIFO) principle.";
    }

    @Override
    public String getAvailableForDimensions() {
        return "2D, 3D";
    }
}
