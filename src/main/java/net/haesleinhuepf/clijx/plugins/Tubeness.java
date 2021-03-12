package net.haesleinhuepf.clijx.plugins;


import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.haesleinhuepf.clij.macro.CLIJMacroPlugin;
import net.haesleinhuepf.clij.macro.CLIJOpenCLProcessor;
import net.haesleinhuepf.clij.macro.documentation.OffersDocumentation;
import net.haesleinhuepf.clij2.AbstractCLIJ2Plugin;
import net.haesleinhuepf.clij2.CLIJ2;
import net.haesleinhuepf.clij2.utilities.HasAuthor;
import net.haesleinhuepf.clij2.utilities.HasClassifiedInputOutput;
import net.haesleinhuepf.clij2.utilities.IsCategorized;
import org.scijava.plugin.Plugin;

import java.util.HashMap;

import static net.haesleinhuepf.clij.utilities.CLIJUtilities.radiusToKernelSize;

/**
 * Author: @haesleinhuepf
 * February 2021
 */
@Plugin(type = CLIJMacroPlugin.class, name = "CLIJx_tubeness")
public class Tubeness extends AbstractCLIJ2Plugin implements CLIJMacroPlugin, CLIJOpenCLProcessor, OffersDocumentation, IsCategorized, HasClassifiedInputOutput {
    @Override
    public String getInputType() {
        return "Image";
    }

    @Override
    public String getOutputType() {
        return "Image";
    }

    @Override
    public String getCategories() {
        return "Filter, Measurement";
    }

    @Override
    public String getParameterHelpText() {
        return "Image input_image, ByRef Image destination";
    }

    @Override
    public boolean executeCL() {
        boolean result = tubeness(getCLIJ2(), (ClearCLBuffer) (args[0]), (ClearCLBuffer) (args[1]));
        return result;
    }

    public static boolean tubeness(CLIJ2 clij2, ClearCLBuffer input_image, ClearCLBuffer destination) {
        ClearCLBuffer temp1 = clij2.create(input_image.getDimensions(), NativeTypeEnum.Float);
        ClearCLBuffer temp2 = clij2.create(input_image.getDimensions(), NativeTypeEnum.Float);

        if (input_image.getDimension() == 2) {
            HessianEigenvalues2D.hessianEigenvalues2D(clij2, input_image, temp1, temp2);

            HashMap<String, Object> parameters = new HashMap<>();
            parameters.put("src_large_eigenvalue", temp2);
            parameters.put("dst", destination);
            clij2.execute(Tubeness.class, "tubeness_2d_x.cl", "tubeness_2d", destination.getDimensions(), destination.getDimensions(), parameters);
        } else { // 3D
            ClearCLBuffer temp3 = clij2.create(input_image.getDimensions(), NativeTypeEnum.Float);
            HessianEigenvalues3D.hessianEigenvalues3D(clij2, input_image, temp1, temp2, temp3);

            HashMap<String, Object> parameters = new HashMap<>();
            parameters.put("src_large_eigenvalue", temp3);
            parameters.put("src_middle_eigenvalue", temp2);
            parameters.put("dst", destination);
            clij2.execute(Tubeness.class, "tubeness_3d_x.cl", "tubeness_3d", destination.getDimensions(), destination.getDimensions(), parameters);

            temp3.close();
        }

        temp1.close();
        temp2.close();

        return true;
    }

    @Override
    public String getDescription() {
        return "Measures tubeness which is define as the absolut of the largest hessian Eigenvalue in 2D or the square root of the two larger Hessian Eigenvalues in 3D.\n\n" +
                "See also https://github.com/fiji/VIB-lib/blob/master/src/main/java/features/TubenessProcessor.java";
    }

    @Override
    public String getAvailableForDimensions() {
        return "2D, 3D";
    }

}
