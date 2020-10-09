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
@Plugin(type = CLIJMacroPlugin.class, name = "CLIJx_generateIntegerGreyValueCooccurrenceCountMatrixHalfDiamond")
public class GenerateIntegerGreyValueCooccurrenceCountMatrixHalfDiamond extends AbstractCLIJ2Plugin implements CLIJMacroPlugin, CLIJOpenCLProcessor, OffersDocumentation, IsCategorized, HasClassifiedInputOutput {
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
        boolean result = generateIntegerGreyValueCooccurrenceCountMatrixHalfDiamond(getCLIJ2(), (ClearCLBuffer)( args[0]), (ClearCLBuffer)(args[1]));
        return result;
    }

    public static boolean generateIntegerGreyValueCooccurrenceCountMatrixHalfDiamond(CLIJ2 clij2, ClearCLBuffer src_label_map1, ClearCLBuffer dst_cooccurrence_matrix) {
        int num_threads = (int) src_label_map1.getDepth();

        long[][][] counts = new long[num_threads][(int)dst_cooccurrence_matrix.getWidth()][(int)dst_cooccurrence_matrix.getHeight()];

        Thread[] threads = new Thread[num_threads];
        Statistician[] statisticians = new Statistician[num_threads];

        ArrayList<float[]> buffers = new ArrayList<>();

        for (int i = 0; i < num_threads; i++) {
            float[] image_slice;
            if (i == 0) {
                ClearCLBuffer image_slice_buffer = clij2.create(src_label_map1.getWidth(), src_label_map1.getHeight());
                clij2.copySlice(src_label_map1, image_slice_buffer, i);
                image_slice = new float[(int) (image_slice_buffer.getWidth() * image_slice_buffer.getHeight())];
                image_slice_buffer.writeTo(FloatBuffer.wrap(image_slice), true);

                buffers.add(image_slice);
            } else {
                image_slice = buffers.get(i);
            }

            float[] image_next_slice;
            if (i < num_threads - 1) {
                ClearCLBuffer image_next_slice_buffer = clij2.create(src_label_map1.getWidth(), src_label_map1.getHeight());
                clij2.copySlice(src_label_map1, image_next_slice_buffer, i + 1);
                image_next_slice = new float[(int) (image_next_slice_buffer.getWidth() * image_next_slice_buffer.getHeight())];
                image_next_slice_buffer.writeTo(FloatBuffer.wrap(image_next_slice), true);

                buffers.add(image_next_slice);
            } else {
                image_next_slice = null;
            }


            statisticians[i] = new Statistician(counts[i], image_slice, image_next_slice, (int)src_label_map1.getWidth(), (int)src_label_map1.getHeight());
            threads[i] = new Thread(statisticians[i]);
            threads[i].start();
        }
        for (int i = 0; i < num_threads; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        buffers.clear();

        float[][] matrix = new float[(int)dst_cooccurrence_matrix.getWidth()][(int)dst_cooccurrence_matrix.getHeight()];

        for (int t = 0; t < num_threads; t++) {
            for (int j = 0; j < counts[0].length; j++) {
                for (int k = 0; k < counts[0][0].length; k++) {
                    matrix[j][k] += counts[t][j][k];
                }
            }
        }

        ClearCLBuffer countMatrix = clij2.pushMat(matrix);
        clij2.copy(countMatrix, dst_cooccurrence_matrix);
        countMatrix.close();

        return true;
    }

    @Override
    public String getParameterHelpText() {
        return "Image integer_image, ByRef Image grey_value_cooccurrence_matrix_destination";
    }

    @Override
    public String getCategories() {
        return "Measurement";
    }


    private static class Statistician implements Runnable{
        private final int width;
        private final int height;

        long[][] counts;

        private float[] image;
        private float[] image_next_plane;

        Statistician(long[][] counts, float[] image, float[] image_next_plane, int width, int height) {
            this.counts = counts;
            this.image = image;
            this.image_next_plane = image_next_plane;
            this.width = width;
            this.height = height;
        }

        @Override
        public void run() {

            int x = 0;
            int y = 0;
            for (int i = 0; i < image.length; i++) {
                int value_1;
                int value_2;

                // right
                if (x < width - 1) {
                    value_1 = (int) image[i];
                    value_2 = (int) image[i + 1];
                    counts[value_1][value_2]++;
                    counts[value_2][value_1]++;
                }
                // bottom
                if (y < height - 1) {
                    value_1 = (int) image[i];
                    value_2 = (int) image[i + width];
                    counts[value_1][value_2]++;
                    counts[value_2][value_1]++;
                }

                // next plane
                if (image_next_plane != null) {
                    value_1 = (int) image[i];
                    value_2 = (int) image_next_plane[i];
                    counts[value_1][value_2]++;
                    counts[value_2][value_1]++;
                }

                x++;
                if (x >= width) {
                    x = 0;
                    y++;
                }
            }
        }
    }


    @Override
    public ClearCLBuffer createOutputBufferFromSource(ClearCLBuffer input)
    {
        double maxValue = getCLIJ2().maximumOfAllPixels((ClearCLBuffer) args[0]) + 1;
        ClearCLBuffer output = clij.createCLBuffer(new long[]{(long)maxValue, (long)maxValue}, NativeTypeEnum.Float);
        return output;
    }

    @Override
    public String getDescription() {
        return "Takes an image and assumes its grey values are integers. It builds up a grey-level co-occurrence matrix of neighboring (left, bottom, back) pixel intensities. \n\n"+
                "Major parts of this operation run on the CPU.";
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

        GenerateIntegerGreyValueCooccurrenceCountMatrixHalfDiamond.generateIntegerGreyValueCooccurrenceCountMatrixHalfDiamond(clij2, buffer, matrix);

        clij2.print(matrix);

    }

}
