package net.haesleinhuepf.clijx.plugins;

import ij.ImagePlus;
import ij.process.ImageProcessor;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.clearcl.interfaces.ClearCLImageInterface;
import net.haesleinhuepf.clij.macro.CLIJMacroPlugin;
import net.haesleinhuepf.clij.macro.CLIJOpenCLProcessor;
import net.haesleinhuepf.clij.macro.documentation.OffersDocumentation;
import net.haesleinhuepf.clij2.AbstractCLIJ2Plugin;
import net.haesleinhuepf.clij2.CLIJ2;
import org.scijava.plugin.Plugin;

import java.nio.FloatBuffer;

/**
 * Author: @haesleinhuepf
 *         April 2020
 */

@Plugin(type = CLIJMacroPlugin.class, name = "CLIJx_pullArray")
public class PullArray extends AbstractCLIJ2Plugin implements CLIJMacroPlugin, CLIJOpenCLProcessor, OffersDocumentation {

    @Override
    public boolean executeCL() {
        float[] result = pullArray(getCLIJ2(), (ClearCLBuffer) args[0]);
        Double[] converted = new Double[result.length];
        for (int i = 0; i < result.length; i++) {
            converted[i] = Double.valueOf(result[i]);
        }
        ((Double[][])args[1])[0] = converted;
        return true;

    }

    public static float[] pullArray(CLIJ2 clij2, ClearCLImageInterface input) {
        ClearCLBuffer buffer = clij2.create(input.getWidth(), input.getHeight(), input.getDepth());
        clij2.copy(input, buffer);

        float[] array = new float[(int)(buffer.getWidth() * buffer.getHeight() * buffer.getDepth())];
        buffer.writeTo(FloatBuffer.wrap(array), true);
        buffer.close();
        return array;
    }

    @Override
    public String getParameterHelpText() {
        return "Image input, ByRef Array destination";
    }

    @Override
    public String getDescription() {
        return "Writes an image into an array.";
    }

    @Override
    public String getAvailableForDimensions() {
        return "2D, 3D";
    }
}
