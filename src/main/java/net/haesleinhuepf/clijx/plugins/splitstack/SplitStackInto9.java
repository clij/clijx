package net.haesleinhuepf.clijx.plugins.splitstack;

import net.haesleinhuepf.clij.macro.CLIJMacroPlugin;
import org.scijava.plugin.Plugin;

@Plugin(type = CLIJMacroPlugin.class, name = "CLIJx_splitStackInto9")
public class SplitStackInto9 extends AbstractSplitStack {

    @Override
    public String getParameterHelpText() {
        return getParameterHelpText(9);
    }


}
