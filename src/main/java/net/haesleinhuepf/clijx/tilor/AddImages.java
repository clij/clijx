package net.haesleinhuepf.clijx.tilor;


import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.plugin.Duplicator;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.macro.AbstractCLIJPlugin;
import net.haesleinhuepf.clij.macro.CLIJMacroPlugin;
import net.haesleinhuepf.clij.macro.CLIJOpenCLProcessor;
import net.haesleinhuepf.clij.macro.documentation.OffersDocumentation;
import net.haesleinhuepf.clij2.AbstractCLIJ2Plugin;
import net.haesleinhuepf.clij2.CLIJ2;
import net.haesleinhuepf.clij2.utilities.ProcessableInTiles;
import org.scijava.plugin.Plugin;

import java.util.ArrayList;

@Plugin(type = CLIJMacroPlugin.class, name = "CLIJxt_addImages")
public class AddImages extends AbstractCLIJ2Plugin implements CLIJMacroPlugin, CLIJOpenCLProcessor, OffersDocumentation, ProcessableInTiles {

    net.haesleinhuepf.clij2.plugins.AddImages master;
    ArrayList<AbstractCLIJ2Plugin> clients = new ArrayList<AbstractCLIJ2Plugin>();

    public AddImages() {
        master = new net.haesleinhuepf.clij2.plugins.AddImages();
    }

    @Override
    public boolean executeCL() {
        master.setCLIJ2(getCLIJ2());
        Tilor t = new Tilor(master, clients);
        t.executeCL(master, clients, args);

        return false;
    }

    @Override
    public String getParameterHelpText() {
        String parameters = getParameterHelpText();
        if (parameters.length() > 0) {
            parameters = parameters + ", ";
        }
        parameters = parameters + "Number tileWidth, Number tileHeight, Number tileDepth, Number marginWidth, Number marginHeight, Number marginDepth";
        return parameters;
    }

    @Override
    public String getDescription() {
        return master.getDescription() + "\n\n" +
                "Tilor version (Experimental)\n" +
                "This is operation is processed in tiles and distributed among all available OpenCL deviced. " +
                "It is recommended to use this operation for 3D images only, " +
                "because on typical 2D images it may not make much sense performance wise.";
    }

    @Override
    public String getAvailableForDimensions() {
        return master.getAvailableForDimensions();
    }

    public static void main(String... args) {
        new ImageJ();
        ImagePlus imp = IJ.openImage("C:/structure/data/2018-02-14-17-26-57-76-Akanksha_nGFP_001111.raw.tif");
        IJ.run(imp, "32-bit", "");
        imp = new Duplicator().run(imp, 65, 104);

        //if (false)
        {
            CLIJ2 clij2 = CLIJ2.getInstance();

            ClearCLBuffer input1 = clij2.push(imp);
            ClearCLBuffer input2 = clij2.create(input1);
            ClearCLBuffer output1 = clij2.create(input1);
            clij2.setRampX(input2);

            long time = System.currentTimeMillis();
            clij2.addImages(input1, input2, output1);
            System.out.println("Processing on the master GPU (" + clij2.getGPUName() + ") took " + (System.currentTimeMillis() - time) + " ms.");
            clij2.show(output1, "output1");
        }

        {
            CLIJ2 clij2 = CLIJ2.getInstance("CPU");

            ClearCLBuffer input1 = clij2.push(imp);
            ClearCLBuffer input2 = clij2.create(input1);
            ClearCLBuffer output2 = clij2.create(input1);
            clij2.setRampX(input2);
            //clij2.set(input2, 1);

            AddImages plugin = new AddImages();
            plugin.setArgs(new Object[]{input1, input2, output2, input1.getWidth() / 4, input1.getHeight() / 4, input1.getDepth() / 4, 10, 10, 10});
            plugin.setClij(clij2.getCLIJ());
            plugin.executeCL();
            clij2.show(output2, "output2");
        }
    }

}