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
import java.util.HashMap;

/**
 * Author: @phaub (Peter Haub)
 * 03 2021
 */

@Plugin(type = CLIJMacroPlugin.class, name = "CLIJ2_localThresholdPhansalkar")
public class LocalThresholdPhansalkar extends AbstractCLIJxPlugin implements CLIJMacroPlugin, CLIJOpenCLProcessor, OffersDocumentation, HasAuthor, IsCategorized, HasClassifiedInputOutput {
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
        return "Image source, ByRef Image destination, Number radius, Number k, Number r, Number whiteobjects, Number originalmode";
    }

    @Override
    public boolean executeCL() {
   	    	
    	CLIJx clijx = getCLIJx();
    	
        boolean result = localThresholdPhansalkar(clijx, (ClearCLBuffer) (args[0]), 
        									  			(ClearCLBuffer) (args[1]),
        									  			asFloat(args[2]),
        									  			asFloat(args[3]),
        									  			asFloat(args[4]),
        									  			asInteger(args[5]),
        									  			asInteger(args[6]));
        										
        return result;
    }

    public static boolean localThresholdPhansalkar(CLIJx clijx, ClearCLBuffer src, 
   													ClearCLBuffer dst,
   													float radius,
    												float k,
    												float r,
    												int whiteobjects,
    												int originalmode
    									)
    {
        assertDifferent(src, dst);

        ClearCLBuffer srcMean = clijx.create(src);       
        ClearCLBuffer srcSqr = clijx.create(src);       
        ClearCLBuffer srcSqrMean = clijx.create(src);  

        clijx.mean2DBox(src, srcMean,  radius, radius);
        clijx.squarePixels(src, srcSqr);
        clijx.mean2DBox(srcSqr, srcSqrMean,  radius, radius);
 

        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("src", src);
        parameters.put("srcMean", srcMean);
        parameters.put("srcSqrMean", srcSqrMean);
        parameters.put("dst", dst);
        parameters.put("k", k);
        parameters.put("r", r);
        parameters.put("whiteobjects", whiteobjects);
        parameters.put("originalmode", originalmode);
        
        clijx.execute(LocalThresholdPhansalkar.class, "localthresholdphansalkar.cl", "localthresholdphansalkar", 
        		dst.getDimensions(), dst.getDimensions(), parameters);
 
        srcMean.close();
        srcSqr.close();
        srcSqrMean.close();
         
        return true;
    }


    @Override
    public String getDescription() {
        return "Computes the local threshold (Fast version) based on \n" +
        		" Auto Local Threshold (Phansalkar method) see: https://imagej.net/Auto_Local_Threshold \n" +
        		" see code in: \n" +
        		" https://github.com/fiji/Auto_Local_Threshold/blob/c955dc18cff58ac61df82f3f001799f7ffaec5cb/src/main/java/fiji/threshold/Auto_Local_Threshold.java \n" +
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
        return "Peter Haub";
    }

    
}
