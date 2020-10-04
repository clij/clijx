package net.haesleinhuepf.clijx.plugins;

import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.clearcl.interfaces.ClearCLImageInterface;
import net.haesleinhuepf.clij.macro.CLIJMacroPlugin;
import net.haesleinhuepf.clij.macro.CLIJOpenCLProcessor;
import net.haesleinhuepf.clij.macro.documentation.OffersDocumentation;
import net.haesleinhuepf.clij2.plugins.Absolute;
import net.haesleinhuepf.clijx.CLIJx;
import net.haesleinhuepf.clijx.utilities.AbstractCLIJxPlugin;
import org.scijava.plugin.Plugin;

import java.util.HashMap;

/**
 * Author: @haesleinhuepf
 *         March 2020
 */
@Deprecated
@Plugin(type = CLIJMacroPlugin.class, name = "CLIJx_customBinaryOperation")
public class CustomBinaryOperation extends AbstractCLIJxPlugin implements CLIJMacroPlugin, CLIJOpenCLProcessor, OffersDocumentation {

    @Override
    public boolean executeCL() {
        return customBinaryOperation(getCLIJx(), (ClearCLBuffer) args[0], (ClearCLBuffer) args[1], (String)args[2]);
    }

    @Deprecated
    private static boolean customBinaryOperation(CLIJx clijx, ClearCLImageInterface image1, ClearCLImageInterface image2, String openCLCode) {
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("image1", image1);
        parameters.put("image2", image2);

        String code =
            "__kernel void custom_binary_operation (\n" +
            "IMAGE_image1_TYPE image1,\n" +
            "IMAGE_image2_TYPE image2\n" +
            ") {\n" +
            "  const sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;\n" +
            openCLCode +
            "}\n";


        clijx.executeCode(code, "custom_binary_operation", image1.getDimensions(), image1.getDimensions(), parameters);

        return true;
    }

    @Override
    public String getParameterHelpText() {
        return "Image image1, Image image2, String opencl_code";
    }

    @Override
    public String getDescription() {
        return "Executes custom OpenCL code on a pair of images.\n\n" +
                "Deprecated: Use customOperation() instead.";
    }

    @Override
    public String getAvailableForDimensions() {
        return "2D, 3D";
    }
}
