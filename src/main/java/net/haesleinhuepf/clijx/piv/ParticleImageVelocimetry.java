package net.haesleinhuepf.clijx.piv;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.NewImage;
import ij.process.ImageProcessor;
import net.haesleinhuepf.clij.CLIJ;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.haesleinhuepf.clij.kernels.Kernels;
import net.haesleinhuepf.clij.macro.AbstractCLIJPlugin;
import net.haesleinhuepf.clij.macro.CLIJMacroPlugin;
import net.haesleinhuepf.clij.macro.CLIJOpenCLProcessor;
import net.haesleinhuepf.clij.macro.documentation.OffersDocumentation;
import net.haesleinhuepf.clij2.AbstractCLIJ2Plugin;
import net.haesleinhuepf.clij2.CLIJ2;
import net.haesleinhuepf.clijx.registration.DeformableRegistration2D;
import org.scijava.plugin.Plugin;

import java.util.HashMap;


/**
 * Author: @haesleinhuepf
 * June 2019
 */
@Plugin(type = CLIJMacroPlugin.class, name = "CLIJx_particleImageVelocimetry")
public class ParticleImageVelocimetry extends AbstractCLIJ2Plugin implements CLIJMacroPlugin, CLIJOpenCLProcessor, OffersDocumentation {

    @Override
    public boolean executeCL() {
        boolean result = particleImageVelocimetry(getCLIJ2(), (ClearCLBuffer) (args[0]), (ClearCLBuffer) (args[1]), (ClearCLBuffer) (args[2]), (ClearCLBuffer) (args[3]), (ClearCLBuffer) (args[4]), asInteger(args[5]), asInteger(args[6]), asInteger(args[7]));
        return result;
    }

    @Override
    public String getParameterHelpText() {
        return "Image source1, Image source2, Image destinationDeltaX, Image destinationDeltaY, Image destinationDeltaZ, Number maxDeltaX, Number maxDeltaY, Number maxDeltaZ";
    }

    @Override
    public String getDescription() {
        return "For every pixel in source image 1, determine the pixel with the most similar intensity in \n" +
                " the local neighborhood with a given radius in source image 2. Write the distance in \n" +
                "X, Y and Z in the three corresponding destination images.";
    }

    @Override
    public String getAvailableForDimensions() {
        return "2D";
    }

    public static boolean particleImageVelocimetry(CLIJ2 clij2, ClearCLBuffer input1, ClearCLBuffer input2, ClearCLBuffer vfX, ClearCLBuffer vfY, ClearCLBuffer vfZ, Integer maxDeltaX, Integer maxDeltaY, Integer maxDeltaZ) {
        // prepare cross-correlation analysis
        int meanRangeX = 3;
        int scanRangeX = meanRangeX; // has influence on precision / correctness
        int meanRangeY = 3;
        int scanRangeY = meanRangeY; // has influence on precision / correctness
        int meanRangeZ = 3;
        int scanRangeZ = meanRangeZ; // has influence on precision / correctness

        ClearCLBuffer meanInput1 = clij2.create(input1);
        ClearCLBuffer meanInput2 = clij2.create(input2);


        ClearCLBuffer crossCorrCoeff = clij2.create(input1.getDimensions(), NativeTypeEnum.Float);
        ClearCLBuffer crossCorrCoeffStack = clij2.create(new long[]{
                input1.getWidth(),
                input1.getHeight(),
                input1.getDepth() * (2 * maxDeltaX + 1) * (2 * maxDeltaY + 1) * (2 * maxDeltaZ + 1)
        }, NativeTypeEnum.Float);

        // analyse shift in X
        clij2.meanBox(input1, meanInput1, meanRangeX, meanRangeY, meanRangeZ);
        clij2.meanBox(input2, meanInput2, meanRangeX, meanRangeY, meanRangeZ);
        analyseShift(clij2, input1, input2, vfX, vfY, vfZ, maxDeltaX, maxDeltaY, maxDeltaZ, scanRangeX, scanRangeY, scanRangeZ, meanInput1, meanInput2, crossCorrCoeff, crossCorrCoeffStack);


        meanInput1.close();
        meanInput2.close();

        crossCorrCoeff.close();
        crossCorrCoeffStack.close();

        return true;
    }

    private static void analyseShift(CLIJ2 clij2, ClearCLBuffer input1, ClearCLBuffer input2, ClearCLBuffer vfX, ClearCLBuffer vfY, ClearCLBuffer vfZ, int maxDeltaX, int maxDeltaY, int maxDeltaZ, int scanRangeX, int scanRangeY, int scanRangeZ, ClearCLBuffer meanInput1, ClearCLBuffer meanInput2, ClearCLBuffer crossCorrCoeff, ClearCLBuffer crossCorrCoeffStack) {

        int count = 0;
        double[][] coords = new double[(int) ((int)crossCorrCoeffStack.getDepth() / crossCorrCoeff.getDepth())][3];
        for (int ix = -maxDeltaX; ix <= maxDeltaX; ix++) {
            for (int iy = -maxDeltaY; iy <= maxDeltaY; iy++) {
                for (int iz = -maxDeltaZ; iz <= maxDeltaZ; iz++) {
                    System.out.println("" + ix + "/" + iy + "/" + iz);
                    crossCorrelation(clij2, input1, meanInput1, input2, meanInput2, crossCorrCoeff, scanRangeX, scanRangeY, scanRangeZ, ix, iy, iz);
                    //System.out.println("ccc " + crossCorrCoeff.getDimension());
                    //System.out.println("cccs " + crossCorrCoeffStack.getDimension());
                    clij2.paste3D(crossCorrCoeff, crossCorrCoeffStack, count * crossCorrCoeff.getDepth(), 0, 0);
                    //clij2.show(crossCorrCoeff, "crossCorrCoeff" );
                    coords[count][0] = ix;
                    coords[count][1] = iy;
                    coords[count][2] = iz;
                    count++;
                    //break;
                }
                //break;
            }
            //break;
        }

        ClearCLBuffer coordMap = doubleArrayToImagePlus(clij2, coords);
        ClearCLBuffer argMaxProj = clij2.create(input1);

        System.out.println("X");
        argMaximumZProjection(clij2, crossCorrCoeffStack, vfX, argMaxProj);
        indexProjection(clij2, argMaxProj, coordMap, vfX, 0, 0, 1, 0, 2);

        System.out.println("Y");
        argMaximumZProjection(clij2, crossCorrCoeffStack, vfY, argMaxProj);
        indexProjection(clij2, argMaxProj, coordMap, vfY, 0, 1, 1, 0, 2);

        System.out.println("Z");
        argMaximumZProjection(clij2, crossCorrCoeffStack, vfZ, argMaxProj);
        indexProjection(clij2, argMaxProj, coordMap, vfZ, 0, 2, 1, 0, 2);

        argMaxProj.close();
    }

    private static boolean argMaximumZProjection(CLIJ2 clij2, ClearCLBuffer crossCorrCoeffStack, ClearCLBuffer vfX, ClearCLBuffer argMaxProj) {
        ClearCLBuffer temp = clij2.create(new long[]{
                crossCorrCoeffStack.getWidth(),
                crossCorrCoeffStack.getHeight(),
                crossCorrCoeffStack.getDepth() / vfX.getDepth()
        }, crossCorrCoeffStack.getNativeType());

        ClearCLBuffer slice1 = clij2.create(new long[]{temp.getWidth(), temp.getHeight()}, temp.getNativeType());
        ClearCLBuffer slice2 = clij2.create(new long[]{temp.getWidth(), temp.getHeight()}, temp.getNativeType());

        for (int z = 0; z < vfX.getDepth(); z++) {
            clij2.reduceStack(crossCorrCoeffStack, temp, vfX.getDepth(), z);
            clij2.argMaximumZProjection(temp, slice1, slice2);
            clij2.copySlice(slice2, argMaxProj, z);
        }

        temp.close();
        return true;
    }

    private static boolean indexProjection(CLIJ2 clij2, ClearCLBuffer index, ClearCLBuffer indexMap, ClearCLBuffer target, int indexDimension,
                                   int fixed1, int fixedDimension1, int fixed2, int fixedDimension2) {

        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("index_src1", index);
        parameters.put("index_map_src1", indexMap);
        parameters.put("dst", target);
        parameters.put("indexDimension", indexDimension);
        parameters.put("fixed1", fixed1);
        parameters.put("fixedDimension1", fixedDimension1);
        parameters.put("fixed2", fixed2);
        parameters.put("fixedDimension2", fixedDimension2);

        //System.out.println(index);
        //System.out.println(indexMap);
        //System.out.println(fixedDimension1);
        //System.out.println(fixedDimension2);

        clij2.execute(ParticleImageVelocimetry.class, "piv_index_projection_" + index.getDimension() + "d_x.cl", "index_projection_" + index.getDimension() + "d", index.getDimensions(), index.getDimensions(), parameters);
        return true;
    }

    private static ClearCLBuffer doubleArrayToImagePlus(CLIJ2 clij2, double[][] coords) {
        ImagePlus imp = NewImage.createFloatImage("img", coords.length, coords[0].length, 1, NewImage.FILL_BLACK);
        ImageProcessor ip = imp.getProcessor();
        for (int x = 0; x < coords.length; x++) {
            for (int y = 0; y < coords[0].length; y++) {
                ip.setf(x, y, (float)coords[x][y]);
            }
        }
        ClearCLBuffer temp1 = clij2.push(imp);
        ClearCLBuffer temp2 = clij2.create(temp1.getWidth(), temp1.getHeight(), 1);
        clij2.copy(temp1, temp2);
        temp1.close();
        return temp2;
    }

    private static boolean crossCorrelation(CLIJ2 clij2, ClearCLBuffer input1, ClearCLBuffer meanInput1, ClearCLBuffer input2, ClearCLBuffer meanInput2, ClearCLBuffer crossCorrCoeff, int scanRangeX, int scanRangeY, int scanRangeZ, int ix, int iy, int iz) {
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("src1", input1);
        parameters.put("mean_src1", meanInput1);
        parameters.put("src2", input2);
        parameters.put("mean_src2", meanInput2);
        parameters.put("dst", crossCorrCoeff);
        parameters.put("radiusx", scanRangeX);
        parameters.put("radiusy", scanRangeY);
        parameters.put("radiusz", scanRangeZ);
        parameters.put("ix", ix);
        parameters.put("iy", iy);
        parameters.put("iz", iz);

        //clij2.show(input1, "input1");
        //clij2.show(meanInput1, "mean 1");
        //clij2.show(input2, "input2");
        //clij2.show(meanInput2, "mean 2");

        //CLIJ.debug = true;
        clij2.execute(ParticleImageVelocimetry.class, "piv_cross_correlation_3d_x.cl", "cross_correlation_3d", input1.getDimensions(), input1.getDimensions(), parameters);
        //CLIJ.debug = false;
        return true;
    }

    public static void main(String[] args) {
        new ImageJ();

        CLIJ2 clij2 = CLIJ2.getInstance();

        ClearCLBuffer stack1 = clij2.push(IJ.openImage("C:\\structure\\data\\Irene\\piv\\C2-ISB200522_well2_pos1cropped_1sphere-2.tif"));
        ClearCLBuffer stack2 = clij2.push(IJ.openImage("C:\\structure\\data\\Irene\\piv\\C2-ISB200522_well2_pos1cropped_1sphere-3.tif"));

        ClearCLBuffer vfX = clij2.create(stack1);
        ClearCLBuffer vfY = clij2.create(stack1);
        ClearCLBuffer vfZ = clij2.create(stack1);

        particleImageVelocimetry(clij2, stack1, stack2, vfX, vfY, vfZ, 1, 1, 1);

        clij2.show(vfX, "x");
        clij2.show(vfY, "y");
        clij2.show(vfZ, "z");
    }
}
