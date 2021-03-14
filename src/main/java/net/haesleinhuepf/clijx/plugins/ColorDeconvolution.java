package net.haesleinhuepf.clijx.plugins;

import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.haesleinhuepf.clij.macro.CLIJMacroPlugin;
import net.haesleinhuepf.clij.macro.CLIJOpenCLProcessor;
import net.haesleinhuepf.clij.macro.documentation.OffersDocumentation;
import net.haesleinhuepf.clij2.AbstractCLIJ2Plugin;
import net.haesleinhuepf.clij2.CLIJ2;
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
 * 03'2021
 */

@Plugin(type = CLIJMacroPlugin.class, name = "CLIJx_colorDeconvolution")
public class ColorDeconvolution extends AbstractCLIJ2Plugin implements CLIJMacroPlugin, CLIJOpenCLProcessor, OffersDocumentation, HasAuthor, IsCategorized, HasClassifiedInputOutput {
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
        return "Image source, Image color_vectors, ByRef Image destination";
    }
    
    @Override
    public boolean executeCL() {
   	    	
    	CLIJ2 clij2 = getCLIJ2();
    	
        boolean result = colorDeconvolution(clij2,
        											(ClearCLBuffer) (args[0]), 
        									  		(ClearCLBuffer) (args[1]),
        									  		(ClearCLBuffer) (args[2]));
        										
        return result;
    }

    
    public static boolean colorDeconvolution(CLIJ2 clij2,
    												ClearCLBuffer src,
                                                    ClearCLBuffer color_vectors_in,
   													ClearCLBuffer dst
    									)
    {
        assertDifferent(src, dst);

        ClearCLBuffer color_vectors = color_vectors_in;
        if (color_vectors.getNativeType() != NativeTypeEnum.Float) {
            color_vectors = clij2.create(color_vectors_in.getDimensions(), NativeTypeEnum.Float);
            clij2.copy(color_vectors_in, color_vectors);
        }

        // get color vectors as float array
        long size = color_vectors.getLength();
        float[] cv = new float[(int) size];
        FloatBuffer buffer = FloatBuffer.wrap(cv);
        color_vectors.writeTo(buffer, 0, size, true);
        
        float detA = cv[0] *(cv[4] *cv[8] - cv[5] * cv[7]) - cv[1] * (cv[3] * cv[8] - cv[6] * cv[5]) + cv[2] * (cv[3] * cv[7] - cv[6] * cv[4]);

        if (detA <= 0) {
            return false;
        }
        
        // Solve linear equation
      	// Color vectors matrix A 
      	//        AR1, AR2, AR3
      	//  A  =  AG1, AG2, AG3
      	//        AB1, AB2, AB3
      	//         0    1    2
      	//         3    4    5
      	//         6    7    8
      	// as float array {AR1, AR2, AR3, AG1, AG2, AG3, AB1, AB2, AB3}
      	//                  0,   1,   2,   3,   4,   5,   6,   7,   8
      	//  see Supplementary Information to
    	//	Haub, P., Meckel, T. A Model based Survey of Colour Deconvolution in Diagnostic Brightfield Microscopy:
    	//	Error Estimation and Spectral Consideration. Sci Rep 5, 12096 (2015). https://doi.org/10.1038/srep12096	

        //c1 = aR   *(cv[4]*cv[8] - cv[5]*cv[7]) - cv[1]*( aG*cv[8]   -   aB*cv[5] ) + cv[2]*( aG*cv[7]   -   aB*cv[4] );
        //c2 = cv[0]*( aG*cv[8]   -   aB*cv[5] ) - aR   *(cv[3]*cv[8] - cv[5]*cv[6]) + cv[2]*( aB*cv[3]   -   aG*cv[6] );
        //c3 = cv[0]*( aB*cv[4]   -   aG*cv[7] ) - cv[1]*( aB*cv[3]   -   aG*cv[6] ) + aR   *(cv[3]*cv[7] - cv[4]*cv[6]);
        // .. transforms to:
        //c1 = aR*(cv[4]*cv[8] - cv[5]*cv[7]) + aG*(cv[2]*cv[7] - cv[1]*cv[8]) + aB*(cv[1]*cv[5] - cv[2]*cv[4]) ;
        //c2 = aR*(cv[5]*cv[6] - cv[3]*cv[8]) + aG*(cv[0]* cv[8] - cv[2]*cv[6]) + aB*(cv[2]*cv[3] - cv[0]*cv[5])   ;
        //c3 = aR*(cv[3]*cv[7] - cv[4]*cv[6]) + aG*(cv[1]*cv[6] - cv[0]*cv[7])   + aB*(cv[0]* cv[4] - cv[1]* cv[3])    ;
        // .. transforms to:
        // Color vectors matrix A transformed to rotation matrix):
        float[] rot = new float[(int) size];
        rot[0] = cv[4] * cv[8] - cv[5] * cv[7];   rot[1] = cv[2] * cv[7] - cv[1] * cv[8];   rot[2] = cv[1] * cv[5] - cv[2] * cv[4];
        rot[3] = cv[5] * cv[6] - cv[3] * cv[8];   rot[4] = cv[0] * cv[8] - cv[2] * cv[6];   rot[5] = cv[2] * cv[3] - cv[0] * cv[5];
        rot[6] = cv[3] * cv[7] - cv[4] * cv[6];   rot[7] = cv[1] * cv[6] - cv[0] * cv[7];   rot[8] = cv[0] * cv[4] - cv[1] * cv[3];
        
        for (int i=0; i<rot.length; i++) {
            rot[i] /= detA;
        }
        
        ClearCLBuffer rotmat = clij2.create(new long[]{rot.length, 1, 1}, NativeTypeEnum.Float);
        FloatBuffer buffer2 = FloatBuffer.wrap(rot);
        rotmat.readFrom(buffer2, true);
        
      	
        float[] lognormxArray = new float[256];
        lognormxArray[0] = 5.0f;   // set to ~ 2 * -Math.log10(1/255.0) = 2.40654
        for (int i=1; i<lognormxArray.length; i++) {
            lognormxArray[i] = (float) -Math.log10(i / 255.0);
        }

        ClearCLBuffer lognormx = clij2.create(new long[]{lognormxArray.length, 1, 1}, NativeTypeEnum.Float);
        FloatBuffer buffer3 = FloatBuffer.wrap(lognormxArray);
        lognormx.readFrom(buffer3, true);
        
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("src", src);
        parameters.put("dst", dst);
        parameters.put("rotmat", rotmat);
        parameters.put("lognormx", lognormx);
        
        clij2.execute(ColorDeconvolution.class, "color_deconvolution.cl", "color_deconvolution",
        		dst.getDimensions(), dst.getDimensions(), parameters);

        if (color_vectors != color_vectors_in) {
            color_vectors.close();
        }
        lognormx.close();
        rotmat.close();

        return true;
    }


    @Override
    public String getDescription() {
        return "Computes the color deconvolution of an 8bit RGB stack color image \n" +
        		" with a given 3x3 matrix of color vectors.\n" +
        		" Note: The input image has to be a stack with three z-slices corresponding to the red, green and blue channel.)\n\n" +
				" Additional information see Supplementary Information to: \n\n" +

    			" Haub, P., Meckel, T. A Model based Survey of Colour Deconvolution in \n" + 
    			" Diagnostic Brightfield Microscopy: Error Estimation and Spectral Consideration. \n"+
    			" Sci Rep 5, 12096 (2015). https://doi.org/10.1038/srep12096 \n";
    }

    @Override
    public ClearCLBuffer createOutputBufferFromSource(ClearCLBuffer input)
    {
        return getCLIJ2().create(input.getDimensions(), NativeTypeEnum.Float);
    }

    @Override
    public String getAvailableForDimensions() {
        return "3D";
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
