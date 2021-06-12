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
import net.haesleinhuepf.clij2.plugins.ExcludeLabelsOutsideSizeRange;
import net.haesleinhuepf.clij2.utilities.HasAuthor;
import net.haesleinhuepf.clij2.utilities.HasClassifiedInputOutput;
import net.haesleinhuepf.clij2.utilities.IsCategorized;
import org.scijava.plugin.Plugin;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import static ij.plugin.filter.Analyzer.setOption;

/**
 * Author: @haesleinhuepf
 *         August 2020
 */
@Plugin(type = CLIJMacroPlugin.class, name = "CLIJx_greyLevelAtttributeFiltering")
public class GreyLevelAtttributeFiltering extends AbstractCLIJ2Plugin implements CLIJMacroPlugin, CLIJOpenCLProcessor, OffersDocumentation, HasAuthor, IsCategorized, HasClassifiedInputOutput {
    @Override
    public String getInputType() {
        return "Image";
    }

    @Override
    public String getOutputType() {
        return "Image";
    }

    @Override
    public String getDescription() {
        return "Inspired by Grayscale attribute filtering from MorpholibJ library by David Legland & Ignacio Arganda-Carreras.\n\n" +
            "This plugin will remove components in a grayscale image based on user-specified area (for 2D: pixels) or volume (3D: voxels).\n" +
            "For each gray level specified in the number of bins, binary images will be generated, followed by exclusion of objects (labels)\n"+ 
            "below a minimum pixel count.\n"+
            "All the binary images for each gray level are combined to form the final image. The output is a grayscale image, where bright objects\n"+ 
            "below pixel count are removed.\n"+
            "It is recommended that low values be used for number of bins, especially for large 3D images, or it may take long time.";
    }

    @Override
    public String getAuthorName() {
        return "Pradeep Rajasekhar and Robert Haase";
    }

    @Override
    public String getParameterHelpText() {
        return "Image source, ByRef Image destination, Number number_of_bins, Number minimum_pixel_count";
    }

    @Override
    public Object[] getDefaultValues() {
        return new Object[]{null, null, 256, 100};
    }

    @Override
    public String getAvailableForDimensions() {
        return "2D, 3D";
    }


    @Override
    public boolean executeCL() {
        boolean result = greyLevelAtttributeFiltering(getCLIJ2(), (ClearCLBuffer)( args[0]), (ClearCLBuffer)(args[1]), asInteger(args[2]), asInteger(args[3]));
        return result;
    }

    public static boolean greyLevelAtttributeFiltering(CLIJ2 clij2, ClearCLBuffer image, ClearCLBuffer dst_filtered_image, Integer number_of_bins, Integer min_pixel_count) {

        // get minimum and maximum intensity in the image stack
        float min = (float) clij2.minimumOfAllPixels(image);
        float max = (float) clij2.maximumOfAllPixels(image);

        System.out.println("Min intensity: " + min);
        System.out.println("Max intensity: " + max);

        //generate histogram using CLIJ, with min,max values and bins of 256
        //using this instead of all grey values for now as a test
        ClearCLBuffer histogram_image = clij2.create(number_of_bins, 1);
        clij2.histogram(image, histogram_image, number_of_bins, min, max, false);

        // store pixel counts for each bin in an array
        float[] stack_histogram = new float[number_of_bins];
        histogram_image.writeTo(FloatBuffer.wrap(stack_histogram), true);
        histogram_image.close();



        //generate an array with histogram bins that have positive counts
        //store nonzero gray_levels in an array
        ArrayList<Float> grey_level_nonzero = new ArrayList<>();

        //bin width for histogram
        float bin_width = (float) ((max - min)/(number_of_bins));

        float bin = min; // we need to start at minimum intesity
        //int idx = 0;

        for(int i = 0; i < number_of_bins; i++)
        {
            if(stack_histogram[i] > 0 && i > 0)
            {
                grey_level_nonzero.add(bin);
                //idx += 1;
            }
            bin+=bin_width;
        }

        //print("Histogram: ");
        //Array.print(stack_histogram);
        //print("Non zero bins ");
        //Array.print(grey_level_nonzero);
        //print(grey_level_nonzero.length);


        //label="label";
        //thr="thresholded";
        ClearCLBuffer thresholded = clij2.create(image.getDimensions(), NativeTypeEnum.UnsignedByte);
        ClearCLBuffer label = clij2.create(image.getDimensions(), NativeTypeEnum.Float);
        ClearCLBuffer label2 = clij2.create(image.getDimensions(), NativeTypeEnum.Float);
        ClearCLBuffer sum_image = clij2.create(image.getDimensions(), NativeTypeEnum.Float);
        ClearCLBuffer temp = label; // reuse some memory
        ClearCLBuffer temp2 = label2; // reuse some memory

        // time measurements
        long time = System.currentTimeMillis();

        // the 3D grayscale attribute filter in CLIJ
        for(int grey = 0; grey < grey_level_nonzero.size(); grey++) {
            float currentThreshold = grey_level_nonzero.get(grey);

            System.out.println("Grey level " + currentThreshold);

            //threshold for each grey level
            clij2.greaterConstant(image, thresholded, currentThreshold);

            //generate connected components labeling
            clij2.connectedComponentsLabelingBox(thresholded, label);

            //get all stats of the background and labelled pixels from which we can get pixel count, which is ~size of object of itnerest
            // that's a new method in clijx
            ExcludeLabelsOutsideSizeRange.excludeLabelsOutsideSizeRange(clij2, label, label2, (float)min_pixel_count, Float.MAX_VALUE);

            // convert label to binary
            clij2.greaterOrEqualConstant(label2, thresholded, 1);

            //Ensures that the binary image generated for each grey level and size range is used in the next loop
            //if loop executs first time (grey=0) stores binary image (label2) in temp_label, does not perform addition
            if(grey == 0)
            {
                // first round: set all pixels selected pixels to bin currentThreshold
                clij2.multiplyImageAndScalar(thresholded, sum_image, currentThreshold);
            }
            else // subsequent rounds
            {
                //using add images to combine the binary images generated in each loop; multiplied by bin width
                //not sure if this is what dlegland meant by combining all binaries to generate a new grayscale image
                // Robert: Pretty sure, yes. We just need to multiply it with the current threshold and then
                //         combine them in the right way; e.g. using a maximum-images operation:
                clij2.multiplyImageAndScalar(thresholded, temp, currentThreshold);
                clij2.copy(sum_image, temp2);
                clij2.maximumImages(temp, temp2, sum_image);
            }
        }

        // clean up
        clij2.copy(sum_image, dst_filtered_image);
        sum_image.close();
        label.close();
        label2.close();
        thresholded.close();

        System.out.println("CLIJ workflow took " + (System.currentTimeMillis() - time) + " msec");

        return true;
    }



    @Override
    public ClearCLBuffer createOutputBufferFromSource(ClearCLBuffer input)
    {
        return getCLIJ2().create(input.getDimensions(), NativeTypeEnum.Float);
    }

    public static void main(String... args) {
        new ImageJ();
        CLIJ2 clij2 = CLIJ2.getInstance();

        ClearCLBuffer buffer = clij2.push(IJ.openImage("src/test/resources/blobs.tif"));
        ClearCLBuffer result = clij2.create(buffer.getDimensions(), NativeTypeEnum.Float);

        GreyLevelAtttributeFiltering.greyLevelAtttributeFiltering(clij2, buffer, result, 256, 100);

        clij2.show(result, "Result");
    }

    @Override
    public String getCategories() {
        return "Filter";
    }
}
