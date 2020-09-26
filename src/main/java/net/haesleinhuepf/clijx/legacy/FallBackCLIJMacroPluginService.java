package net.haesleinhuepf.clijx.legacy;

public class FallBackCLIJMacroPluginService extends net.haesleinhuepf.clij2.legacy.FallBackCLIJMacroPluginService {

    public FallBackCLIJMacroPluginService() {
        FallBackCLIJMacroPluginServiceInitializer.initialize(this);
    }
}
