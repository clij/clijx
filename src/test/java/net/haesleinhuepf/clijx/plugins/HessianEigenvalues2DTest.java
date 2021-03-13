package net.haesleinhuepf.clijx.plugins;

import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij2.CLIJ2;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HessianEigenvalues2DTest {

	@Test
	public void test() {
		Img< FloatType > input = ArrayImgs.floats(10, 10);
		LoopBuilder.setImages(Intervals.positions(input), input).forEachPixel( (pos, pixel) -> {
			long x = pos.getLongPosition(0);
			long y = pos.getLongPosition(1);
			pixel.setReal(x * x + 2 * y * y + 3 * x * y);
		});
		// The hessian matrix of this image is everywhere:
		//   [2, 3]
		//   [3, 4]
		CLIJ2 clij2 = CLIJ2.getInstance();
		try {
			ClearCLBuffer inputCl = clij2.push(input);
			ClearCLBuffer small_eigenvalue = clij2.create(inputCl);
			ClearCLBuffer large_eigenvalue = clij2.create(inputCl);
			HessianEigenvalues2D.hessianEigenvalues2D(clij2, inputCl, small_eigenvalue, large_eigenvalue);
			RandomAccessibleInterval<FloatType> small = clij2.pullRAI(small_eigenvalue);
			RandomAccessibleInterval<FloatType> large = clij2.pullRAI(large_eigenvalue);
			assertEquals(-0.16228f, small.getAt(1, 1).getRealFloat(), 0.001);
			assertEquals(6.162f, large.getAt(1, 1).getRealFloat(), 0.001);
		} finally {
			clij2.clear();
		}
	}

}
