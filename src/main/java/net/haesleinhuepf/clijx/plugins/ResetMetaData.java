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

@Plugin(type = CLIJMacroPlugin.class, name = "CLIJx_resetMetaData")
public class ResetMetaData extends AbstractCLIJxPlugin implements CLIJMacroPlugin, CLIJOpenCLProcessor, OffersDocumentation {

    static Stack<Calibration> metaData = new Stack<>();

    @Override
    public boolean executeCL() {
        metaData.clear();
        return true;
    }

    @Override
    public String getParameterHelpText() {
        return "";
    }

    @Override
    public String getDescription() {
        return "Resets the meta data stack.";
    }

    @Override
    public String getAvailableForDimensions() {
        return "2D, 3D";
    }
}
