package net.haesleinhuepf.clijx.gui.stickyfilters;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.ImageWindow;
import ij.gui.Toolbar;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clijx.CLIJx;
import net.haesleinhuepf.clijx.gui.InteractiveWindowPosition;
import net.haesleinhuepf.clijx.gui.stickyfilters.implementations.*;

public abstract class AbstractStickyFilter implements PlugInFilter {
    private ImagePlus source;
    private ImagePlus target;

    @Override
    public int setup(String arg, ImagePlus imp) {
        return DOES_ALL;
    }

    protected ImagePlus getSource() {
        return source;
    }

    protected ImagePlus setTarget(ImagePlus result) {
        if (!(result instanceof StickyImagePlus)) {
            result = new StickyImagePlus(result.getStack(), this);
        }
        if (target == null) {
            target = result;
            target.show();
        } else {
            target.setStack(result.getStack());
        }
        refreshRelated(target);
        return target;
    }

    private void refreshRelated(ImagePlus potential_source) {
        for (int id : WindowManager.getIDList()) {
            ImagePlus imp = WindowManager.getImage(id);
            if (imp instanceof StickyImagePlus){
                AbstractStickyFilter filter = ((StickyImagePlus) imp).getFilter();
                if (filter.source == potential_source) {
                    filter.compute();
                }
            }
        }
    }

    @Override
    public void run(ImageProcessor ip) {
        source = IJ.getImage();
        compute();
        source = null;
    }

    public static void handleCoordinates(StickyImagePlus simp) {
        ImageWindow win = simp.getWindow();

        int s_left_x = win.getX();
        int s_top_y = win.getY();
        int s_right_x = win.getX() + win.getWidth();
        int s_bottom_y = win.getY() + win.getHeight();

        for (int id : WindowManager.getIDList()) {
            ImagePlus imp = WindowManager.getImage(id);
            win = imp.getWindow();

            int left_x = win.getX();
            int top_y = win.getY();
            int right_x = win.getX() + win.getWidth();
            int bottom_y = win.getY() + win.getHeight();

            if ( (s_top_y > top_y && s_top_y < bottom_y) ||  (s_bottom_y > top_y && s_bottom_y < bottom_y)) {
                // fits in y
                if (s_left_x > (left_x + right_x) / 2 && s_left_x < right_x) {
                    // fits in x
                    simp.getFilter().source = imp;
                    simp.getFilter().compute();
                    break;
                }
            }
        }
    }


    public static void main(String[] args) {
        new ImageJ();
        IJ.openImage("C:/structure/data/blobs.tif").show();
        IJ.openImage("C:/structure/data/blobs.tif").show();

        Toolbar.addPlugInTool(new InteractiveWindowPosition());

        new SoftBlur().run(null);
        new HeavyBlur().run(null);
        new Denoise().run(null);
        new ThresholdHuang().run(null);
        new ThresholdOtsu().run(null);
        new ConnectedCompontentsLabelling().run(null);
        new ExcludeLabelEdges().run(null);
    }

    protected abstract void computUnaryOperation(CLIJx clijx, ClearCLBuffer input, ClearCLBuffer output);

    protected void compute() {

        System.out.println("Compute " + getClass().getSimpleName());
        ImagePlus source = getSource();
        if (source == null) {
            return;
        }

        CLIJx clijx = CLIJx.getInstance();
        ClearCLBuffer input = clijx.pushCurrentZStack(source);
        ClearCLBuffer output = clijx.create(input);

        computUnaryOperation(clijx, input, output);

        ImagePlus result = clijx.pull(output);
        ImagePlus target = setTarget(result);
        target.setDisplayRange(clijx.getMinimumOfAllPixels(output), clijx.getMaximumOfAllPixels(output));
        if (getClass().getSimpleName().contains("Label")) {
            try {
                IJ.run(target, "glasbey_on_dark", "");
            } catch (Exception e) {
                System.out.println(e);
            }
        }
        input.close();
        output.close();
    }
}
