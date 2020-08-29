package net.haesleinhuepf.clijx.piv;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import net.haesleinhuepf.clij.CLIJ;
import net.haesleinhuepf.clij2.AbstractCLIJ2Plugin;
import net.haesleinhuepf.clij2.CLIJ2;
import net.haesleinhuepf.clijx.piv.visualisation.VisualiseVectorFieldsPlugin;
import net.haesleinhuepf.clijx.plugins.CrossCorrelation;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.clearcl.ClearCLImage;
import net.haesleinhuepf.clij.kernels.Kernels;
import net.haesleinhuepf.clij.macro.AbstractCLIJPlugin;
import net.haesleinhuepf.clij.macro.CLIJMacroPlugin;
import net.haesleinhuepf.clij.macro.CLIJOpenCLProcessor;
import net.haesleinhuepf.clij.macro.documentation.OffersDocumentation;
import org.scijava.plugin.Plugin;

/**
 * Author: @haesleinhuepf
 * December 2018
 */
@Plugin(type = CLIJMacroPlugin.class, name = "CLIJx_fastParticleImageVelocimetry")
public class FastParticleImageVelocimetry extends AbstractCLIJ2Plugin implements CLIJMacroPlugin, CLIJOpenCLProcessor, OffersDocumentation {

    @Override
    public boolean executeCL() {
        boolean result = particleImageVelocimetry2D(getCLIJ2(), (ClearCLBuffer)( args[0]), (ClearCLBuffer)(args[1]), (ClearCLBuffer)(args[2]), (ClearCLBuffer)(args[3]), asInteger(args[4]));
        return result;
    }

    @Override
    public String getParameterHelpText() {
        return "Image source1, Image source2, Image destinationDeltaX, Image destinationDeltaY, Number maxDelta";
    }

    @Override
    public String getDescription() {
        return "For every pixel in source image 1, determine the pixel with the most similar intensity in \n" +
                " the local neighborhood with a given radius in source image 2. Write the distance in \n" +
                "X and Y in the two corresponding destination images.";
    }

    @Override
    public String getAvailableForDimensions() {
        return "2D";
    }

    public static boolean particleImageVelocimetry2D(CLIJ2 clij2, ClearCLBuffer input1, ClearCLBuffer input2, ClearCLBuffer vfX, ClearCLBuffer vfY, Integer maxDelta ) {
        // prepare cross-correlation analysis
        int meanRange = maxDelta + 1;
        int scanRange = 5; // has influence on precision / correctness

        ClearCLBuffer meanInput1 = clij2.create(input1);
        ClearCLBuffer meanInput2 = clij2.create(input2);


        ClearCLBuffer crossCorrCoeff = clij2.create(input1);
        ClearCLBuffer crossCorrCoeffStack = clij2.create(new long[] {input1.getWidth(), input1.getHeight(), 2 * maxDelta + 1}, input1.getNativeType());

        // analyse shift in X
        clij2.meanBox(input1, meanInput1, meanRange, 0, 0);
        clij2.meanBox(input2, meanInput2, meanRange, 0, 0);
        analyseShift(clij2, input1, input2, vfX, maxDelta, scanRange, meanInput1, meanInput2, crossCorrCoeff, crossCorrCoeffStack, 0);

        clij2.meanBox(input1, meanInput1, 0, meanRange, 0);
        clij2.meanBox(input2, meanInput2, 0, meanRange, 0);
        analyseShift(clij2, input1, input2, vfY, maxDelta, scanRange, meanInput1, meanInput2, crossCorrCoeff, crossCorrCoeffStack, 1);

        meanInput1.close();
        meanInput2.close();

        crossCorrCoeff.close();
        crossCorrCoeffStack.close();

        return true;
    }

    private static void analyseShift(CLIJ2 clij2, ClearCLBuffer input1, ClearCLBuffer input2, ClearCLBuffer vf, int maxDelta, int scanRange, ClearCLBuffer meanInput1, ClearCLBuffer meanInput2, ClearCLBuffer crossCorrCoeff, ClearCLBuffer crossCorrCoeffStack, int dimension) {
        for (int i = -maxDelta; i <=maxDelta; i++) {
            CrossCorrelation.crossCorrelation(clij2, input1, meanInput1, input2, meanInput2, crossCorrCoeff, scanRange, i, dimension);
            clij2.copySlice(crossCorrCoeff, crossCorrCoeffStack, i + maxDelta);
        }

        ClearCLBuffer argMaxProj = clij2.create(input1);

        clij2.argMaximumZProjection(crossCorrCoeffStack, vf, argMaxProj);
        //clij2.show(crossCorrCoeffStack, "stack");

        clij2.addImageAndScalar(argMaxProj, vf, new Float(-maxDelta));
        argMaxProj.close();
    }

    public static void main(String[] args) {
        new ImageJ();

        ImagePlus input = IJ.openImage("C:/structure/data/Irene/" +
                //"ISB200522_well1_pos1_fast_cropped2.tif"
                "piv/C2-ISB200522_well2_pos1cropped_1sphere-1.tif"
        );

        CLIJ2 clij2 = CLIJ2.getInstance("RTX");

        input.setT(1);
        ClearCLBuffer in1 = clij2.pushCurrentZStack(input);
        input.setT(2);
        ClearCLBuffer in2 = clij2.pushCurrentZStack(input);

        ClearCLBuffer max1 = clij2.create(in1.getWidth(), in1.getHeight());
        ClearCLBuffer max2 = clij2.create(in2.getWidth(), in2.getHeight());

        ClearCLBuffer vfx = clij2.create(in2.getWidth(), in2.getHeight());
        ClearCLBuffer vfy = clij2.create(in2.getWidth(), in2.getHeight());

        clij2.maximumZProjection(in1, max1);
        clij2.maximumZProjection(in2, max2);


        ClearCLBuffer blur1 = clij2.create(in1.getWidth(), in1.getHeight());
        ClearCLBuffer blur2 = clij2.create(in2.getWidth(), in2.getHeight());

        clij2.mean2DBox(max1, blur1, 3, 3);
        clij2.mean2DBox(max2, blur2, 3, 3);

        particleImageVelocimetry2D(clij2, blur1, blur2, vfx, vfy, 5);

        ImagePlus result = VisualiseVectorFieldsPlugin.visualiseVectorField(
                clij2.pull(max1),
                clij2.pull(vfx),
                clij2.pull(vfy),
                5
        );
        result.show();

        /*
        VisualiseVectorFieldsPlugin vvpd = new VisualiseVectorFieldsPlugin();
        ImagePlus imp = clij2.pull(max1);
        IJ.run(imp, "Enhance Contrast", "saturated=0.35");
        //imp.show();
        //if (true) return;
        //IJ.run(imp, "8-bit", "");
        //imp.show();
        vvpd.setInputImage(imp);
        vvpd.setVectorXImage(clij2.pull(vfx));
        vvpd.setVectorYImage(clij2.pull(vfy));
        vvpd.setSilent(true);
        vvpd.setShowResult(false);
        vvpd.setMaximumLength(5);
        vvpd.setMinimumLength(2);
        vvpd.setStepSize(5);
        vvpd.setLineWidth(1);
        vvpd.run();
        vvpd.getOutputImage().show();
*/


        //clij2.show(max1, "max1");
        //clij2.show(max2, "max2");

        //clij2.show(vfx, "vfx");
        //clij2.show(vfy, "vfy");
    }
}
