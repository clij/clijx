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

@Plugin(type = CLIJMacroPlugin.class, name = "CLIJx_localThresholdMidGrey")
public class LocalThresholdMidGrey extends AbstractCLIJxPlugin implements CLIJMacroPlugin, CLIJOpenCLProcessor, OffersDocumentation, HasAuthor, IsCategorized, HasClassifiedInputOutput {
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
        return "Image source, ByRef Image destination, Number radius, Number c_value";
    }

    @Override
    public Object[] getDefaultValues() {
        return new Object[]{null, null, 15, 7};
    }

    @Override
    public boolean executeCL() {
   	    	
    	CLIJx clijx = getCLIJx();
    	
        boolean result = localThresholdMidGrey(clijx, 
        											(ClearCLBuffer) (args[0]), 
        									  		(ClearCLBuffer) (args[1]),
        									  		asFloat(args[2]),
        									  		asFloat(args[3]));
        
        return result;
    }

    public static boolean localThresholdMidGrey(CLIJx clijx, 
    												ClearCLBuffer src, 
   													ClearCLBuffer dst,
   													float radius,
    												float c_value
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
        parameters.put("c_value", c_value);
        
        
        clijx.execute(LocalThresholdMidGrey.class, "local_threshold_midgrey.cl", "local_threshold_midgrey", 
        		dst.getDimensions(), dst.getDimensions(), parameters);
 
        srcMin.close();
        srcMax.close();
        
        return true;
    }

    // The following information is retrieved from:
    // https://github.com/fiji/Auto_Local_Threshold/blob/master/src/main/java/fiji/threshold/Auto_Local_Threshold.java

	// See: Image Processing Learning Resourches HIPR2
	// http://homepages.inf.ed.ac.uk/rbf/HIPR2/adpthrsh.htm

    @Override
    public String getDescription() {
        return "Computes the local threshold based on \n" +
        		" Auto Local Threshold (MidGrey method) see: https://imagej.net/Auto_Local_Threshold \n" +
				" see code in: \n" +
        		" https://github.com/fiji/Auto_Local_Threshold/blob/master/src/main/java/fiji/threshold/Auto_Local_Threshold.java \n" +
        		" Formular: \n" +
        		"<pre>if (value > ( (max + min)/2.0 - c_value) ) value = 0 </pre>";

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
