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

@Plugin(type = CLIJMacroPlugin.class, name = "CLIJx_localThresholdMedian")
public class LocalThresholdMedian extends AbstractCLIJxPlugin implements CLIJMacroPlugin, CLIJOpenCLProcessor, OffersDocumentation, HasAuthor, IsCategorized, HasClassifiedInputOutput {
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
        // from https://github.com/fiji/Auto_Local_Threshold/blob/master/src/main/java/fiji/threshold/Auto_Local_Threshold.java#L361
        // and https://homepages.inf.ed.ac.uk/rbf/HIPR2/adpthrsh.htm
        return new Object[]{null, null, 15, 7};
    }

    @Override
    public boolean executeCL() {
   	    	
    	CLIJx clijx = getCLIJx();
    	
        boolean result = localThresholdMedian(clijx, 
        											(ClearCLBuffer) (args[0]), 
        									  		(ClearCLBuffer) (args[1]),
        									  		asFloat(args[2]),
        									  		asFloat(args[3]));
        
        return result;
    }

    public static boolean localThresholdMedian(CLIJx clijx, 
    												ClearCLBuffer src, 
   													ClearCLBuffer dst,
   													float radius,
    												float c_value
    									)
    {
        assertDifferent(src, dst);

        ClearCLBuffer srcMedian = clijx.create(src.getDimensions(), clijx.Float);       

        if (src.getDimension() == 2) {
            clijx.median2DSphere(src, srcMedian,  radius, radius);
        } else {
            clijx.median3DSphere(src, srcMedian,  radius, radius, radius);
       }

        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("src", src);
        parameters.put("srcMedian", srcMedian);
        parameters.put("dst", dst);
        parameters.put("c_value", c_value);
                       
        
        clijx.execute(LocalThresholdMedian.class, "local_threshold_median.cl", "local_threshold_median", 
        		dst.getDimensions(), dst.getDimensions(), parameters);
 
        srcMedian.close();
         
        return true;
    }

    // The following information is retrieved from:
    // https://github.com/fiji/Auto_Local_Threshold/blob/master/src/main/java/fiji/threshold/Auto_Local_Threshold.java

	// See: Image Processing Learning Resourches HIPR2
	// http://homepages.inf.ed.ac.uk/rbf/HIPR2/adpthrsh.htm

    @Override
    public String getDescription() {
        return "Computes the local threshold based on \n" +
        		" Auto Local Threshold (Median method) see: https://imagej.net/Auto_Local_Threshold \n" +
				" see code in: \n" +
        		" https://github.com/fiji/Auto_Local_Threshold/blob/master/src/main/java/fiji/threshold/Auto_Local_Threshold.java \n" +
        		" Formular: \n" +
        		"<pre>if(value > (median - c_value)) value = 0 </pre>";

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
