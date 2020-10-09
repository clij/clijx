package net.haesleinhuepf.clijx.plugins;

import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.haesleinhuepf.clij.macro.CLIJMacroPlugin;
import net.haesleinhuepf.clij.macro.CLIJOpenCLProcessor;
import net.haesleinhuepf.clij.macro.documentation.OffersDocumentation;
import net.haesleinhuepf.clij2.AbstractCLIJ2Plugin;
import net.haesleinhuepf.clij2.CLIJ2;
import net.haesleinhuepf.clij2.utilities.HasClassifiedInputOutput;
import net.haesleinhuepf.clij2.utilities.IsCategorized;
import org.scijava.plugin.Plugin;

@Plugin(type = CLIJMacroPlugin.class, name = "CLIJx_driftCorrectionByCenterOfMassFixation")
public class DriftCorrectionByCenterOfMassFixation extends AbstractCLIJ2Plugin implements CLIJMacroPlugin, CLIJOpenCLProcessor, OffersDocumentation, IsCategorized, HasClassifiedInputOutput {
    @Override
    public String getInputType() {
        return "Image";
    }

    @Override
    public String getOutputType() {
        return "Image";
    }

    @Override
    public String getParameterHelpText() {
        return "Image input, ByRef Image destination, Number relative_center_x, Number relative_center_y, Number relative_center_z";
    }

    @Override
    public Object[] getDefaultValues() {
        return new Object[]{null, null, 0.5, 0.5, 0.5, 0};
    }

    @Override
    public boolean executeCL() {
        return driftCorrectionByCenterOfMassFixation(getCLIJ2(), (ClearCLBuffer) args[0], (ClearCLBuffer) args[1], asFloat(args[2]), asFloat(args[3]), asFloat(args[4]));
    }

    public static boolean driftCorrectionByCenterOfMassFixation(CLIJ2 clij2, ClearCLBuffer pushed, ClearCLBuffer result,
                                          Float relative_center_x,
                                          Float relative_center_y,
                                            Float relative_center_z) {

        System.out.println("relative_center_x = " + relative_center_x);
        System.out.println("relative_center_y = " + relative_center_y);
        System.out.println("relative_center_z = " + relative_center_z);


        double[] centerOfMass = clij2.centerOfMass(pushed);

        double[] delta = new double[centerOfMass.length];
        double[] relative_target = new double[centerOfMass.length];

        relative_target[0] = relative_center_x * pushed.getWidth();
        relative_target[1] = relative_center_y * pushed.getHeight();
        if (relative_target.length > 2) {
            relative_target[2] = relative_center_z * pushed.getDepth();
        }
        for (int d = 0; d < delta.length; d++) {
            delta[d] = relative_target[d] - centerOfMass[d];
        }
        if (pushed.getDimension() > 2 && pushed.getDepth() > 1) {
            clij2.translate3D(pushed, result, delta[0], delta[1], delta[2]);
        } else {
            clij2.translate2D(pushed, result, delta[0], delta[1]);
        }

        return true;
    }

    @Override
    public String getDescription() {
        return "Determines the centerOfMass of the image stack and translates it so that it stays in a defined position.";
    }

    @Override
    public String getAvailableForDimensions() {
        return "2D, 3D";
    }

    @Override
    public String getCategories() {
        return "Transform";
    }
}
