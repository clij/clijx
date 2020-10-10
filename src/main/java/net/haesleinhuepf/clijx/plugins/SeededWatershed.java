package net.haesleinhuepf.clijx.plugins;

import ij.ImagePlus;
import net.haesleinhuepf.clij.CLIJ;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.clearcl.ClearCLKernel;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.haesleinhuepf.clij.macro.CLIJMacroPlugin;
import net.haesleinhuepf.clij.macro.CLIJOpenCLProcessor;
import net.haesleinhuepf.clij.macro.documentation.OffersDocumentation;
import net.haesleinhuepf.clij2.AbstractCLIJ2Plugin;
import net.haesleinhuepf.clij2.CLIJ2;
import net.haesleinhuepf.clij2.utilities.HasClassifiedInputOutput;
import net.haesleinhuepf.clij2.utilities.IsCategorized;
import org.scijava.plugin.Plugin;

import java.nio.ByteBuffer;
import java.util.HashMap;

/**
 * Author: @haesleinhuepf
 * July 2020
 */
@Deprecated
@Plugin(type = CLIJMacroPlugin.class, name = "CLIJx_seededWatershed")
public class SeededWatershed extends AbstractCLIJ2Plugin implements CLIJMacroPlugin, CLIJOpenCLProcessor, OffersDocumentation, IsCategorized, HasClassifiedInputOutput {

    @Override
    public String getInputType() {
        return "Label Image, Image";
    }

    @Override
    public String getOutputType() {
        return "Label Image";
    }

        @Override
    public String getCategories() {
        return "Binary, Filter, Label";
    }

    @Override
    public boolean executeCL() {
        return seededWatershed(getCLIJ2(), (ClearCLBuffer)( args[0]), (ClearCLBuffer)(args[1]), (ClearCLBuffer)(args[2]), asFloat(args[3]));
    }

    public static boolean seededWatershed(CLIJ2 clij2, ClearCLBuffer labelmap_seeds, ClearCLBuffer input, ClearCLBuffer output_labelmap, Float threshold) {
        ClearCLBuffer distanceMap2 = clij2.create(input);
        SeededWatershed.dilateLabelsUntilNoChange(clij2, input, labelmap_seeds, distanceMap2, output_labelmap, threshold);
        clij2.release(distanceMap2);
        return true;
    }

    static ClearCLKernel dilateLabelsUntilNoChange(CLIJ2 clij2, ClearCLBuffer distanceMapIn, ClearCLBuffer labelMapIn, ClearCLBuffer flag, ClearCLBuffer distanceMapOut, ClearCLBuffer labelMapOut, ClearCLKernel kernel, Float threshold) {
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("src_labelmap", labelMapIn);
        parameters.put("src_distancemap", distanceMapIn);
        parameters.put("dst_labelmap", labelMapOut);
        parameters.put("dst_distancemap", distanceMapOut);
        parameters.put("flag_dst", flag);
        parameters.put("threshold", threshold);
        return clij2.executeSubsequently(SeededWatershed.class, "seeded_watershed_local_maximum_" + labelMapOut.getDimension() + "d_x.cl", "watershed_local_maximum_" + labelMapOut.getDimension() + "d", labelMapOut.getDimensions(), labelMapOut.getDimensions(), parameters, kernel);
    }

    static boolean dilateLabelsUntilNoChange(CLIJ2 clij2, ClearCLBuffer distanceMapIn, ClearCLBuffer labelMapIn, ClearCLBuffer distanceMapOut, ClearCLBuffer labelMapOut, Float threshold) {

        ClearCLBuffer flag = clij2.create(new long[]{1,1,1}, NativeTypeEnum.Byte);
        ByteBuffer aByteBufferWithAZero = ByteBuffer.allocate(1);
        aByteBufferWithAZero.put((byte)0);
        flag.readFrom(aByteBufferWithAZero, true);

        clij2.set(labelMapOut, 0f);
        clij2.set(distanceMapOut, 0f);


        final int[] iterationCount = {0};
        int flagValue = 1;

        ClearCLKernel flipkernel = null;
        ClearCLKernel flopkernel = null;

        while (flagValue > 0) {
            if (iterationCount[0] % 2 == 0) {
                if (flipkernel == null) {
                    flipkernel = dilateLabelsUntilNoChange(clij2, distanceMapIn, labelMapIn, flag, distanceMapOut, labelMapOut, flipkernel, threshold);
                } else {
                    flipkernel.run(true);
                }
                //clijx.saveAsTIF(distanceMapOut, "c:/structure/temp/dst/" + iterationCount[0] + ".tif");
                //clijx.saveAsTIF(labelMapOut, "c:/structure/temp/lab/" + iterationCount[0] + ".tif");
            } else {
                if (flopkernel == null) {
                    flopkernel = dilateLabelsUntilNoChange(clij2, distanceMapOut, labelMapOut, flag, distanceMapIn, labelMapIn, flopkernel, threshold);
                } else {
                    flopkernel.run(true);
                }
                //clijx.saveAsTIF(distanceMapIn, "c:/structure/temp/dst/" + iterationCount[0] + ".tif");
                //clijx.saveAsTIF(labelMapOut, "c:/structure/temp/lab/" + iterationCount[0] + ".tif");
            }

            ImagePlus flagImp = clij2.pull(flag);
            flagValue = flagImp.getProcessor().get(0,0);
            flag.readFrom(aByteBufferWithAZero, true);
            iterationCount[0] = iterationCount[0] + 1;
            //System.out.println("cycling " + iterationCount[0]);
        }
        flag.close();

        if (iterationCount[0] % 2 == 0) {
            clij2.copy(labelMapIn, labelMapOut);
            clij2.copy(distanceMapIn, distanceMapOut);
        }
        if (flipkernel != null) {
            flipkernel.close();
        }
        if (flopkernel != null) {
            flopkernel.close();
        }

        return true;
    }


    @Override
    public String getParameterHelpText() {
        return "Image label_map_seeds, Image input, ByRef Image label_map_destination, Float threshold";
    }

    @Override
    public String getDescription() {
        return "Takes a label map (seeds) and an input image with gray values to apply the watershed algorithm and split the image above a given threshold in labels.";
    }

    @Override
    public String getAvailableForDimensions() {
        return "2D, 3D";
    }
}
