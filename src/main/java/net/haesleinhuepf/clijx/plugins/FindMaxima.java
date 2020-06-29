package net.haesleinhuepf.clijx.plugins;

import net.haesleinhuepf.clij.CLIJ;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.macro.CLIJMacroPlugin;
import net.haesleinhuepf.clij.macro.CLIJOpenCLProcessor;
import net.haesleinhuepf.clij.macro.documentation.OffersDocumentation;
import net.haesleinhuepf.clij2.AbstractCLIJ2Plugin;
import net.haesleinhuepf.clij2.CLIJ2;
import net.imglib2.img.array.ArrayImgs;
import org.scijava.plugin.Plugin;

import java.nio.FloatBuffer;
import java.util.HashMap;

/**
 * Author: @haesleinhuepf
 *         June 2020
 */
@Plugin(type = CLIJMacroPlugin.class, name = "CLIJ2_findMaxima")
public class FindMaxima extends AbstractCLIJ2Plugin implements CLIJMacroPlugin, CLIJOpenCLProcessor, OffersDocumentation {

    @Override
    public boolean executeCL() {
        return findMaxima(getCLIJ2(), (ClearCLBuffer)( args[0]), (ClearCLBuffer)(args[1]), asFloat(args[2]));
    }

    public static boolean findMaxima(CLIJ2 clij2, ClearCLBuffer input, ClearCLBuffer output, Float noise_threshold) {
        ClearCLBuffer local_maxima1 = clij2.create(output.getDimensions(), clij2.UnsignedByte);
        ClearCLBuffer local_maxima2 = clij2.create(output.getDimensions(), clij2.UnsignedByte);
        clij2.detectMaximaBox(input, local_maxima2, 0, 0, 0);

        //clij2.print(input);
        //System.out.println("");
        //clij2.print(local_maxima2);
        //if (true) return true;

        float[] flag_arr = new float[]{0};
        FloatBuffer flag_buffer = FloatBuffer.wrap(flag_arr);

        ClearCLBuffer local_maxima_values1 = clij2.create(input);
        ClearCLBuffer local_maxima_values2 = clij2.create(input);
        clij2.multiplyImages(local_maxima2, input, local_maxima_values1);

        ClearCLBuffer flag = clij2.create(1, 1, 1);

        for (int i = 0; i < 10; i++) {
            flag_arr[0] = 0;
            flag.readFrom(flag_buffer, true);

            clij2.dilateSphere(local_maxima2, local_maxima1);

            System.out.println("--------------------------\n" + i);
            if (i % 2 == 0) {
                extendMaxima(clij2, flag, local_maxima1, input, local_maxima_values1, local_maxima_values2, noise_threshold);
                System.out.println("local_maxima_values2");
                clij2.print(local_maxima_values2);
            } else {
                extendMaxima(clij2, flag, local_maxima1, input, local_maxima_values2, local_maxima_values1, noise_threshold);
                System.out.println("local_maxima_values1");
                clij2.print(local_maxima_values1);
            }
            clij2.print(flag);

            flag.writeTo(flag_buffer, true);
            if (flag_arr[0] == 0) {
                break;
            }

            clij2.notEqual(local_maxima_values1, local_maxima_values2, local_maxima2);
            //System.out.println("local_maxima1");
            //clij2.print(local_maxima1);
            //System.out.println("local_maxima_values1");
            //clij2.print(local_maxima_values1);

        }
        //clij2.dilateSphere(local_maxima2, local_maxima1);






        return false;
    }

    private static boolean extendMaxima(CLIJ2 clij2, ClearCLBuffer flag, ClearCLBuffer binary, ClearCLBuffer input, ClearCLBuffer values_in, ClearCLBuffer values_out, Float noise_threshold) {
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("flag", flag);
        parameters.put("binary", binary);
        parameters.put("input", input);
        parameters.put("values_in", values_in);
        parameters.put("values_out", values_out);
        parameters.put("noise_threshold", noise_threshold);

        clij2.execute(FindMaxima.class, "find_maxima_extend_maxima_x.cl", "find_maxima_extend_maxima", values_out.getDimensions(), values_out.getDimensions(), parameters);
        return true;
    }


    @Override
    public String getParameterHelpText() {
        return "Image source, ByRef Image destination, Number noise_threshold";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public String getAvailableForDimensions() {
        return "2D, 3D";
    }

    public static void main(String ... args) {
        CLIJ2 clij2 = CLIJ2.getInstance();

        /*
        ClearCLBuffer input = clij2.pushString("" +
                "0 0 0 0 0 0 0 0 0 0\n" +
                "0 1 3 2 1 0 0 0 0 0\n" +
                "0 0 0 0 0 0 0 0 0 0"
        );
*/
        ClearCLBuffer input = clij2.pushString("" +
                "0 0 0 0 0 0 0 0 0 0\n" +
                "0 2 4 3 4 2 4 0 3 0\n" +
                "0 0 0 0 0 0 0 0 0 0"
        );


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
        ClearCLBuffer output = clij2.create(input);

        findMaxima(clij2, input, output, 1f);




    }





}
