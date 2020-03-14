package net.haesleinhuepf.clijx.plugins;

import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.Roi;
import ij.plugin.Duplicator;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.macro.CLIJHandler;
import net.haesleinhuepf.clij.macro.CLIJMacroPlugin;
import net.haesleinhuepf.clij.macro.CLIJOpenCLProcessor;
import net.haesleinhuepf.clij.macro.documentation.OffersDocumentation;
import net.haesleinhuepf.clij2.AbstractCLIJ2Plugin;
import net.haesleinhuepf.clij2.CLIJ2;
import org.scijava.plugin.Plugin;

/**
 * Author: @haesleinhuepf
 *         March 2020
 */

@Plugin(type = CLIJMacroPlugin.class, name = "CLIJx_pushTile")
public class PushTile extends AbstractCLIJ2Plugin implements CLIJMacroPlugin, CLIJOpenCLProcessor, OffersDocumentation {

    @Override
    public boolean executeCL() {
        String imageName = (String) args[0];
        int tileX = asInteger(args[1]);
        int tileY = asInteger(args[2]);
        int tileZ = asInteger(args[3]);
        int width = asInteger(args[4]);
        int height = asInteger(args[5]);
        int depth = asInteger(args[6]);
        int marginWidth = asInteger(args[7]);
        int marginHeight = asInteger(args[8]);
        int marginDepth = asInteger(args[9]);

        if (WindowManager.getImage(imageName) == null) {
            //Macro.abort();
            throw new IllegalArgumentException("You tried to push the image '" + args[0] + "' to the GPU.\n" +
                    "However, this image doesn't exist.");
        }

        ImagePlus imp = WindowManager.getImage(imageName);

        pushTile(getCLIJ2(), imp, imageName, tileX, tileY, tileZ, width, height, depth, marginWidth, marginHeight, marginDepth);

        return true;
    }

    private void pushTile(CLIJ2 clij2, ImagePlus imp, String imageName, int tileX, int tileY, int tileZ, int width, int height, int depth, int marginWidth, int marginHeight, int marginDepth) {
        Roi roi = imp.getRoi();
        imp.setRoi(tileX * width - marginWidth, tileY * height - marginWidth, width + marginWidth * 2, height + marginHeight * 2);

        int zEnd = (tileZ + 1) * depth - marginDepth;
        int zStart = tileZ * depth + marginDepth;

        System.out.println("Start z " + zStart);
        System.out.println("End z " + zEnd);

        ImagePlus tile = new Duplicator().run(imp, imp.getChannel(), imp.getChannel(), zStart + 1, zEnd + 1, imp.getFrame(), imp.getFrame());
        ClearCLBuffer buffer = clij2.push(tile);
        CLIJHandler.getInstance().pushInternal(buffer, imageName);
        imp.setRoi(roi);
    }

    @Override
    public ClearCLBuffer createOutputBufferFromSource(ClearCLBuffer input) {

        int width = asInteger(args[4]);
        int height = asInteger(args[5]);
        int depth = asInteger(args[6]);

        if (input.getDimension() == 2) {
            return getCLIJ2().create(new long[]{width, height}, input.getNativeType());
        } else {
            return getCLIJ2().create(new long[]{width, height, depth}, input.getNativeType());
        }
    }

    @Override
    public String getParameterHelpText() {
        return "String image, Number tileIndexX, Number tileIndexY, Number tileIndexZ, Number width, Number height, Number depth, Number marginWidth, Number marginHeight, Number marginDepth";
    }

    @Override
    public String getDescription() {
        return "Copies a tile in an image specified by its name, position and size to GPU memory in order to process it there later.";
    }

    @Override
    public String getAvailableForDimensions() {
        return "2D, 3D";
    }

}
