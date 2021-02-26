package net.haesleinhuepf.clijx.plugins;

import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij2.CLIJ2;

import java.util.HashMap;
import java.util.Map;

/**
 * Calculate the pixel wise eigenvalues of the hessian matrices in a 2d image.
 */
public class HessianEigenvalues2D {

	public static void hessian2d(CLIJ2 clij2,
			ClearCLBuffer inputCl,
			ClearCLBuffer small_eigenvalue,
			ClearCLBuffer large_eigenvalue)
	{
		Map< String, Object > parameters = new HashMap<>();
		parameters.put("src", inputCl);
		parameters.put("small_eigenvalue", small_eigenvalue);
		parameters.put("large_eigenvalue", large_eigenvalue);
		long[] dimensions = inputCl.getDimensions();
		clij2.execute(HessianEigenvalues2D.class, "hessian_eigenvalues_2d.cl", "hessian_eigenvalues_2d",
				dimensions, dimensions, parameters);
	}
}
