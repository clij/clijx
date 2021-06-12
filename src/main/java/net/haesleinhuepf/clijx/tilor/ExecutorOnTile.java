package net.haesleinhuepf.clijx.tilor;

import net.haesleinhuepf.clij.CLIJ;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.macro.CLIJOpenCLProcessor;
import net.haesleinhuepf.clij2.AbstractCLIJ2Plugin;
import net.haesleinhuepf.clij2.plugins.PullTile;
import net.haesleinhuepf.clij2.plugins.PushTile;

import java.util.HashMap;
public class ExecutorOnTile implements Runnable {

    private static Object pushPullMutex = new Object();

    private AbstractCLIJ2Plugin master;
    private AbstractCLIJ2Plugin client;
    private HashMap<String, ClearCLBuffer> parameters;
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

    ExecutorOnTile(AbstractCLIJ2Plugin master,
                   AbstractCLIJ2Plugin client,
                   HashMap<String, ClearCLBuffer> parameters,
                   Object[] args,
                   int tileX, int tileY, int tileZ,
                   int tileWidth,
                   int tileHeight,
                   int tileDepth,
                   int marginWidth,
                   int marginHeight,
                   int marginDepth
    ) {
        this.master = master;
        this.client = client;
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
        String[] argumentNames = client.getParameterHelpText().split(",");

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
                ClearCLBuffer wholeImage = parameters.get(parameterName);
                synchronized (pushPullMutex) {
                    //System.out.println("push " + parameterName);
                    ClearCLBuffer tileOnMaster = PushTile.pushTile(master.getCLIJ2(), wholeImage, tileX, tileY, tileZ, tileWidth, tileHeight, tileDepth, marginWidth, marginHeight, marginDepth);
                    ClearCLBuffer tileOnClient = client.getCLIJ2().transfer(tileOnMaster);
                    //client.getCLIJ2().show(tileOnClient, "tile on client");
                    master.getCLIJ2().release(tileOnMaster);
                    newArgs[a] = tileOnClient;
                    //System.out.println("args depth" + ((ClearCLBuffer)newArgs[a]).getDepth());
                }
            } else {
                newArgs[a] = args[a];
            }
        }

        client.setArgs(newArgs);
        if (client instanceof CLIJOpenCLProcessor) {
            ((CLIJOpenCLProcessor) client).executeCL();
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
                ClearCLBuffer wholeImage = parameters.get(parameterName);
                synchronized (pushPullMutex) {
                    ClearCLBuffer tileOnMaster = master.getCLIJ2().transfer((ClearCLBuffer) newArgs[a]);
                    PullTile.pullTile(master.getCLIJ2(), tileOnMaster, wholeImage, tileX, tileY, tileZ, tileWidth, tileHeight, tileDepth, marginWidth, marginHeight, marginDepth);
                    master.getCLIJ2().release(tileOnMaster);
                    client.getCLIJ2().release((ClearCLBuffer) newArgs[a]);
                }
                client.getCLIJ2().release((ClearCLBuffer) newArgs[a]);
            }
        }

        System.out.println("Processing a tile on " + client.getCLIJ2().getGPUName() + " took " + (System.currentTimeMillis() - time) + " ms.");
        finished = true;
    }

    public AbstractCLIJ2Plugin getPlugin() {
        return client;
    }

    public boolean isFinished() {
        return finished;
    }
}
