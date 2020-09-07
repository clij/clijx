package net.haesleinhuepf.clijx.piv;

import ij.IJ;
import ij.ImageJ;
import net.haesleinhuepf.clij.CLIJ;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.clearcl.ClearCLImage;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.haesleinhuepf.clij.kernels.Kernels;
import net.haesleinhuepf.clij.macro.AbstractCLIJPlugin;
import net.haesleinhuepf.clij.macro.CLIJMacroPlugin;
import net.haesleinhuepf.clij.macro.CLIJOpenCLProcessor;
import net.haesleinhuepf.clij.macro.documentation.OffersDocumentation;
import net.haesleinhuepf.clij2.AbstractCLIJ2Plugin;
import net.haesleinhuepf.clij2.CLIJ2;
import net.haesleinhuepf.clijx.plugins.CrossCorrelation;
import org.scijava.plugin.Plugin;

/**
 * Author: @haesleinhuepf
 * December 2018
 */
@Plugin(type = CLIJMacroPlugin.class, name = "CLIJx_fastParticleImageVelocimetry3D")
public class FastParticleImageVelocimetry3D extends AbstractCLIJ2Plugin implements CLIJMacroPlugin, CLIJOpenCLProcessor, OffersDocumentation {

    @Override
    public boolean executeCL() {
        boolean result = fastParticleImageVelocimetry3D(getCLIJ2(), (ClearCLBuffer)( args[0]), (ClearCLBuffer)(args[1]), (ClearCLBuffer)(args[2]), (ClearCLBuffer)(args[3]), (ClearCLBuffer)(args[4]), asInteger(args[5]));
        return result;
    }

    @Override
    public ClearCLBuffer createOutputBufferFromSource(ClearCLBuffer input) {
        return getCLIJ2().create(input.getDimensions(), NativeTypeEnum.Float);
    }

    @Override
    public String getParameterHelpText() {
        return "Image source1, Image source2, ByRef Image destinationDeltaX, ByRef Image destinationDeltaY, ByRef Image destinationDeltaZ, Number maxDelta";
    }

    @Override
    public String getDescription() {
        return "For every pixel in source image 1, determine the pixel with the most similar intensity in \n" +
                " the local neighborhood with a given radius in source image 2. Write the distance in \n" +
                "X and Y in the two corresponding destination images.";
    }

    @Override
    public String getAvailableForDimensions() {
        return "3D";
    }

    public static boolean fastParticleImageVelocimetry3D(CLIJ2 clij2, ClearCLBuffer input1, ClearCLBuffer input2, ClearCLBuffer vfX, ClearCLBuffer vfY, ClearCLBuffer vfZ, Integer maxDelta ) {
        // analyse shift
        analyseShiftX(clij2, input1, input2, vfX, maxDelta);
        analyseShiftY(clij2, input1, input2, vfY, maxDelta);
        analyseShiftZ(clij2, input1, input2, vfZ, maxDelta);

        return true;
    }

    private static void analyseShiftX(CLIJ2 clij2, ClearCLBuffer input1, ClearCLBuffer input2, ClearCLBuffer vf, int maxDelta) {
        analyseShift_stack(clij2 ,input1, input2, vf, maxDelta, 0);
    }
    private static void analyseShiftY(CLIJ2 clij2, ClearCLBuffer input1, ClearCLBuffer input2, ClearCLBuffer vf, int maxDelta) {
        analyseShift_stack(clij2 ,input1, input2, vf, maxDelta, 1);
    }

    private static void analyseShiftZ(CLIJ2 clij2, ClearCLBuffer input1, ClearCLBuffer input2, ClearCLBuffer vf, int maxDelta) {
        ClearCLBuffer t_Input1 = clij2.create(new long[]{input1.getDepth(), input1.getHeight(), input1.getWidth()}, input1.getNativeType());
        ClearCLBuffer t_Input2 = clij2.create(new long[]{input2.getDepth(), input2.getHeight(), input2.getWidth()}, input2.getNativeType());
        ClearCLBuffer t_vf = clij2.create(new long[]{vf.getDepth(), vf.getHeight(), vf.getWidth()}, vf.getNativeType());

        clij2.transposeXZ(input1, t_Input1);
        clij2.transposeXZ(input2, t_Input2);

        analyseShift_stack(clij2 ,t_Input1, t_Input2, t_vf, maxDelta, 0);

        clij2.transposeXZ(t_vf, vf);

        t_Input1.close();
        t_Input2.close();
        t_vf.close();
    }

    private static void analyseShift_stack(CLIJ2 clij2, ClearCLBuffer input1, ClearCLBuffer input2, ClearCLBuffer vf, int maxDelta, int dimension) {
        ClearCLBuffer slice1 = clij2.create(new long[]{input1.getWidth(), input1.getHeight()}, input1.getNativeType());
        ClearCLBuffer slice2 = clij2.create(new long[]{input2.getWidth(), input2.getHeight()}, input2.getNativeType());
        ClearCLBuffer sliceVF = clij2.create(new long[]{vf.getWidth(), vf.getHeight()}, vf.getNativeType());

        ClearCLBuffer mean1 = clij2.create(slice1);
        ClearCLBuffer mean2 = clij2.create(slice2);

        ClearCLBuffer crossCorrCoeff = clij2.create(slice1.getDimensions(), NativeTypeEnum.Float);
        ClearCLBuffer crossCorrCoeffStack = clij2.create(new long[] {slice1.getWidth(), slice1.getHeight(), 2 * maxDelta + 1}, NativeTypeEnum.Float);

        for (int z = 0; z < input1.getDepth(); z++) {
            System.out.println("Z " + z);
            clij2.copySlice(input1, slice1, z);
            clij2.copySlice(input2, slice2, z);
            if (dimension == 0) {
                clij2.mean2DBox(slice1, mean1, maxDelta, 0);
                clij2.mean2DBox(slice2, mean2, maxDelta, 0);
            } else {
                clij2.mean2DBox(slice1, mean1, 0, maxDelta);
                clij2.mean2DBox(slice2, mean2, 0, maxDelta);
            }

            analyseShift_slice(clij2, slice1, slice2, sliceVF, maxDelta, mean1, mean2, crossCorrCoeff, crossCorrCoeffStack, dimension);

            clij2.copySlice(sliceVF, vf, z);


            //clij2.show(slice1, "slice1");
            //clij2.show(slice2, "slice2");
            //clij2.show(mean1, "mean1");
            //clij2.show(mean2, "mean2");
            //clij2.show(sliceVF, "sliceVF");

            //break;
        }

        crossCorrCoeff.close();
        crossCorrCoeffStack.close();

        mean1.close();
        mean2.close();

        slice1.close();
        slice2.close();

        sliceVF.close();
    }

    private static void analyseShift_slice(CLIJ2 clij2, ClearCLBuffer input1, ClearCLBuffer input2, ClearCLBuffer vf, int maxDelta, ClearCLBuffer meanInput1, ClearCLBuffer meanInput2, ClearCLBuffer crossCorrCoeff, ClearCLBuffer crossCorrCoeffStack, int dimension) {
        for (int i = -maxDelta; i <=maxDelta; i++) {
            CrossCorrelation.crossCorrelation(clij2, input1, meanInput1, input2, meanInput2, crossCorrCoeff, maxDelta, i, dimension);
            clij2.copySlice(crossCorrCoeff, crossCorrCoeffStack, i + maxDelta);

        }

        //clij2.show(crossCorrCoeffStack, "crossCorrCoeffStack");

        ClearCLBuffer argMaxProj = clij2.create(input1.getDimensions(), NativeTypeEnum.Float);

        clij2.argMaximumZProjection(crossCorrCoeffStack, vf, argMaxProj);
        clij2.addImageAndScalar(argMaxProj, vf, new Float(-maxDelta));
        argMaxProj.close();
    }

    public static void main(String[] args) {
        new ImageJ();

        CLIJ2 clij2 = CLIJ2.getInstance();

        ClearCLBuffer stack1 = clij2.push(IJ.openImage("C:\\structure\\data\\Irene\\piv\\ISB200522_well1_pos1_fast_cropped2-1.tif"));
        ClearCLBuffer stack2 = clij2.push(IJ.openImage("C:\\structure\\data\\Irene\\piv\\ISB200522_well1_pos1_fast_cropped2-2.tif"));

        ClearCLBuffer vfX = clij2.create(stack1.getDimensions(), NativeTypeEnum.Float);
        ClearCLBuffer vfY = clij2.create(stack1.getDimensions(), NativeTypeEnum.Float);
        ClearCLBuffer vfZ = clij2.create(stack1.getDimensions(), NativeTypeEnum.Float);

        fastParticleImageVelocimetry3D(clij2, stack1, stack2, vfX, vfY, vfZ, 3);

        //clij2.show(vfX, "x");
        //clij2.show(vfY, "y");
        clij2.show(vfZ, "z");
    }
}
