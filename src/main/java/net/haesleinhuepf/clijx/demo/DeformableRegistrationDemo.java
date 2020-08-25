package net.haesleinhuepf.clijx.demo;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import net.haesleinhuepf.clij.CLIJ;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij2.CLIJ2;
import net.haesleinhuepf.clijx.registration.DeformableRegistration2D;

public class DeformableRegistrationDemo {
    public static void main(String... args) {
        new ImageJ();
        CLIJ2 clij2 = CLIJ2.getInstance();

        ImagePlus imp = IJ.openImage("C:\\structure\\data\\piv\\julia\\z16_t40-50.tif");
        //ImagePlus imp = IJ.openImage("C:\\structure\\data\\piv\\bruno\\G1.tif");
        IJ.run(imp, "32-bit", "");

        imp.setT(10);
        ClearCLBuffer slice1 = clij2.pushCurrentSlice(imp);
        imp.setT(11);
        ClearCLBuffer slice2 = clij2.pushCurrentSlice(imp);

        ClearCLBuffer deformedSlice2 = clij2.create(slice1);

        int maxDelta = 5;

        DeformableRegistration2D.deformableRegistration2D(clij2, slice1, slice2, deformedSlice2, maxDelta, maxDelta);

        clij2.show(slice1, "slice1");
        clij2.show(slice2, "slice2");
        clij2.show(deformedSlice2, "deformedSlice2");

        slice1.close();
        slice2.close();
        deformedSlice2.close();

    }
}
