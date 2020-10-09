package net.haesleinhuepf.clijx.plugins;

import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.clearcl.interfaces.ClearCLImageInterface;
import net.haesleinhuepf.clij.macro.CLIJMacroPlugin;
import net.haesleinhuepf.clij.macro.CLIJOpenCLProcessor;
import net.haesleinhuepf.clij.macro.documentation.OffersDocumentation;
import net.haesleinhuepf.clij2.AbstractCLIJ2Plugin;
import net.haesleinhuepf.clij2.CLIJ2;
import net.haesleinhuepf.clij2.plugins.FloodFillDiamond;
import net.haesleinhuepf.clij2.utilities.HasClassifiedInputOutput;
import net.haesleinhuepf.clij2.utilities.IsCategorized;
import org.scijava.plugin.Plugin;

import static net.haesleinhuepf.clij.utilities.CLIJUtilities.assertDifferent;

/**
 * Author: @haesleinhuepf
 *         August 2020
 */

@Plugin(type = CLIJMacroPlugin.class, name = "CLIJx_binaryFillHolesSliceBySlice")
public class BinaryFillHolesSliceBySlice extends AbstractCLIJ2Plugin implements CLIJMacroPlugin, CLIJOpenCLProcessor, OffersDocumentation, IsCategorized, HasClassifiedInputOutput {
    @Override
    public String getInputType() {
        return "Binary Image";
    }

    @Override
    public String getOutputType() {
        return "Binary Image";
    }

    @Override
    public boolean executeCL() {
        binaryFillHolesSliceBySlice(getCLIJ2(), (ClearCLBuffer)( args[0]), (ClearCLBuffer)(args[1]));
        return true;
    }


    public static boolean binaryFillHolesSliceBySlice(CLIJ2 clij2, ClearCLImageInterface src, ClearCLImageInterface dst) {
        assertDifferent(src, dst);
        if (src.getDimension() == 2) {
            clij2.binaryFillHoles(src, dst);
            return true;
        }

        ClearCLBuffer slice1 = clij2.create(new long[]{src.getWidth(), src.getHeight()}, src.getNativeType());
        ClearCLBuffer slice2 = clij2.create(new long[]{src.getWidth(), src.getHeight()}, src.getNativeType());
        for (int z = 0; z < dst.getDepth(); z++) {
            clij2.copySlice(src, slice1, z);
            clij2.binaryFillHoles(slice1, slice2);
            clij2.copySlice(slice2, dst, z);
        }
        slice1.close();
        slice2.close();

        return true;
    }

    @Override
    public String getParameterHelpText() {
        return "Image source, ByRef Image destination";
    }

    @Override
    public String getDescription() {
        return "Fills holes (pixels with value 0 surrounded by pixels with value 1) in a binary image stack slice by slice.";
    }

    @Override
    public String getAvailableForDimensions() {
        return "2D, 3D";
    }

    @Override
    public String getCategories() {
        return "Binary, Filter";
    }
}
