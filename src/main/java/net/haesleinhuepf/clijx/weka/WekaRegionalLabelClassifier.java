package net.haesleinhuepf.clijx.weka;


import ij.measure.ResultsTable;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.clearcl.enums.ImageChannelDataType;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.haesleinhuepf.clij.macro.CLIJMacroPlugin;
import net.haesleinhuepf.clij.macro.CLIJOpenCLProcessor;
import net.haesleinhuepf.clij.macro.documentation.OffersDocumentation;
import net.haesleinhuepf.clij2.AbstractCLIJ2Plugin;
import net.haesleinhuepf.clij2.CLIJ2;
import net.haesleinhuepf.clij2.utilities.HasClassifiedInputOutput;
import net.haesleinhuepf.clij2.utilities.IsCategorized;
import net.haesleinhuepf.clij2.plugins.GenerateProximalNeighborsMatrix;
import org.scijava.plugin.Plugin;
import weka.filters.supervised.instance.ClassBalancer;

import java.io.File;

@Plugin(type = CLIJMacroPlugin.class, name = "CLIJx_wekaRegionalLabelClassifier")
public class WekaRegionalLabelClassifier extends AbstractCLIJ2Plugin implements CLIJMacroPlugin, CLIJOpenCLProcessor, OffersDocumentation, IsCategorized, HasClassifiedInputOutput {

    private static CLIJxWeka2 clijxWeka2 = null;
    private static String last_loaded_filename = "";

    @Override
    public String getParameterHelpText() {
        return "Image input, Image label_map, ByRef Image destination, String features, String modelfilename, Number radius_of_maximum, Number radius_of_minimum, Number radius_of_mean, Number radius_of_standard_deviation";
    }

    @Override
    public Object[] getDefaultValues() {
        return new Object[]{null, null, null, GenerateLabelFeatureImage.defaultFeatures(), "object_classifier.model", 0, 0, 1, 0};
    }

    @Override
    public boolean executeCL() {
        ClearCLBuffer input = (ClearCLBuffer) args[0];
        ClearCLBuffer labelmap = (ClearCLBuffer) args[1];
        ClearCLBuffer output = (ClearCLBuffer) args[2];

        String features = (String) args[3];
        String model_filename = (String) args[4];
        int radius_of_maximum = asInteger(args[5]);
        int radius_of_minimum = asInteger(args[6]);
        int radius_of_mean = asInteger(args[7]);
        int radius_of_stddev = asInteger(args[8]);

        return wekaRegionalLabelClassifier(getCLIJ2(), input, labelmap, output, features, model_filename, radius_of_maximum, radius_of_minimum, radius_of_mean, radius_of_stddev);
    }

    public static boolean wekaRegionalLabelClassifier(CLIJ2 clij2, ClearCLBuffer input, ClearCLBuffer labelMap, ClearCLBuffer output, String features, String model_filename, Integer radius_of_maximum, Integer radius_of_minimum, Integer radius_of_mean, Integer radius_of_standard_deviation)
    {
        if (!new File(model_filename).exists()) {
            clij2.set(output, 0);
            System.out.println("Model " + model_filename + " not found. Cancelling WekaLabelClassifier.");
            return true;
        }

        ClearCLBuffer featureImage = generateRegionalLabelFeatureImage(clij2, input, labelMap, features, radius_of_maximum, radius_of_minimum, radius_of_mean, radius_of_standard_deviation);

        ResultsTable table = new ResultsTable();
        clij2.pullToResultsTable(featureImage, table);
        featureImage.close();


        if (!(clijxWeka2 != null && last_loaded_filename.compareTo(model_filename) == 0)) {
            clijxWeka2 = new CLIJxWeka2(clij2, null, model_filename);
            last_loaded_filename = model_filename;
            System.out.println("Load model");
        }

        ApplyWekaToTable.applyWekaToTable(clij2, table, "CLASS", clijxWeka2);

        //table.show("PREDICTION");

        ClearCLBuffer vector = clij2.create(table.size(), 1, 1);
        clij2.pushResultsTableColumn(vector, table, "CLASS");

        ClearCLBuffer vector_with_background = clij2.create(table.size() + 1, 1, 1);
        clij2.set(vector_with_background, 0);
        clij2.paste(vector, vector_with_background, 1, 0, 0);

        //System.out.println("Vector");
        //clij2.print(vector);
        //System.out.println("Vector with bg");
        //clij2.print(vector_with_background);

        clij2.replaceIntensities(labelMap, vector_with_background, output);

        //clij2.show(labelMap, "labels");
        //clij2.show(output, "output");

        vector.close();
        vector_with_background.close();

        return true;
    }

    public static ClearCLBuffer generateRegionalLabelFeatureImage(CLIJ2 clij2, ClearCLBuffer input, ClearCLBuffer labelMap, String features, int radius_of_maximum, int radius_of_minimum, int radius_of_mean, int radius_of_standard_deviation) {
        ClearCLBuffer featureImage = GenerateLabelFeatureImage.generateLabelFeatureImage(clij2, input, labelMap, features);
        // clij2.print(featureImage);

        int max_radius = Math.max(radius_of_maximum,
                Math.max(radius_of_minimum,
                        Math.max(radius_of_mean,
                                radius_of_standard_deviation
                        )));

        if (max_radius > 0) {
            int num_blocks = ((radius_of_maximum > 0)?1:0) +
                    ((radius_of_minimum > 0)?1:0) +
                    ((radius_of_mean > 0)?1:0) +
                    ((radius_of_standard_deviation > 0)?1:0) +
                    1;

            ClearCLBuffer feature_image_t = clij2.create(featureImage.getHeight(), featureImage.getWidth());
            clij2.transposeXY(featureImage, feature_image_t);

            ClearCLBuffer feature_image_extd_t = clij2.create(feature_image_t.getWidth(), feature_image_t.getHeight() * num_blocks);
            ClearCLBuffer vector_in = clij2.create(feature_image_t.getWidth(), 1);
            ClearCLBuffer vector_out = clij2.create(feature_image_t.getWidth(), 1);

            int num_labels = (int) clij2.maximumOfAllPixels(labelMap);
            ClearCLBuffer neighbor_matrix = clij2.create(new long[]{num_labels + 1, num_labels + 1}, NativeTypeEnum.UnsignedByte);
            ClearCLBuffer centroids = clij2.create(new long[]{num_labels + 1, labelMap.getDimension()}, NativeTypeEnum.Float);
            clij2.centroidsOfBackgroundAndLabels(labelMap, centroids);
            ClearCLBuffer distance_matrix = clij2.create(new long[]{num_labels + 1, num_labels + 1}, NativeTypeEnum.UnsignedByte);
            clij2.generateDistanceMatrix(centroids, centroids, distance_matrix);
            centroids.close();

            int count = 0;
            clij2.paste(feature_image_t, feature_image_extd_t, 0, 0);
            count += feature_image_t.getHeight();

            // neighborize it
            for (int f = 0; f < feature_image_t.getHeight(); f++) {
                // read a feature vector
                clij2.crop(feature_image_t, vector_in, 0, f);

                if (radius_of_maximum > 0) {
                    GenerateProximalNeighborsMatrix.generateProximalNeighborsMatrix(clij2, distance_matrix, neighbor_matrix, (float)0, (float)radius_of_maximum);
                    clij2.maximumOfTouchingNeighbors(vector_in, neighbor_matrix, vector_out);
                    clij2.paste(vector_out, feature_image_extd_t, 0, count);
                    count++;
                }
                if (radius_of_minimum > 0) {
                    GenerateProximalNeighborsMatrix.generateProximalNeighborsMatrix(clij2, distance_matrix, neighbor_matrix, (float)0, (float)radius_of_minimum);
                    clij2.minimumOfTouchingNeighbors(vector_in, neighbor_matrix, vector_out);
                    clij2.paste(vector_out, feature_image_extd_t, 0, count);
                    count++;
                }
                if (radius_of_mean > 0) {
                    GenerateProximalNeighborsMatrix.generateProximalNeighborsMatrix(clij2, distance_matrix, neighbor_matrix, (float)0, (float)radius_of_mean);
                    clij2.meanOfTouchingNeighbors(vector_in, neighbor_matrix, vector_out);
                    clij2.paste(vector_out, feature_image_extd_t, 0, count);
                    count++;
                }
                if (radius_of_standard_deviation > 0) {
                    GenerateProximalNeighborsMatrix.generateProximalNeighborsMatrix(clij2, distance_matrix, neighbor_matrix, (float)0, (float)radius_of_standard_deviation);
                    clij2.standardDeviationOfTouchingNeighbors(vector_in, neighbor_matrix, vector_out);
                    clij2.paste(vector_out, feature_image_extd_t, 0, count);
                    count++;
                }
            }
            feature_image_t.close();
            vector_in.close();
            vector_out.close();
            neighbor_matrix.close();
            distance_matrix.close();

            ClearCLBuffer feature_image_extd = clij2.create(feature_image_extd_t.getHeight(), feature_image_extd_t.getWidth());
            clij2.transposeXY(feature_image_extd_t, feature_image_extd);
            feature_image_extd_t.close();

            featureImage.close();
            featureImage = feature_image_extd;
        }
        return featureImage;
    }

    @Override
    public String getDescription() {
        return "Applies a pre-trained CLIJx-Weka model to an image and a corresponding label map to classify labeled objects.\n\n" +
                "Given radii allow to configure if values of proximal neighbors, other labels with centroids closer \n" +
                "than given radius, should be taken into account, e.g. for determining the regional maximum.\n\n" +
                "Make sure that the handed over feature list and radii are the same used while training the model.";
    }

    @Override
    public String getAvailableForDimensions() {
        return "2D, 3D";
    }

    @Override
    public String getCategories() {
        return "Label,Segmentation";
    }

    public static void invalidateCache() {
        last_loaded_filename = "";
    }


    @Override
    public String getInputType() {
        return "Label Image";
    }

    @Override
    public String getOutputType() {
        return "Label Image";
    }
}
