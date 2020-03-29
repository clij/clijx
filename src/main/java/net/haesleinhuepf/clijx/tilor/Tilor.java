package net.haesleinhuepf.clijx.tilor;

import ij.ImagePlus;
import net.haesleinhuepf.clij.CLIJ;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij2.AbstractCLIJ2Plugin;
import net.haesleinhuepf.clij2.plugins.AddImages;
import net.haesleinhuepf.clijx.CLIJx;

import java.util.ArrayList;
import java.util.HashMap;

import static net.haesleinhuepf.clij2.AbstractCLIJ2Plugin.asInteger;


public class Tilor{

    private AbstractCLIJ2Plugin master;

    int tileWidth;
    int tileHeight;
    int tileDepth;
    int marginWidth;
    int marginHeight;
    int marginDepth;
    int tileIndexX;
    int tileIndexY;
    int tileIndexZ;
    private int numTilesX;
    private int numTilesY;
    private int numTilesZ;

    public Tilor(AbstractCLIJ2Plugin master, ArrayList<AbstractCLIJ2Plugin> clients) {
        //System.out.println("Initializing Tilor: " + master.getName());
        this.master = master;

        for (int i = 0; i < CLIJ.getAvailableDeviceNames().size(); i++) {
            try {
                AbstractCLIJ2Plugin clone = master.getClass().newInstance();
                CLIJ.debug = true;
                clone.setClij(new CLIJ(i));
                CLIJ.debug = false;
                //System.out.println("dev" + i + " " + clone.getCLIJ2().getGPUName());
                clients.add(clone);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        // TODO: Check if it's necessary to close all instances by the end.
    }

    public void executeCL(AbstractCLIJ2Plugin master, ArrayList<AbstractCLIJ2Plugin> clients, Object[] args) {
        long time = System.currentTimeMillis();

        String[] argumentNames = master.getParameterHelpText().split(",");

        ImagePlus anyImp = null;
        HashMap<String, ImagePlus> imageParameters = new HashMap<>();
        CLIJx clijx = CLIJx.getInstance();
        clijx.stopWatch("");
        for (int a = 0; a < argumentNames.length; a++) {
            String[] parameterParts = argumentNames[a].trim().split(" ");
            String parameterType = parameterParts[0];
            String parameterName = parameterParts[1];
            boolean byRef = false;
            if (parameterType.compareTo("ByRef") == 0) {
                parameterType = parameterParts[1];
                parameterName = parameterParts[2];
                byRef = true;
            }

            if (parameterType.compareTo("Image") == 0) {
                ImagePlus imp = master.getCLIJ2().pull(args[a]);
                //imp.show();
                //System.out.println("pulled imp " + imp.getWidth()+"/" + imp.getHeight() + "/" + imp.getNSlices());
                imageParameters.put(parameterName, imp);
                anyImp = imp;
            }
        }
        clijx.stopWatch("Divided images");

        tileWidth =    asInteger(args[args.length - 6]);
        tileHeight =   asInteger(args[args.length - 5]);
        tileDepth =    asInteger(args[args.length - 4]);
        marginWidth =  asInteger(args[args.length - 3]);
        marginHeight = asInteger(args[args.length - 2]);
        marginDepth =  asInteger(args[args.length - 1]);

        // now we have all images in CPU memory. Let's push them tile by tile to the GPU.
        tileIndexX = 0;
        tileIndexY = 0;
        tileIndexZ = 0;

        numTilesX = anyImp.getWidth() / tileWidth;
        numTilesY = anyImp.getHeight() / tileHeight;
        numTilesZ = anyImp.getNSlices() / tileDepth;

        boolean addMoreProcessors = true;

        ArrayList<ExecutorOnTile> executors = new ArrayList<ExecutorOnTile>();
        for (AbstractCLIJ2Plugin plugin : clients) {
            if (!addExecutor(args, imageParameters, executors, plugin)) {
                addMoreProcessors = false;
                break;
            }
        }
        while(executors.size() > 0) {
            //System.out.println("Num exec: " + executors.size() );
            for (int i = 0; i < executors.size(); i++) {
                ExecutorOnTile executor = executors.get(i);

                if (executor.isFinished()) {
                    executors.remove(executor);
                    if (addMoreProcessors) {
                        if (!addExecutor(args, imageParameters, executors, executor.getPlugin())) {
                            //System.out.println("Added more executores");
                            addMoreProcessors = false;
                            break;
                        }
                    }
                }
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }

        clijx.stopWatch("");
        for (int a = 0; a < argumentNames.length; a++) {
            String[] parameterParts = argumentNames[a].trim().split(" ");
            String parameterType = parameterParts[0];
            String parameterName = parameterParts[1];
            boolean byRef = false;
            if (parameterType.compareTo("ByRef") == 0) {
                parameterType = parameterParts[1];
                parameterName = parameterParts[2];
                byRef = true;
            }

            if (parameterType.compareTo("Image") == 0) {
                ImagePlus imp = imageParameters.get(parameterName);
                ClearCLBuffer buffer = master.getCLIJ2().push(imp);
                master.getCLIJ2().copy(buffer, (ClearCLBuffer)args[a]);
                master.getCLIJ2().release(buffer);
            }
        }
        clijx.stopWatch("Combined images");

        System.out.println("Processing on " + clients.size() + " OpenCL devices took " + (System.currentTimeMillis() - time) + "ms.");
    }

    private boolean addExecutor(Object[] args, HashMap<String, ImagePlus> imageParameters, ArrayList<ExecutorOnTile> executors, AbstractCLIJ2Plugin plugin) {

        ExecutorOnTile executor = new ExecutorOnTile(plugin, imageParameters, args, tileIndexX, tileIndexY, tileIndexZ, tileWidth, tileHeight, tileDepth, marginWidth, marginHeight, marginDepth);
        new Thread(executor).start();
        executors.add(executor);

        tileIndexX++;
        System.out.println("\t" + tileIndexX + "\t" + tileIndexY + "\t" + tileIndexZ);
        if (tileIndexX >= numTilesX) {
            //System.out.println("TilesX over");
            tileIndexX = 0;
            tileIndexY ++;
            if (tileIndexY >= numTilesY) {
                tileIndexY = 0;
                tileIndexZ++;
                if (tileIndexZ >= numTilesZ) {
                    return false;
                }
            }
        }


        return true;
    }
}
