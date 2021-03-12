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

@Plugin(type = CLIJMacroPlugin.class, name = "CLIJx_localThresholdSauvola")
public class LocalThresholdSauvola extends AbstractCLIJxPlugin implements CLIJMacroPlugin, CLIJOpenCLProcessor, OffersDocumentation, HasAuthor, IsCategorized, HasClassifiedInputOutput {
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
        return "Image source, ByRef Image destination, Number radius, Number par1, Number par2";
    }

    @Override
    public Object[] getDefaultValues() {
        return new Object[]{null, null, 15, 0.5, 128.0};
    }

    @Override
    public boolean executeCL() {
   	    	
    	CLIJx clijx = getCLIJx();
    	
        boolean result = localThresholdSauvola(clijx, 
        											(ClearCLBuffer) (args[0]), 
        									  		(ClearCLBuffer) (args[1]),
        									  		asFloat(args[2]),
        									  		asFloat(args[3]),
        									  		asFloat(args[4]));
        
        return result;
    }

    public static boolean localThresholdSauvola(CLIJx clijx, 
    												ClearCLBuffer src, 
   													ClearCLBuffer dst,
   													float radius,
    												float k_value,
    												float r_value
    									)
    {
        assertDifferent(src, dst);

        // Set to default if params = 0
        if (k_value == 0) {
            System.out.println("Warning: localThresholdSauvola k_value is overwritten with 0.5 ");
        	k_value = 0.5f;
        }
        if (r_value == 0) {
            System.out.println("Warning: localThresholdSauvola r_value is overwritten with 128 ");
        	r_value = 128.0f;
        }
       
        ClearCLBuffer srcMean = clijx.create(src.getDimensions(), clijx.Float);       
        ClearCLBuffer srcSqr = clijx.create(src.getDimensions(), clijx.Float);       
        ClearCLBuffer srcSqrMean = clijx.create(src.getDimensions(), clijx.Float);  

       clijx.power(src, srcSqr, 2);
        if (src.getDimension() == 2) {
            clijx.mean2DSphere(srcSqr, srcSqrMean, radius, radius);
            clijx.mean2DSphere(src, srcMean, radius, radius);
        } else {
            clijx.mean3DSphere(srcSqr, srcSqrMean, radius, radius, radius);
            clijx.mean3DSphere(src, srcMean, radius, radius, radius);
        }
        srcSqr.close();

        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("src", src);
        parameters.put("srcMean", srcMean);
        parameters.put("srcSqrMean", srcSqrMean);
        parameters.put("dst", dst);
        parameters.put("k_value", k_value);
        parameters.put("r_value", r_value);
        
        clijx.execute(LocalThresholdSauvola.class, "local_threshold_sauvola.cl", "local_threshold_sauvola", 
        		dst.getDimensions(), dst.getDimensions(), parameters);
 
        srcMean.close();
        srcSqrMean.close();
         
        return true;
    }

    // The following information is retrievd from:
    // https://github.com/fiji/Auto_Local_Threshold/blob/master/src/main/java/fiji/threshold/Auto_Local_Threshold.java

	// Sauvola recommends K_VALUE = 0.5 and R_VALUE = 128.
	// This is a modification of Niblack's thresholding method.
	// Sauvola J. and Pietaksinen M. (2000) "Adaptive Document Image Binarization"
	// Pattern Recognition, 33(2): 225-236
	// http://www.ee.oulu.fi/mvg/publications/show_pdf.php?ID=24
	// Ported to ImageJ plugin from E Celebi's fourier_0.8 routines
	// This version uses a circular local window, instead of a rectagular one

    @Override
    public String getDescription() {
        return "Computes the local threshold based on \n" +
        		" Auto Local Threshold (Sauvola method) see: https://imagej.net/Auto_Local_Threshold \n" +
				" see code in: \n" +
        		" https://github.com/fiji/Auto_Local_Threshold/blob/master/src/main/java/fiji/threshold/Auto_Local_Threshold.java \n" +
        		" Formular: \n" +
        		"<pre>t = mean * (1.0 + k_value * (stddev / r_value - 1.0)) </pre>";

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
