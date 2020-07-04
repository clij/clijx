package net.haesleinhuepf.clijx.plugins;

import ij.IJ;
import ij.ImageJ;
import net.haesleinhuepf.clij.CLIJ;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
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
        ClearCLBuffer initial_maxima = clij2.create(output.getDimensions(), clij2.UnsignedByte);
        ClearCLBuffer local_maxima1 = clij2.create(output.getDimensions(), clij2.UnsignedByte);
        ClearCLBuffer local_maxima2 = clij2.create(output.getDimensions(), clij2.UnsignedByte);
        ClearCLBuffer temp_binary = clij2.create(output.getDimensions(), clij2.UnsignedByte);
        detectMaxima(clij2, input, initial_maxima);
        clij2.show(input, "input");
        clij2.show(initial_maxima, "maxima initially");

        //clij2.print(input);
        //System.out.println("");
        //clij2.print(local_maxima2);
        //if (true) return true;

        float[] flag_arr = new float[]{0};
        FloatBuffer flag_buffer = FloatBuffer.wrap(flag_arr);

        ClearCLBuffer local_maxima_values1 = clij2.create(input);
        ClearCLBuffer local_maxima_values2 = clij2.create(input);
        clij2.multiplyImages(initial_maxima, input, local_maxima_values1);

        clij2.show(local_maxima_values1, "maxima before");
        clij2.print(local_maxima_values2);

        ClearCLBuffer flag = clij2.create(1, 1, 1);

        for (int i = 0; i < 10; i++) {
            flag_arr[0] = 0;
            flag.readFrom(flag_buffer, true);

            clij2.dilateSphere(local_maxima2, local_maxima1);

            System.out.println("-------------------------- " + i);
            if (i % 2 == 0) {
                extendMaxima(clij2, initial_maxima, flag, local_maxima1, input, local_maxima_values1, local_maxima_values2, noise_threshold);
                //System.out.println("local_maxima_values2");
                clij2.show(local_maxima_values2, "value " + i);
                //clij2.print(local_maxima_values2);
            } else {
                extendMaxima(clij2, initial_maxima, flag, local_maxima1, input, local_maxima_values2, local_maxima_values1, noise_threshold);
                //System.out.println("local_maxima_values1");
                clij2.show(local_maxima_values1, "value " + i);
                //clij2.print(local_maxima_values1);
            }
            clij2.print(flag);

            flag.writeTo(flag_buffer, true);
            if (flag_arr[0] == 0) {
                break;
            }

            clij2.notEqual(local_maxima_values1, local_maxima_values2, local_maxima2);

            clij2.binaryOr(local_maxima1, local_maxima2, temp_binary);
            clij2.copy(temp_binary, local_maxima2);
            //System.out.println("local_maxima1");
            //clij2.print(local_maxima1);
            //System.out.println("local_maxima_values1");
            //clij2.print(local_maxima_values1);

        }
        //clij2.dilateSphere(local_maxima2, local_maxima1);

        clij2.onlyzeroOverwriteMaximumDiamond(local_maxima_values2, flag, local_maxima_values1);
        clij2.onlyzeroOverwriteMaximumDiamond(local_maxima_values1, flag, local_maxima_values2);

        {
            ClearCLBuffer edges = local_maxima1; // reuse memory
            ClearCLBuffer binary = local_maxima2; // reuse memory

            clij2.detectLabelEdges(local_maxima_values2, edges);
            clij2.binaryNot(edges, binary);

            clij2.mask(local_maxima_values2, binary, local_maxima_values1);
            clij2.greaterConstant(local_maxima_values1, temp_binary, 0);
        }

        {
            ClearCLBuffer label_map = local_maxima_values2; // reuse memory
            clij2.connectedComponentsLabelingDiamond(temp_binary, label_map);

            int number_of_objects = (int) clij2.maximumOfAllPixels(label_map);

            ClearCLBuffer pointlist = clij2.create(number_of_objects, label_map.getDimension());
            clij2.centroidsOfLabels(label_map, pointlist);

            ClearCLBuffer pointlist_with_values = clij2.create(number_of_objects, label_map.getDimension() + 1);
            clij2.setRampX(pointlist_with_values);

            clij2.paste(pointlist, pointlist_with_values, 0, 0);
            pointlist.close();

            clij2.set(output, 0);
            clij2.writeValuesToPositions(pointlist_with_values, output);

            pointlist_with_values.close();
        }

        local_maxima1.close();
        local_maxima2.close();
        local_maxima_values1.close();
        local_maxima_values2.close();
        temp_binary.close();
        flag.close();

        return true;
    }

    private static boolean extendMaxima(CLIJ2 clij2, ClearCLBuffer initial_maxima, ClearCLBuffer flag, ClearCLBuffer binary, ClearCLBuffer input, ClearCLBuffer values_in, ClearCLBuffer values_out, Float noise_threshold) {
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("initial_maxima", initial_maxima);
        parameters.put("flag", flag);
        parameters.put("binary", binary);
        parameters.put("input", input);
        parameters.put("values_in", values_in);
        parameters.put("values_out", values_out);
        parameters.put("noise_threshold", noise_threshold);

        clij2.execute(FindMaxima.class, "find_maxima_extend_maxima_x.cl", "find_maxima_extend_maxima", values_out.getDimensions(), values_out.getDimensions(), parameters);
        return true;
    }

    private static boolean detectMaxima(CLIJ2 clij2, ClearCLBuffer input, ClearCLBuffer output) {
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("src", input);
        parameters.put("dst", output);

        clij2.execute(FindMaxima.class, "find_maxima_detect_maxima_x.cl", "find_maxima_detect_maxima", output.getDimensions(), output.getDimensions(), parameters);
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

        //clij2.copy(input, temp);
        clij2.gaussianBlur(input, temp, 10, 10);

        findMaxima(clij2, temp, output, 10f);

        clij2.show(output, "output");




    }







}
