package net.haesleinhuepf.clijx.plugins;

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

import ij.IJ;

import java.util.HashMap;

/**
 * Author: @phaub (Peter Haub)
 * 03 2021
 */

@Plugin(type = CLIJMacroPlugin.class, name = "CLIJx_localThresholdContrast")
public class LocalThresholdContrast extends AbstractCLIJxPlugin implements CLIJMacroPlugin, CLIJOpenCLProcessor, OffersDocumentation, HasAuthor, IsCategorized, HasClassifiedInputOutput {
    @Override
    public String getInputType() {
        return "Image";
    }

    @Override
    public String getOutputType() {
        return "Binary Image";
    }

    @Override
    public String getParameterHelpText() {
        return "Image source, ByRef Image destination, Number radius";
    }

    @Override
    public Object[] getDefaultValues() {
        return new Object[]{null, null, 15};
    }

    @Override
    public boolean executeCL() {
   	    	
    	CLIJx clijx = getCLIJx();
    	
        boolean result = localThresholdContrast(clijx, 
        											(ClearCLBuffer) (args[0]), 
        									  		(ClearCLBuffer) (args[1]),
        									  		asFloat(args[2]));
        
        return result;
    }

    public static boolean localThresholdContrast(CLIJx clijx, 
    													ClearCLBuffer src, 
    													ClearCLBuffer dst,
    													float radius
    									)
    {
        assertDifferent(src, dst);

        ClearCLBuffer srcMin = clijx.create(src.getDimensions(), clijx.Float);       
        ClearCLBuffer srcMax = clijx.create(src.getDimensions(), clijx.Float);       

        if (src.getDimension() == 2) {
	        clijx.minimum2DSphere(src, srcMin, radius, radius);
	        clijx.maximum2DSphere(src, srcMax, radius, radius);
        } else {
	        clijx.minimum3DSphere(src, srcMin, radius, radius, radius);
	        clijx.maximum3DSphere(src, srcMax, radius, radius, radius);
        }

        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("src", src);
        parameters.put("srcMin", srcMin);
        parameters.put("srcMax", srcMax);
        parameters.put("dst", dst);
                
        clijx.execute(LocalThresholdContrast.class, "local_threshold_contrast.cl", "local_threshold_contrast", 
        		dst.getDimensions(), dst.getDimensions(), parameters);
        
        srcMin.close();
        srcMax.close();
         
        return true;
    }


    // The following information is retrieved from:
    // https://github.com/fiji/Auto_Local_Threshold/blob/master/src/main/java/fiji/threshold/Auto_Local_Threshold.java

	// G. Landini, 2013
	// Based on a simple contrast toggle. This procedure does not have user-provided parameters other than the kernel radius
	// Sets the pixel value to either white or black depending on whether its current value is closest to the local Max or Min respectively
	// The procedure is similar to Toggle Contrast Enhancement (see Soille, Morphological Image Analysis (2004), p. 259

    @Override
    public String getDescription() {
        return "Computes the local threshold based on \n" +
        		" Auto Local Threshold (Contrast method) see: https://imagej.net/Auto_Local_Threshold \n" +
				" see code in: \n" +
        		" https://github.com/fiji/Auto_Local_Threshold/blob/master/src/main/java/fiji/threshold/Auto_Local_Threshold.java \n" +
        		" Formular: \n" +
        		"<pre>if (abs(value - min) >= abs(max - value) && (value != 0)) value = 0 </pre>";
    }

    @Override
    public String getAvailableForDimensions() {
        return "2D, 3D";
    }

    @Override
    public String getCategories() {
        return "Filter";
    }
    
    @Override
    public String getAuthorName() {
        return "Peter Haub (based on work by G. Landini and Fiji developers)";
    }

    
}
