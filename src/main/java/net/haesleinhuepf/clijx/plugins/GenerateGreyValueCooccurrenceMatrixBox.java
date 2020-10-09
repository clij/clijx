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

import java.nio.FloatBuffer;
import java.util.ArrayList;

/**
 * Author: @haesleinhuepf
 *         August 2020
 */
@Plugin(type = CLIJMacroPlugin.class, name = "CLIJx_generateGreyValueCooccurrenceMatrixBox")
public class GenerateGreyValueCooccurrenceMatrixBox extends AbstractCLIJ2Plugin implements CLIJMacroPlugin, CLIJOpenCLProcessor, OffersDocumentation, IsCategorized, HasClassifiedInputOutput {
    @Override
    public String getInputType() {
        return "Image";
    }

    @Override
    public String getOutputType() {
        return "Matrix";
    }

    @Override
    public boolean executeCL() {
        boolean result = generateGreyValueCooccurrenceMatrixBox(getCLIJ2(), (ClearCLBuffer)( args[0]), (ClearCLBuffer)(args[1]), asFloat(args[2]), asFloat(args[3]));
        return result;
    }

    public static boolean generateGreyValueCooccurrenceMatrixBox(CLIJ2 clij2, ClearCLBuffer src, ClearCLBuffer dst_cooccurrence_matrix, Float minimum_intensity, Float maximum_intensity) {
        ClearCLBuffer flip = clij2.create(src.getDimensions(), NativeTypeEnum.Float);
        ClearCLBuffer flop = clij2.create(src.getDimensions(), NativeTypeEnum.Float);

        clij2.addImageAndScalar(src, flip, -minimum_intensity);
        clij2.minimumImageAndScalar(flip, flop, maximum_intensity - minimum_intensity);
        clij2.maximumImageAndScalar(flop, flip, 0);
        flop.close();

        ClearCLBuffer temp_matrix = clij2.create(dst_cooccurrence_matrix.getDimensions(), NativeTypeEnum.Float);

        GenerateIntegerGreyValueCooccurrenceCountMatrixHalfBox.generateIntegerGreyValueCooccurrenceCountMatrixHalfBox(clij2, flip, temp_matrix);
        double sum = clij2.sumOfAllPixels(temp_matrix);

        clij2.multiplyImageAndScalar(temp_matrix, dst_cooccurrence_matrix, 1.0 / sum);
        temp_matrix.close();

        flip.close();

        return true;
    }

    @Override
    public String getParameterHelpText() {
        return "Image integer_image, ByRef Image grey_value_cooccurrence_matrix_destination, Number min_grey_value, Number max_grey_value";
    }


    @Override
    public ClearCLBuffer createOutputBufferFromSource(ClearCLBuffer input)
    {
        double range = asFloat(args[3]) - asFloat(args[2]) + 1;
        ClearCLBuffer output = clij.createCLBuffer(new long[]{(long)range, (long)range}, NativeTypeEnum.Float);
        return output;
    }

    @Override
    public String getDescription() {
        return "Takes an image and an intensity range to determine a grey value co-occurrence matrix.\n\n" +
                "For determining which pixel intensities are neighbors, the box neighborhood is taken into account.\n" +
                "Pixels with intensity below minimum of the given range are considered having the minimum intensity.\n" +
                "Pixels with intensity above the maximimum of the given range are treated analogously.\n" +
                "The resulting co-occurrence matrix contains probability values between 0 and 1.";
    }

    @Override
    public String getAvailableForDimensions() {
        return "2D, 3D";
    }

    public static void main(String... args) {
        CLIJ2 clij2 = CLIJ2.getInstance();

        ClearCLBuffer buffer = clij2.pushString(
                "0 0 0\n" +
                    "0 1 0\n" +
                    "0 0 0\n\n" +
                    "2 2 2\n" +
                    "2 2 2\n" +
                    "2 2 2\n\n" +
                    "0 0 0\n" +
                    "0 0 0\n" +
                    "0 0 0"
        );

        ClearCLBuffer matrix = clij2.create(3, 3);

        GenerateGreyValueCooccurrenceMatrixBox.generateGreyValueCooccurrenceMatrixBox(clij2, buffer, matrix, 0f, 2f);

        clij2.print(matrix);
    }

    @Override
    public String getCategories() {
        return "Measurement";
    }
}
