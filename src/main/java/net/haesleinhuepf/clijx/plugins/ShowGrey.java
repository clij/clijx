package net.haesleinhuepf.clijx.plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import net.haesleinhuepf.clij.CLIJ;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.macro.AbstractCLIJPlugin;
import net.haesleinhuepf.clij.macro.CLIJMacroPlugin;
import net.haesleinhuepf.clij.macro.CLIJOpenCLProcessor;
import net.haesleinhuepf.clij.macro.documentation.OffersDocumentation;
import org.scijava.plugin.Plugin;

import javax.swing.*;

/**
 * Author: @haesleinhuepf
 *         November 2019
 */

@Plugin(type = CLIJMacroPlugin.class, name = "CLIJx_showGrey")
public class ShowGrey extends AbstractCLIJPlugin implements CLIJMacroPlugin, CLIJOpenCLProcessor, OffersDocumentation {

    @Override
    public boolean executeCL() {

        showGrey(clij, (ClearCLBuffer) args[0], (String)args[1]);
        return true;
    }

    public static ImagePlus showGrey(CLIJ clij, ClearCLBuffer input, String name) {
        final ImagePlus imp = clij.pull(input);

        IJ.run(imp, "Enhance Contrast", "saturated=0.35");

        ImagePlus resultImp = WindowManager.getImage(name);
        if (resultImp == null) {
            resultImp = imp;
            resultImp.setTitle(name);
            resultImp.show();
        } else {
            resultImp.setProcessor(imp.getProcessor());
            resultImp.updateAndDraw();
        }
        resultImp.show();
        return resultImp;
    }

    @Override
    public String getParameterHelpText() {
        return "Image input, String title";
    }

    @Override
    public String getDescription() {
        return "Visualises a single 2D image.";
    }

    @Override
    public String getAvailableForDimensions() {
        return "2D";
    }
}
