package net.haesleinhuepf.clijx.tilor.implementations;

import net.haesleinhuepf.clij.macro.CLIJMacroPlugin;
import net.haesleinhuepf.clij2.AbstractCLIJ2Plugin;
import org.scijava.plugin.Plugin;

@Plugin(type = CLIJMacroPlugin.class, name = "CLIJxt_blur3D")// this is generated code. See CLIJxtGenerator for details. 
public class Blur3D extends AbstractTileWiseProcessableCLIJ2Plugin {

    public Blur3D() {
        master = new net.haesleinhuepf.clij2.plugins.Blur3D();
    }
}