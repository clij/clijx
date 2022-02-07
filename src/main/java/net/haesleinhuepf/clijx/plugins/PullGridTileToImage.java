/**
 * 
 */
package net.haesleinhuepf.clijx.plugins;

import org.scijava.plugin.Plugin;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.process.Blitter;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.haesleinhuepf.clij.macro.CLIJMacroPlugin;
import net.haesleinhuepf.clij.macro.CLIJOpenCLProcessor;
import net.haesleinhuepf.clij.macro.documentation.OffersDocumentation;
import net.haesleinhuepf.clij2.AbstractCLIJ2Plugin;
import net.haesleinhuepf.clij2.CLIJ2;
import net.haesleinhuepf.clij2.plugins.AverageNeighborDistanceMap;
import net.haesleinhuepf.clij2.plugins.ConnectedComponentsLabelingDiamond;
import net.haesleinhuepf.clij2.plugins.ExcludeLabelsOnEdges;
import net.haesleinhuepf.clij2.plugins.ExtendLabelingViaVoronoi;
import net.haesleinhuepf.clij2.plugins.Mask;
import net.haesleinhuepf.clij2.utilities.HasAuthor;

/**
 * @author 	Jan Brocher (BioVoxxel)
 * 			January 2022
 *
 */

@Plugin(type = CLIJMacroPlugin.class, name = "CLIJ2_pullGridTileToImage")
public class PullGridTileToImage  extends AbstractCLIJ2Plugin implements CLIJMacroPlugin, CLIJOpenCLProcessor, OffersDocumentation, HasAuthor {

	
	public boolean executeCL() {

		ClearCLBuffer gridTile = (ClearCLBuffer) args[0];
		String imageName = (String) args[1];
		
		ImagePlus imp = WindowManager.getImage(imageName);
		
		if (imp == null) {
		    throw new IllegalArgumentException("You tried to push the image '" + args[0] + "' to the GPU.\n" +
		            "However, this image doesn't exist.");
		}
		
		int tileCountX = asInteger(args[2]);
		int tileCountY = asInteger(args[3]);
		int tileCountZ = asInteger(args[4]);
		int tileX = asInteger(args[5]);
		int tileY = asInteger(args[6]);
		int tileZ = asInteger(args[7]);
		float percentageOverlap = asFloat(args[8]);
		
		int fusionMode = asInteger(args[9]);
				
		pullGridTileToImage(getCLIJ2(), gridTile, imp, tileCountX, tileCountY, tileCountZ, tileX, tileY, tileZ, percentageOverlap, fusionMode);
		
		return true;
	}

	/**
	 * 
	 * @param clij2 - CLIJ2 instance
	 * @param gridTile - ClearCLBuffer instance holding the grid tile image on GPU
	 * @param imp - the destination ImagePlus which should have the same dimensions as the image the tile was derived from. 
	 * @param tileX - tile identifier in x direction (0....n)
	 * @param tileY - tile identifier in y direction (0....n)
	 * @param tileZ - tile (stack-block) identifier in z direction (0....n)
	 * @param percentageOverlap - tile overlap in percent (0 - 100)
	 * @param fusionMode - the {@link ij.process.Blitter} interface can be used to specify the fusionMode input such as <i>Blitter.COPY</i>
	 */
	public static void pullGridTileToImage(CLIJ2 clij2, ClearCLBuffer gridTile, ImagePlus imp, Integer tileCountX, Integer tileCountY, Integer tileCountZ, Integer tileX, Integer tileY, Integer tileZ, Float percentageOverlap, Integer fusionMode) {
		
		if (imp == null) {
			imp = WindowManager.getCurrentImage();
			if (imp == null) {
				throw new NullPointerException("The image you specified as target is not existing or there is no open image available.");
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
		
		long[] tileDimensions = gridTile.getDimensions();
		
		int tileWidth = (int) tileDimensions[0];
		int tileHeight = (int) tileDimensions[1];
		int tileDepth;
		if (tileDimensions.length >= 3) {
			tileDepth = (int) tileDimensions[2];
		} else {
			tileDepth = 1;
			tileCountZ = 1;
			tileZ = 0;
		}
		
		
				
		int baseTileWidth = getTileSize(tileCountX, 0, nonOverlapFactor, imp.getWidth());
		int baseTileHeight = getTileSize(tileCountY, 0, nonOverlapFactor, imp.getHeight());
		int baseTileDepth = getTileSize(tileCountZ, 0, nonOverlapFactor, imp.getNSlices());
	
		int x_overlap = (int) Math.ceil(baseTileWidth * overlapFactor);
		int y_overlap = (int) Math.ceil(baseTileHeight * overlapFactor);
		int z_overlap = (int) Math.ceil(baseTileDepth * overlapFactor);
		
		
		ImageStack destinationStack = imp.getImageStack();
		
		for (int slice = 1; slice <= tileDepth; slice++) {
			
			ImagePlus gridTileImagePlus = clij2.pull(gridTile);
			
			int xLoc = tileX * baseTileWidth - tileX * x_overlap;
			int yLoc = tileY * baseTileHeight - tileY * y_overlap;
			int zLoc = tileZ * baseTileDepth - tileZ * z_overlap + slice;
			
			System.out.println("xLoc = " + xLoc);
			System.out.println("yLoc = " + yLoc);
			System.out.println("zLoc = " + zLoc);
			
			destinationStack.getProcessor(zLoc).copyBits(gridTileImagePlus.getStack().getProcessor(slice), xLoc, yLoc, fusionMode);
		}
		
		imp.resetDisplayRange();
		
		System.out.println("Target image = " + imp);
		System.out.println("baseTileWidth = " + baseTileWidth);
		System.out.println("baseTileHeight = " + baseTileHeight);
		System.out.println("baseTileDepth = " + baseTileDepth);
		System.out.println("tileWidth = " + tileWidth);
		System.out.println("tileHeight = " + tileHeight);
		System.out.println("tileDepth = " + tileDepth);
		System.out.println("x_overlap = " + x_overlap);
		System.out.println("y_overlap = " + y_overlap);
		System.out.println("z_overlap = " + z_overlap);
		System.out.println("effective x_overlap = " + (100f / tileWidth * x_overlap));
		System.out.println("effective y_overlap = " + (100f / tileHeight * y_overlap));
		System.out.println("effective z_overlap = " + (100f / tileDepth * z_overlap));
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
	
	
	public String getDescription() {
		
		return "Pulles a grid tile image (uploaded before with the pushGridTile command) from GPU and fuses it at the specified position into the destination image"
				+ "Possible blending methods are specified with integers according to the following list:\n"
				+ " 0 --> COPY\n"
				+ " 1 --> COPY_INVERTED\n"
				+ " 2 --> COPY_TRANSPARENT\n"
				+ " 3 --> ADD\n"
				+ " 4 --> SUBTRACT\n"
				+ " 5 --> MULTIPLY\n"
				+ " 6 --> DIVIDE\n"
				+ " 7 --> AVERAGE\n"
				+ " 8 --> DIFFERENCE\n"
				+ " 9 --> AND\n"
				+ "10 --> OR\n"
				+ "11 --> XOR\n"
				+ "12 --> MIN\n"
				+ "13 --> MAX\n"
				+ "14 --> COPY_ZERO_TRANSPARENT\n"
				+ "\n"
				+ "Blending modes can also be checked out under: {@link ij.process.Blitter}";
	}

	
	public String getAvailableForDimensions() {
		return "2D, 3D";
	}


	public String getParameterHelpText() {	
		return "Image gridTile, String destination_image_name, Number tileCountX, Number tileCountY, Number tileCountZ, Number tileX, Number tileY, Number tileZ, Number percentageOverlap, Number fusionMode";
	}
	
	public String getAuthorName() {
		return "Jan Brocher";
	}

	
	//Test plugin
		public static void main(String[] args) {
			
			CLIJ2 clij2 = CLIJ2.getInstance();
			clij2.clear();
			
			IJ.run("Particles");
			//IJ.open("path/to/image.file");
			ImagePlus input_image = WindowManager.getCurrentImage();
			
			IJ.newImage("TargetImage", "32-bit black", input_image.getWidth(), input_image.getHeight(), input_image.getNSlices());
			ImagePlus target_image = WindowManager.getCurrentImage();
			
			
			int x_grid = 4;
			int y_grid = 2;
			int z_grid = 2;
			float overlapPercentage = 40f;
			int fusionMethod = Blitter.COPY_ZERO_TRANSPARENT;
			for (int z = 0; z < z_grid; z++) {
				for (int y = 0; y < y_grid; y++) {
					for (int x = 0; x < x_grid; x++) {
					
						ClearCLBuffer currentTile = PushGridTile.pushGridTile(clij2, input_image, x_grid, y_grid, z_grid, x, y, z, overlapPercentage);
						ClearCLBuffer ccl = clij2.create(currentTile.getDimensions(), NativeTypeEnum.Float);
						
						ConnectedComponentsLabelingDiamond.connectedComponentsLabelingDiamond(clij2, currentTile, ccl);
						currentTile.close();
						
						ClearCLBuffer voronoi = clij2.create(ccl);
						ExtendLabelingViaVoronoi.extendLabelingViaVoronoi(clij2, ccl, voronoi);
						ccl.close();
						
						ClearCLBuffer no_edges = clij2.create(voronoi);
						ExcludeLabelsOnEdges.excludeLabelsOnEdges(clij2, voronoi, no_edges);
						
						ClearCLBuffer neighbors = clij2.create(voronoi);
						AverageNeighborDistanceMap.averageNeighborDistanceMap(clij2, voronoi, neighbors);
						voronoi.close();
						
						ClearCLBuffer masked_neighbors = clij2.create(neighbors);
						Mask.mask(clij2, neighbors, no_edges, masked_neighbors);
						neighbors.close();
						no_edges.close();
						
						PullGridTileToImage.pullGridTileToImage(clij2, masked_neighbors, target_image, x_grid, y_grid, z_grid, x, y, z, overlapPercentage, fusionMethod);
						masked_neighbors.close();
					}
				}
			}
			
			clij2.clear();
			clij2.close();
			target_image.show();
			
		}

}
