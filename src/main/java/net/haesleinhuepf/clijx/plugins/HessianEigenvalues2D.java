package net.haesleinhuepf.clijx.plugins;

import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.macro.CLIJMacroPlugin;
import net.haesleinhuepf.clij.macro.CLIJOpenCLProcessor;
import net.haesleinhuepf.clij.macro.documentation.OffersDocumentation;
import net.haesleinhuepf.clij2.AbstractCLIJ2Plugin;
import net.haesleinhuepf.clij2.CLIJ2;
import net.haesleinhuepf.clij2.utilities.HasAuthor;
import net.haesleinhuepf.clij2.utilities.HasClassifiedInputOutput;
import org.scijava.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

/**
 * Calculate the pixel wise eigenvalues of the hessian matrices in a 2d image.
 */
@Plugin(type = CLIJMacroPlugin.class, name = "CLIJx_hessianEigenvalues2D")
public class HessianEigenvalues2D extends AbstractCLIJ2Plugin implements CLIJMacroPlugin, CLIJOpenCLProcessor, OffersDocumentation, HasClassifiedInputOutput, HasAuthor {

	public static boolean hessianEigenvalues2D(CLIJ2 clij2,
											ClearCLBuffer input,
											ClearCLBuffer small_eigenvalue,
											ClearCLBuffer large_eigenvalue)
	{
		Map< String, Object > parameters = new HashMap<>();
		parameters.put("src", input);
		parameters.put("small_eigenvalue", small_eigenvalue);
		parameters.put("large_eigenvalue", large_eigenvalue);
		long[] dimensions = input.getDimensions();
		clij2.execute(HessianEigenvalues2D.class, "hessian_eigenvalues_2d.cl", "hessian_eigenvalues_2d",
				dimensions, dimensions, parameters);

		return true;
	}

	@Override
	public String getParameterHelpText() {
		return "Image input, ByRef Image small_eigenvalue_destination, ByRef Image large_eigenvalue_destination";
	}

	@Override
	public boolean executeCL() {
		ClearCLBuffer input = (ClearCLBuffer) args[0];
		ClearCLBuffer small_eigenvalue = (ClearCLBuffer) args[1];
		ClearCLBuffer large_eigenvalue = (ClearCLBuffer) args[2];
		return hessianEigenvalues2D(getCLIJ2(), input, small_eigenvalue, large_eigenvalue);
	}

	@Override
	public String getDescription() {
		return "Computes the eigenvalues of the hessian matrix of a 2d image.\n" +
				"\n" +
				"  Hessian matrix:\n" +
				"    [Ixx, Ixy]\n" +
				"    [Ixy, Iyy]\n" +
				"  Where Ixx denotes the second derivative in x.\n" +
				"\n" +
				"  Ixx and Iyy are calculated by convolving the image with the 1d kernel [1 -2 1].\n" +
				"  Ixy is calculated by a convolution with the 2d kernel:\n" +
				"    [ 0.25 0 -0.25]\n" +
				"    [    0 0     0]\n" +
				"    [-0.25 0  0.25]";
	}

	@Override
	public String getAvailableForDimensions() {
		return "2D";
	}

	@Override
	public String getAuthorName() {
		return "Matthias Arzt";
	}

	@Override
	public String getInputType() {
		return "Image";
	}

	@Override
	public String getOutputType() {
		return "Image";
	}
}
