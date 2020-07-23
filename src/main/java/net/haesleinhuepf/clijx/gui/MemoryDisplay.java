package net.haesleinhuepf.clijx.gui;

import ij.IJ;
import ij.ImageJ;
import ij.ImageListener;
import ij.ImagePlus;
import ij.plugin.PlugIn;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clijx.CLIJx;

import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

public class MemoryDisplay implements PlugIn, ImageListener {
    static ImagePlus viewer;
    float[] measurements = new float[200];
    int measurement_count = 200;

    Timer heartbeat = null;

    static String status = "";

    final static int delay = 500;

    @Override
    public void run(String arg) {
        if (viewer == null) {
            viewer = new ImagePlus(CLIJx.getInstance().getGPUName(), generateDisplay());
            viewer.show();
            Rectangle bounds = IJ.getInstance().getBounds();
            viewer.getWindow().setLocation(bounds.x + bounds.width - viewer.getWindow().getWidth(), bounds.y + bounds.height);
        } else {
            viewer.show();
            return;
        }

        Font font = new Font("Arial", 0, 12);


        heartbeat = new Timer();
        heartbeat.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (viewer != null) {
                    viewer.setProcessor(generateDisplay());
                }
            }
        }, delay, delay);
    }


    private ImageProcessor generateDisplay() {
        ColorProcessor ip = new ColorProcessor(200, 100);
        ip.setColor(new Color(255, 255, 255));
        ip.fill();

        for (int i = 1; i < 200; i++) {
            measurements[i - 1] = measurements[i];
        }

        measurements[measurement_count - 1] = getMemoryConsumption(CLIJx.getInstance());

        int now = (int) (System.currentTimeMillis() / delay);
        for (int i = 0; i < measurement_count; i++) {
            float relative = measurements[i];
            ip.setRoi(i, (int) (100 * (1.0 - relative)) + 1, 1, (int) (100 * (relative)));
            ip.setColor(new Color(relative, (1.0f - relative), 0));
            ip.fill();

            if ((now + i) % 20 == 0) {
                ip.setRoi(i, 97, 1, 3);
                ip.setColor(Color.black);
                ip.fill();
            }
            if ((now + i) % 60 == 0) {
                ip.setRoi(i, 95, 1, 5);
                ip.setColor(Color.black);
                ip.fill();
            }
        }

//        TextRoi roi = new TextRoi(status, 0, 15, font);
  //      roi.setStrokeColor(Color.black);
    //    viewer.setRoi(roi);

        ip.setColor(Color.black);
        ip.drawString(CLIJx.getInstance().getGPUName() + "\n" + status, 3, 17);
        return ip;
    }

    private static float getMemoryConsumption(CLIJx instance) {
        String report = instance.reportMemory();

        double sum = 0;
        for(String line : report.split("\n")) {
            //System.out.println(line);
            if (!line.contains("* ")) {
                if (!line.startsWith("= ")) {
                    String[] temp = line.split(" ");
                    double memory = Double.parseDouble(temp[temp.length - 2]) * unitToFactor(temp[temp.length - 1]);
                    sum += memory;
                }
            }
        }
        long available_bytes = instance.getCLIJ().getGPUMemoryInBytes();

        long unit_factor = 1024 * 1024 * 1024;
        status = String.format("%.1f / %.1f GB", (float) sum / unit_factor, (float)available_bytes / unit_factor);

        return (float) Math.min(sum / available_bytes, 1.0);

    }

    private static double unitToFactor(String s) {
        if (s.compareTo("bytes") == 0) {
            return 1;
        }
        if (s.compareTo("kB") == 0) {
            return 1024;
        }
        if (s.compareTo("MB") == 0) {
            return 1024 * 1024;
        }
        if (s.compareTo("GB") == 0) {
            return 1024 * 1024 * 1024;
        }
        return 1;
    }

    @Override
    public void imageOpened(ImagePlus imp) {


    }

    @Override
    public void imageClosed(ImagePlus imp) {
        if(imp == viewer) {
            heartbeat.cancel();
            heartbeat = null;
            viewer = null;
        }
    }

    @Override
    public void imageUpdated(ImagePlus imp) {

    }

    public static void main(String... args) throws InterruptedException {
        CLIJx clijx = CLIJx.getInstance();
        ClearCLBuffer buffer1 = clijx.create(1024, 1024, 100);
        ClearCLBuffer buffer2 = clijx.create(1024, 1024, 50);
        ClearCLBuffer buffer3 = clijx.create(1024, 1024, 150);

        new ImageJ();
        new MemoryDisplay().run("");

        Thread.sleep(5000);

        buffer2.close();
    }

    public static String getStatus() {
        getMemoryConsumption(CLIJx.getInstance());
        return status;
    }
}
