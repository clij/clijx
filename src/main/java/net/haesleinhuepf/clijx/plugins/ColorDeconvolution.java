package net.haesleinhuepf.clijx.plugins;

import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
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

import java.nio.FloatBuffer;
import java.util.HashMap;

/**
 * Author: @phaub (Peter Haub)
 * 03 2021
 */

@Plugin(type = CLIJMacroPlugin.class, name = "CLIJx_colorDeconvolution")
public class ColorDeconvolution extends AbstractCLIJxPlugin implements CLIJMacroPlugin, CLIJOpenCLProcessor, OffersDocumentation, HasAuthor, IsCategorized, HasClassifiedInputOutput {
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
        return "Image source, ByRef Image destination, Image srccolorvectors";
    }
    
    @Override
    public boolean executeCL() {
   	    	
    	CLIJx clijx = getCLIJx();
    	
        boolean result = colorDeconvolution(clijx, 
        											(ClearCLBuffer) (args[0]), 
        									  		(ClearCLBuffer) (args[1]),
        									  		(ClearCLBuffer) (args[2]));
        										
        return result;
    }

    
    public static boolean colorDeconvolution(CLIJx clijx, 
    												ClearCLBuffer src, 
   													ClearCLBuffer dst,
   													ClearCLBuffer srccv
    									)
    {
        assertDifferent(src, dst);

        // get srccv (color vectors) as float array
        long size = srccv.getLength();
        float[] cv = new float[(int) size];
        FloatBuffer buffer = FloatBuffer.wrap(cv);
        srccv.writeTo(buffer, 0, size, true);
        
      	// Color vectors matrix A 
      	//        AR1, AR2, AR3
      	//  A  =  AG1, AG2, AG3
      	//        AB1, AB2, AB3
      	//         0    1    2
      	//         3    4    5
      	//         6    7    8
        float detA = cv[0]*(cv[4]*cv[8] - cv[5]*cv[7]) - cv[1]*(cv[3]*cv[8] - cv[6]*cv[5]) + cv[2]*(cv[3]*cv[7] - cv[6]*cv[4]);
        
        float[] lognormxArray = new float[256];
        lognormxArray[0] = 5.0f;   // set to ~ 2 * -Math.log10(1/255.0) = 2.40654
        for (int i=1; i<lognormxArray.length; i++)
        	lognormxArray[i] = (float) -Math.log10(i/255.0);

        ClearCLBuffer lognormx = clijx.create(new long[]{lognormxArray.length, 1, 1}, NativeTypeEnum.Float);
        FloatBuffer buffer2 = FloatBuffer.wrap(lognormxArray);
        lognormx.readFrom(buffer2, true);
        
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("src", src);
        parameters.put("dst", dst);
        parameters.put("cv", srccv);
        parameters.put("lognormx", lognormx);
        parameters.put("detA", detA);
        
        clijx.execute(ColorDeconvolution.class, "color_deconvolution.cl", "color_deconvolution", 
        		dst.getDimensions(), dst.getDimensions(), parameters);
 
        return true;
    }


    @Override
    public String getDescription() {
        return "Computes the color deconvolution of an 8bit RGB stack color image \n" +
        		" with a given 3x3 matrix of color vectors.\n" +
        		" (Image has to be pushed as float32 stack.)\n\n" +
				" Additional information see Supplementary Information to: \n\n" +

    			" Haub, P., Meckel, T. A Model based Survey of Colour Deconvolution in \n" + 
    			" Diagnostic Brightfield Microscopy: Error Estimation and Spectral Consideration. \n"+
    			" Sci Rep 5, 12096 (2015). https://doi.org/10.1038/srep12096 \n";
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
        return "Peter Haub";
    }

    
}
