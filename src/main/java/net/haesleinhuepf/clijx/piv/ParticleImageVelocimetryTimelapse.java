package net.haesleinhuepf.clijx.piv;

import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.CLIJ;
import net.haesleinhuepf.clij.macro.AbstractCLIJPlugin;
import net.haesleinhuepf.clij.macro.CLIJMacroPlugin;
import net.haesleinhuepf.clij.macro.CLIJOpenCLProcessor;
import net.haesleinhuepf.clij.macro.documentation.OffersDocumentation;
import net.haesleinhuepf.clij2.AbstractCLIJ2Plugin;
import net.haesleinhuepf.clij2.CLIJ2;
import org.scijava.plugin.Plugin;

/**
 *
 *
 * Author: @haesleinhuepf
 * 12 2018
 */
@Plugin(type = CLIJMacroPlugin.class, name = "CLIJx_particleImageVelocimetryTimelapse")
public class ParticleImageVelocimetryTimelapse extends AbstractCLIJ2Plugin implements CLIJMacroPlugin, CLIJOpenCLProcessor, OffersDocumentation {

    @Override
    public boolean executeCL() {
        boolean result = particleImageVelocimetryTimelapse(getCLIJ2(), (ClearCLBuffer)( args[0]), (ClearCLBuffer)(args[1]), (ClearCLBuffer)(args[2]), (ClearCLBuffer)(args[3]), asInteger(args[4]), asInteger(args[5]), asInteger(args[6]), asBoolean(args[7]));
        return result;
    }

    public static boolean particleImageVelocimetryTimelapse(CLIJ2 clij2, ClearCLBuffer input, ClearCLBuffer destinationDeltaX, ClearCLBuffer destinationDeltaY, ClearCLBuffer destinationDeltaZ, Integer maxDeltaX, Integer maxDeltaY, Integer maxDeltaZ, Boolean correctLocalShift) {
        ClearCLBuffer slice1 = clij2.create(new long[] {input.getWidth(), input.getHeight()}, input.getNativeType());
        ClearCLBuffer slice2 = clij2.create(slice1);
        ClearCLBuffer deltaXslice = clij2.create(slice1);
        ClearCLBuffer deltaYslice = clij2.create(slice1);
        ClearCLBuffer deltaZslice = clij2.create(slice1);
        for (int t = 0; t < input.getDepth() - 1; t++) {
            System.out.println("PIVt " + t + "/" + input.getDepth());
            clij2.copySlice(input, slice1, t);
            clij2.copySlice(input, slice2, t + 1);

            ParticleImageVelocimetry.particleImageVelocimetry(clij2, slice1, slice2, deltaXslice, deltaYslice, deltaZslice, maxDeltaX, maxDeltaY, maxDeltaZ);

            clij2.copySlice(deltaXslice, destinationDeltaX, t);
            clij2.copySlice(deltaYslice, destinationDeltaY, t);
            clij2.copySlice(deltaZslice, destinationDeltaZ, t);
        }
        slice1.close();
        slice2.close();
        deltaXslice.close();
        deltaYslice.close();
        deltaZslice.close();
        return true;
    }

    @Override
    public String getParameterHelpText() {
        return "Image source, Image destinationDeltaX, Image destinationDeltaY, Image destinationDeltaZ, Number maxDeltaX, Number maxDeltaY, Number maxDeltaZ, Boolean correctLocalShift";
    }

    @Override
    public String getDescription() {
        return "Run particle image velocimetry on a 2D+t timelapse.";
    }

    @Override
    public String getAvailableForDimensions() {
        return "2D+t";
    }

    @Override
    public ClearCLBuffer createOutputBufferFromSource(ClearCLBuffer input) {
        return clij.create(new long[]{input.getWidth(), input.getHeight(), input.getDepth() - 1}, input.getNativeType());
    }

}