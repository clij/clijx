package net.haesleinhuepf.clijx.plugins;

import ij.measure.ResultsTable;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.macro.CLIJMacroPlugin;
import net.haesleinhuepf.clij.macro.CLIJOpenCLProcessor;
import net.haesleinhuepf.clij.macro.documentation.OffersDocumentation;
import net.haesleinhuepf.clij2.AbstractCLIJ2Plugin;
import net.haesleinhuepf.clij2.CLIJ2;
import net.haesleinhuepf.clij2.plugins.StatisticsOfLabelledPixels;
import org.scijava.plugin.Plugin;

import java.util.HashMap;
import java.util.Set;

/**
 * Author: @haesleinhuepf
 *         January 2021
 */

@Plugin(type = CLIJMacroPlugin.class, name = "CLIJx_generateNeighborizedLabelFeatureImage")
public class GenerateNeighborizedLabelFeatureImage extends AbstractCLIJ2Plugin implements CLIJMacroPlugin, CLIJOpenCLProcessor, OffersDocumentation {

    final static StatisticsOfLabelledPixels.STATISTICS_ENTRY[] supported_features = {
            StatisticsOfLabelledPixels.STATISTICS_ENTRY.BOUNDING_BOX_DEPTH,
            StatisticsOfLabelledPixels.STATISTICS_ENTRY.BOUNDING_BOX_WIDTH,
            StatisticsOfLabelledPixels.STATISTICS_ENTRY.BOUNDING_BOX_HEIGHT,
            /*StatisticsOfLabelledPixels.STATISTICS_ENTRY.BOUNDING_BOX_X,
            StatisticsOfLabelledPixels.STATISTICS_ENTRY.BOUNDING_BOX_Y,
            StatisticsOfLabelledPixels.STATISTICS_ENTRY.BOUNDING_BOX_Z,
            StatisticsOfLabelledPixels.STATISTICS_ENTRY.BOUNDING_BOX_END_X,
            StatisticsOfLabelledPixels.STATISTICS_ENTRY.BOUNDING_BOX_END_Y,
            StatisticsOfLabelledPixels.STATISTICS_ENTRY.BOUNDING_BOX_END_Z,*/
            StatisticsOfLabelledPixels.STATISTICS_ENTRY.CENTROID_X,
            StatisticsOfLabelledPixels.STATISTICS_ENTRY.CENTROID_Y,
            StatisticsOfLabelledPixels.STATISTICS_ENTRY.CENTROID_Z,
            StatisticsOfLabelledPixels.STATISTICS_ENTRY.MASS_CENTER_X,
            StatisticsOfLabelledPixels.STATISTICS_ENTRY.MASS_CENTER_Y,
            StatisticsOfLabelledPixels.STATISTICS_ENTRY.MASS_CENTER_Z,
            StatisticsOfLabelledPixels.STATISTICS_ENTRY.MAX_DISTANCE_TO_CENTROID,
            StatisticsOfLabelledPixels.STATISTICS_ENTRY.MAX_DISTANCE_TO_MASS_CENTER,
            StatisticsOfLabelledPixels.STATISTICS_ENTRY.MEAN_DISTANCE_TO_CENTROID,
            StatisticsOfLabelledPixels.STATISTICS_ENTRY.MEAN_DISTANCE_TO_MASS_CENTER,
            StatisticsOfLabelledPixels.STATISTICS_ENTRY.MAX_MEAN_DISTANCE_TO_CENTROID_RATIO,
            StatisticsOfLabelledPixels.STATISTICS_ENTRY.MAX_MEAN_DISTANCE_TO_MASS_CENTER_RATIO,
            StatisticsOfLabelledPixels.STATISTICS_ENTRY.MAXIMUM_INTENSITY,
            StatisticsOfLabelledPixels.STATISTICS_ENTRY.MEAN_INTENSITY,
            StatisticsOfLabelledPixels.STATISTICS_ENTRY.MINIMUM_INTENSITY,
            StatisticsOfLabelledPixels.STATISTICS_ENTRY.SUM_INTENSITY,
            StatisticsOfLabelledPixels.STATISTICS_ENTRY.STANDARD_DEVIATION_INTENSITY,
            StatisticsOfLabelledPixels.STATISTICS_ENTRY.PIXEL_COUNT
    };


    @Override
    public boolean executeCL() {
        return generateNeighborizedLabelFeatureImage(getCLIJ2(), (ClearCLBuffer)( args[0]), (ClearCLBuffer)(args[1]), (ClearCLBuffer)(args[2]), (String)args[3], asInteger(args[4]));
    }

    public static ClearCLBuffer generateNeighborizedLabelFeatureImage(CLIJ2 clij2, ClearCLBuffer input, ClearCLBuffer label_map, String featureDefinitions, Integer neighbor_radius) {
        ClearCLBuffer feature_image_t =  new LabelFeatureGenerator(clij2, input, label_map, featureDefinitions, neighbor_radius).getImage();
        ClearCLBuffer feature_image = clij2.create(feature_image_t.getHeight(), feature_image_t.getWidth());
        clij2.transposeXY(feature_image_t, feature_image);
        feature_image_t.close();

        return feature_image;
    }

    public static boolean generateNeighborizedLabelFeatureImage(CLIJ2 clij2, ClearCLBuffer input, ClearCLBuffer label_map, ClearCLBuffer feature_image, String featureDefinitions, Integer neighbor_radius) {
        ClearCLBuffer feature_image_t = new LabelFeatureGenerator(clij2, input, label_map, featureDefinitions, neighbor_radius).getImage();
        clij2.transposeXY(feature_image_t, feature_image);
        feature_image_t.close();

        return true;
    }

    private interface Computer {
        void compute();
    }

    private static class LabelFeatureGenerator {

        private CLIJ2 clij2;
        private ClearCLBuffer input;
        private ClearCLBuffer label_map;
        int number_of_labels = -1;
        ClearCLBuffer touch_matrix = null;
        ClearCLBuffer distance_matrix = null;
        ClearCLBuffer pointlist = null;
        ResultsTable statistics_of_labels = null;


        ClearCLBuffer measurement_vector = null;
        ClearCLBuffer temp_vector = null;
        double numericParameter = 0;


        final static HashMap<String, Computer> computers = new HashMap<>();

        ClearCLBuffer result = null;

        private LabelFeatureGenerator() {
            this(null, null, null, null, null);
        }

        private LabelFeatureGenerator(CLIJ2 clij2, ClearCLBuffer input, ClearCLBuffer label_map, String featureDefinitions, Integer neighbor_radius) {
            if (neighbor_radius < 0) {
                System.out.println("Warning: neighbor_radius < 0.");
                neighbor_radius = 0;
            }

            // ---------------------------------------------------------------------------------------------------------
            // todo: not sure where to put this
            computers.put("average_touch_pixel_count", () -> {
                ClearCLBuffer touch_count_matrix = clij2.create(distance_matrix);
                clij2.generateTouchCountMatrix(label_map, touch_count_matrix);
                ClearCLBuffer sum_vector = clij2.create(touch_count_matrix.getWidth(), 1L);
                clij2.sumYProjection(touch_count_matrix, sum_vector);
                ClearCLBuffer count_vector = clij2.create(touch_count_matrix.getWidth(), 1L);
                clij2.countTouchingNeighbors(touch_matrix, count_vector);
                clij2.divideImages(count_vector, sum_vector, measurement_vector);
                touch_count_matrix.close();
                sum_vector.close();
                count_vector.close();
            });

            computers.put("average_distance_n_closest_neighbors", () -> {
                clij2.averageDistanceOfNClosestPoints(distance_matrix, measurement_vector, numericParameter);
            });
            computers.put("average_distance_of_touching_neighbors", () -> {
                clij2.averageDistanceOfTouchingNeighbors(distance_matrix, touch_matrix, measurement_vector);
            });
            computers.put("count_touching_neighbors", () -> {
                clij2.countTouchingNeighbors(touch_matrix, measurement_vector);
            });

            // ---------------------------------------------------------------------------------------------------------
            computers.put("local_maximum_average_distance_n_closest_neighbors", () -> {
                clij2.averageDistanceOfNClosestPoints(distance_matrix, temp_vector, numericParameter);
                clij2.maximumOfTouchingNeighbors(temp_vector, touch_matrix, measurement_vector);
            });
            computers.put("local_maximum_average_distance_of_touching_neighbors", () -> {
                clij2.averageDistanceOfTouchingNeighbors(distance_matrix, touch_matrix, temp_vector);
                clij2.maximumOfTouchingNeighbors(temp_vector, touch_matrix, measurement_vector);
            });
            computers.put("local_maximum_count_touching_neighbors", () -> {
                clij2.countTouchingNeighbors(touch_matrix, temp_vector);
                clij2.maximumOfTouchingNeighbors(temp_vector, touch_matrix, measurement_vector);
            });
            // ---------------------------------------------------------------------------------------------------------
            computers.put("local_minimum_average_distance_n_closest_neighbors", () -> {
                clij2.averageDistanceOfNClosestPoints(distance_matrix, temp_vector, numericParameter);
                clij2.minimumOfTouchingNeighbors(temp_vector, touch_matrix, measurement_vector);
            });
            computers.put("local_minimum_average_distance_of_touching_neighbors", () -> {
                clij2.averageDistanceOfTouchingNeighbors(distance_matrix, touch_matrix, temp_vector);
                clij2.minimumOfTouchingNeighbors(temp_vector, touch_matrix, measurement_vector);
            });
            computers.put("local_minimum_count_touching_neighbors", () -> {
                clij2.countTouchingNeighbors(touch_matrix, temp_vector);
                clij2.minimumOfTouchingNeighbors(temp_vector, touch_matrix, measurement_vector);
            });
            // ---------------------------------------------------------------------------------------------------------
            computers.put("local_mean_average_distance_n_closest_neighbors", () -> {
                clij2.averageDistanceOfNClosestPoints(distance_matrix, temp_vector, numericParameter);
                clij2.meanOfTouchingNeighbors(temp_vector, touch_matrix, measurement_vector);
            });
            computers.put("local_mean_average_distance_of_touching_neighbors", () -> {
                clij2.averageDistanceOfTouchingNeighbors(distance_matrix, touch_matrix, temp_vector);
                clij2.meanOfTouchingNeighbors(temp_vector, touch_matrix, measurement_vector);
            });
            computers.put("local_mean_count_touching_neighbors", () -> {
                clij2.countTouchingNeighbors(touch_matrix, temp_vector);
                clij2.meanOfTouchingNeighbors(temp_vector, touch_matrix, measurement_vector);
            });
            // ---------------------------------------------------------------------------------------------------------
            computers.put("local_standard_deviation_average_distance_n_closest_neighbors", () -> {
                clij2.averageDistanceOfNClosestPoints(distance_matrix, temp_vector, numericParameter);
                clij2.standardDeviationOfTouchingNeighbors(temp_vector, touch_matrix, measurement_vector);
            });
            computers.put("local_standard_deviation_average_distance_of_touching_neighbors", () -> {
                clij2.averageDistanceOfTouchingNeighbors(distance_matrix, touch_matrix, temp_vector);
                clij2.standardDeviationOfTouchingNeighbors(temp_vector, touch_matrix, measurement_vector);
            });
            computers.put("local_standard_deviation_count_touching_neighbors", () -> {
                clij2.countTouchingNeighbors(touch_matrix, temp_vector);
                clij2.standardDeviationOfTouchingNeighbors(temp_vector, touch_matrix, measurement_vector);
            });
            // ---------------------------------------------------------------------------------------------------------
            if (clij2 == null) {
                return;
            }

            this.clij2 = clij2;
            this.input = input;
            this.label_map = label_map;

            number_of_labels = (int) clij2.maximumOfAllPixels(label_map);

            touch_matrix = clij2.create(number_of_labels + 1, number_of_labels + 1);
            clij2.generateTouchMatrix(label_map, touch_matrix);

            pointlist = clij2.create(number_of_labels, label_map.getDimension());
            clij2.centroidsOfLabels(label_map, pointlist);

            distance_matrix = clij2.create(number_of_labels + 1, number_of_labels + 1);
            clij2.generateDistanceMatrix(pointlist, pointlist, distance_matrix);

            temp_vector = clij2.create(number_of_labels, 1, 1);


            // generate features
            String[] definitionsArray = preparseFeatures(featureDefinitions);
            result = clij2.create(number_of_labels, definitionsArray.length * (neighbor_radius + 1));
            System.out.println("feature table has size " + result.getWidth() + "/" + result.getHeight());

            int row = 0;
            for (String featureDefinition : definitionsArray) {
                ClearCLBuffer buffer = generateFeature(featureDefinition);
                if (buffer != null) {
                    System.out.println("Buffer " + row + " has size " + buffer.getWidth() + "/" + buffer.getHeight());
                    clij2.paste(buffer, result, 0, row);

                    ClearCLBuffer propagate_feature = clij2.create(buffer);
                    ClearCLBuffer temp = clij2.create(touch_matrix);
                    ClearCLBuffer neighbor_matrix = clij2.create(touch_matrix);
                    clij2.copy(touch_matrix, neighbor_matrix);
                    for (int i = 1; i < neighbor_radius; i ++) {

                        clij2.meanOfTouchingNeighbors(buffer, neighbor_matrix, propagate_feature);
                        row++;
                        clij2.paste(propagate_feature, result, 0, row);

                        if (i < neighbor_radius - 1) {
                            clij2.neighborsOfNeighbors(neighbor_matrix, temp);
                            clij2.copy(temp, neighbor_matrix);
                        }
                    }
                    neighbor_matrix.close();
                    propagate_feature.close();
                    buffer.close();
                } else {
                    System.out.println("Warning: feature " + featureDefinition + " not implemented.");
                    clij2.setRow(result, row, 0);
                }
                row++;
            }

            touch_matrix.close();
            pointlist.close();
            distance_matrix.close();
            temp_vector.close();
        }

        private ClearCLBuffer generateFeature(String featureDefinition) {
            System.out.println("Determining " + featureDefinition);

            String[] temp = featureDefinition.split("=");
            String featureName = temp[0];
            String parameter = temp.length > 1 ? temp[1] : "0";
            numericParameter = Double.parseDouble(parameter);

            measurement_vector = clij2.create(number_of_labels, 1, 1);

            for (StatisticsOfLabelledPixels.STATISTICS_ENTRY supported_stats_feature : supported_features) {
                if (featureName.compareTo(supported_stats_feature.toString().toLowerCase()) == 0) {
                    clij2.pushResultsTableColumn(measurement_vector, getStatistics(), supported_stats_feature.toString());
                    //temp_vector.close();
                    clij2.print(measurement_vector);
                    return measurement_vector;
                }
            }

            for (String key : computers.keySet()) {
                if (key.compareTo(featureDefinition.toLowerCase()) == 0) {
                    computers.get(key).compute();
                    //temp_vector.close();
                    clij2.print(measurement_vector);
                    return measurement_vector;
                }
            }
            System.out.println("NONE");
            return null;
        }


        private ClearCLBuffer getImage() {
            return result;
        }

        private ResultsTable getStatistics() {
            if (statistics_of_labels == null) {
                statistics_of_labels = new ResultsTable();
                clij2.statisticsOfLabelledPixels(input, label_map, statistics_of_labels);
            }
            return statistics_of_labels;
        }

        private Set<String> getLabelPropertyNames() {
            return computers.keySet();
        }
    }

    @Override
    public ClearCLBuffer createOutputBufferFromSource(ClearCLBuffer input) {
        long number_of_labels = (long)getCLIJ2().maximumOfAllPixels((ClearCLBuffer)args[2]);
        String[] featureDefinitions = preparseFeatures((String)args[3]);

        return getCLIJ2().create(featureDefinitions.length, number_of_labels);
    }

    private static String[] preparseFeatures(String featureDefinitions) {
        featureDefinitions = featureDefinitions.toLowerCase();
        featureDefinitions = featureDefinitions.trim();
        featureDefinitions = featureDefinitions.replace("\r", " ");
        featureDefinitions = featureDefinitions.replace("\n", " ");
        while (featureDefinitions.contains("  ")) {
            featureDefinitions = featureDefinitions.replace("  ", " ");
        }
        System.out.println("F:" + featureDefinitions);
        return featureDefinitions.split(" ");
    }

    @Override
    public String getParameterHelpText() {
        return "Image input, Image label_map, Image label_feature_image_destination, String feature_definitions, Number neighbor_radius";
    }

    @Override
    public String getDescription() {
        String description = "Generates a feature image for Trainable Segmentation / Clustering. \n\n" +
                "Use this terminology to specify which features should be generated:\n";

        for (StatisticsOfLabelledPixels.STATISTICS_ENTRY supported_stats_feature : supported_features) {
            description = description + "* " + supported_stats_feature.toString() + "\n";
        }
        for (String feature : new LabelFeatureGenerator().getLabelPropertyNames()) {
            description = description + "* " + feature.toString() + "\n";
        }
        description = description +
                "\n" +
                "Example: \"" + StatisticsOfLabelledPixels.STATISTICS_ENTRY.MEAN_INTENSITY.toString() + " count_touching_neighbors\"";

        return description;
    }

    public static String defaultFeatures() {
        return StatisticsOfLabelledPixels.STATISTICS_ENTRY.MEAN_INTENSITY.toString() + " "  +
                StatisticsOfLabelledPixels.STATISTICS_ENTRY.STANDARD_DEVIATION_INTENSITY.toString() + " " +
                StatisticsOfLabelledPixels.STATISTICS_ENTRY.MAX_MEAN_DISTANCE_TO_CENTROID_RATIO.toString() + " " +
                StatisticsOfLabelledPixels.STATISTICS_ENTRY.PIXEL_COUNT.toString() + " " +
                "count_touching_neighbors " +
                "average_distance_of_touching_neighbors";
    }

    public static String[] allFeatures() {
        Set<String> set1 = new LabelFeatureGenerator().getLabelPropertyNames();

        String[] result = new String[set1.size() + supported_features.length];

        int count = 0;
        for (StatisticsOfLabelledPixels.STATISTICS_ENTRY entry : supported_features) {
            result[count] = entry.toString();
            count ++;
        }

        for (String entry : set1) {
            result[count] = entry;
            count ++;
        }

        return result;
    }

    @Override
    public String getAvailableForDimensions() {
        return "2D, 3D";
    }

}
