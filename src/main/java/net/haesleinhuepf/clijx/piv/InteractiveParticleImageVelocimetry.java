package net.haesleinhuepf.clijx.piv;

import ij.*;
import ij.gui.GenericDialog;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.gui.TextRoi;
import ij.plugin.Duplicator;
import ij.plugin.RGBStackMerge;
import ij.plugin.StackCombiner;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.converters.implementations.ImagePlusToClearCLBufferConverter;
import net.haesleinhuepf.clij2.CLIJ2;
import net.haesleinhuepf.clijx.piv.visualisation.VisualiseVectorFieldsPlugin;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class InteractiveParticleImageVelocimetry implements PlugInFilter, ImageListener {
    ImagePlus output = null;
    ImagePlus input = null;
    ImagePlus event_imp;
    GenericDialog dialog = null;

    @Override
    public void run(ImageProcessor ip) {
        event_imp = IJ.getImage();
        input = new Duplicator().run(event_imp, 1, event_imp.getNChannels(), 1, event_imp.getNSlices(), 1, event_imp.getNFrames() );

        dialog = new GenericDialog("Interactive PIV");
        dialog.addNumericField("Mean radius", 5);

        dialog.addNumericField("Min distance", 1);
        dialog.addNumericField("Max distance", 5);

        dialog.addNumericField("Line step", 5);
        dialog.addNumericField("Line width", 1);

        dialog.addNumericField("Mean field radius", 3);

        dialog.addStringField("Vector lookup table", "16 colors");
        dialog.addStringField("Field lookup table", "Phase");

        //dialog.addCheckbox("Calibration bar", true);
        //dialog.addCheckbox("Fast PIV", true);

        dialog.setModal( false);
        dialog.showDialog();


        for (KeyListener listener : dialog.getKeyListeners()) {
            dialog.removeKeyListener(listener);
        }
        dialog.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.isActionKey()) {
                    // this is to prevent the dialog from closing
                    // todo: check if this is necessary
                    return;
                }
                super.keyTyped(e);
                refresh();
            }
        });

        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                refresh();
            }
        };
        KeyAdapter keyAdapter = new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                refresh();
            }
        };

        ArrayList<Component> gui_components = new ArrayList<>();
        if (dialog.getCheckboxes() != null) {
            gui_components.addAll(dialog.getCheckboxes());
        }
        if (dialog.getSliders() != null) {
            gui_components.addAll(dialog.getSliders());
        }
        if (dialog.getNumericFields() != null) {
            gui_components.addAll(dialog.getNumericFields());
        }
        if (dialog.getChoices() != null) {
            gui_components.addAll(dialog.getChoices());
        }
        for (Component item : gui_components) {
            item.addKeyListener(keyAdapter);
            item.addMouseListener(mouseAdapter);
        }



        ImagePlus.addImageListener(this);
        refresh();
    }

    public void refresh()
    {
        Roi blind_roi = event_imp.getRoi();


        int blur_radius;
        int blur_field_radius;
        double min_distance;
        int max_distance;
        int line_step;
        int line_width;
        String arrow_lookup_table;
        String vf_lookup_table;
        boolean fast_piv = true;
        //boolean calibration_bar = true;

        try {
            blur_radius = (int) Double.parseDouble(((TextField) dialog.getNumericFields().get(0)).getText());
            min_distance = Double.parseDouble(((TextField) dialog.getNumericFields().get(1)).getText());
            max_distance = (int) Double.parseDouble(((TextField) dialog.getNumericFields().get(2)).getText());
            line_step = (int) Double.parseDouble(((TextField) dialog.getNumericFields().get(3)).getText());
            line_width = (int) Double.parseDouble(((TextField) dialog.getNumericFields().get(4)).getText());
            blur_field_radius = (int) Double.parseDouble(((TextField) dialog.getNumericFields().get(0)).getText());
            arrow_lookup_table = ((TextField) dialog.getStringFields().get(0)).getText();
            vf_lookup_table = ((TextField) dialog.getStringFields().get(1)).getText();
            //fast_piv = ((Checkbox)dialog.getCheckboxes().get(0)).getState();
            //calibration_bar = ((Checkbox)dialog.getCheckboxes().get(0)).getState();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        CLIJ2 clij2 = CLIJ2.getInstance("RTX");

        input.setZ(event_imp.getZ());
        input.setC(event_imp.getC());
        input.setT(event_imp.getFrame());
        ClearCLBuffer in1 = clij2.pushCurrentSlice(input);
        input.setT(event_imp.getFrame() + 1);
        ClearCLBuffer in2 = clij2.pushCurrentSlice(input);

        //ClearCLBuffer max1 = clij2.create(in1.getWidth(), in1.getHeight());
        //ClearCLBuffer max2 = clij2.create(in2.getWidth(), in2.getHeight());

        ClearCLBuffer vfx = clij2.create(in2.getWidth(), in2.getHeight());
        ClearCLBuffer vfy = clij2.create(in2.getWidth(), in2.getHeight());
        ClearCLBuffer vfz = clij2.create(in2.getWidth(), in2.getHeight());

        //clij2.maximumZProjection(in1, max1);
        //clij2.maximumZProjection(in2, max2);


        ClearCLBuffer blur1 = clij2.create(in1.getWidth(), in1.getHeight());
        ClearCLBuffer blur2 = clij2.create(in2.getWidth(), in2.getHeight());

        clij2.mean2DBox(in1, blur1, blur_radius, blur_radius);
        clij2.mean2DBox(in2, blur2, blur_radius, blur_radius);

        if (fast_piv) {
            FastParticleImageVelocimetry.particleImageVelocimetry2D(clij2, blur1, blur2, vfx, vfy, max_distance);
        } else {
            ParticleImageVelocimetry.particleImageVelocimetry(clij2, blur1, blur2, vfx, vfy, vfz, max_distance, max_distance, max_distance);
        }

        clij2.copy(vfx, blur1);
        clij2.copy(vfy, blur2);
        clij2.mean2DBox(blur1, vfx, blur_field_radius, blur_field_radius);
        clij2.mean2DBox(blur2, vfy, blur_field_radius, blur_field_radius);

        ImagePlus imp1 = clij2.pull(in1);
        //IJ.run(imp, "Enhance Contrast", "saturated=0.35");
        imp1.setDisplayRange(event_imp.getDisplayRangeMin(), event_imp.getDisplayRangeMax());

        ImagePlus imp2 = clij2.pull(in2);
        //IJ.run(imp, "Enhance Contrast", "saturated=0.35");
        imp2.setDisplayRange(event_imp.getDisplayRangeMin(), event_imp.getDisplayRangeMax());

        ImageStack merged = RGBStackMerge.mergeStacks(imp1.getStack(), imp2.getStack(), imp1.getStack(), true);
        ImagePlus merged_imp = new ImagePlus("merged", merged);

        //ImagePlus result = VisualiseVectorFieldsPlugin.visualiseVectorField(
        //        imp,
        //        clij2.pull(vfx),
        //        clij2.pull(vfy),
        //        5
        //);

        ImagePlus vfx_imp = clij2.pull(vfx);
        ImagePlus vfy_imp = clij2.pull(vfy);
        if ( blind_roi != null) {
            vfx_imp.setRoi(blind_roi);

            IJ.run(vfx_imp, "Multiply...", "value=0");
            //vfx_imp.getProcessor().multiply(0);
            vfx_imp.killRoi();

            vfy_imp.setRoi(blind_roi);
            IJ.run(vfy_imp, "Multiply...", "value=0");
            //vfy_imp.getProcessor().multiply(0);
            vfy_imp.killRoi();
        }

        VisualiseVectorFieldsPlugin vvpd = new VisualiseVectorFieldsPlugin();
        //ImagePlus imp = clij2.pull(max1);
        //IJ.run(imp, "Enhance Contrast", "saturated=0.35");
        //imp.show();
        //if (true) return;
        //IJ.run(imp, "8-bit", "");
        //imp.show();
        vvpd.setInputImage(imp1);
        vvpd.setVectorXImage(vfx_imp);
        vvpd.setVectorYImage(vfy_imp);
        vvpd.setSilent(true);
        vvpd.setShowResult(false);
        vvpd.setMaximumLength(max_distance);
        vvpd.setMinimumLength(min_distance);
        vvpd.setStepSize(line_step);
        vvpd.setLineWidth(line_width);
        vvpd.setLookupTable(arrow_lookup_table);
        vvpd.run();
        ImagePlus result = vvpd.getOutputImage(); //.show();

        //IJ.run(result,"Calibration Bar...", "location=[Upper Right] fill=White label=Black number=5 decimal=0 font=12 zoom=0.5 overlay");
        overlayText(result,"PIV vectors");
        result = result.flatten();

        overlayText(imp1,"t = " + event_imp.getFrame() );
        ImagePlus orig_view1 = imp1.flatten();
        overlayText(imp2, "t + 1 = " + ( + event_imp.getFrame() + 1));
        ImagePlus orig_view2 = imp2.flatten();

        overlayText(merged_imp,"t (magenta), t+1 (green)");
        ImagePlus merged_view = merged_imp.flatten();
        try {
            IJ.run(vfx_imp, vf_lookup_table, "");
            IJ.run(vfy_imp, vf_lookup_table, "");
        } catch(Exception e) {}

        IJ.setMinAndMax(vfx_imp, -max_distance, max_distance);
        IJ.setMinAndMax(vfy_imp, -max_distance, max_distance);

        //IJ.run(vfx_imp,"Calibration Bar...", "location=[Upper Right] fill=White label=Black number=5 decimal=0 font=12 zoom=0.5 overlay");
        overlayText(vfx_imp, "Shift X" + (fast_piv?"(estd)":""));
        ImagePlus vfx_imp_view = vfx_imp.flatten();
        //IJ.run(vfy_imp,"Calibration Bar...", "location=[Upper Right] fill=White label=Black number=5 decimal=0 font=12 zoom=0.5 overlay");
        overlayText(vfy_imp,"Shift Y" + (fast_piv?"(estd)":""));
        ImagePlus vfy_imp_view = vfy_imp.flatten();


        ImageStack stack =
            new StackCombiner().combineVertically(
                    new StackCombiner().combineHorizontally(
                            merged_view.getStack(),
                            result.getStack()
                    ),
                    new StackCombiner().combineVertically(
                    new StackCombiner().combineHorizontally(
                        orig_view1.getStack(),
                        orig_view2.getStack()
                    ),
                    new StackCombiner().combineHorizontally(
                        vfx_imp_view.getStack(),
                        vfy_imp_view.getStack()
                    )
                )
            );

        result = new ImagePlus("res", stack);


        if (output == null) {
            output = result;
            result.show();
        } else {
            output.setProcessor(result.getProcessor());
        }

        /*
*/


        //clij2.show(max1, "max1");
        //clij2.show(max2, "max2");

        //clij2.show(vfx, "vfx");
        //clij2.show(vfy, "vfy");

        clij2.clear();
    }

    private Overlay overlayText(ImagePlus imp, String text) {
        Overlay overlay = imp.getOverlay();
        if (overlay == null) {
            overlay = new Overlay();
        }
        TextRoi roi = new TextRoi(text,0, 15, new Font("Arial", 0, 15));
        roi.setStrokeColor(Color.white);
        overlay.add(roi);
        imp.setOverlay(overlay);
        return overlay;
    }

    public static void main(String[] args) {
        new ImageJ();

        ImagePlus input = IJ.openImage("C:/structure/data/Irene/" +
                //"ISB200522_well1_pos1_fast_cropped2.tif"
                "piv/C2-ISB200522_well2_pos1cropped_1sphere-1.tif"
        );
        input.show();
        new InteractiveParticleImageVelocimetry().run(null);

    }

    @Override
    public int setup(String arg, ImagePlus imp) {
        return DOES_ALL;
    }


    @Override
    public void imageOpened(ImagePlus imp) {

    }

    @Override
    public void imageClosed(ImagePlus imp) {
        if (imp == event_imp) {
            ImagePlus.removeImageListener(this);
        }
    }

    @Override
    public void imageUpdated(ImagePlus imp) {
        if( imp == event_imp) {
            refresh();
        }
    }
}
