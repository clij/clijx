package net.haesleinhuepf.clijx.plugins;

import ij.measure.ResultsTable;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.clearcl.ClearCLKernel;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.haesleinhuepf.clij.macro.CLIJMacroPlugin;
import net.haesleinhuepf.clij.macro.CLIJOpenCLProcessor;
import net.haesleinhuepf.clij.macro.documentation.OffersDocumentation;
import net.haesleinhuepf.clij2.AbstractCLIJ2Plugin;
import net.haesleinhuepf.clij2.CLIJ2;
import net.haesleinhuepf.clij2.plugins.StatisticsOfLabelledPixels;
import org.scijava.plugin.Plugin;

@Plugin(type = CLIJMacroPlugin.class, name = "CLIJx_extendLabelsWithMaximumRadius")
public class ExtendLabelsWithMaximumRadius extends AbstractCLIJ2Plugin implements CLIJMacroPlugin, CLIJOpenCLProcessor, OffersDocumentation {

    @Override
    public String getParameterHelpText() {
        return "Image input, ByRef Image destination, Number radius";
    }

    @Override
    public boolean executeCL() {
        return extendLabelsWithMaximumRadius(getCLIJ2(), (ClearCLBuffer) args[0], (ClearCLBuffer) args[1], asInteger(args[2]));
    }

    public static boolean extendLabelsWithMaximumRadius(CLIJ2 clij2, ClearCLBuffer pushed, ClearCLBuffer result, Integer radius) {
        ClearCLBuffer temp = clij2.create(result);
        clij2.copy(pushed, temp);

        ClearCLBuffer flag = clij2.create(1,1,1);

        ClearCLKernel flip_kernel = null;
        ClearCLKernel flop_kernel = null;

        for (int i = 0; i < radius; i++) {
            if (i % 2 == 0) {
                flip_kernel = clij2.onlyzeroOverwriteMaximumBox(temp, flag, result, flip_kernel);
            } else {
                flop_kernel = clij2.onlyzeroOverwriteMaximumDiamond(result, flag, temp, flop_kernel);
            }
        }
        if (radius % 2 == 0) {
            clij2.copy(temp, result);
        }

        if (flip_kernel != null) {
            flip_kernel.close();
        }
        if (flop_kernel != null) {
            flop_kernel.close();
        }
        flag.close();
        temp.close();


        return true;
    }

    @Override
    public String getDescription() {
        return "Extend labels with a given radius.\n\n" +
                "This is actually a local maximum filter applied to a label map which does not overwrite labels.";
    }

    @Override
    public String getAvailableForDimensions() {
        return "2D, 3D";
    }
}
