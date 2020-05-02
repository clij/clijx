package net.haesleinhuepf.clijx.plugins;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
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


@Plugin(type = CLIJMacroPlugin.class, name = "CLIJ2_getBinaryImageEigenValues3D")
public class GetBinaryImageEigenValues3D extends AbstractCLIJ2Plugin implements CLIJMacroPlugin, CLIJOpenCLProcessor, OffersDocumentation {


    @Override
    public String getParameterHelpText() {
        return "Image binary_input, ByRef Number eigen_value_x, ByRef Number eigen_value_y, ByRef Number eigen_value_z";
    }

    @Override
    public boolean executeCL() {
        double[] eigenvalues = getBinaryImageEigenValues3D(getCLIJ2(), (ClearCLBuffer) (args[0]));
        ((Double[])args[1])[0] = eigenvalues[0];
        ((Double[])args[2])[0] = eigenvalues[1];
        ((Double[])args[3])[0] = eigenvalues[2];
        return true;
    }

    public static double[] getBinaryImageEigenValues3D(CLIJ2 clij2, ClearCLBuffer binary_input) {
        ClearCLBuffer moments = clij2.create(3,3,3);
        BinaryImageMoments3D.binaryImageMoments3D(clij2, binary_input, moments);

        float[][][] tim = (float[][][]) clij2.pullMatXYZ(moments);
        moments.close();

        if (tim == null || tim[0][0][0] == 0) {
            return null;
        }
        double[][] covXYZ = {{tim[2][0][0] / tim[0][0][0], tim[1][1][0] / tim[0][0][0], tim[1][0][1] / tim[0][0][0]},
                {tim[1][1][0] / tim[0][0][0], tim[0][2][0] / tim[0][0][0], tim[0][1][1] / tim[0][0][0]},
                {tim[1][0][1] / tim[0][0][0], tim[0][1][1] / tim[0][0][0], tim[0][0][2] / tim[0][0][0]}};

        Matrix covXYZMatrix = new Matrix(covXYZ);
        System.out.println("covxyz matrix");
        System.out.println(Arrays.toString(covXYZ[0]));
        System.out.println(Arrays.toString(covXYZ[1]));
        System.out.println(Arrays.toString(covXYZ[2]));

        EigenvalueDecomposition eigenvalueDecomposition = new EigenvalueDecomposition(covXYZMatrix);
        return eigenvalueDecomposition.getRealEigenvalues();
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public String getAvailableForDimensions() {
        return "3D";
    }
}
