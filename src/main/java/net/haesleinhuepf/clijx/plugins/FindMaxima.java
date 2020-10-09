package net.haesleinhuepf.clijx.plugins;

import ij.IJ;
import ij.ImageJ;
import ij.measure.ResultsTable;
import net.haesleinhuepf.clij.CLIJ;
import net.haesleinhuepf.clij.clearcl.ClearCL;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.clearcl.interfaces.ClearCLImageInterface;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.haesleinhuepf.clij.macro.CLIJMacroPlugin;
import net.haesleinhuepf.clij.macro.CLIJOpenCLProcessor;
import net.haesleinhuepf.clij.macro.documentation.OffersDocumentation;
import net.haesleinhuepf.clij2.AbstractCLIJ2Plugin;
import net.haesleinhuepf.clij2.CLIJ2;
import net.haesleinhuepf.clij2.plugins.MultiplyImages;
import net.haesleinhuepf.clij2.plugins.StatisticsOfLabelledPixels;
import net.haesleinhuepf.clij2.utilities.HasClassifiedInputOutput;
import net.haesleinhuepf.clij2.utilities.IsCategorized;
import net.haesleinhuepf.clijx.CLIJx;
import net.imglib2.img.array.ArrayImgs;
import org.scijava.plugin.Plugin;

import java.nio.FloatBuffer;
import java.util.HashMap;

import static net.haesleinhuepf.clij.utilities.CLIJUtilities.assertDifferent;
import static net.haesleinhuepf.clij2.utilities.CLIJUtilities.checkDimensions;

/**
 * Author: @haesleinhuepf
 *         June 2020
 */
@Plugin(type = CLIJMacroPlugin.class, name = "CLIJx_findMaxima")
public class FindMaxima extends AbstractCLIJ2Plugin implements CLIJMacroPlugin, CLIJOpenCLProcessor, OffersDocumentation, IsCategorized, HasClassifiedInputOutput {
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
        return findMaxima(getCLIJ2(), (ClearCLBuffer)( args[0]), (ClearCLBuffer)(args[1]), asFloat(args[2]));
    }

    public static boolean findMaxima(CLIJ2 clij2, ClearCLBuffer input, ClearCLBuffer output, Float noise_threshold) {
        ClearCLBuffer labelled_spots1 = clij2.create(output.getDimensions(), clij2.Float);
        //ClearCLBuffer label_map = clij2.create(labelled_spots1);

        //clij2.show(input, "inp");
        //detectMaxima(clij2, input, labelled_spots1);
        //clij2.connectedComponentsLabelingBox(labelled_spots1, label_map);
        //clij2.show(label_map, "init label");
        //eliminateWrongMaxima(clij2, input, label_map, labelled_spots1);
        //clij2.show(labelled_spots1, "init label corrected");

        FindMaximaPlateaus.findMaximaPlateaus(clij2, input, labelled_spots1);
        //clij2.show(labelled_spots1, "init");

        ClearCLBuffer initially_labeled_spots = clij2.create(output.getDimensions(), clij2.Float);
        clij2.connectedComponentsLabelingDiamond(labelled_spots1, initially_labeled_spots);
        //clij2.show(initially_labeled_spots, "init label");

        int number_of_objects = (int) clij2.maximumOfAllPixels(initially_labeled_spots);
        if (number_of_objects == 0) {
            System.err.println("Warning: No maxima found (CLIJx_findMaxima)");
            clij2.set(output, 0);
            labelled_spots1.close();
            initially_labeled_spots.close();
            return false;
        }

        ClearCLBuffer labelled_spots2 = clij2.create(output.getDimensions(), clij2.Float);

        ClearCLBuffer pointlist = clij2.create(number_of_objects, initially_labeled_spots.getDimension());

        clij2.labelledSpotsToPointList(initially_labeled_spots, pointlist);

        ClearCLBuffer intensities = clij2.create(number_of_objects + 1, 1);
        readIntensities(clij2, pointlist, input, intensities);
        pointlist.close();

        ClearCLBuffer threshold_list = clij2.create(number_of_objects, 1);
        ClearCLBuffer threshold_list2 = clij2.create(number_of_objects, 1);
        clij2.addImageAndScalar(intensities, threshold_list, -noise_threshold);
        //clij2.show(threshold_list, "threshold_list")
        //
        //clij2.show(input, "input");
        //clij2.show(initially_labeled_spots, "initially_labeled_spots");
        //if (true) return true;

        float[] flag_arr = new float[]{0};
        FloatBuffer flag_buffer = FloatBuffer.wrap(flag_arr);
        ClearCLBuffer flag = clij2.create(1, 1, 1);


        //clij2.show(initially_labeled_spots, "before");

        ClearCLBuffer touching_labels = initially_labeled_spots;
        ClearCLBuffer former_touching_labels = initially_labeled_spots;

        long timeout = 60000;
        long start_Time = System.currentTimeMillis();
        for (int i = 0; i > -1; i++) { // endless loop
            if (System.currentTimeMillis() - start_Time > timeout) {
                System.err.println("Warning: Time out while applying Find Maxima on GPU: CLIJx_findMxima.");
                break;
            }

            flag_arr[0] = 0;
            flag.readFrom(flag_buffer, true);

            //System.out.println("-------------------------- " + i);
            if (i % 2 == 0) {
                touching_labels = labelled_spots2;
            } else {
                touching_labels = labelled_spots1;
            }
            localThreshold(clij2, input, flag, threshold_list, former_touching_labels, touching_labels);

            // adapt threshold
            {
                clij2.maximumOfTouchingNeighbors(threshold_list, touching_labels, threshold_list2);
                ClearCLBuffer holder = threshold_list;
                threshold_list = threshold_list2;
                threshold_list2 = holder;
            }

            //clij2.show(touching_labels, "value " + i);
            //clij2.print(flag);

            flag.writeTo(flag_buffer, true);
            if (flag_arr[0] == 0) {
                break;
            }
            former_touching_labels = touching_labels;
        }
        clij2.closeIndexGapsInLabelMap(touching_labels, former_touching_labels);

        mergeTouchingLabelsSpecial(clij2, initially_labeled_spots, former_touching_labels, intensities, touching_labels);
        clij2.onlyzeroOverwriteMaximumDiamond(touching_labels, flag, output);

        initially_labeled_spots.close();
        labelled_spots1.close();
        labelled_spots2.close();
        flag.close();
        intensities.close();
        threshold_list.close();
        threshold_list2.close();


        ResultsTable table = new ResultsTable();
        clij2.statisticsOfBackgroundAndLabelledPixels(input, output, table);
        //table.show("t" +
        //        "");

        int min_index = table.getColumnIndex("" + StatisticsOfLabelledPixels.STATISTICS_ENTRY.MINIMUM_INTENSITY);
        int max_index = table.getColumnIndex("" + StatisticsOfLabelledPixels.STATISTICS_ENTRY.MAXIMUM_INTENSITY);
        int x_index = table.getColumnIndex("" + StatisticsOfLabelledPixels.STATISTICS_ENTRY.CENTROID_X);
        int y_index = table.getColumnIndex("" + StatisticsOfLabelledPixels.STATISTICS_ENTRY.CENTROID_Y);
        int z_index = table.getColumnIndex("" + StatisticsOfLabelledPixels.STATISTICS_ENTRY.CENTROID_Z);


        int value_index = (output.getDimension() > 2)?3:2;
        double [][] point_value_list = new double[table.size()][value_index + 1];
        int count = 0;
            for (int i = 0; i < table.size(); i++ ) {
            double minimum = table.getValueAsDouble(min_index, i );
            double maximum = table.getValueAsDouble(max_index, i );
            double x = table.getValueAsDouble(x_index, i );
            double y = table.getValueAsDouble(y_index, i );
            double z = table.getValueAsDouble(z_index, i );

            point_value_list[i][0] = x;
            point_value_list[i][1] = y;
            if (output.getDimension() > 2) {
                point_value_list[i][2] = z;
            }

            //if ( (maximum - minimum) > 5) {
            //    System.out.println("" + i + " range " + (maximum - minimum));
            //}

            if (maximum - minimum >= noise_threshold && i > 0) {
                count++;
                point_value_list[i][value_index] = count;
            } else {
                point_value_list[i][value_index] = 0;
            }
        }

        ClearCLBuffer pointvalue_image = clij2.pushMatXYZ(point_value_list);
        clij2.print(pointvalue_image);
        clij2.set(output, 0);
        clij2.writeValuesToPositions(pointvalue_image, output);
        pointvalue_image.close();
        //clij2.show(output, "p");

        return true;
    }

    private static boolean eliminateWrongMaxima(CLIJ2 clij2, ClearCLBuffer intensity, ClearCLBuffer src, ClearCLBuffer dst) {

        int number_of_objects = (int) clij2.maximumOfAllPixels(src);
        ClearCLBuffer exclude_vector = clij2.create(number_of_objects + 1, 1);
        clij2.set(exclude_vector, 0);

        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("intensity", intensity);
        parameters.put("src_labels", src);
        parameters.put("dst_vector", exclude_vector);

        clij2.execute(FindMaxima.class, "find_maxima_determine_labels_to_exclude_x.cl", "find_maxima_determine_labels_to_exclude", dst.getDimensions(), dst.getDimensions(), parameters);

        clij2.excludeLabels(exclude_vector, src, dst);
        exclude_vector.close();

        return true;
    }

    public static boolean mergeTouchingLabelsSpecial(CLIJ2 clij2, ClearCLBuffer initially_labeled_spots, ClearCLBuffer input, ClearCLBuffer intensities_vector, ClearCLBuffer output) {
        int number_of_objects = (int) clij2.maximumOfAllPixels(input);
        System.out.println("Object found : " + number_of_objects);
        //clij2.show(input, "label map?");

        ClearCLBuffer touch_matrix = clij2.create(number_of_objects + 1, number_of_objects + 1);
        clij2.generateTouchMatrix(input, touch_matrix);

        ClearCLBuffer adjacency_matrix = clij2.create(number_of_objects + 1, number_of_objects + 1);
        clij2.touchMatrixToAdjacencyMatrix(touch_matrix, adjacency_matrix);
        touch_matrix.close();

        clij2.setWhereXequalsY(adjacency_matrix, 1);
        clij2.setRow(adjacency_matrix, 0, 0);

        ClearCLBuffer adjacency_matrix_transposed = clij2.create(number_of_objects + 1, 1, number_of_objects + 1);
        ClearCLBuffer temp2 = clij2.create(adjacency_matrix);
        multiplyImages(clij2, intensities_vector, adjacency_matrix, temp2);

        //clij2.show(adjacency_matrix, "adj");
        //clij2.show(temp2, "temp");
        adjacency_matrix.close();

        clij2.transposeYZ(temp2, adjacency_matrix_transposed);
        temp2.close();

        ClearCLBuffer max = clij2.create(number_of_objects + 1, 1);
        ClearCLBuffer arg_max = clij2.create(number_of_objects + 1, 1);

        clij2.argMaximumZProjection(adjacency_matrix_transposed, max, arg_max);
        adjacency_matrix_transposed.close();
        max.close();

        ClearCLBuffer rampX = clij2.create(arg_max);
        clij2.setRampX(rampX);

        ClearCLBuffer binary = clij2.create(arg_max);
        clij2.notEqual(rampX, arg_max, binary);
        //clij2.show(rampX, "rampx");
        //clij2.show(arg_max, "arg_max");

        clij2.setColumn(binary, 0, 0); // background stays background

        clij2.excludeLabels(binary, initially_labeled_spots, output);

        arg_max.close();
        binary.close();
        rampX.close();

        return true;
    }


    private static boolean multiplyImages(CLIJ2 clij2, ClearCLImageInterface src, ClearCLImageInterface src1, ClearCLImageInterface dst) {
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("src", src);
        parameters.put("src1", src1);
        parameters.put("dst", dst);

        clij2.execute(FindMaxima.class, "find_maxima_multiply_images_x.cl", "find_maxima_multiply_images", dst.getDimensions(), dst.getDimensions(), parameters);
        return true;
    }

    private static boolean localThreshold(CLIJ2 clij2, ClearCLBuffer input, ClearCLBuffer flag, ClearCLBuffer threshold_list, ClearCLBuffer labelled_spots1, ClearCLBuffer labelled_spots2) {
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("input", input);
        parameters.put("flag", flag);
        parameters.put("threshold_list", threshold_list);
        parameters.put("labelmap_src", labelled_spots1);
        parameters.put("labelmap_dst", labelled_spots2);

        clij2.execute(FindMaxima.class, "find_maxima_local_threshold_x.cl", "find_maxima_local_threshold", labelled_spots2.getDimensions(), labelled_spots2.getDimensions(), parameters);
        return true;
    }

    private static boolean readIntensities(CLIJ2 clij2, ClearCLBuffer pointlist, ClearCLBuffer input, ClearCLBuffer intensities) {
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("pointlist", pointlist);
        parameters.put("input", input);
        parameters.put("intensities", intensities);

        clij2.execute(FindMaxima.class, "find_maxima_read_intensities_x.cl", "find_maxima_read_intensities", intensities.getDimensions(), new long[]{intensities.getWidth()}, parameters);
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
        return "Finds and labels local maxima with neighboring maxima and background above a given tolerance threshold.\n\n";
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
        findMaxima(clij2, temp, output, 20f);
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
