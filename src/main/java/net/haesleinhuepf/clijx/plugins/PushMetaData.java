package net.haesleinhuepf.clijx.plugins;

import ij.IJ;
import ij.measure.Calibration;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.macro.CLIJMacroPlugin;
import net.haesleinhuepf.clij.macro.CLIJOpenCLProcessor;
import net.haesleinhuepf.clij.macro.documentation.OffersDocumentation;
import net.haesleinhuepf.clij2.plugins.Absolute;
import net.haesleinhuepf.clijx.CLIJx;
import net.haesleinhuepf.clijx.utilities.AbstractCLIJxPlugin;
import org.scijava.plugin.Plugin;

import java.util.Stack;

/**
 * Author: @haesleinhuepf
 */

@Plugin(type = CLIJMacroPlugin.class, name = "CLIJx_pushMetaData")
public class PushMetaData extends AbstractCLIJxPlugin implements CLIJMacroPlugin, CLIJOpenCLProcessor, OffersDocumentation {

    static Stack<Calibration> metaData = new Stack<>();

    @Override
    public boolean executeCL() {
        metaData.push(IJ.getImage().getCalibration().copy());
        return true;
    }

    @Override
    public String getParameterHelpText() {
        return "";
    }

    @Override
    public String getDescription() {
        return "Stores meta data in a stack. The stack implements the Last-In-First-Out (LIFO) principle.";
    }

    @Override
    public String getAvailableForDimensions() {
        return "2D, 3D";
    }
}
