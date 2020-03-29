package net.haesleinhuepf.clijx.tilor;

import ij.ImagePlus;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.macro.CLIJOpenCLProcessor;
import net.haesleinhuepf.clij2.AbstractCLIJ2Plugin;
import net.haesleinhuepf.clijx.plugins.PullTile;
import net.haesleinhuepf.clijx.plugins.PushTile;

import java.util.HashMap;
public class ExecutorOnTile implements Runnable {

    private static Object pushPullMutex = new Object();

    private AbstractCLIJ2Plugin plugin;
    private HashMap<String, ImagePlus> parameters;
    private Object[] args;
    private final int tileX;
    private final int tileY;
    private final int tileZ;
    private final int tileWidth;
    private final int tileHeight;
    private final int tileDepth;
    private final int marginWidth;
    private final int marginHeight;
    private final int marginDepth;

    boolean finished = false;

    ExecutorOnTile(AbstractCLIJ2Plugin plugin,
                   HashMap<String, ImagePlus> parameters,
                   Object[] args,
                   int tileX, int tileY, int tileZ,
                   int tileWidth,
                   int tileHeight,
                   int tileDepth,
                   int marginWidth,
                   int marginHeight,
                   int marginDepth
    ) {
        this.plugin = plugin;
        this.parameters = parameters;
        this.args = args;
        this.tileX = tileX;
        this.tileY = tileY;
        this.tileZ = tileZ;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        this.tileDepth = tileDepth;
        this.marginWidth = marginWidth;
        this.marginHeight = marginHeight;
        this.marginDepth = marginDepth;
    }

    @Override
    public void run() {
        long time = System.currentTimeMillis();
        String[] argumentNames = plugin.getParameterHelpText().split(",");

        Object[] newArgs = new Object[args.length];
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
                ImagePlus imp = parameters.get(parameterName);
                synchronized (pushPullMutex) {
                    //System.out.println("push " + parameterName);
                    newArgs[a] = PushTile.pushTile(plugin.getCLIJ2(), imp, tileX, tileY, tileZ, tileWidth, tileHeight, tileDepth, marginWidth, marginHeight, marginDepth);
                    //System.out.println("args depth" + ((ClearCLBuffer)newArgs[a]).getDepth());
                }
            } else {
                newArgs[a] = args[a];
            }
        }

        plugin.setArgs(newArgs);
        if (plugin instanceof CLIJOpenCLProcessor) {
            ((CLIJOpenCLProcessor) plugin).executeCL();
        }

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
                ImagePlus imp = parameters.get(parameterName);
                synchronized (pushPullMutex) {
                    //System.out.println("pull " + parameterName);
                    PullTile.pullTile(plugin.getCLIJ2(), imp, (ClearCLBuffer) newArgs[a], tileX, tileY, tileZ, tileWidth, tileHeight, tileDepth, marginWidth, marginHeight, marginDepth);
                }
                plugin.getCLIJ2().release((ClearCLBuffer) newArgs[a]);
            }
        }

        System.out.println("Processing a tile on " + plugin.getCLIJ2().getGPUName() + " took " + (System.currentTimeMillis() - time) + " ms.");
        finished = true;
    }

    public AbstractCLIJ2Plugin getPlugin() {
        return plugin;
    }

    public boolean isFinished() {
        return finished;
    }
}
