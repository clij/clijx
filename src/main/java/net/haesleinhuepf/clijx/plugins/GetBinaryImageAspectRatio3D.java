package net.haesleinhuepf.clijx.plugins;

import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.macro.CLIJMacroPlugin;
import net.haesleinhuepf.clij.macro.CLIJOpenCLProcessor;
import net.haesleinhuepf.clij.macro.documentation.OffersDocumentation;
import net.haesleinhuepf.clij2.AbstractCLIJ2Plugin;
import net.haesleinhuepf.clij2.CLIJ2;
import org.scijava.plugin.Plugin;

import java.util.Arrays;

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


@Plugin(type = CLIJMacroPlugin.class, name = "CLIJ2_getBinaryImageAspectRatio3D")
public class GetBinaryImageAspectRatio3D extends AbstractCLIJ2Plugin implements CLIJMacroPlugin, CLIJOpenCLProcessor, OffersDocumentation {


    @Override
    public String getParameterHelpText() {
        return "Image binary_input, ByRef Number aspect_ratio";
    }

    @Override
    public boolean executeCL() {
        ((Double[])args[1])[0] = getBinaryImageAspectRatio(getCLIJ2(), (ClearCLBuffer) (args[0]));
        return true;
    }

    public static double getBinaryImageAspectRatio(CLIJ2 clij2, ClearCLBuffer binary_input) {
        double[] ev = GetBinaryImageEigenValues3D.getBinaryImageEigenValues3D(clij2, binary_input);

        System.out.println("Ev: " + Arrays.toString(ev));

        if (ev == null) {
            return 0;
        }

        double maxEv = ev[0];
        double minEv = ev[0];
        for (int j = 1; j < ev.length; j++) {
            if (minEv > ev[j]) {
                minEv = ev[j];
            }
            if (maxEv < ev[j]) {
                maxEv = ev[j];
            }
        }
        return maxEv / minEv;
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public String getAvailableForDimensions() {
        return "3D";
    }


    public static void main(String[] args) {
        CLIJ2 clij2 = CLIJ2.getInstance();

        ClearCLBuffer binary_input_3D = clij2.pushString(""+
                "0 0 0\n" +
                "0 0 0\n" +
                "0 0 0\n\n" +

                "0 0 0\n" +
                "0 1 1\n" +
                "0 0 0\n\n" +

                "0 0 0\n" +
                "0 0 0\n" +
                "0 0 0"
                );

        double aspect_ratio = getBinaryImageAspectRatio(clij2, binary_input_3D);
        System.out.println("AR:");
        System.out.println(aspect_ratio);

        ClearCLBuffer temp = clij2.create(binary_input_3D);
        clij2.transposeXY(binary_input_3D, temp);
        aspect_ratio = getBinaryImageAspectRatio(clij2, temp);
        System.out.println("AR:");
        System.out.println(aspect_ratio);

        clij2.transposeXZ(binary_input_3D, temp);
        aspect_ratio = getBinaryImageAspectRatio(clij2, temp);
        System.out.println("AR:");
        System.out.println(aspect_ratio);

    }
}
