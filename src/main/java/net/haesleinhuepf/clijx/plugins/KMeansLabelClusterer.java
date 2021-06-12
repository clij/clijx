package net.haesleinhuepf.clijx.plugins;


import ij.IJ;
import ij.measure.ResultsTable;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.macro.CLIJMacroPlugin;
import net.haesleinhuepf.clij.macro.CLIJOpenCLProcessor;
import net.haesleinhuepf.clij.macro.documentation.OffersDocumentation;
import net.haesleinhuepf.clij2.AbstractCLIJ2Plugin;
import net.haesleinhuepf.clij2.CLIJ2;
import net.haesleinhuepf.clij2.plugins.ModeOfTouchingNeighbors;
import net.haesleinhuepf.clij2.utilities.HasClassifiedInputOutput;
import net.haesleinhuepf.clij2.utilities.IsCategorized;
import net.haesleinhuepf.clijx.weka.ApplyWekaToTable;
import net.haesleinhuepf.clijx.weka.CLIJxWeka2;
import net.haesleinhuepf.clijx.weka.GenerateLabelFeatureImage;
import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.ml.clustering.DoublePoint;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.scijava.plugin.Plugin;

import javax.xml.transform.Result;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * Author: @haesleinhuepf
 * January 2021
 */
@Plugin(type = CLIJMacroPlugin.class, name = "CLIJx_kMeansLabelClusterer")
public class KMeansLabelClusterer extends AbstractCLIJ2Plugin implements CLIJMacroPlugin, CLIJOpenCLProcessor, OffersDocumentation, IsCategorized, HasClassifiedInputOutput {

    private static String last_loaded_filename = "";
    private static List<CentroidCluster<DoublePoint>> centroids = null;

    @Override
    public String getParameterHelpText() {
        return "Image input, Image label_map, ByRef Image destination, String features, String modelfilename, Number number_of_classes, Number neighbor_radius, Boolean train";
    }

    @Override
    public Object[] getDefaultValues() {
        return new Object[]{null, null, null, GenerateLabelFeatureImage.defaultFeatures(), "kmeans_clusterer.model.csv", 2, 0, true};
    }

    @Override
    public boolean executeCL() {
        ClearCLBuffer input = (ClearCLBuffer) args[0];
        ClearCLBuffer labelmap = (ClearCLBuffer) args[1];
        ClearCLBuffer output = (ClearCLBuffer) args[2];

        String features = (String) args[3];
        String model_filename = (String) args[4];
        int num_classes = asInteger(args[5]);
        int neighbor_radius = asInteger(args[6]);
        boolean train = asBoolean(args[7]);

        return kMeansLabelClusterer(getCLIJ2(), input, labelmap, output, features, model_filename, num_classes, neighbor_radius, train);
    }

    public static boolean kMeansLabelClusterer(CLIJ2 clij2, ClearCLBuffer input, ClearCLBuffer labelMap, ClearCLBuffer output, String features, String model_filename, Integer num_classes, Integer neighbor_radius, Boolean train)
    {
        if (!new File(model_filename).exists()) {
            clij2.set(output, 0);
            System.out.println("Model " + model_filename + " not found. Will train new KMeansLabelClusterer.");
            train = true;
        }
        if (centroids != null) {
            if (num_classes != centroids.size()) {
                System.out.println("Number of classes doesn't match to trained model. Will train new KMeansLabelClusterer.");
                train = true;
            }
        }

        ClearCLBuffer featureImage = GenerateLabelFeatureImage.generateLabelFeatureImage(clij2, input, labelMap, features);
        clij2.print(featureImage);

        ResultsTable table = new ResultsTable();
        clij2.pullToResultsTable(featureImage, table);
        featureImage.close();

        if (!(centroids != null && last_loaded_filename.compareTo(model_filename) == 0)) {
            centroids = centroidsFromDisc(model_filename);
            last_loaded_filename = model_filename;
            System.out.println("Load model");
        }
        if (centroids != null && centroids.size() > 0) {
            // check if number of features fits
            int num_trained_features = centroids.get(0).getCenter().getPoint().length;
            if (featureImage.getHeight() != num_trained_features) {
                System.out.println("Number of features doesn't match. Will train new KMeansLabelClusterer.");
                train = true;
            }
        }
        if (centroids == null || centroids.size() == 0 || train || num_classes != centroids.size()) {
            System.out.println("Train model");
            centroids = trainKMeansClustering(table, model_filename, num_classes);
        }

        predictKMeansClustering(table, centroids, "CLASS");

        ClearCLBuffer vector = clij2.create(table.size(), 1, 1);
        clij2.pushResultsTableColumn(vector, table, "CLASS");

        ClearCLBuffer vector_with_background = clij2.create(table.size() + 1, 1, 1);
        clij2.set(vector_with_background, 0);
        clij2.paste(vector, vector_with_background, 1, 0, 0);

        if (neighbor_radius > 0) {
            int number_of_labels = (int) clij2.maximumOfAllPixels(labelMap);
            ClearCLBuffer touch_matrix = clij2.create(number_of_labels + 1, number_of_labels + 1);
            clij2.generateTouchMatrix(labelMap, touch_matrix);
            clij2.setColumn(touch_matrix, 0, 0);

            ClearCLBuffer propagate_feature = clij2.create(vector_with_background);
            ClearCLBuffer neighbor_matrix = clij2.create(touch_matrix);
            clij2.copy(touch_matrix, neighbor_matrix);
            for (int i = 1; i < neighbor_radius; i++) {
                clij2.neighborsOfNeighbors(touch_matrix, neighbor_matrix);
                clij2.copy(neighbor_matrix, touch_matrix);
            }
            neighbor_matrix.close();

            ModeOfTouchingNeighbors.modeOfTouchingNeighbors(clij2, vector_with_background, touch_matrix, propagate_feature);
            clij2.copy(propagate_feature, vector_with_background);
            clij2.setColumn(vector_with_background, 0, 0);

            propagate_feature.close();
            touch_matrix.close();
        }

        clij2.replaceIntensities(labelMap, vector_with_background, output);

        vector.close();
        vector_with_background.close();

        if (train) {
            centroidsToDisc(centroids, model_filename);
            System.out.println("Saved model to " + model_filename);
        }

        return true;
    }

    public static void centroidsToDisc(List<CentroidCluster<DoublePoint>> centroids, String filename) {
        ResultsTable table = new ResultsTable();
        int j = 0;
        for (CentroidCluster<DoublePoint> centroid : centroids) {
            double[] point = centroid.getCenter().getPoint();
            for (int i = 0; i < point.length; i++) {
                table.setValue(i, j, point[i]);
            }
            j++;
        }
        table.save(filename);
    }

    public static List<CentroidCluster<DoublePoint>> centroidsFromDisc(String filename) {
        List<CentroidCluster<DoublePoint>> list = new ArrayList<>();
        ResultsTable table;
        try {
            table = ResultsTable.open(filename);
        } catch (IOException e) {
            return list;
        }

        for (int j = 0; j < table.size(); j++) {
            String[] elements = table.getRowAsString(j).split("\t");
            double[] values = new double[elements.length];
            for (int i = 0; i < elements.length; i++) {
                values[i] = Double.parseDouble(elements[i]);
            }
            list.add(new CentroidCluster<>(new DoublePoint(values)));
        }
        return list;
    }

    private static List<CentroidCluster<DoublePoint>> trainKMeansClustering(ResultsTable table, String model_filename, int num_classes) {
        KMeansPlusPlusClusterer<DoublePoint> clusterer = new KMeansPlusPlusClusterer<DoublePoint>(num_classes);

        List<DoublePoint> list = tableToList(table);
        List<CentroidCluster<DoublePoint>> clusters = clusterer.cluster(list);

        return clusters;
    }

    private static void predictKMeansClustering(ResultsTable table, List<CentroidCluster<DoublePoint>> clusters, String column_to_add) {

        double[] prediction = predictKMeansClustering(table, clusters);
        for (int i = 0; i < prediction.length; i++) {
            table.setValue(column_to_add, i, prediction[i]);
        }
    }

    private static double[] predictKMeansClustering(ResultsTable table, List<CentroidCluster<DoublePoint>> clusters) {
        int num_classes = clusters.size();

        List<DoublePoint> list = tableToList(table);
        double[] classes = new double[list.size()];
        EuclideanDistance ed = new EuclideanDistance();
        for (int row = 0; row < table.size(); row++) {
            int klass = 0; // not classified
            double min_distance = Double.MAX_VALUE;
            for (int k = 0; k < num_classes; k++) {
                Clusterable center = clusters.get(k).getCenter();
                double distance = ed.compute(center.getPoint(), list.get(row).getPoint());
                if (distance < min_distance) {
                    min_distance = distance;
                    klass = k + 1; // because 0 corresponds to background
                }
            }
            classes[row] = klass;
        }
        return classes;
    }


    public static List<DoublePoint> tableToList(ResultsTable table) {
        List<DoublePoint> list = new ArrayList<DoublePoint>();

        for (int row = 0; row < table.size(); row++) {
            String[] elements = table.getRowAsString(row).split("\t");
            double[] values = new double[elements.length];
            int i = 0;
            for (String elem : elements) {
                values[i] = Double.parseDouble(elem);
                i++;
            }
            list.add(new DoublePoint(values));
        }
        return list;
    }


    @Override
    public String getDescription() {
        return "Applies K-Means clustering to an image and a corresponding label map. \n\n" +
                "See also: https://commons.apache.org/proper/commons-math/javadocs/api-3.6/org/apache/commons/math3/ml/clustering/KMeansPlusPlusClusterer.html\n" +
                "Make sure that the handed over feature list is the same used while training the model.\n" +
                "The neighbor_radius specifies a correction step which allows to use a region where the mode of \n" +
                "classification results (the most popular class) will be determined after clustering.";
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
