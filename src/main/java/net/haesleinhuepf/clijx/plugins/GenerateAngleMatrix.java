package net.haesleinhuepf.clijx.plugins;

import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.haesleinhuepf.clij.macro.CLIJMacroPlugin;
import net.haesleinhuepf.clij.macro.CLIJOpenCLProcessor;
import net.haesleinhuepf.clij.macro.documentation.OffersDocumentation;
import net.haesleinhuepf.clij2.AbstractCLIJ2Plugin;
import net.haesleinhuepf.clij2.CLIJ2;
import net.haesleinhuepf.clij2.plugins.GenerateDistanceMatrix;
import net.haesleinhuepf.clij2.utilities.HasClassifiedInputOutput;
import net.haesleinhuepf.clij2.utilities.IsCategorized;
import org.scijava.plugin.Plugin;

import java.util.HashMap;

/**
 * Author: @haesleinhuepf
 * December 2018
 */
@Plugin(type = CLIJMacroPlugin.class, name = "CLIJx_generateAngleMatrix")
public class GenerateAngleMatrix extends AbstractCLIJ2Plugin implements CLIJMacroPlugin, CLIJOpenCLProcessor, OffersDocumentation, IsCategorized, HasClassifiedInputOutput {
    @Override
    public String getInputType() {
        return "Vector";
    }

    @Override
    public String getOutputType() {
        return "Matrix";
    }


    @Override
    public boolean executeCL() {
        boolean result = generateAngleMatrix(getCLIJ2(), (ClearCLBuffer)( args[0]), (ClearCLBuffer)(args[1]), (ClearCLBuffer)(args[2]));
        return result;
    }

    public static boolean generateAngleMatrix(CLIJ2 clij2, ClearCLBuffer src_pointlist1, ClearCLBuffer src_pointlist2, ClearCLBuffer dst_distance_matrix) {
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("src_point_list1", src_pointlist1);
        parameters.put("src_point_list2", src_pointlist2);
        parameters.put("dst_matrix", dst_distance_matrix);

        long[] globalSizes = new long[]{src_pointlist1.getWidth(),  1, 1};

        clij2.activateSizeIndependentKernelCompilation();
        clij2.execute(GenerateDistanceMatrix.class, "generate_angle_matrix_2d_x.cl", "generate_angle_matrix", globalSizes, globalSizes, parameters);
        return true;
    }

    @Override
    public String getParameterHelpText() {
        return "Image coordinate_list1, Image coordinate_list2, ByRef Image angle_matrix_destination";
    }

    @Override
    public ClearCLBuffer createOutputBufferFromSource(ClearCLBuffer input)
    {
        ClearCLBuffer input1 = (ClearCLBuffer) args[0];
        ClearCLBuffer input2 = (ClearCLBuffer) args[1];
        return clij.createCLBuffer(new long[]{input1.getWidth() + 1, input2.getWidth() + 1}, NativeTypeEnum.Float);
    }

    @Override
    public String getDescription() {
        return "Computes the angle in radians between all point coordinates given in two point lists.\n" +
                "\n" +
                " Takes two images containing pointlists (dimensionality n * d, n: number of \n" +
                "points and d: dimensionality) and builds up a matrix containing the \n" +
                "angles between these points.\n" +
                "\n" +
                "Convention: Values range from -90 to 90 degrees (-0.5 to 0.5 pi radians)\n" +
                "* -90 degreess (-0.5 pi radians): Top\n" +
                "* 0 defrees (0 radians): Right\n" +
                "* 90 degrees (0.5 pi radians): Bottom\n" +
                "\n" +
                "Convention: Given two point lists with dimensionality n * d and m * d, the distance \n" +
                "matrix will be of size(n + 1) * (m + 1). The first row and column \n" +
                "contain zeros. They represent the distance of the objects to a \n" +
                "theoretical background object. In that way, distance matrices are of \n" +
                "the same size as touch matrices (see generateTouchMatrix). Thus, one \n" +
                "can threshold a distance matrix to generate a touch matrix out of it \n" +
                "for drawing meshes. \n" +
                "\n" +
                "Implemented for 2D only at the moment.\n" +
                "\n" +
                "Parameters\n" +
                "----------\n" +
                "coordinate_list1 : Image\n" +
                "coordinate_list2 : Image\n" +
                "angle_matrix_destination : Image\n" +
                "\n" +
                "Returns\n" +
                "-------\n" +
                "angle_matrix_destination";
    }

    @Override
    public String getAvailableForDimensions() {
        return "2D";
    }

    @Override
    public String getCategories() {
        return "Measurement, Graph";
    }
}
