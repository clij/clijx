package net.haesleinhuepf.clijx.tilor.implementations;

import ij.IJ;
import net.haesleinhuepf.clij.macro.CLIJMacroPlugin;
import org.scijava.plugin.Plugin;

@Plugin(type = CLIJMacroPlugin.class, name = "CLIJxt_connectedComponentsLabelingBox")
// this is generated code. See CLIJxtGenerator for details. // this is generated code. See CLIJxtGenerator for details. // this is generated code. See CLIJxtGenerator for details. // this is generated code. See CLIJxtGenerator for details. // this is generated code. See CLIJxtGenerator for details. // this is generated code. See CLIJxtGenerator for details. // this is generated code. See CLIJxtGenerator for details. // this is generated code. See CLIJxtGenerator for details. // this is generated code. See CLIJxtGenerator for details. 
public class ConnectedComponentsLabelingBox extends AbstractTileWiseProcessableCLIJ2Plugin {

    public ConnectedComponentsLabelingBox() {
        master = new net.haesleinhuepf.clijx.clij2wrappers.ConnectedComponentsLabelingBox();
    }

    @Override
    public boolean executeCL() {
        System.err.println("CLIJxt warning: Distributed connected components analysis has been implemented for academic purposes only.\n" +
                "It's known to not work well in various scenarios." );
        return super.executeCL();
    }
}