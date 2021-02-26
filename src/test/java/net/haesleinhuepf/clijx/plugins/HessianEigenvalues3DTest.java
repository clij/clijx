package net.haesleinhuepf.clijx.plugins;

import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clijx.CLIJx;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.Localizable;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.test.ImgLib2Assert;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Localizables;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

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
		CLIJx clijx = CLIJx.getInstance();
		ClearCLBuffer inputCl = clijx.push(input);
		ClearCLBuffer small_eigenvalue = clijx.create(inputCl);
		ClearCLBuffer middle_eigenvalue = clijx.create(inputCl);
		ClearCLBuffer large_eigenvalue = clijx.create(inputCl);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("src", inputCl);
		parameters.put("small_eigenvalue", small_eigenvalue);
		parameters.put("middle_eigenvalue", middle_eigenvalue);
		parameters.put("large_eigenvalue", large_eigenvalue);
		clijx.execute(SobelSliceBySlice.class, "hessian_3d.cl", "hessian_3d", inputCl.getDimensions(), inputCl.getDimensions(), parameters);
		RandomAccessibleInterval<FloatType> small = clijx.pullRAI(small_eigenvalue);
		RandomAccessibleInterval<FloatType> middle = clijx.pullRAI(middle_eigenvalue);
		RandomAccessibleInterval<FloatType> large = clijx.pullRAI(large_eigenvalue);
		assertEquals(-0.516f, small.getAt(1, 1, 1).getRealFloat(), 0.001);
		assertEquals(0.171f, middle.getAt(1, 1, 1).getRealFloat(), 0.001);
		assertEquals(11.345f, large.getAt(1, 1, 1).getRealFloat(), 0.001);
		clijx.close();
	}
}
