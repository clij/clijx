package net.haesleinhuepf.clijx.plugins;

import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.clearcl.ClearCLImage;
import net.haesleinhuepf.clij.clearcl.enums.ImageChannelDataType;
import net.haesleinhuepf.clij.macro.CLIJMacroPlugin;
import net.haesleinhuepf.clij.macro.CLIJOpenCLProcessor;
import net.haesleinhuepf.clij.macro.documentation.OffersDocumentation;
import net.haesleinhuepf.clij2.AbstractCLIJ2Plugin;
import net.haesleinhuepf.clij2.CLIJ2;
import net.imglib2.realtransform.AffineTransform3D;
import org.scijava.plugin.Plugin;

@Plugin(type = CLIJMacroPlugin.class, name = "CLIJx_makeIsotropic")
public class MakeIsotropic extends AbstractCLIJ2Plugin implements CLIJMacroPlugin, CLIJOpenCLProcessor, OffersDocumentation {

    @Override
    public Object[] getDefaultValues() {
        return new Object[]{null, null, 1, 1, 1, 1};
    }

    @Override
    public String getParameterHelpText() {
        return "Image input, ByRef Image destination, Number original_voxel_size_x, Number original_voxel_size_y, Number original_voxel_size_z, Number new_voxel_size";
    }

    @Override
    public boolean executeCL() {
        return makeIsoTropic(getCLIJ2(), (ClearCLBuffer) args[0], (ClearCLBuffer) args[1], asFloat(args[2]), asFloat(args[3]), asFloat(args[4]), asFloat(args[5]));
    }

    public static boolean makeIsoTropic(CLIJ2 clij2, ClearCLBuffer pushed, ClearCLBuffer result, Float original_voxel_size_x, Float original_voxel_size_y, Float original_voxel_size_z, Float new_voxel_size) {
        float scale1X = (float) (original_voxel_size_x / new_voxel_size);
        float scale1Y = (float) (original_voxel_size_y / new_voxel_size);
        float scale1Z = (float) (original_voxel_size_z / new_voxel_size);

        ClearCLImage temp = clij2.create(pushed.getDimensions(), ImageChannelDataType.Float);

        clij2.copy(pushed, temp);
        pushed.close();

        AffineTransform3D scaleTransform = new AffineTransform3D();
        scaleTransform.scale(1.0 / scale1X, 1.0 / scale1Y, 1.0 / scale1Z);
        clij2.affineTransform3D(temp, result, scaleTransform);

        temp.close();

        return true;
    }

    @Override
    public ClearCLBuffer createOutputBufferFromSource(ClearCLBuffer input) {
        float original_voxel_size_x = asFloat(args[2]);
        float original_voxel_size_y = asFloat(args[3]);
        float original_voxel_size_z = asFloat(args[4]);
        float new_voxel_size_in_microns = asFloat(args[5]);

        float scale1X = (float) (original_voxel_size_x / new_voxel_size_in_microns);
        float scale1Y = (float) (original_voxel_size_y / new_voxel_size_in_microns);
        float scale1Z = (float) (original_voxel_size_z / new_voxel_size_in_microns);

        return getCLIJ2().create(
                (long) (input.getWidth() * scale1X),
                (long) (input.getHeight() * scale1Y),
                (long) (input.getDepth() * scale1Z));
    }

    @Override
    public String getDescription() {
        return "Applies a scaling operation using linear interpolation to generate an image stack with a given isotropic voxel size.";
    }

    @Override
    public String getAvailableForDimensions() {
        return "2D, 3D";
    }
}
