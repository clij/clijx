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

@Plugin(type = CLIJMacroPlugin.class, name = "CLIJx_driftCorrectionByCentroidFixation")
public class DriftCorrectionByCentroidFixation extends AbstractCLIJ2Plugin implements CLIJMacroPlugin, CLIJOpenCLProcessor, OffersDocumentation, IsCategorized, HasClassifiedInputOutput {
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
        return "Image input, ByRef Image destination, Number relative_center_x, Number relative_center_y, Number relative_center_z, Number threshold";
    }

    @Override
    public Object[] getDefaultValues() {
        return new Object[]{null, null, 0.5, 0.5, 0.5, 0};
    }

    @Override
    public boolean executeCL() {
        return driftCorrectionByCentroidFixation(getCLIJ2(), (ClearCLBuffer) args[0], (ClearCLBuffer) args[1], asFloat(args[2]), asFloat(args[3]), asFloat(args[4]), asFloat(args[5]));
    }

    public static boolean driftCorrectionByCentroidFixation(CLIJ2 clij2, ClearCLBuffer pushed, ClearCLBuffer result,
                                          Float relative_center_x,
                                          Float relative_center_y,
                                            Float relative_center_z, Float threshold) {

        System.out.println("relative_center_x = " + relative_center_x);
        System.out.println("relative_center_y = " + relative_center_y);
        System.out.println("relative_center_z = " + relative_center_z);

        ClearCLBuffer thresholded = clij2.create(pushed.getDimensions(), NativeTypeEnum.UnsignedByte);
        clij2.greaterOrEqualConstant(pushed, thresholded, threshold);

        double[] centroid = clij2.centerOfMass(thresholded);
        thresholded.close();

        double[] delta = new double[centroid.length];
        double[] relative_target = new double[centroid.length];

        relative_target[0] = relative_center_x * pushed.getWidth();
        relative_target[1] = relative_center_y * pushed.getHeight();
        if (relative_target.length > 2) {
            relative_target[2] = relative_center_z * pushed.getDepth();
        }
        for (int d = 0; d < delta.length; d++) {
            delta[d] = relative_target[d] - centroid[d];
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
        return "Threshold the image stack, determines the centroid of the resulting binary image and \n" +
                "translates the image stack so that its centroid sits in a defined position.";
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
