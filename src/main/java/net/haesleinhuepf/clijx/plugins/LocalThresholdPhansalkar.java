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
 * Author: @phaub (Peter Haub),
 *         @haesleinhuepf (Robert Haase)
 * 03 2021
 */

@Plugin(type = CLIJMacroPlugin.class, name = "CLIJx_localThresholdPhansalkar")
public class LocalThresholdPhansalkar extends AbstractCLIJxPlugin implements CLIJMacroPlugin, CLIJOpenCLProcessor, OffersDocumentation, HasAuthor, IsCategorized, HasClassifiedInputOutput {
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
        return "Image source, ByRef Image destination, Number radius, Number k, Number r";
    }

    @Override
    public Object[] getDefaultValues() {
        return new Object[]{null, null, 15, 0.25, 0.5};
    }

    @Override
    public boolean executeCL() {
   	    	
    	CLIJx clijx = getCLIJx();

        boolean result = localThresholdPhansalkar(clijx, (ClearCLBuffer) (args[0]),
        									  			(ClearCLBuffer) (args[1]),
        									  			asFloat(args[2]),
        									  			asFloat(args[3]),
        									  			asFloat(args[4]));

        return result;
    }

    public static boolean localThresholdPhansalkar(CLIJx clijx, ClearCLBuffer src,
   													ClearCLBuffer dst,
   													float radius,
    												float k,
    												float r
    									) {
        assertDifferent(src, dst);

        // Set to default if params = 0
        if (k == 0) {
            System.out.println("Warning: localThresholdPhansalkarFast k is overwritten with 0.25 ");
            k = 0.25f;
        }
      	if (r == 0) {
            System.out.println("Warning: localThresholdPhansalkarFast r is overwritten with 0.25 ");
            r = 0.5f;
        }

        ClearCLBuffer srcNorm = clijx.create(src.getDimensions(), clijx.Float);
        ClearCLBuffer srcMean = clijx.create(src.getDimensions(), clijx.Float);
        ClearCLBuffer srcSqr = clijx.create(src.getDimensions(), clijx.Float);
        ClearCLBuffer srcSqrMean = clijx.create(src.getDimensions(), clijx.Float);

        clijx.multiplyImageAndScalar(src, srcNorm, 1.0/255);
        clijx.power(srcNorm, srcSqr, 2);
        if (src.getDimension() == 2) {
            clijx.mean2DSphere(srcSqr, srcSqrMean, radius, radius);
            clijx.mean2DSphere(srcNorm, srcMean, radius, radius);
        } else {
            clijx.mean3DSphere(srcSqr, srcSqrMean, radius, radius, radius);
            clijx.mean3DSphere(srcNorm, srcMean, radius, radius, radius);
        }
        srcSqr.close();

        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("src", srcNorm);
        parameters.put("srcMean", srcMean);
        parameters.put("srcSqrMean", srcSqrMean);
        parameters.put("dst", dst);
        parameters.put("k", k);
        parameters.put("r", r);
        
        clijx.execute(LocalThresholdPhansalkar.class, "local_threshold_phansalkar_x.cl", "local_threshold_phansalkar",
        		dst.getDimensions(), dst.getDimensions(), parameters);
 
        srcNorm.close();
        srcMean.close();
        srcSqrMean.close();
         
        return true;
    }


    @Override
    public String getDescription() {
        return "Computes the local threshold (Fast version) based on \n" +
        		" Auto Local Threshold (Phansalkar method) see: https://imagej.net/Auto_Local_Threshold \n" +
        		" see code in: \n" +
        		" https://github.com/fiji/Auto_Local_Threshold/blob/c955dc18cff58ac61df82f3f001799f7ffaec5cb/src/main/java/fiji/threshold/Auto_Local_Threshold.java#L636 \n" +
                //" The version here has been adapted to use normalization my multiplying the image with 1.0 / max_intensity instead of 1.0/255. \n" +
                " Formulary: \n" +
        		"<pre>t = mean * (1 + p * exp(-q * mean) + k * ((stdev / r) - 1))</pre>";
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
        return "Peter Haub, Robert Haase";
    }


}
