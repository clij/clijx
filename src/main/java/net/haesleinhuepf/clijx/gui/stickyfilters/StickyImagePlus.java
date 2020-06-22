package net.haesleinhuepf.clijx.gui.stickyfilters;

import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.ImageWindow;


public class StickyImagePlus extends ImagePlus {
    private final AbstractStickyFilter filter;

    public StickyImagePlus(ImageStack stack, AbstractStickyFilter filter) {
        super(filter.getClass().getSimpleName(), stack);
        this.filter = filter;
    }

    public AbstractStickyFilter getFilter() {
        return filter;
    }
}
