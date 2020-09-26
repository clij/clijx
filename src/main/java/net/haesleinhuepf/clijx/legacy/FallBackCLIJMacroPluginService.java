package net.haesleinhuepf.clijx.legacy;

public class FallBackCLIJMacroPluginService extends net.haesleinhuepf.clij2.legacy.FallBackCLIJMacroPluginService {

    public FallBackCLIJMacroPluginService() {
        net.haesleinhuepf.clij2.legacy.FallBackCLIJMacroPluginServiceInitializer.initialize(this);
    }
}
