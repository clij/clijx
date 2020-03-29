package net.haesleinhuepf.clijx.tilor.implementations;

import net.haesleinhuepf.clij.macro.CLIJMacroPlugin;
import net.haesleinhuepf.clij.macro.CLIJOpenCLProcessor;
import net.haesleinhuepf.clij.macro.documentation.OffersDocumentation;
import net.haesleinhuepf.clij2.AbstractCLIJ2Plugin;
import net.haesleinhuepf.clijx.tilor.Tilor;

import java.util.ArrayList;

abstract class AbstractTileWiseProcessableCLIJ2Plugin extends AbstractCLIJ2Plugin implements CLIJMacroPlugin, CLIJOpenCLProcessor, OffersDocumentation {

    protected AbstractCLIJ2Plugin master;
    ArrayList<AbstractCLIJ2Plugin> clients = new ArrayList<AbstractCLIJ2Plugin>();

    @Override
    public boolean executeCL() {
        master.setCLIJ2(getCLIJ2());
        Tilor t = new Tilor(master, clients);
        t.executeCL(master, clients, args);

        return false;
    }

    @Override
    public String getParameterHelpText() {
        String parameters = master.getParameterHelpText();
        if (parameters.length() > 0) {
            parameters = parameters + ", ";
        }
        parameters = parameters + "Number tileWidth, Number tileHeight, Number tileDepth, Number marginWidth, Number marginHeight, Number marginDepth";
        return parameters;
    }

    @Override
    public String getDescription() {
        if (master instanceof OffersDocumentation) {
            return ((OffersDocumentation) master).getDescription() + "\n\n" +
                    "xt version (Experimental)\n" +
                    "This is operation is processed in tiles and distributed among all available OpenCL devices. " +
                    "It is recommended to use this operation for 3D images only, " +
                    "because on typical 2D images it may not make much sense performance wise.";
        }
        return "";
    }

    @Override
    public String getAvailableForDimensions() {
        if (master instanceof OffersDocumentation) {
            return ((OffersDocumentation) master).getAvailableForDimensions();
        }
        return "";
    }
}
