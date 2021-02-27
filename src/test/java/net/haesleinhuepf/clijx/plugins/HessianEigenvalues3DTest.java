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

public class HessianEigenvalues3DTest {

	@Test
	public void test() {
		Img< FloatType > input = ArrayImgs.floats(10, 10, 10);
		LoopBuilder.setImages(Intervals.positions(input), input).forEachPixel((position, pixel) -> {
			double x = position.getDoublePosition(0);
			double y = position.getDoublePosition(1);
			double z = position.getDoublePosition(2);
			pixel.setReal( 0.5 * x * x + 2 * x * y + 3 * x * z + 2 * y * y + 5 * y * z + 3 * z * z);
		});
		// The hessian of the input image is:
		// [1 2 3]
		// [2 4 5]
		// [3 5 6]
		CLIJ2 clij2 = CLIJ2.getInstance();
		try {
			ClearCLBuffer inputCl = clij2.push(input);
			ClearCLBuffer small_eigenvalue = clij2.create(inputCl);
			ClearCLBuffer middle_eigenvalue = clij2.create(inputCl);
			ClearCLBuffer large_eigenvalue = clij2.create(inputCl);
			HessianEigenvalues3D.hessianEigenvalues3D(clij2, inputCl, small_eigenvalue, middle_eigenvalue, large_eigenvalue);
			RandomAccessibleInterval< FloatType > small = clij2.pullRAI(small_eigenvalue);
			RandomAccessibleInterval< FloatType > middle = clij2.pullRAI(middle_eigenvalue);
			RandomAccessibleInterval< FloatType > large = clij2.pullRAI(large_eigenvalue);
			assertEquals(-0.516f, small.getAt(1, 1, 1).getRealFloat(), 0.001);
			assertEquals(0.171f, middle.getAt(1, 1, 1).getRealFloat(), 0.001);
			assertEquals(11.345f, large.getAt(1, 1, 1).getRealFloat(), 0.001);
		}
		finally {
			clij2.close();
		}
	}

}
