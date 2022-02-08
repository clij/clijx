
package net.haesleinhuepf.clijx.plugins;

import org.scijava.plugin.Plugin;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.macro.CLIJHandler;
import net.haesleinhuepf.clij.macro.CLIJMacroPlugin;
import net.haesleinhuepf.clij.macro.CLIJOpenCLProcessor;
import net.haesleinhuepf.clij.macro.documentation.OffersDocumentation;
import net.haesleinhuepf.clij2.AbstractCLIJ2Plugin;
import net.haesleinhuepf.clij2.CLIJ2;
import net.haesleinhuepf.clij2.utilities.HasAuthor;


/**
 * @author 	Jan Brocher (BioVoxxel)
 * 			January 2022
 *
 */

@Plugin(type = CLIJMacroPlugin.class, name = "CLIJ2_pushGridTile")
public class PushGridTile extends AbstractCLIJ2Plugin implements CLIJMacroPlugin, CLIJOpenCLProcessor, OffersDocumentation, HasAuthor {

		
	public boolean executeCL() {
		
		String imageName = (String) args[0];
		
		ImagePlus imp = WindowManager.getImage(imageName);
		
		if (imp == null) {
		    throw new IllegalArgumentException("You tried to push the image '" + args[0] + "' to the GPU.\n" +
		            "However, this image doesn't exist.");
		}
		
		int tileCountX = asInteger(args[1]);
		int tileCountY = asInteger(args[2]);
		int tileCountZ = asInteger(args[3]);
		int tileX = asInteger(args[4]);
		int tileY = asInteger(args[5]);
		int tileZ = asInteger(args[6]);
		float percentageOverlap = asFloat(args[7]);
					
		pushGridTile(getCLIJ2(), imp, imageName, tileCountX, tileCountY, tileCountZ, tileX, tileY, tileZ, percentageOverlap);
				
		return true;
	}

	/**
	 * Method called via Macros
	 */
	public static void pushGridTile(CLIJ2 clij2, ImagePlus imp, String imageName, Integer tileCountX, Integer tileCountY, Integer tileCountZ, Integer tileX, Integer tileY, Integer tileZ, Float percentageOverlap) {
		ClearCLBuffer buffer = pushGridTile(clij2, imp, tileCountX, tileCountY, tileCountZ, tileX, tileY, tileZ, percentageOverlap);
		CLIJHandler.getInstance().pushInternal(buffer, imageName);
	}
	
	/**
	 * 
	 * @param clij2 - CLIJ2 instance
	 * @param imp - Source image
	 * @param tileCountX - tile number in x-direction (1...n)
	 * @param tileCountY - tile number in y-direction (1...n)
	 * @param tileCountZ - tile-blocks number in z-direction (1...n)
	 * @param tileX - tile identifier in x direction (0....n)
	 * @param tileY - tile identifier in y direction (0....n)
	 * @param tileZ - tile (stack-block) identifier in z direction (0....n)
	 * @param percentageOverlap - tile overlap in percent (0 - 100)
	 * @return ClearCLBuffer
	 */
	public static ClearCLBuffer pushGridTile(CLIJ2 clij2, ImagePlus imp, Integer tileCountX, Integer tileCountY, Integer tileCountZ, Integer tileX, Integer tileY, Integer tileZ, Float percentageOverlap) {
		
		
		if (imp == null) {
			imp = WindowManager.getCurrentImage();
			if (imp == null) {
				throw new NullPointerException("The image you specified is not existing or there is no open image available.");
			}
		}
		
		if (percentageOverlap < 0) {
			percentageOverlap = 0.0f;
		}
		
		if (percentageOverlap > 99) {
			percentageOverlap = 99.0f;
		}
		
		
		float overlapFactor =  percentageOverlap / 100f;
		float nonOverlapFactor = 1f - overlapFactor;
		
		int imageWidth = imp.getWidth();
		int imageHeight = imp.getHeight();
		int imageDepth = imp.getNSlices();
		
		tileCountX = Math.max(1, tileCountX);
		tileCountY = Math.max(1, tileCountY);
		tileCountZ = Math.max(1, tileCountZ);
		
		
		if (tileCountZ > imageDepth) {
			tileCountZ = imageDepth;
		}
		
		if (imageDepth == 1) {
			tileZ = 0;
		}

		int tileWidth = getTileSize(tileCountX, tileX, nonOverlapFactor, imageWidth);
		int tileHeight = getTileSize(tileCountY, tileY, nonOverlapFactor, imageHeight);
		int tileDepth = getTileSize(tileCountZ, tileZ, nonOverlapFactor, imageDepth);
		
		int baseTileWidth = getTileSize(tileCountX, 0, nonOverlapFactor, imageWidth);
		int baseTileHeight = getTileSize(tileCountY, 0, nonOverlapFactor, imageHeight);;
		int baseTileDepth = getTileSize(tileCountZ, 0, nonOverlapFactor, imageDepth);
		
		
		int x_overlap = (int) Math.ceil(baseTileWidth * overlapFactor);
		int y_overlap = (int) Math.ceil(baseTileHeight * overlapFactor);
		int z_overlap = (int) Math.ceil(baseTileDepth * overlapFactor);
		
		
		int xLoc = tileX * baseTileWidth - tileX * x_overlap;
		int yLoc = tileY * baseTileHeight - tileY * y_overlap;
		int zLoc = tileZ * baseTileDepth - tileZ * z_overlap;
		
		
		ImageStack tileStack = imp.getImageStack().crop(xLoc, yLoc, zLoc, tileWidth, tileHeight, tileDepth);
		
		ImagePlus tileImagePlus = new ImagePlus("current_tile", tileStack);
		
		ClearCLBuffer buffer = clij2.push(tileImagePlus);
		
		System.out.println("---> Processing tile = " + tileX + " / " + tileY + " / " + tileZ);
		System.out.println("Working on image = " + imp);
		System.out.println("tileWidth * overlapFactor = " + baseTileWidth * overlapFactor);
		System.out.println("x_overlap = " + x_overlap);
		System.out.println("tileHeight * overlapFactor = " + baseTileHeight * overlapFactor);
		System.out.println("y_overlap = " + y_overlap);
		System.out.println("tileDepth * overlapFactor = " + baseTileDepth * overlapFactor);
		System.out.println("z_overlap = " + z_overlap);
		System.out.println("xLoc = " + xLoc);
		System.out.println("yLoc = " + yLoc);
		System.out.println("zLoc = " + zLoc);
		System.out.println("Pushing " + tileImagePlus + " to GPU");
		System.out.println(buffer);
		
		return buffer;
	}


	private static int getTileSize(Integer gridTileCount, Integer tilePositionID, float nonOverlapFactor, int imageSize) {
		int tileSize = (int) Math.floor(imageSize / (1 + (gridTileCount - 1) * nonOverlapFactor));
		//in case the current tile is the last one in this row / column / slice-block use the rest of the remaining image 
		if (tilePositionID == gridTileCount-1) {
			tileSize = (int) Math.floor(imageSize - (Math.floor(tileSize * nonOverlapFactor) * (gridTileCount - 1)));
		}
		//System.out.println("tileSize = " + tileSize);
		return tileSize;
	}
	
	
	
	@Override
    public ClearCLBuffer createOutputBufferFromSource(ClearCLBuffer input) {
		
		int imageWidth = (int) input.getWidth();
		int imageHeight = (int) input.getHeight();
		int imageDepth = (int) input.getDepth();
		
		float percentageOverlap = asFloat(args[7]);
		float overlapFactor =  percentageOverlap / 100f;
		float nonOverlapFactor = 1f - overlapFactor;
		
		int tileCountX = asInteger(args[1]);
		int tileCountY = asInteger(args[2]);
		int tileCountZ = asInteger(args[3]);
		int tileX = asInteger(args[4]);
		int tileY = asInteger(args[5]);
		int tileZ = asInteger(args[6]);
		
				
		int tileWidth = getTileSize(tileCountX, tileX, nonOverlapFactor, imageWidth);
		
		int tileHeight = getTileSize(tileCountY, tileY, nonOverlapFactor, imageHeight);
		
		int tileDepth = getTileSize(tileCountZ, tileZ, nonOverlapFactor, imageDepth);

        if (input.getDimension() == 2) {
            return getCLIJ2().create(new long[]{tileWidth, tileHeight}, input.getNativeType());
        } else {
            return getCLIJ2().create(new long[]{tileWidth, tileHeight, tileDepth}, input.getNativeType());
        }
    }
	
	

	public String getParameterHelpText() {
		return "String image, Number tileCountX, Number tileCountY, Number tileCountZ, Number tileX, Number tileY, Number tileZ, Number percentageOverlap";
	}

	


	public String getDescription() {
		return "Pushes a tile defined by its name and a grid specification (columns / rows / slice-blocks) together with a tile overlap percentage to GPU memory for further processing";
	}

	public String getAvailableForDimensions() {
		return "2D, 3D";
	}


	public String getAuthorName() {
		return "Jan Brocher";
	}

	//Test plugin
	public static void main(String[] args) {
	
		IJ.run("Boats");
		ImagePlus test_image = WindowManager.getCurrentImage();
		CLIJ2 clij2 = CLIJ2.getInstance();

		ClearCLBuffer test = clij2.push(test_image);
		ImagePlus imp = clij2.pull(test);
		imp.show();

		// Test starts here
		ClearCLBuffer tile = pushGridTile(clij2, imp, 4, 2, 1, 1, 1, 0, 40f);
		System.out.println(tile);

		clij2.print(tile);
		ImagePlus tileImp = clij2.pull(tile);
		tileImp.show();
	}	

	
}
