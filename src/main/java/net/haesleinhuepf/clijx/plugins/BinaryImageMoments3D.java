package net.haesleinhuepf.clijx.plugins;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;
import net.haesleinhuepf.clij.clearcl.ClearCL;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.haesleinhuepf.clij.macro.CLIJMacroPlugin;
import net.haesleinhuepf.clij.macro.CLIJOpenCLProcessor;
import net.haesleinhuepf.clij.macro.documentation.OffersDocumentation;
import net.haesleinhuepf.clij2.AbstractCLIJ2Plugin;
import net.haesleinhuepf.clij2.CLIJ2;
import org.scijava.plugin.Plugin;

import java.util.Arrays;
import java.util.HashMap;

/**
 * This class allows calculating moments up to any order of 3D objects stored in a label map. It furthermore contains accessors to Eigenvalues of the objects.
 * <p>
 * <p>
 * Note: An object ranging from x=0 to x=9 has its center between x=4 and x=5. thus, its centerX=4.5, not 5!
 * <p>
 * <p>
 * <p>
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden,
 * rhaase@mpi-cbg.de
 * Date: November 2015
 * <p>
 * Copyright 2017 Max Planck Institute of Molecular Cell Biology and Genetics,
 * Dresden, Germany
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */


@Plugin(type = CLIJMacroPlugin.class, name = "CLIJ2_binaryImageMoments3D")
public class BinaryImageMoments3D extends AbstractCLIJ2Plugin implements CLIJMacroPlugin, CLIJOpenCLProcessor, OffersDocumentation {


    @Override
    public String getParameterHelpText() {
        return "Image binary_input, ByRef Image moments";
    }

    @Override
    public boolean executeCL() {
        return binaryImageMoments3D(getCLIJ2(), (ClearCLBuffer) (args[0]), (ClearCLBuffer) (args[1]));
    }


    @Override
    public ClearCLBuffer createOutputBufferFromSource(ClearCLBuffer input) {
        return getCLIJ2().create(new long[]{maxOrder + 1, maxOrder + 1, maxOrder + 1}, getCLIJ2().Float);
    }

    @Override
    public String getDescription() {
        return "Transforms a binary image with single pixles set to 1 to a labelled spots image. \n\n" +
                "Transforms a spots image as resulting from maximum/minimum detection in an image of the same size where every spot has " +
                "a number 1, 2, ... n.";
    }

    @Override
    public String getAvailableForDimensions() {
        return "3D";
    }

    private final static int maxOrder = 2;



    public static boolean binaryImageMoments3D(CLIJ2 clij2, ClearCLBuffer input, ClearCLBuffer output) {
        ClearCLBuffer temp = clij2.create(input.getDimensions(), clij2.Float);

        double[][][] moments = new double[maxOrder + 1][maxOrder + 1][maxOrder + 1];

        long[] dimensions = input.getDimensions();

        double[] center = clij2.getCenterOfMass(input);
        System.out.println("Center:");
        System.out.println(Arrays.toString(center));


        for (int i = 0; i <= maxOrder; i++) {
            for (int j = 0; j <= maxOrder; j++) {
                for (int k = 0; k <= maxOrder; k++) {

                    HashMap<String, Object> parameters = new HashMap<>();
                    parameters.put("src", input);
                    parameters.put("dst", temp);


                    parameters.put("center_x", new Float(center[0]));
                    parameters.put("center_y", new Float(center[1]));
                    parameters.put("center_z", new Float(center[2]));

                    parameters.put("order_x", new Float(i));
                    parameters.put("order_y", new Float(j));
                    parameters.put("order_z", new Float(k));

                    clij2.execute(BinaryImageMoments3D.class, "binary_image_moments_3d_x.cl", "binary_image_moments_3d", dimensions, dimensions, parameters);
                    moments[i][j][k] += clij2.sumOfAllPixels(temp);
                }
            }
        }

        temp.close();

        ClearCLBuffer temp2 = clij2.pushMatXYZ(moments);
        System.out.println("Moments:");
        clij2.print(temp2);
        clij2.copy(temp2, output);
        temp2.close();

        return true;
    }
}
