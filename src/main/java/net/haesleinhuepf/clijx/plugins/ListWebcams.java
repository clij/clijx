package net.haesleinhuepf.clijx.plugins;

import com.github.sarxos.webcam.Webcam;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.measure.ResultsTable;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.haesleinhuepf.clij.macro.CLIJMacroPlugin;
import net.haesleinhuepf.clij.macro.CLIJOpenCLProcessor;
import net.haesleinhuepf.clij.macro.documentation.OffersDocumentation;
import net.haesleinhuepf.clijx.CLIJx;
import net.haesleinhuepf.clijx.utilities.AbstractCLIJxPlugin;
import org.scijava.plugin.Plugin;

import javax.xml.transform.Result;
import java.awt.*;
import java.awt.geom.RectangularShape;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Author: @haesleinhuepf
 *         March 2020
 */

@Plugin(type = CLIJMacroPlugin.class, name = "CLIJx_listWebcams")
public class ListWebcams extends AbstractCLIJxPlugin implements CLIJMacroPlugin, CLIJOpenCLProcessor, OffersDocumentation {

    @Override
    public boolean executeCL() {
        ResultsTable table = listWebcams();
        table.show("Results");
        return true;
    }

    public static ResultsTable listWebcams() {

        ResultsTable table = ResultsTable.getResultsTable();

        List<Webcam> webcams = Webcam.getWebcams();
        for (int i = 0; i < webcams.size(); i++) {
            Webcam cam = webcams.get(i);

            Dimension[] viewSizes = cam.getViewSizes();
            for (int v = 0; v  < viewSizes.length; v++) {
                Dimension dim = viewSizes[v];

                table.incrementCounter();
                table.addValue("Camera_Index", i);
                table.addValue("Camera_Name", cam.getName());
                table.addValue("Image_Width", dim.getWidth());
                table.addValue("Image_Height", dim.getHeight());
            }
        }

        return table;
    }

    public static void main(String... args) {
        new ImageJ();
        ListWebcams.listWebcams().show("Results");
    }

    @Override
    public ClearCLBuffer createOutputBufferFromSource(ClearCLBuffer input) {
        // RGB images of given size
        return getCLIJx().create(new long[]{asInteger(args[2]), asInteger(args[3]), 3}, NativeTypeEnum.UnsignedByte);
    }

    @Override
    public String getParameterHelpText() {
        return "";
    }

    @Override
    public String getDescription() {
        return "Lists available webcams and resolutions.";
    }

    @Override
    public String getAvailableForDimensions() {
        return "2D, 3D";
    }
}
