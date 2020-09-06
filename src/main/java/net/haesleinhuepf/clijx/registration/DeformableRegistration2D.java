package net.haesleinhuepf.clijx.registration;

import net.haesleinhuepf.clij.CLIJ;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.haesleinhuepf.clij.macro.AbstractCLIJPlugin;
import net.haesleinhuepf.clij.macro.CLIJMacroPlugin;
import net.haesleinhuepf.clij.macro.CLIJOpenCLProcessor;
import net.haesleinhuepf.clij.macro.documentation.OffersDocumentation;
import net.haesleinhuepf.clij2.AbstractCLIJ2Plugin;
import net.haesleinhuepf.clij2.CLIJ2;
import net.haesleinhuepf.clijx.piv.ParticleImageVelocimetry;
import org.scijava.plugin.Plugin;

@Plugin(type = CLIJMacroPlugin.class, name = "CLIJx_deformableRegistration2D")
public class DeformableRegistration2D extends AbstractCLIJ2Plugin implements CLIJMacroPlugin, CLIJOpenCLProcessor, OffersDocumentation {

    @Override
    public String getParameterHelpText() {
        return "Image input1, Image input2, ByRef Image destination, Number maxDeltaX, Number maxDeltaY";
    }

    @Override
    public boolean executeCL() {
        boolean result = deformableRegistration2D(getCLIJ2(), (ClearCLBuffer)( args[0]), (ClearCLBuffer)(args[1]), (ClearCLBuffer)(args[2]), asInteger(args[3]), asInteger(args[4]));
        return result;
    }

    public static boolean deformableRegistration2D(CLIJ2 clij2, ClearCLBuffer input1, ClearCLBuffer input2, ClearCLBuffer output, Integer maxDeltaX, Integer maxDeltaY) {
        ClearCLBuffer vectorfieldX = clij2.create(input1.getDimensions(), NativeTypeEnum.Float);
        ClearCLBuffer vectorfieldY = clij2.create(vectorfieldX);
        ClearCLBuffer tempX = clij2.create(vectorfieldX);
        ClearCLBuffer tempY = clij2.create(vectorfieldX);

        ParticleImageVelocimetry.particleImageVelocimetry(clij2, input1, input2, vectorfieldX, vectorfieldY, tempX, maxDeltaX, maxDeltaY, 0);

        clij2.gaussianBlur(vectorfieldX, tempX, (float)maxDeltaX, (float)maxDeltaY);
        clij2.gaussianBlur(vectorfieldY, tempY, (float)maxDeltaX, (float)maxDeltaY);

        clij2.applyVectorField(input2, tempX, tempY, output);

        vectorfieldX.close();
        vectorfieldY.close();
        tempX.close();
        tempY.close();

        return true;
    }

    @Override
    public String getDescription() {
        return "Applies particle image velocimetry to two images and registers them afterwards by warping input image 2 with a smoothed vector field.";
    }

    @Override
    public String getAvailableForDimensions() {
        return "2D+t";
    }
}
