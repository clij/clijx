package net.haesleinhuepf.clijx.plugins;

import ij.IJ;
import ij.ImageJ;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.haesleinhuepf.clij.macro.CLIJMacroPlugin;
import net.haesleinhuepf.clij.macro.CLIJOpenCLProcessor;
import net.haesleinhuepf.clij.macro.documentation.OffersDocumentation;
import net.haesleinhuepf.clij2.AbstractCLIJ2Plugin;
import net.haesleinhuepf.clij2.CLIJ2;
import net.haesleinhuepf.clij2.utilities.HasClassifiedInputOutput;
import net.haesleinhuepf.clij2.utilities.IsCategorized;
import net.haesleinhuepf.clijx.CLIJx;
import org.scijava.plugin.Plugin;

import java.util.HashMap;

/**
 * Author: @haesleinhuepf
 *         July 2020
 */
@Plugin(type = CLIJMacroPlugin.class, name = "CLIJx_findMaximaPlateaus")
public class FindMaximaPlateaus extends AbstractCLIJ2Plugin implements CLIJMacroPlugin, CLIJOpenCLProcessor, OffersDocumentation, IsCategorized, HasClassifiedInputOutput {
    @Override
    public String getInputType() {
        return "Image";
    }

    @Override
    public String getOutputType() {
        return "Label Image";
    }

    @Override
    public boolean executeCL() {
        return findMaximaPlateaus(getCLIJ2(), (ClearCLBuffer)( args[0]), (ClearCLBuffer)(args[1]));
    }

    public static boolean findMaximaPlateaus(CLIJ2 clij2, ClearCLBuffer input, ClearCLBuffer output) {
        ClearCLBuffer result = null;

        if ( output.getWidth() > 1) {
            result = clij2.create(output.getDimensions(), NativeTypeEnum.UnsignedByte);
            findMaxima1D(clij2, input, result, 0);
        }

        if ( output.getHeight() > 1) {
            ClearCLBuffer temp1 = clij2.create(output.getDimensions(), NativeTypeEnum.UnsignedByte);
            findMaxima1D(clij2, input, temp1, 1);

            if (result == null) {
                result = temp1;
            } else {
                ClearCLBuffer temp2 = clij2.create(output.getDimensions(), NativeTypeEnum.UnsignedByte);
                clij2.binaryAnd(result, temp1, temp2);
                temp1.close();
                result.close();
                result = temp2;
            }
        }

        if ( output.getDepth() > 1) {
            if (result == null) {
                findMaxima1D(clij2, input, output, 2);
            } else {
                ClearCLBuffer temp1 = clij2.create(output.getDimensions(), NativeTypeEnum.UnsignedByte);
                findMaxima1D(clij2, input, temp1, 2);
                clij2.binaryAnd(result, temp1, output);
                temp1.close();
                result.close();
            }
        } else {
            clij2.copy(result, output);
        }

        if (result != null) {
            result.close();
        } else {
            clij2.set(output, 1);
        }
        return true;
    }

    public static boolean findMaxima1D(CLIJ2 clij2, ClearCLBuffer input, ClearCLBuffer output, Integer dimension) {

        clij2.set(output, 0);

        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("src", input);
        parameters.put("dst", output);

        long[] global_sizes = new long[3];
        global_sizes[0] = output.getWidth();
        global_sizes[1] = output.getHeight();
        global_sizes[2] = output.getDepth();

        if (dimension == 0) {
            global_sizes[0] = 1;
            clij2.execute(FindMaximaPlateaus.class, "find_maxima_plateau_x.cl", "find_maxima_1d_x", output.getDimensions(), global_sizes, parameters);
        } else if (dimension == 1) {
            global_sizes[1] = 1;
            clij2.execute(FindMaximaPlateaus.class, "find_maxima_plateau_x.cl", "find_maxima_1d_y", output.getDimensions(), global_sizes, parameters);
        } else if (dimension == 2) {
            global_sizes[2] = 1;
            clij2.execute(FindMaximaPlateaus.class, "find_maxima_plateau_x.cl", "find_maxima_1d_z", output.getDimensions(), global_sizes, parameters);
        }
        return true;
    }


    @Override
    public String getParameterHelpText() {
        return "Image source, ByRef Image destination";
    }

    @Override
    public String getDescription() {
        return "Finds local maxima, which might be groups of pixels with the same intensity and marks them in a binary image.";
    }

    @Override
    public String getAvailableForDimensions() {
        return "2D, 3D";
    }

    public static void main(String ... args) {
        new ImageJ();


        CLIJ2 clij2 = CLIJ2.getInstance("HD");
        System.out.println(clij2.getGPUName());

        ClearCLBuffer input = clij2.push(IJ.openImage("src/test/resources/blobs.tif"));

        /*ClearCLBuffer input = clij2.pushString("" +
                "0 1 3 4 5 6 3 2 1 0 0 0 1 4"
        );*/
        /*
        ClearCLBuffer input = clij2.pushString("" +
                "0 0 0 0 0 0 0 0 0 0\n" +
                "0 2 4 3 4 2 4 0 3 0\n" +
                "0 0 0 0 0 0 0 0 0 0"
        );
         */


        /*
        ClearCLBuffer input = clij2.push(ArrayImgs.floats(new float[]{
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 1, 1, 1, 1, 1, 0, 1, 1, 0,
                        0, 1, 2, 2, 2, 2, 1, 2, 2, 1,
                        0, 1, 2, 3, 3, 3, 2, 3, 2, 1,
                        0, 0, 1, 2, 2, 1, 1, 1, 1, 0,
                        0, 0, 0, 1, 1, 1, 1, 1, 0, 0,
                        0, 0, 0, 0, 1, 1, 1, 2, 1, 0,
                        0, 0, 0, 0, 1, 1, 2, 3, 2, 0,
                        0, 0, 0, 0, 1, 1, 1, 2, 1, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                }, new long[]{10, 10}
        ));
*/
        ClearCLBuffer temp = clij2.create(input.getDimensions(), NativeTypeEnum.Float);
        ClearCLBuffer output = clij2.create(input);

        clij2.copy(input, temp);
        //clij2.gaussianBlur(input, temp, 10, 10);

        CLIJx clijx = CLIJx.getInstance();
        clijx.stopWatch("");
        findMaximaPlateaus(clij2, temp, output);
        clijx.stopWatch("first");
        //findMaxima(clij2, temp, output, 3f);
        //clijx.stopWatch("second");

        clij2.show(output, "output");

        input.close();
        temp.close();
        output.close();
        System.out.println(clij2.reportMemory());

    }


    @Override
    public String getCategories() {
        return "Binary, Detection";
    }
}
