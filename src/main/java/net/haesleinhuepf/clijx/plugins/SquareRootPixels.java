package net.haesleinhuepf.clijx.plugins;

//import net.haesleinhuepf.clij.CLIJ;
//import net.haesleinhuepf.clij.clearcl.interfaces.ClearCLImageInterface;
//import net.haesleinhuepf.clij2.AbstractCLIJ2Plugin;
//import net.haesleinhuepf.clij2.CLIJ2;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.macro.CLIJMacroPlugin;
import net.haesleinhuepf.clij.macro.CLIJOpenCLProcessor;
import net.haesleinhuepf.clij.macro.documentation.OffersDocumentation;
import net.haesleinhuepf.clij2.utilities.HasAuthor;
import net.haesleinhuepf.clij2.utilities.HasClassifiedInputOutput;
import net.haesleinhuepf.clij2.utilities.IsCategorized;
import net.haesleinhuepf.clijx.CLIJx;
import net.haesleinhuepf.clijx.utilities.AbstractCLIJxPlugin;
import static net.haesleinhuepf.clij.utilities.CLIJUtilities.assertDifferent;
import org.scijava.plugin.Plugin;
import java.util.HashMap;

/**
 * Author: @phaub (Peter Haub)
 * 03 2021
 */

@Plugin(type = CLIJMacroPlugin.class, name = "CLIJ2_squareRootPixels")
public class SquareRootPixels extends AbstractCLIJxPlugin implements CLIJMacroPlugin, CLIJOpenCLProcessor, OffersDocumentation, HasAuthor, IsCategorized, HasClassifiedInputOutput {
    @Override
    public String getInputType() {
        return "Image";
    }

    @Override
    public String getOutputType() {
        return "Image";
    }

    @Override
    public String getParameterHelpText() {
        return "Image source, ByRef Image destination";
    }

    @Override
    public boolean executeCL() {
   	    	
    	CLIJx clijx = getCLIJx();
    	
        boolean result = squareRootPixels(clijx, (ClearCLBuffer) (args[0]), 
        									  (ClearCLBuffer) (args[1]));
        										
        return result;
    }

    public static boolean squareRootPixels(CLIJx clijx, ClearCLBuffer src, 
   													ClearCLBuffer dst
    									)
    {
        assertDifferent(src, dst); 

        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("src", src);
        parameters.put("dst", dst);
        
        clijx.execute(SquareRootPixels.class, "squareroot_2d.cl", "squareroot_2d", 
        		dst.getDimensions(), dst.getDimensions(), parameters);
 
        return true;
    }


    @Override
    public String getDescription() {
        return "Computes the square root image";
    }

    @Override
    public String getAvailableForDimensions() {
        return "2D";
    }

    @Override
    public String getCategories() {
        return "Filter";
    }
    
    @Override
    public String getAuthorName() {
        return "Peter Haub";
    }

    
}
