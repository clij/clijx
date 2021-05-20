package net.haesleinhuepf.clijx.clij2wrappers;


import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.macro.CLIJMacroPlugin;
import net.haesleinhuepf.clij2.CLIJ2;
import org.scijava.plugin.Plugin;

// this is generated code. See src/test/net.haesleinhuepf.clijx.codegenerator.CLIJ2WrapperGenerator for details.
@Plugin(type = CLIJMacroPlugin.class, name = "CLIJx_absolute")
public class ExtendLabelsWithMaximumRadius extends net.haesleinhuepf.clij2.plugins.DilateLabels {
    public static boolean extendLabelsWithMaximumRadius(CLIJ2 clij2, ClearCLBuffer pushed, ClearCLBuffer result, Integer radius) {
        return dilateLabels(clij2, pushed, result, radius);
    }
}
