package net.haesleinhuepf.clijx.plugins;

import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.haesleinhuepf.clij.macro.CLIJMacroPlugin;
import net.haesleinhuepf.clij.macro.CLIJOpenCLProcessor;
import net.haesleinhuepf.clij.macro.documentation.OffersDocumentation;
import net.haesleinhuepf.clij2.AbstractCLIJ2Plugin;
import net.haesleinhuepf.clij2.CLIJ2;
import net.haesleinhuepf.clijx.CLIJx;
import net.haesleinhuepf.clijx.utilities.AbstractCLIJxPlugin;
import org.scijava.plugin.Plugin;

@Plugin(type = CLIJMacroPlugin.class, name = "CLIJx_findAndLabelMaxima")
public class FindAndLabelMaxima extends AbstractCLIJxPlugin implements CLIJMacroPlugin, CLIJOpenCLProcessor, OffersDocumentation {


    @Override
    public Object[] getDefaultValues() {
        return new Object[]{null, null, 10, true};
    }

    @Override
    public String getParameterHelpText() {
        return "Image input, ByRef Image destination, Number tolerance, Boolean invert";
    }

    @Override
    public boolean executeCL() {
        return findAndLabelMaxima(getCLIJx(), (ClearCLBuffer) args[0], (ClearCLBuffer) args[1], asFloat(args[2]), asBoolean(args[3]));
    }

    public static boolean findAndLabelMaxima(CLIJx clijx, ClearCLBuffer pushed, ClearCLBuffer result, Float tolerance, Boolean invert) {

        if (invert) {
            ClearCLBuffer inverted = clijx.create(pushed.getDimensions(), NativeTypeEnum.Float);
            clijx.invert(pushed, inverted);
            clijx.findMaxima(inverted, result, tolerance);
            inverted.close();
        } else {
            clijx.findMaxima(pushed, result, tolerance);
        }
        return true;
    }

    @Override
    public String getDescription() {
        return "Determine maxima with a given tolerance to surrounding maxima and background and label them.";
    }

    @Override
    public String getAvailableForDimensions() {
        return "2D, 3D";
    }
}
