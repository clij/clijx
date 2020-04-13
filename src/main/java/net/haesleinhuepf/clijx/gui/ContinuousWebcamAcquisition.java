package net.haesleinhuepf.clijx.gui;

import ij.IJ;
import ij.IJEventListener;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.Toolbar;
import ij.measure.ResultsTable;
import ij.plugin.tool.PlugInTool;
import net.haesleinhuepf.clij.CLIJ;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.haesleinhuepf.clijx.CLIJx;
import net.haesleinhuepf.clijx.plugins.CaptureWebcamImage;
import net.haesleinhuepf.clijx.plugins.ConvertRGBStackToGraySlice;
import net.haesleinhuepf.clijx.plugins.ListWebcams;
import net.haesleinhuepf.clijx.plugins.StartContinuousWebcamAcquisition;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static net.haesleinhuepf.clijx.gui.Utilities.ignoreEvent;

public class ContinuousWebcamAcquisition extends PlugInTool {
    public ContinuousWebcamAcquisition () {
        IJ.addEventListener(new IJEventListener() {
            @Override
            public void eventOccurred(int eventID) {
                if (ignoreEvent) {
                    return;
                }
                if (eventID == IJEventListener.TOOL_CHANGED) {
                    if (IJ.getToolName().compareTo(getToolName()) == 0 ) {
                        System.out.println("Start acquisition");
                        run("");
                    }
                }
            }
        });
    }

    CLIJx clijx = null;
    int camera_index;
    int image_width;
    int image_height;

    double frame_rate = 4;
    boolean rgb_visualisation = true;

    Timer timer = null;

    ClearCLBuffer input;
    ClearCLBuffer input_r;
    ClearCLBuffer input_g;
    ClearCLBuffer input_b;

    String viewerName = null;

    Object lock = new Object();

    GenericDialog cancelDialog = null;

    @Override
    public void runMenuTool(String name, String command) {
        run("");
    }

    @Override
    public void showPopupMenu(MouseEvent e, Toolbar tb) {
        run("");
    }

    @Override
    public void run(String arg) {
        Toolbar.addPlugInTool(this);
        ResultsTable resultsTable = ListWebcams.listWebcams();

        if (arg.compareTo("no dialog") != 0)
        {
            ArrayList<String> availableDeviceNames = CLIJ.getAvailableDeviceNames();
            String[] clDeviceChoices = new String[availableDeviceNames.size()];
            availableDeviceNames.toArray(clDeviceChoices);

            String[] cameraChoices = new String[resultsTable.size()];

            for (int i = 0; i < cameraChoices.length; i++) {
                cameraChoices[i] = resultsTable.getStringValue("Camera_Name", i) + "(" +
                        resultsTable.getValue("Image_Width", i) + "/" +
                        resultsTable.getValue("Image_Height", i) + ")";
            }

            GenericDialog dialog = new GenericDialog("Webcam acquisition");
            dialog.addChoice("cl_device", clDeviceChoices, CLIJ.getInstance().getGPUName());
            dialog.addChoice("Camera: ", cameraChoices, cameraChoices[cameraChoices.length - 1]);
            dialog.addNumericField("Frames per second", frame_rate, 0);
            dialog.addCheckbox("RGB visualisiation", rgb_visualisation);
            dialog.showDialog();

            if (dialog.wasCanceled()) {
                return;
            }

            String clDeviceName = dialog.getNextChoice();
            clijx = CLIJx.getInstance(clDeviceName);

            int camera_table_index = dialog.getNextChoiceIndex();
            camera_index = (int)resultsTable.getValue("Camera_Index", camera_table_index);
            System.out.println("Camera choice: " + camera_index);
            image_width = (int) resultsTable.getValue("Image_Width", camera_table_index);
            image_height = (int) resultsTable.getValue("Image_Height", camera_table_index);

            frame_rate = dialog.getNextNumber();
            viewerName = cameraChoices[camera_index];

            rgb_visualisation = dialog.getNextBoolean();

            cancelDialog = new GenericDialog("Abort acquisition");
            cancelDialog.addMessage("Click cancel to stop acquistion.");
            cancelDialog.setOKLabel("Cancel");
            cancelDialog.setModal(false);
            cancelDialog.showDialog();
        }

        StartContinuousWebcamAcquisition.startContinuousWebcamAcquisition(clijx, camera_index, image_width, image_height);

        synchronized (lock) {

            cancelAcquistion();

            timer = new Timer(true);
            System.out.println("Start timer");
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    acquire();
                }
            }, 0, (int) (1000 / frame_rate));

            input = clijx.create(new long[]{image_width, image_height, 3}, NativeTypeEnum.UnsignedByte);
            input_r = clijx.create(new long[]{image_width, image_height}, NativeTypeEnum.UnsignedByte);
            input_g = clijx.create(new long[]{image_width, image_height}, NativeTypeEnum.UnsignedByte);
            input_b = clijx.create(new long[]{image_width, image_height}, NativeTypeEnum.UnsignedByte);
        }
    }

    private void acquire() {
        synchronized (lock) {
            clijx.captureWebcamImage(input, camera_index, image_width, image_height);

            if (rgb_visualisation) {
                clijx.copySlice(input, input_r, 0);
                clijx.copySlice(input, input_g, 1);
                clijx.copySlice(input, input_b, 2);
                clijx.showRGB(input_r, input_g, input_b, viewerName);
                IJ.showStatus("RGB");
            } else {
                clijx.convertRGBStackToGraySlice(input, input_r);
                clijx.showGrey(input_r, viewerName);
                IJ.showStatus("grey");
            }

            if (cancelDialog.wasOKed() || cancelDialog.wasCanceled()) {
                cancelAcquistion();
            }
        }
    }

    @Override
    public void mouseReleased(ImagePlus imp, MouseEvent e) {
        synchronized (lock) {
            if (imp.getTitle().compareTo(viewerName) == 0) {
                if (timer != null) {
                    cancelAcquistion();
                } else {
                    run("no dialog");
                }
            }
        }
    }

    private void cancelAcquistion() {
        if (timer != null) {
            timer.cancel();
            timer = null;
            clijx.stopContinuousWebcamAcquisition(camera_index);
        }

        if (input != null) {
            clijx.release(input);
        }
        if (input_r != null) {
            clijx.release(input);
        }
        if (input_g != null) {
            clijx.release(input_g);
        }
        if (input_b != null) {
            clijx.release(input_b);
        }

        input = null;
        input_r = null;
        input_g = null;
        input_b = null;
    }

    @Override
    public String getToolName() {
        return "Continous webcam acquisition";
    }

    @Override
    public String getToolIcon()
    {
        return Utilities.generateIconCodeString(
                getToolIconString()
        );

    }

    public static String getToolIconString()
    {
        return
                //        0123456789ABCDEF
                /*0*/	 "                " +
                /*1*/	 "                " +
                /*2*/	 "                " +
                /*3*/	 "     ######     " +
                /*4*/	 "    #      #    " +
                /*5*/	 "####  ####  ####" +
                /*6*/	 "#    #    #    #" +
                /*7*/	 "#    # ## #    #" +
                /*8*/	 "#    # ## #    #" +
                /*9*/	 "#    #    #    #" +
                /*A*/	 "####  ####  ####" +
                /*B*/	 "    ########    " +
                /*C*/	 "                " +
                /*D*/	 "                " +
                /*E*/	 "                " +
                /*F*/	 "                " ;
    }

    public static void main(String[] args) {
        new ImageJ();
        new ContinuousWebcamAcquisition().run("");
    }
}
