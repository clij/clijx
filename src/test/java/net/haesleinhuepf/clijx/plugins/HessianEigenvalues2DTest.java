package net.haesleinhuepf.clijx.plugins;

import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clijx.CLIJx;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static net.imglib2.test.ImgLib2Assert.assertImageEquals;
import static org.junit.Assert.assertEquals;

public class HessianEigenvalues2DTest {

	@Test
	public void test() {
		CLIJx clijx = CLIJx.getInstance();
		Img< FloatType > input = ArrayImgs.floats(100, 100);
		LoopBuilder.setImages(Intervals.positions(input), input).forEachPixel( (pos, pixel) -> {
			long x = pos.getLongPosition(0);
			long y = pos.getLongPosition(1);
			pixel.setReal(x * x + 2 * y * y + 3 * x * y);
		});
		ClearCLBuffer inputCl = clijx.push(input);
		ClearCLBuffer small_eigenvalue = clijx.create(inputCl);
		ClearCLBuffer large_eigenvalue = clijx.create(inputCl);
		Map< String, Object > parameters = new HashMap<>();
		parameters.put("src", inputCl);
		parameters.put("small_eigenvalue", small_eigenvalue);
		parameters.put("large_eigenvalue", large_eigenvalue);
		clijx.execute(SobelSliceBySlice.class, "hessian_2d.cl", "hessian_2d", inputCl.getDimensions(), inputCl.getDimensions(), parameters);
		RandomAccessibleInterval<FloatType> small = clijx.pullRAI(small_eigenvalue);
		RandomAccessibleInterval<FloatType> large = clijx.pullRAI(large_eigenvalue);
		assertEquals(-0.16228f, small.getAt(1, 1).getRealFloat(), 0.001);
		assertEquals(6.162f, large.getAt(1, 1).getRealFloat(), 0.001);
		clijx.close();
	}
}
