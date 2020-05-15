package net.haesleinhuepf.clijx.utilities;
import net.haesleinhuepf.clij2.CLIJ2;
import net.haesleinhuepf.clijx.CLIJx;
import net.haesleinhuepf.clijx.clij2wrappers.SaveAsTIF;
import net.haesleinhuepf.clijx.weka.CLIJxWeka2;
import net.haesleinhuepf.clij.CLIJ;
import net.haesleinhuepf.clij.clearcl.ClearCLKernel;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.clearcl.ClearCLImage;
import net.haesleinhuepf.clij.clearcl.interfaces.ClearCLImageInterface;
import ij.measure.ResultsTable;
import ij.gui.Roi;
import ij.plugin.frame.RoiManager;
import java.util.HashMap;
import ij.ImagePlus;
import java.util.List;
import java.util.ArrayList;
import net.haesleinhuepf.clij.kernels.Kernels;
import net.haesleinhuepf.clijx.plugins.CrossCorrelation;
import net.haesleinhuepf.clijx.plugins.Extrema;
import net.haesleinhuepf.clijx.plugins.LocalExtremaBox;
import net.haesleinhuepf.clijx.plugins.LocalID;
import net.haesleinhuepf.clijx.plugins.Presign;
import net.haesleinhuepf.clijx.plugins.StackToTiles;
import net.haesleinhuepf.clijx.plugins.SubtractBackground2D;
import net.haesleinhuepf.clijx.plugins.SubtractBackground3D;
import net.haesleinhuepf.clijx.piv.FastParticleImageVelocimetry;
import net.haesleinhuepf.clijx.piv.ParticleImageVelocimetry;
import net.haesleinhuepf.clijx.piv.ParticleImageVelocimetryTimelapse;
import net.haesleinhuepf.clijx.registration.DeformableRegistration2D;
import net.haesleinhuepf.clijx.registration.TranslationRegistration;
import net.haesleinhuepf.clijx.registration.TranslationTimelapseRegistration;
import net.haesleinhuepf.clijx.io.ReadImageFromDisc;
import net.haesleinhuepf.clijx.io.ReadRawImageFromDisc;
import net.haesleinhuepf.clijx.io.PreloadFromDisc;
import net.haesleinhuepf.clijx.plugins.GaussJordan;
import net.haesleinhuepf.clijx.plugins.StopWatch;
import net.haesleinhuepf.clijx.plugins.DrawTwoValueLine;
import net.haesleinhuepf.clijx.plugins.ConnectedComponentsLabelingInplace;
import net.haesleinhuepf.clijx.plugins.AutomaticThresholdInplace;
import net.haesleinhuepf.clijx.plugins.DifferenceOfGaussianInplace3D;
import net.haesleinhuepf.clijx.plugins.AbsoluteInplace;
import net.haesleinhuepf.clijx.plugins.Watershed;
import net.haesleinhuepf.clijx.plugins.ShowRGB;
import net.haesleinhuepf.clijx.plugins.ShowGrey;
import net.haesleinhuepf.clijx.gui.OrganiseWindows;
import net.haesleinhuepf.clijx.plugins.TopHatOctagon;
import net.haesleinhuepf.clijx.plugins.ShowGlasbeyOnGrey;
import net.haesleinhuepf.clijx.plugins.BlurSliceBySlice;
import net.haesleinhuepf.clijx.plugins.splitstack.AbstractSplitStack;
import net.haesleinhuepf.clijx.plugins.TopHatOctagonSliceBySlice;
import net.haesleinhuepf.clijx.io.WriteVTKLineListToDisc;
import net.haesleinhuepf.clijx.io.WriteXYZPointListToDisc;
import net.haesleinhuepf.clijx.plugins.AverageAngleBetweenAdjacentTriangles;
import net.haesleinhuepf.clijx.plugins.tenengradfusion.TenengradFusion;
import net.haesleinhuepf.clijx.plugins.Skeletonize;
import net.haesleinhuepf.clijx.plugins.PushTile;
import net.haesleinhuepf.clijx.plugins.PullTile;
import net.haesleinhuepf.clijx.weka.autocontext.ApplyAutoContextWekaModel;
import net.haesleinhuepf.clijx.weka.autocontext.TrainAutoContextWekaModel;
import net.haesleinhuepf.clijx.weka.ApplyWekaModel;
import net.haesleinhuepf.clijx.weka.ApplyWekaToTable;
import net.haesleinhuepf.clijx.weka.GenerateFeatureStack;
import net.haesleinhuepf.clijx.weka.TrainWekaModel;
import net.haesleinhuepf.clijx.weka.TrainWekaFromTable;
import net.haesleinhuepf.clijx.weka.TrainWekaModelWithOptions;
import net.haesleinhuepf.clijx.plugins.StartContinuousWebcamAcquisition;
import net.haesleinhuepf.clijx.plugins.StopContinuousWebcamAcquisition;
import net.haesleinhuepf.clijx.plugins.CaptureWebcamImage;
import net.haesleinhuepf.clijx.plugins.ConvertRGBStackToGraySlice;
// this is generated code. See src/test/java/net/haesleinhuepf/clijx/codegenerator for details
public abstract interface CLIJxOps {
   CLIJ getCLIJ();
   CLIJ2 getCLIJ2();
   CLIJx getCLIJx();
   boolean doTimeTracing();
   void recordMethodStart(String method);
   void recordMethodEnd(String method);
   

    // net.haesleinhuepf.clij.kernels.Kernels
    //----------------------------------------------------
    /**
     * Deforms an image according to distances provided in the given vector images. It is recommended to use 32-bit images for input, output and vector images. 
     */
    default boolean applyVectorfield(ClearCLBuffer source, ClearCLBuffer vectorX, ClearCLBuffer vectorY, ClearCLBuffer destination) {
        if (doTimeTracing()) {recordMethodStart("Kernels");}
        boolean result = Kernels.applyVectorfield(getCLIJ(), source, vectorX, vectorY, destination);
        if (doTimeTracing()) {recordMethodEnd("Kernels");}
        return result;
    }

    /**
     * Deforms an image according to distances provided in the given vector images. It is recommended to use 32-bit images for input, output and vector images. 
     */
    default boolean applyVectorfield(ClearCLBuffer arg1, ClearCLBuffer arg2, ClearCLBuffer arg3, ClearCLBuffer arg4, ClearCLBuffer arg5) {
        if (doTimeTracing()) {recordMethodStart("Kernels");}
        boolean result = Kernels.applyVectorfield(getCLIJ(), arg1, arg2, arg3, arg4, arg5);
        if (doTimeTracing()) {recordMethodEnd("Kernels");}
        return result;
    }

    /**
     * Deforms an image according to distances provided in the given vector images. It is recommended to use 32-bit images for input, output and vector images. 
     */
    default boolean applyVectorfield(ClearCLImage source, ClearCLImage vectorX, ClearCLImage vectorY, ClearCLImage destination) {
        if (doTimeTracing()) {recordMethodStart("Kernels");}
        boolean result = Kernels.applyVectorfield(getCLIJ(), source, vectorX, vectorY, destination);
        if (doTimeTracing()) {recordMethodEnd("Kernels");}
        return result;
    }

    /**
     * Deforms an image according to distances provided in the given vector images. It is recommended to use 32-bit images for input, output and vector images. 
     */
    default boolean applyVectorfield(ClearCLImage arg1, ClearCLImage arg2, ClearCLImage arg3, ClearCLImage arg4, ClearCLImage arg5) {
        if (doTimeTracing()) {recordMethodStart("Kernels");}
        boolean result = Kernels.applyVectorfield(getCLIJ(), arg1, arg2, arg3, arg4, arg5);
        if (doTimeTracing()) {recordMethodEnd("Kernels");}
        return result;
    }

    /**
     * 
     */
    default boolean convertToImageJBinary(ClearCLBuffer arg1, ClearCLBuffer arg2) {
        if (doTimeTracing()) {recordMethodStart("Kernels");}
        boolean result = Kernels.convertToImageJBinary(getCLIJ(), arg1, arg2);
        if (doTimeTracing()) {recordMethodEnd("Kernels");}
        return result;
    }

    /**
     * 
     */
    default boolean convertToImageJBinary(ClearCLImage arg1, ClearCLImage arg2) {
        if (doTimeTracing()) {recordMethodStart("Kernels");}
        boolean result = Kernels.convertToImageJBinary(getCLIJ(), arg1, arg2);
        if (doTimeTracing()) {recordMethodEnd("Kernels");}
        return result;
    }

    /**
     * 
     */
    default boolean detectOptima(ClearCLBuffer arg1, ClearCLBuffer arg2, double arg3, boolean arg4) {
        if (doTimeTracing()) {recordMethodStart("Kernels");}
        boolean result = Kernels.detectOptima(getCLIJ(), arg1, arg2, new Double (arg3).intValue(), arg4);
        if (doTimeTracing()) {recordMethodEnd("Kernels");}
        return result;
    }

    /**
     * 
     */
    default boolean detectOptima(ClearCLImage arg1, ClearCLImage arg2, double arg3, boolean arg4) {
        if (doTimeTracing()) {recordMethodStart("Kernels");}
        boolean result = Kernels.detectOptima(getCLIJ(), arg1, arg2, new Double (arg3).intValue(), arg4);
        if (doTimeTracing()) {recordMethodEnd("Kernels");}
        return result;
    }

    /**
     * 
     */
    default boolean detectOptimaSliceBySlice(ClearCLBuffer arg1, ClearCLBuffer arg2, double arg3, boolean arg4) {
        if (doTimeTracing()) {recordMethodStart("Kernels");}
        boolean result = Kernels.detectOptimaSliceBySlice(getCLIJ(), arg1, arg2, new Double (arg3).intValue(), arg4);
        if (doTimeTracing()) {recordMethodEnd("Kernels");}
        return result;
    }

    /**
     * 
     */
    default boolean detectOptimaSliceBySlice(ClearCLImage arg1, ClearCLImage arg2, double arg3, boolean arg4) {
        if (doTimeTracing()) {recordMethodStart("Kernels");}
        boolean result = Kernels.detectOptimaSliceBySlice(getCLIJ(), arg1, arg2, new Double (arg3).intValue(), arg4);
        if (doTimeTracing()) {recordMethodEnd("Kernels");}
        return result;
    }

    /**
     * Applies Gaussian blur to the input image twice with different sigma values resulting in two images which are then subtracted from each other.
     * 
     * It is recommended to apply this operation to images of type Float (32 bit) as results might be negative.
     */
    default boolean differenceOfGaussian(ClearCLImage arg1, ClearCLImage arg2, double arg3, double arg4, double arg5) {
        if (doTimeTracing()) {recordMethodStart("Kernels");}
        boolean result = Kernels.differenceOfGaussian(getCLIJ(), arg1, arg2, new Double (arg3).intValue(), new Double (arg4).floatValue(), new Double (arg5).floatValue());
        if (doTimeTracing()) {recordMethodEnd("Kernels");}
        return result;
    }

    /**
     * 
     */
    default boolean differenceOfGaussianSliceBySlice(ClearCLImage arg1, ClearCLImage arg2, double arg3, double arg4, double arg5) {
        if (doTimeTracing()) {recordMethodStart("Kernels");}
        boolean result = Kernels.differenceOfGaussianSliceBySlice(getCLIJ(), arg1, arg2, new Double (arg3).intValue(), new Double (arg4).floatValue(), new Double (arg5).floatValue());
        if (doTimeTracing()) {recordMethodEnd("Kernels");}
        return result;
    }

    /**
     * Determines the maximum projection of an image along a given dimension. Furthermore, the X and Y
     *  dimesions of the resulting image must be specified by the user according to its definition:
     * X = 0
     * Y = 1
     * Z = 2
     * 
     */
    default boolean maximumXYZProjection(ClearCLBuffer arg1, ClearCLBuffer arg2, double arg3, double arg4, double arg5) {
        if (doTimeTracing()) {recordMethodStart("Kernels");}
        boolean result = Kernels.maximumXYZProjection(getCLIJ(), arg1, arg2, new Double (arg3).intValue(), new Double (arg4).intValue(), new Double (arg5).intValue());
        if (doTimeTracing()) {recordMethodEnd("Kernels");}
        return result;
    }

    /**
     * Determines the maximum projection of an image along a given dimension. Furthermore, the X and Y
     *  dimesions of the resulting image must be specified by the user according to its definition:
     * X = 0
     * Y = 1
     * Z = 2
     * 
     */
    default boolean maximumXYZProjection(ClearCLImage arg1, ClearCLImage arg2, double arg3, double arg4, double arg5) {
        if (doTimeTracing()) {recordMethodStart("Kernels");}
        boolean result = Kernels.maximumXYZProjection(getCLIJ(), arg1, arg2, new Double (arg3).intValue(), new Double (arg4).intValue(), new Double (arg5).intValue());
        if (doTimeTracing()) {recordMethodEnd("Kernels");}
        return result;
    }

    /**
     * 
     */
    default boolean multiplySliceBySliceWithScalars(ClearCLBuffer arg1, ClearCLBuffer arg2, float[] arg3) {
        if (doTimeTracing()) {recordMethodStart("Kernels");}
        boolean result = Kernels.multiplySliceBySliceWithScalars(getCLIJ(), arg1, arg2, arg3);
        if (doTimeTracing()) {recordMethodEnd("Kernels");}
        return result;
    }

    /**
     * 
     */
    default boolean multiplySliceBySliceWithScalars(ClearCLImage arg1, ClearCLImage arg2, float[] arg3) {
        if (doTimeTracing()) {recordMethodStart("Kernels");}
        boolean result = Kernels.multiplySliceBySliceWithScalars(getCLIJ(), arg1, arg2, arg3);
        if (doTimeTracing()) {recordMethodEnd("Kernels");}
        return result;
    }

    /**
     * 
     */
    default double[] sumPixelsSliceBySlice(ClearCLBuffer arg1) {
        if (doTimeTracing()) {recordMethodStart("Kernels");}
        double[] result = Kernels.sumPixelsSliceBySlice(getCLIJ(), arg1);
        if (doTimeTracing()) {recordMethodEnd("Kernels");}
        return result;
    }

    /**
     * 
     */
    default double[] sumPixelsSliceBySlice(ClearCLImage arg1) {
        if (doTimeTracing()) {recordMethodStart("Kernels");}
        double[] result = Kernels.sumPixelsSliceBySlice(getCLIJ(), arg1);
        if (doTimeTracing()) {recordMethodEnd("Kernels");}
        return result;
    }


    // net.haesleinhuepf.clijx.plugins.CrossCorrelation
    //----------------------------------------------------
    /**
     * Performs cross correlation analysis between two images. 
     * 
     * The second image is shifted by deltaPos in the given dimension. The cross correlation coefficient is calculated for each pixel in a range around the given pixel with given radius in the given dimension. Together with the original images it is recommended to hand over mean filtered images using the same radius.  
     */
    default boolean crossCorrelation(ClearCLBuffer arg1, ClearCLBuffer arg2, ClearCLBuffer arg3, ClearCLBuffer arg4, ClearCLBuffer arg5, double arg6, double arg7, double arg8) {
        if (doTimeTracing()) {recordMethodStart("CrossCorrelation");}
        boolean result = CrossCorrelation.crossCorrelation(getCLIJ(), arg1, arg2, arg3, arg4, arg5, new Double (arg6).intValue(), new Double (arg7).intValue(), new Double (arg8).intValue());
        if (doTimeTracing()) {recordMethodEnd("CrossCorrelation");}
        return result;
    }

    /**
     * Performs cross correlation analysis between two images. 
     * 
     * The second image is shifted by deltaPos in the given dimension. The cross correlation coefficient is calculated for each pixel in a range around the given pixel with given radius in the given dimension. Together with the original images it is recommended to hand over mean filtered images using the same radius.  
     */
    default boolean crossCorrelation(ClearCLImage arg1, ClearCLImage arg2, ClearCLImage arg3, ClearCLImage arg4, ClearCLImage arg5, double arg6, double arg7, double arg8) {
        if (doTimeTracing()) {recordMethodStart("CrossCorrelation");}
        boolean result = CrossCorrelation.crossCorrelation(getCLIJ(), arg1, arg2, arg3, arg4, arg5, new Double (arg6).intValue(), new Double (arg7).intValue(), new Double (arg8).intValue());
        if (doTimeTracing()) {recordMethodEnd("CrossCorrelation");}
        return result;
    }


    // net.haesleinhuepf.clijx.plugins.Extrema
    //----------------------------------------------------
    /**
     * Returns an image with pixel values most distant from 0: 
     * 
     * f(x, y) = x if abs(x) > abs(y), y else.
     */
    default boolean extrema(ClearCLBuffer input1, ClearCLBuffer input2, ClearCLBuffer destination) {
        if (doTimeTracing()) {recordMethodStart("Extrema");}
        boolean result = Extrema.extrema(getCLIJ(), input1, input2, destination);
        if (doTimeTracing()) {recordMethodEnd("Extrema");}
        return result;
    }


    // net.haesleinhuepf.clijx.plugins.LocalExtremaBox
    //----------------------------------------------------
    /**
     * Applies a local minimum and maximum filter. 
     * 
     * Afterwards, the value is returned which is more far from zero.
     */
    default boolean localExtremaBox(ClearCLBuffer arg1, ClearCLBuffer arg2, double arg3, double arg4, double arg5) {
        if (doTimeTracing()) {recordMethodStart("LocalExtremaBox");}
        boolean result = LocalExtremaBox.localExtremaBox(getCLIJ(), arg1, arg2, new Double (arg3).intValue(), new Double (arg4).intValue(), new Double (arg5).intValue());
        if (doTimeTracing()) {recordMethodEnd("LocalExtremaBox");}
        return result;
    }


    // net.haesleinhuepf.clijx.plugins.LocalID
    //----------------------------------------------------
    /**
     * local id
     */
    default boolean localID(ClearCLBuffer input, ClearCLBuffer destination) {
        if (doTimeTracing()) {recordMethodStart("LocalID");}
        boolean result = LocalID.localID(getCLIJ(), input, destination);
        if (doTimeTracing()) {recordMethodEnd("LocalID");}
        return result;
    }


    // net.haesleinhuepf.clijx.plugins.Presign
    //----------------------------------------------------
    /**
     * Determines the extrema of pixel values: 
     * 
     * f(x) = x / abs(x).
     */
    default boolean presign(ClearCLBuffer input, ClearCLBuffer destination) {
        if (doTimeTracing()) {recordMethodStart("Presign");}
        boolean result = Presign.presign(getCLIJ(), input, destination);
        if (doTimeTracing()) {recordMethodEnd("Presign");}
        return result;
    }


    // net.haesleinhuepf.clijx.plugins.StackToTiles
    //----------------------------------------------------
    /**
     * Stack to tiles.
     */
    default boolean stackToTiles(ClearCLImageInterface arg1, ClearCLImageInterface arg2, double arg3, double arg4) {
        if (doTimeTracing()) {recordMethodStart("StackToTiles");}
        boolean result = StackToTiles.stackToTiles(getCLIJx(), arg1, arg2, new Double (arg3).intValue(), new Double (arg4).intValue());
        if (doTimeTracing()) {recordMethodEnd("StackToTiles");}
        return result;
    }


    // net.haesleinhuepf.clijx.plugins.SubtractBackground2D
    //----------------------------------------------------
    /**
     * Applies Gaussian blur to the input image and subtracts the result from the original image.
     */
    default boolean subtractBackground(ClearCLImageInterface arg1, ClearCLImageInterface arg2, double arg3, double arg4) {
        if (doTimeTracing()) {recordMethodStart("SubtractBackground2D");}
        boolean result = SubtractBackground2D.subtractBackground(getCLIJx(), arg1, arg2, new Double (arg3).floatValue(), new Double (arg4).floatValue());
        if (doTimeTracing()) {recordMethodEnd("SubtractBackground2D");}
        return result;
    }

    /**
     * Applies Gaussian blur to the input image and subtracts the result from the original image.
     */
    default boolean subtractBackground2D(ClearCLImageInterface arg1, ClearCLImageInterface arg2, double arg3, double arg4) {
        if (doTimeTracing()) {recordMethodStart("SubtractBackground2D");}
        boolean result = SubtractBackground2D.subtractBackground2D(getCLIJx(), arg1, arg2, new Double (arg3).floatValue(), new Double (arg4).floatValue());
        if (doTimeTracing()) {recordMethodEnd("SubtractBackground2D");}
        return result;
    }


    // net.haesleinhuepf.clijx.plugins.SubtractBackground3D
    //----------------------------------------------------
    /**
     * Applies Gaussian blur to the input image and subtracts the result from the original image.
     */
    default boolean subtractBackground(ClearCLImageInterface arg1, ClearCLImageInterface arg2, double arg3, double arg4, double arg5) {
        if (doTimeTracing()) {recordMethodStart("SubtractBackground3D");}
        boolean result = SubtractBackground3D.subtractBackground(getCLIJx(), arg1, arg2, new Double (arg3).floatValue(), new Double (arg4).floatValue(), new Double (arg5).floatValue());
        if (doTimeTracing()) {recordMethodEnd("SubtractBackground3D");}
        return result;
    }

    /**
     * Applies Gaussian blur to the input image and subtracts the result from the original image.
     */
    default boolean subtractBackground3D(ClearCLImageInterface arg1, ClearCLImageInterface arg2, double arg3, double arg4, double arg5) {
        if (doTimeTracing()) {recordMethodStart("SubtractBackground3D");}
        boolean result = SubtractBackground3D.subtractBackground3D(getCLIJx(), arg1, arg2, new Double (arg3).floatValue(), new Double (arg4).floatValue(), new Double (arg5).floatValue());
        if (doTimeTracing()) {recordMethodEnd("SubtractBackground3D");}
        return result;
    }


    // net.haesleinhuepf.clijx.piv.FastParticleImageVelocimetry
    //----------------------------------------------------
    /**
     * 
     */
    default boolean particleImageVelocimetry2D(ClearCLBuffer arg1, ClearCLBuffer arg2, ClearCLBuffer arg3, ClearCLBuffer arg4, double arg5) {
        if (doTimeTracing()) {recordMethodStart("FastParticleImageVelocimetry");}
        boolean result = FastParticleImageVelocimetry.particleImageVelocimetry2D(getCLIJ(), arg1, arg2, arg3, arg4, new Double (arg5).intValue());
        if (doTimeTracing()) {recordMethodEnd("FastParticleImageVelocimetry");}
        return result;
    }

    /**
     * 
     */
    default boolean particleImageVelocimetry2D(ClearCLImage arg1, ClearCLImage arg2, ClearCLImage arg3, ClearCLImage arg4, double arg5) {
        if (doTimeTracing()) {recordMethodStart("FastParticleImageVelocimetry");}
        boolean result = FastParticleImageVelocimetry.particleImageVelocimetry2D(getCLIJ(), arg1, arg2, arg3, arg4, new Double (arg5).intValue());
        if (doTimeTracing()) {recordMethodEnd("FastParticleImageVelocimetry");}
        return result;
    }


    // net.haesleinhuepf.clijx.piv.ParticleImageVelocimetry
    //----------------------------------------------------
    /**
     * For every pixel in source image 1, determine the pixel with the most similar intensity in 
     *  the local neighborhood with a given radius in source image 2. Write the distance in 
     * X and Y in the two corresponding destination images.
     */
    default boolean particleImageVelocimetry(ClearCLBuffer arg1, ClearCLBuffer arg2, ClearCLBuffer arg3, ClearCLBuffer arg4, ClearCLBuffer arg5, double arg6, double arg7, double arg8, boolean arg9) {
        if (doTimeTracing()) {recordMethodStart("ParticleImageVelocimetry");}
        boolean result = ParticleImageVelocimetry.particleImageVelocimetry(getCLIJ(), arg1, arg2, arg3, arg4, arg5, new Double (arg6).intValue(), new Double (arg7).intValue(), new Double (arg8).intValue(), arg9);
        if (doTimeTracing()) {recordMethodEnd("ParticleImageVelocimetry");}
        return result;
    }


    // net.haesleinhuepf.clijx.piv.ParticleImageVelocimetryTimelapse
    //----------------------------------------------------
    /**
     * Run particle image velocimetry on a 2D+t timelapse.
     */
    default boolean particleImageVelocimetryTimelapse(ClearCLBuffer arg1, ClearCLBuffer arg2, ClearCLBuffer arg3, ClearCLBuffer arg4, double arg5, double arg6, double arg7, boolean arg8) {
        if (doTimeTracing()) {recordMethodStart("ParticleImageVelocimetryTimelapse");}
        boolean result = ParticleImageVelocimetryTimelapse.particleImageVelocimetryTimelapse(getCLIJ(), arg1, arg2, arg3, arg4, new Double (arg5).intValue(), new Double (arg6).intValue(), new Double (arg7).intValue(), arg8);
        if (doTimeTracing()) {recordMethodEnd("ParticleImageVelocimetryTimelapse");}
        return result;
    }


    // net.haesleinhuepf.clijx.registration.DeformableRegistration2D
    //----------------------------------------------------
    /**
     * Applies particle image velocimetry to two images and registers them afterwards by warping input image 2 with a smoothed vector field.
     */
    default boolean deformableRegistration2D(ClearCLBuffer arg1, ClearCLBuffer arg2, ClearCLBuffer arg3, double arg4, double arg5) {
        if (doTimeTracing()) {recordMethodStart("DeformableRegistration2D");}
        boolean result = DeformableRegistration2D.deformableRegistration2D(getCLIJ(), arg1, arg2, arg3, new Double (arg4).intValue(), new Double (arg5).intValue());
        if (doTimeTracing()) {recordMethodEnd("DeformableRegistration2D");}
        return result;
    }


    // net.haesleinhuepf.clijx.registration.TranslationRegistration
    //----------------------------------------------------
    /**
     * Measures center of mass of thresholded objects in the two input images and translates the second image so that it better fits to the first image.
     */
    default boolean translationRegistration(ClearCLBuffer arg1, ClearCLBuffer arg2, double[] arg3) {
        if (doTimeTracing()) {recordMethodStart("TranslationRegistration");}
        boolean result = TranslationRegistration.translationRegistration(getCLIJ(), arg1, arg2, arg3);
        if (doTimeTracing()) {recordMethodEnd("TranslationRegistration");}
        return result;
    }

    /**
     * Measures center of mass of thresholded objects in the two input images and translates the second image so that it better fits to the first image.
     */
    default boolean translationRegistration(ClearCLBuffer input1, ClearCLBuffer input2, ClearCLBuffer destination) {
        if (doTimeTracing()) {recordMethodStart("TranslationRegistration");}
        boolean result = TranslationRegistration.translationRegistration(getCLIJ(), input1, input2, destination);
        if (doTimeTracing()) {recordMethodEnd("TranslationRegistration");}
        return result;
    }


    // net.haesleinhuepf.clijx.registration.TranslationTimelapseRegistration
    //----------------------------------------------------
    /**
     * Applies 2D translation registration to every pair of t, t+1 slices of a 2D+t image stack.
     */
    default boolean translationTimelapseRegistration(ClearCLBuffer input, ClearCLBuffer output) {
        if (doTimeTracing()) {recordMethodStart("TranslationTimelapseRegistration");}
        boolean result = TranslationTimelapseRegistration.translationTimelapseRegistration(getCLIJ(), input, output);
        if (doTimeTracing()) {recordMethodEnd("TranslationTimelapseRegistration");}
        return result;
    }


    // net.haesleinhuepf.clijx.io.ReadImageFromDisc
    //----------------------------------------------------
    /**
     * Read an image from disc.
     */
    default ClearCLBuffer readImageFromDisc(String arg1) {
        if (doTimeTracing()) {recordMethodStart("ReadImageFromDisc");}
        ClearCLBuffer result = ReadImageFromDisc.readImageFromDisc(getCLIJ(), arg1);
        if (doTimeTracing()) {recordMethodEnd("ReadImageFromDisc");}
        return result;
    }


    // net.haesleinhuepf.clijx.io.ReadRawImageFromDisc
    //----------------------------------------------------
    /**
     * Reads a raw file from disc and pushes it immediately to the GPU.
     */
    default boolean readRawImageFromDisc(ClearCLBuffer arg1, String arg2) {
        if (doTimeTracing()) {recordMethodStart("ReadRawImageFromDisc");}
        boolean result = ReadRawImageFromDisc.readRawImageFromDisc(getCLIJ(), arg1, arg2);
        if (doTimeTracing()) {recordMethodEnd("ReadRawImageFromDisc");}
        return result;
    }

    /**
     * Reads a raw file from disc and pushes it immediately to the GPU.
     */
    default ClearCLBuffer readRawImageFromDisc(String arg1, double arg2, double arg3, double arg4, double arg5) {
        if (doTimeTracing()) {recordMethodStart("ReadRawImageFromDisc");}
        ClearCLBuffer result = ReadRawImageFromDisc.readRawImageFromDisc(getCLIJ(), arg1, new Double (arg2).intValue(), new Double (arg3).intValue(), new Double (arg4).intValue(), new Double (arg5).intValue());
        if (doTimeTracing()) {recordMethodEnd("ReadRawImageFromDisc");}
        return result;
    }


    // net.haesleinhuepf.clijx.io.PreloadFromDisc
    //----------------------------------------------------
    /**
     * This plugin takes two image filenames and loads them into RAM. The first image is returned immediately, the second image is loaded in the background and  will be returned when the plugin is called again.
     * 
     *  It is assumed that all images have the same size. If this is not the case, call release(image) before  getting the second image.
     */
    default ClearCLBuffer preloadFromDisc(ClearCLBuffer destination, String filename, String nextFilename, String loaderId) {
        if (doTimeTracing()) {recordMethodStart("PreloadFromDisc");}
        ClearCLBuffer result = PreloadFromDisc.preloadFromDisc(getCLIJ(), destination, filename, nextFilename, loaderId);
        if (doTimeTracing()) {recordMethodEnd("PreloadFromDisc");}
        return result;
    }


    // net.haesleinhuepf.clijx.plugins.GaussJordan
    //----------------------------------------------------
    /**
     * Gauss Jordan elimination algorithm for solving linear equation systems. 
     * 
     * Ent the equation coefficients as an n*n sized image A and an n*1 sized image B:
     * <pre>a(1,1)*x + a(2,1)*y + a(3,1)+z = b(1)
     * a(2,1)*x + a(2,2)*y + a(3,2)+z = b(2)
     * a(3,1)*x + a(3,2)*y + a(3,3)+z = b(3)
     * </pre>
     * The results will then be given in an n*1 image with values [x, y, z].
     * 
     * Adapted from: 
     * https://github.com/qbunia/rodinia/blob/master/opencl/gaussian/gaussianElim_kernels.cl
     * L.G. Szafaryn, K. Skadron and J. Saucerman. "Experiences Accelerating MATLAB Systems
     * //Biology Applications." in Workshop on Biomedicine in Computing (BiC) at the International
     * //Symposium on Computer Architecture (ISCA), June 2009.
     */
    default boolean gaussJordan(ClearCLBuffer A_matrix, ClearCLBuffer B_result_vector, ClearCLBuffer solution_destination) {
        if (doTimeTracing()) {recordMethodStart("GaussJordan");}
        boolean result = GaussJordan.gaussJordan(getCLIJ(), A_matrix, B_result_vector, solution_destination);
        if (doTimeTracing()) {recordMethodEnd("GaussJordan");}
        return result;
    }


    // net.haesleinhuepf.clijx.plugins.StopWatch
    //----------------------------------------------------
    /**
     * Measures time and outputs delay to last call.
     */
    default boolean stopWatch(String text) {
        if (doTimeTracing()) {recordMethodStart("StopWatch");}
        boolean result = StopWatch.stopWatch(getCLIJ(), text);
        if (doTimeTracing()) {recordMethodEnd("StopWatch");}
        return result;
    }


    // net.haesleinhuepf.clijx.plugins.DrawTwoValueLine
    //----------------------------------------------------
    /**
     * Draws a line between two points with a given thickness. 
     * 
     * Pixels close to point 1 are set to value1. Pixels closer to point 2 are set to value2 All pixels other than on the line are untouched. Consider using clij.set(buffer, 0); in advance.
     */
    default boolean drawTwoValueLine(ClearCLBuffer arg1, double arg2, double arg3, double arg4, double arg5, double arg6, double arg7, double arg8, double arg9, double arg10) {
        if (doTimeTracing()) {recordMethodStart("DrawTwoValueLine");}
        boolean result = DrawTwoValueLine.drawTwoValueLine(getCLIJx(), arg1, new Double (arg2).floatValue(), new Double (arg3).floatValue(), new Double (arg4).floatValue(), new Double (arg5).floatValue(), new Double (arg6).floatValue(), new Double (arg7).floatValue(), new Double (arg8).floatValue(), new Double (arg9).floatValue(), new Double (arg10).floatValue());
        if (doTimeTracing()) {recordMethodEnd("DrawTwoValueLine");}
        return result;
    }


    // net.haesleinhuepf.clijx.plugins.SaveAsTIF
    //----------------------------------------------------
    /**
     * Pulls an image from the GPU memory and saves it as TIF to disc.
     */
    default boolean saveAsTIF(ClearCLBuffer input, String filename) {
        if (doTimeTracing()) {recordMethodStart("SaveAsTIF");}
        boolean result = SaveAsTIF.saveAsTIF(getCLIJ2(), input, filename);
        if (doTimeTracing()) {recordMethodEnd("SaveAsTIF");}
        return result;
    }


    // net.haesleinhuepf.clijx.plugins.ConnectedComponentsLabelingInplace
    //----------------------------------------------------
    /**
     * Performs connected components analysis to a binary image and generates a label map.
     */
    default boolean connectedComponentsLabelingInplace(ClearCLBuffer binary_source_labeling_destination) {
        if (doTimeTracing()) {recordMethodStart("ConnectedComponentsLabelingInplace");}
        boolean result = ConnectedComponentsLabelingInplace.connectedComponentsLabelingInplace(getCLIJx(), binary_source_labeling_destination);
        if (doTimeTracing()) {recordMethodEnd("ConnectedComponentsLabelingInplace");}
        return result;
    }


    // net.haesleinhuepf.clijx.plugins.AutomaticThresholdInplace
    //----------------------------------------------------
    /**
     * The automatic thresholder utilizes the threshold methods from ImageJ on a histogram determined on 
     * the GPU to create binary images as similar as possible to ImageJ 'Apply Threshold' method. Enter one 
     * of these methods in the method text field:
     * [Default, Huang, Intermodes, IsoData, IJ_IsoData, Li, MaxEntropy, Mean, MinError, Minimum, Moments, Otsu, Percentile, RenyiEntropy, Shanbhag, Triangle, Yen]
     */
    default boolean automaticThresholdInplace(ClearCLBuffer input_and_destination, String method) {
        if (doTimeTracing()) {recordMethodStart("AutomaticThresholdInplace");}
        boolean result = AutomaticThresholdInplace.automaticThresholdInplace(getCLIJx(), input_and_destination, method);
        if (doTimeTracing()) {recordMethodEnd("AutomaticThresholdInplace");}
        return result;
    }


    // net.haesleinhuepf.clijx.plugins.DifferenceOfGaussianInplace3D
    //----------------------------------------------------
    /**
     * Applies Gaussian blur to the input image twice with different sigma values resulting in two images which are then subtracted from each other.
     * 
     * It is recommended to apply this operation to images of type Float (32 bit) as results might be negative.
     */
    default boolean differenceOfGaussianInplace3D(ClearCLBuffer arg1, double arg2, double arg3, double arg4, double arg5, double arg6, double arg7) {
        if (doTimeTracing()) {recordMethodStart("DifferenceOfGaussianInplace3D");}
        boolean result = DifferenceOfGaussianInplace3D.differenceOfGaussianInplace3D(getCLIJ(), arg1, new Double (arg2).floatValue(), new Double (arg3).floatValue(), new Double (arg4).floatValue(), new Double (arg5).floatValue(), new Double (arg6).floatValue(), new Double (arg7).floatValue());
        if (doTimeTracing()) {recordMethodEnd("DifferenceOfGaussianInplace3D");}
        return result;
    }


    // net.haesleinhuepf.clijx.plugins.AbsoluteInplace
    //----------------------------------------------------
    /**
     * Computes the absolute value of every individual pixel x in a given image.
     * 
     * <pre>f(x) = |x| </pre>
     */
    default boolean absoluteInplace(ClearCLBuffer source_destination) {
        if (doTimeTracing()) {recordMethodStart("AbsoluteInplace");}
        boolean result = AbsoluteInplace.absoluteInplace(getCLIJx(), source_destination);
        if (doTimeTracing()) {recordMethodEnd("AbsoluteInplace");}
        return result;
    }


    // net.haesleinhuepf.clijx.plugins.Watershed
    //----------------------------------------------------
    /**
     * Apply a binary watershed to a binary image and introduces black pixels between objects.
     */
    default boolean watershed(ClearCLBuffer binary_source, ClearCLBuffer destination) {
        if (doTimeTracing()) {recordMethodStart("Watershed");}
        boolean result = Watershed.watershed(getCLIJx(), binary_source, destination);
        if (doTimeTracing()) {recordMethodEnd("Watershed");}
        return result;
    }


    // net.haesleinhuepf.clijx.plugins.ShowRGB
    //----------------------------------------------------
    /**
     * Visualises three 2D images as one RGB image
     */
    default boolean showRGB(ClearCLBuffer red, ClearCLBuffer green, ClearCLBuffer blue, String title) {
        if (doTimeTracing()) {recordMethodStart("ShowRGB");}
        boolean result = ShowRGB.showRGB(getCLIJ(), red, green, blue, title);
        if (doTimeTracing()) {recordMethodEnd("ShowRGB");}
        return result;
    }


    // net.haesleinhuepf.clijx.plugins.ShowGrey
    //----------------------------------------------------
    /**
     * Visualises a single 2D image.
     */
    default ImagePlus showGrey(ClearCLBuffer input, String title) {
        if (doTimeTracing()) {recordMethodStart("ShowGrey");}
        ImagePlus result = ShowGrey.showGrey(getCLIJ(), input, title);
        if (doTimeTracing()) {recordMethodEnd("ShowGrey");}
        return result;
    }


    // net.haesleinhuepf.clijx.gui.OrganiseWindows
    //----------------------------------------------------
    /**
     * Organises windows on screen.
     */
    default boolean organiseWindows(double arg1, double arg2, double arg3, double arg4, double arg5, double arg6) {
        if (doTimeTracing()) {recordMethodStart("OrganiseWindows");}
        boolean result = OrganiseWindows.organiseWindows(getCLIJ(), new Double (arg1).intValue(), new Double (arg2).intValue(), new Double (arg3).intValue(), new Double (arg4).intValue(), new Double (arg5).intValue(), new Double (arg6).intValue());
        if (doTimeTracing()) {recordMethodEnd("OrganiseWindows");}
        return result;
    }


    // net.haesleinhuepf.clijx.plugins.TopHatOctagon
    //----------------------------------------------------
    /**
     * Applies a minimum filter with kernel size 3x3 n times to an image iteratively. 
     * 
     *  Odd iterations are done with box neighborhood, even iterations with a diamond. Thus, with n > 2, the filter shape is an octagon. The given number of iterations - 2 makes the filter result very similar to minimum sphere.
     */
    default boolean topHatOctagon(ClearCLBuffer arg1, ClearCLBuffer arg2, double arg3) {
        if (doTimeTracing()) {recordMethodStart("TopHatOctagon");}
        boolean result = TopHatOctagon.topHatOctagon(getCLIJx(), arg1, arg2, new Double (arg3).intValue());
        if (doTimeTracing()) {recordMethodEnd("TopHatOctagon");}
        return result;
    }


    // net.haesleinhuepf.clijx.plugins.ShowGlasbeyOnGrey
    //----------------------------------------------------
    /**
     * Visualises two 2D images as one RGB image. 
     * 
     * The first channel is shown in grey, the second with glasbey LUT.
     */
    default boolean showGlasbeyOnGrey(ClearCLBuffer red, ClearCLBuffer labelling, String title) {
        if (doTimeTracing()) {recordMethodStart("ShowGlasbeyOnGrey");}
        boolean result = ShowGlasbeyOnGrey.showGlasbeyOnGrey(getCLIJ(), red, labelling, title);
        if (doTimeTracing()) {recordMethodEnd("ShowGlasbeyOnGrey");}
        return result;
    }


    // net.haesleinhuepf.clijx.plugins.BlurSliceBySlice
    //----------------------------------------------------
    /**
     * Computes the Gaussian blurred image of an image given two sigma values in X and Y. Thus, the filterkernel can have non-isotropic shape.
     * 
     * The Gaussian blur is applied slice by slice in 2D.
     */
    @Deprecated
    default boolean blurSliceBySlice(ClearCLImageInterface arg1, ClearCLImageInterface arg2, double arg3, double arg4) {
        if (doTimeTracing()) {recordMethodStart("BlurSliceBySlice");}
        boolean result = BlurSliceBySlice.blurSliceBySlice(getCLIJx(), arg1, arg2, new Double (arg3).floatValue(), new Double (arg4).floatValue());
        if (doTimeTracing()) {recordMethodEnd("BlurSliceBySlice");}
        return result;
    }

    /**
     * Computes the Gaussian blurred image of an image given two sigma values in X and Y. Thus, the filterkernel can have non-isotropic shape.
     * 
     * The Gaussian blur is applied slice by slice in 2D.
     */
    @Deprecated
    default boolean blurSliceBySlice(ClearCLImageInterface arg1, ClearCLImageInterface arg2, double arg3, double arg4, double arg5, double arg6) {
        if (doTimeTracing()) {recordMethodStart("BlurSliceBySlice");}
        boolean result = BlurSliceBySlice.blurSliceBySlice(getCLIJx(), arg1, arg2, new Double (arg3).intValue(), new Double (arg4).intValue(), new Double (arg5).floatValue(), new Double (arg6).floatValue());
        if (doTimeTracing()) {recordMethodEnd("BlurSliceBySlice");}
        return result;
    }


    // net.haesleinhuepf.clijx.plugins.splitstack.AbstractSplitStack
    //----------------------------------------------------

    // net.haesleinhuepf.clijx.plugins.TopHatOctagonSliceBySlice
    //----------------------------------------------------
    /**
     * Applies a minimum filter with kernel size 3x3 n times to an image iteratively. 
     * 
     * Odd iterations are done with box neighborhood, even iterations with a diamond. Thus, with n > 2, the filter shape is an octagon. The given number of iterations - 2 makes the filter result very similar to minimum sphere.
     */
    default boolean topHatOctagonSliceBySlice(ClearCLBuffer arg1, ClearCLBuffer arg2, double arg3) {
        if (doTimeTracing()) {recordMethodStart("TopHatOctagonSliceBySlice");}
        boolean result = TopHatOctagonSliceBySlice.topHatOctagonSliceBySlice(getCLIJx(), arg1, arg2, new Double (arg3).intValue());
        if (doTimeTracing()) {recordMethodEnd("TopHatOctagonSliceBySlice");}
        return result;
    }


    // net.haesleinhuepf.clijx.io.WriteVTKLineListToDisc
    //----------------------------------------------------
    /**
     * Takes a point list image representing n points (n*2 for 2D points, n*3 for 3D points) and a corresponding touch matrix , sized (n+1)*(n+1), and exports them in VTK format.
     */
    default boolean writeVTKLineListToDisc(ClearCLBuffer pointlist, ClearCLBuffer touch_matrix, String filename) {
        if (doTimeTracing()) {recordMethodStart("WriteVTKLineListToDisc");}
        boolean result = WriteVTKLineListToDisc.writeVTKLineListToDisc(getCLIJx(), pointlist, touch_matrix, filename);
        if (doTimeTracing()) {recordMethodEnd("WriteVTKLineListToDisc");}
        return result;
    }


    // net.haesleinhuepf.clijx.io.WriteXYZPointListToDisc
    //----------------------------------------------------
    /**
     * Takes a point list image representing n points (n*2 for 2D points, n*3 for 3D points) and exports them in XYZ format.
     */
    default boolean writeXYZPointListToDisc(ClearCLBuffer pointlist, String filename) {
        if (doTimeTracing()) {recordMethodStart("WriteXYZPointListToDisc");}
        boolean result = WriteXYZPointListToDisc.writeXYZPointListToDisc(getCLIJx(), pointlist, filename);
        if (doTimeTracing()) {recordMethodEnd("WriteXYZPointListToDisc");}
        return result;
    }


    // net.haesleinhuepf.clijx.plugins.AverageAngleBetweenAdjacentTriangles
    //----------------------------------------------------
    /**
     * Takes a pointlist and a touch matrix to determine the average angle of adjacent triangles in a surface mesh. For every point, the average angle of adjacent triangles is saved.
     */
    default boolean averageAngleBetweenAdjacentTriangles(ClearCLBuffer pointlist, ClearCLBuffer touch_matrix, ClearCLBuffer average_distancelist_destination) {
        if (doTimeTracing()) {recordMethodStart("AverageAngleBetweenAdjacentTriangles");}
        boolean result = AverageAngleBetweenAdjacentTriangles.averageAngleBetweenAdjacentTriangles(getCLIJx(), pointlist, touch_matrix, average_distancelist_destination);
        if (doTimeTracing()) {recordMethodEnd("AverageAngleBetweenAdjacentTriangles");}
        return result;
    }


    // net.haesleinhuepf.clijx.plugins.tenengradfusion.TenengradFusion
    //----------------------------------------------------
    /**
     * Fuses #n# image stacks using Tenengrads algorithm.
     */
    default boolean tenengradFusion(ClearCLBuffer arg1, float[] arg2, float arg3, ClearCLBuffer[] arg4) {
        if (doTimeTracing()) {recordMethodStart("TenengradFusion");}
        boolean result = TenengradFusion.tenengradFusion(getCLIJx(), arg1, arg2, arg3, arg4);
        if (doTimeTracing()) {recordMethodEnd("TenengradFusion");}
        return result;
    }

    /**
     * Fuses #n# image stacks using Tenengrads algorithm.
     */
    default boolean tenengradFusion(ClearCLBuffer arg1, ClearCLBuffer arg2, double arg3, double arg4, double arg5, double arg6, double arg7) {
        if (doTimeTracing()) {recordMethodStart("TenengradFusion");}
        boolean result = TenengradFusion.tenengradFusion(getCLIJx(), arg1, arg2, new Double (arg3).intValue(), new Double (arg4).floatValue(), new Double (arg5).floatValue(), new Double (arg6).floatValue(), new Double (arg7).floatValue());
        if (doTimeTracing()) {recordMethodEnd("TenengradFusion");}
        return result;
    }


    // net.haesleinhuepf.clijx.plugins.Skeletonize
    //----------------------------------------------------
    /**
     * Erodes a binary image until just its skeleton is left. 
     * 
     * The result is similar to Skeletonize3D in Fiji.
     */
    default boolean skeletonize(ClearCLBuffer source, ClearCLBuffer destination) {
        if (doTimeTracing()) {recordMethodStart("Skeletonize");}
        boolean result = Skeletonize.skeletonize(getCLIJ2(), source, destination);
        if (doTimeTracing()) {recordMethodEnd("Skeletonize");}
        return result;
    }


    // net.haesleinhuepf.clijx.plugins.PushTile
    //----------------------------------------------------
    /**
     * Push a tile in an image specified by its name, position and size to GPU memory in order to process it there later.
     */
    default ClearCLBuffer pushTile(ImagePlus arg1, int arg2, int arg3, int arg4, int arg5, int arg6, int arg7, int arg8, int arg9, int arg10) {
        if (doTimeTracing()) {recordMethodStart("PushTile");}
        ClearCLBuffer result = PushTile.pushTile(getCLIJ2(), arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10);
        if (doTimeTracing()) {recordMethodEnd("PushTile");}
        return result;
    }

    /**
     * Push a tile in an image specified by its name, position and size to GPU memory in order to process it there later.
     */
    default ClearCLBuffer pushTile(ClearCLBuffer arg1, int arg2, int arg3, int arg4, int arg5, int arg6, int arg7, int arg8, int arg9, int arg10) {
        if (doTimeTracing()) {recordMethodStart("PushTile");}
        ClearCLBuffer result = PushTile.pushTile(getCLIJ2(), arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10);
        if (doTimeTracing()) {recordMethodEnd("PushTile");}
        return result;
    }

    /**
     * Push a tile in an image specified by its name, position and size to GPU memory in order to process it there later.
     */
    default void pushTile(ImagePlus arg1, String arg2, int arg3, int arg4, int arg5, int arg6, int arg7, int arg8, int arg9, int arg10, int arg11) {
        if (doTimeTracing()) {recordMethodStart("PushTile");}
        PushTile.pushTile(getCLIJ2(), arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11);
        if (doTimeTracing()) {recordMethodEnd("PushTile");}
    }


    // net.haesleinhuepf.clijx.plugins.PullTile
    //----------------------------------------------------
    /**
     * Pushes a tile in an image specified by its name, position and size from GPU memory.
     */
    default void pullTile(ImagePlus arg1, String arg2, int arg3, int arg4, int arg5, int arg6, int arg7, int arg8, int arg9, int arg10, int arg11) {
        if (doTimeTracing()) {recordMethodStart("PullTile");}
        PullTile.pullTile(getCLIJ2(), arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11);
        if (doTimeTracing()) {recordMethodEnd("PullTile");}
    }

    /**
     * Pushes a tile in an image specified by its name, position and size from GPU memory.
     */
    default void pullTile(ImagePlus arg1, ClearCLBuffer arg2, int arg3, int arg4, int arg5, int arg6, int arg7, int arg8, int arg9, int arg10, int arg11) {
        if (doTimeTracing()) {recordMethodStart("PullTile");}
        PullTile.pullTile(getCLIJ2(), arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11);
        if (doTimeTracing()) {recordMethodEnd("PullTile");}
    }

    /**
     * Pushes a tile in an image specified by its name, position and size from GPU memory.
     */
    default void pullTile(ClearCLBuffer arg1, ClearCLBuffer arg2, int arg3, int arg4, int arg5, int arg6, int arg7, int arg8, int arg9, int arg10, int arg11) {
        if (doTimeTracing()) {recordMethodStart("PullTile");}
        PullTile.pullTile(getCLIJ2(), arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11);
        if (doTimeTracing()) {recordMethodEnd("PullTile");}
    }


    // net.haesleinhuepf.clijx.weka.autocontext.ApplyAutoContextWekaModel
    //----------------------------------------------------
    /**
     * 
     */
    default boolean applyAutoContextWekaModelWithOptions(ClearCLBuffer arg1, ClearCLBuffer arg2, String arg3, String arg4, int arg5) {
        if (doTimeTracing()) {recordMethodStart("ApplyAutoContextWekaModel");}
        boolean result = ApplyAutoContextWekaModel.applyAutoContextWekaModelWithOptions(getCLIJ2(), arg1, arg2, arg3, arg4, arg5);
        if (doTimeTracing()) {recordMethodEnd("ApplyAutoContextWekaModel");}
        return result;
    }


    // net.haesleinhuepf.clijx.weka.autocontext.TrainAutoContextWekaModel
    //----------------------------------------------------
    /**
     * 
     */
    default boolean trainAutoContextWekaModelWithOptions(ClearCLBuffer arg1, ClearCLBuffer arg2, String arg3, String arg4, int arg5, double arg6, double arg7, double arg8) {
        if (doTimeTracing()) {recordMethodStart("TrainAutoContextWekaModel");}
        boolean result = TrainAutoContextWekaModel.trainAutoContextWekaModelWithOptions(getCLIJ2(), arg1, arg2, arg3, arg4, arg5, new Double (arg6).intValue(), new Double (arg7).intValue(), new Double (arg8).intValue());
        if (doTimeTracing()) {recordMethodEnd("TrainAutoContextWekaModel");}
        return result;
    }


    // net.haesleinhuepf.clijx.weka.ApplyWekaModel
    //----------------------------------------------------
    /**
     * Applies a Weka model using functionality of Fijis Trainable Weka Segmentation plugin. 
     * 
     * It takes a 3D feature stack (e.g. first plane original image, second plane blurred, third plane edge image)and applies a pre-trained a Weka model. Take care that the feature stack has been generated in the sameway as for training the model!
     */
    default boolean applyWekaModel(ClearCLBuffer arg1, ClearCLBuffer arg2, CLIJxWeka2 arg3) {
        if (doTimeTracing()) {recordMethodStart("ApplyWekaModel");}
        boolean result = ApplyWekaModel.applyWekaModel(getCLIJ2(), arg1, arg2, arg3);
        if (doTimeTracing()) {recordMethodEnd("ApplyWekaModel");}
        return result;
    }

    /**
     * Applies a Weka model using functionality of Fijis Trainable Weka Segmentation plugin. 
     * 
     * It takes a 3D feature stack (e.g. first plane original image, second plane blurred, third plane edge image)and applies a pre-trained a Weka model. Take care that the feature stack has been generated in the sameway as for training the model!
     */
    default CLIJxWeka2 applyWekaModel(ClearCLBuffer featureStack3D, ClearCLBuffer prediction2D_destination, String loadModelFilename) {
        if (doTimeTracing()) {recordMethodStart("ApplyWekaModel");}
        CLIJxWeka2 result = ApplyWekaModel.applyWekaModel(getCLIJ2(), featureStack3D, prediction2D_destination, loadModelFilename);
        if (doTimeTracing()) {recordMethodEnd("ApplyWekaModel");}
        return result;
    }


    // net.haesleinhuepf.clijx.weka.ApplyWekaToTable
    //----------------------------------------------------
    /**
     * Applies a Weka model using functionality of Fijis Trainable Weka Segmentation plugin. 
     * 
     * It takes a Results Table, sorts its columns by name alphabetically and uses it as extracted features (rows correspond to feature vectors) and applies a pre-trained a Weka model. Take care that the table has been generated in the sameway as for training the model!
     */
    default boolean applyWekaToTable(ResultsTable arg1, String arg2, String arg3) {
        if (doTimeTracing()) {recordMethodStart("ApplyWekaToTable");}
        boolean result = ApplyWekaToTable.applyWekaToTable(getCLIJ2(), arg1, arg2, arg3);
        if (doTimeTracing()) {recordMethodEnd("ApplyWekaToTable");}
        return result;
    }

    /**
     * Applies a Weka model using functionality of Fijis Trainable Weka Segmentation plugin. 
     * 
     * It takes a Results Table, sorts its columns by name alphabetically and uses it as extracted features (rows correspond to feature vectors) and applies a pre-trained a Weka model. Take care that the table has been generated in the sameway as for training the model!
     */
    default boolean applyWekaToTable(ResultsTable arg1, String arg2, CLIJxWeka2 arg3) {
        if (doTimeTracing()) {recordMethodStart("ApplyWekaToTable");}
        boolean result = ApplyWekaToTable.applyWekaToTable(getCLIJ2(), arg1, arg2, arg3);
        if (doTimeTracing()) {recordMethodEnd("ApplyWekaToTable");}
        return result;
    }


    // net.haesleinhuepf.clijx.weka.GenerateFeatureStack
    //----------------------------------------------------
    /**
     * Generates a feature stack for Trainable Weka Segmentation. 
     * 
     * Use this terminology to specifiy which stacks should be generated:
     * * "original" original slice
     * * "GaussianBlur=s" Gaussian blurred image with sigma s
     * * "LaplacianOfGaussian=s" Laplacian of Gaussian blurred image with sigma s
     * * "SobelOfGaussian=s" Sobel filter applied to Gaussian blurred image with sigma s
     * * "minimum=r" local minimum with radius r
     * * "maximum=r" local maximum with radius r
     * * "mean=r" local mean with radius r
     * * "entropy=r" local entropy with radius r
     * * "gradientX" local gradient in X direction
     * * "gradientY" local gradient in Y direction
     * 
     * Use sigma=0 to apply a filter to the original image. Feature definitions are not case sensitive.
     * 
     * Example: "original gaussianBlur=1 gaussianBlur=5 laplacianOfGaussian=1 laplacianOfGaussian=7 entropy=3"
     */
    default boolean generateFeatureStack(ClearCLBuffer input, ClearCLBuffer feature_stack_destination, String feature_definitions) {
        if (doTimeTracing()) {recordMethodStart("GenerateFeatureStack");}
        boolean result = GenerateFeatureStack.generateFeatureStack(getCLIJ2(), input, feature_stack_destination, feature_definitions);
        if (doTimeTracing()) {recordMethodEnd("GenerateFeatureStack");}
        return result;
    }

    /**
     * Generates a feature stack for Trainable Weka Segmentation. 
     * 
     * Use this terminology to specifiy which stacks should be generated:
     * * "original" original slice
     * * "GaussianBlur=s" Gaussian blurred image with sigma s
     * * "LaplacianOfGaussian=s" Laplacian of Gaussian blurred image with sigma s
     * * "SobelOfGaussian=s" Sobel filter applied to Gaussian blurred image with sigma s
     * * "minimum=r" local minimum with radius r
     * * "maximum=r" local maximum with radius r
     * * "mean=r" local mean with radius r
     * * "entropy=r" local entropy with radius r
     * * "gradientX" local gradient in X direction
     * * "gradientY" local gradient in Y direction
     * 
     * Use sigma=0 to apply a filter to the original image. Feature definitions are not case sensitive.
     * 
     * Example: "original gaussianBlur=1 gaussianBlur=5 laplacianOfGaussian=1 laplacianOfGaussian=7 entropy=3"
     */
    default ClearCLBuffer generateFeatureStack(ClearCLBuffer arg1, String arg2) {
        if (doTimeTracing()) {recordMethodStart("GenerateFeatureStack");}
        ClearCLBuffer result = GenerateFeatureStack.generateFeatureStack(getCLIJ2(), arg1, arg2);
        if (doTimeTracing()) {recordMethodEnd("GenerateFeatureStack");}
        return result;
    }


    // net.haesleinhuepf.clijx.weka.TrainWekaModel
    //----------------------------------------------------
    /**
     * Trains a Weka model using functionality of Fijis Trainable Weka Segmentation plugin. 
     * 
     * It takes a 3D feature stack (e.g. first plane original image, second plane blurred, third plane edge image)and trains a Weka model. This model will be saved to disc.
     * The given groundTruth image is supposed to be a label map where pixels with value 1 represent class 1, pixels with value 2 represent class 2 and so on. Pixels with value 0 will be ignored for training.
     */
    default CLIJxWeka2 trainWekaModel(ClearCLBuffer featureStack3D, ClearCLBuffer groundTruth2D, String saveModelFilename) {
        if (doTimeTracing()) {recordMethodStart("TrainWekaModel");}
        CLIJxWeka2 result = TrainWekaModel.trainWekaModel(getCLIJ2(), featureStack3D, groundTruth2D, saveModelFilename);
        if (doTimeTracing()) {recordMethodEnd("TrainWekaModel");}
        return result;
    }


    // net.haesleinhuepf.clijx.weka.TrainWekaFromTable
    //----------------------------------------------------
    /**
     * Trains a Weka model using functionality of Fijis Trainable Weka Segmentation plugin. 
     * 
     * It takes the given Results Table, sorts its columns alphabetically as extracted features (rows correspond to feature vectors) and a given column name containing the ground truth to train a Weka model. This model will be saved to disc.
     * The given groundTruth column is supposed to be numeric with values 1 represent class 1,  value 2 represent class 2 and so on. Value 0 will be ignored for training.
     * 
     * Default values for options are:
     * * trees = 200
     * * features = 2
     * * maxDepth = 0
     */
    default CLIJxWeka2 trainWekaFromTable(ResultsTable arg1, String arg2, double arg3, double arg4, double arg5) {
        if (doTimeTracing()) {recordMethodStart("TrainWekaFromTable");}
        CLIJxWeka2 result = TrainWekaFromTable.trainWekaFromTable(getCLIJ2(), arg1, arg2, new Double (arg3).intValue(), new Double (arg4).intValue(), new Double (arg5).intValue());
        if (doTimeTracing()) {recordMethodEnd("TrainWekaFromTable");}
        return result;
    }

    /**
     * Trains a Weka model using functionality of Fijis Trainable Weka Segmentation plugin. 
     * 
     * It takes the given Results Table, sorts its columns alphabetically as extracted features (rows correspond to feature vectors) and a given column name containing the ground truth to train a Weka model. This model will be saved to disc.
     * The given groundTruth column is supposed to be numeric with values 1 represent class 1,  value 2 represent class 2 and so on. Value 0 will be ignored for training.
     * 
     * Default values for options are:
     * * trees = 200
     * * features = 2
     * * maxDepth = 0
     */
    default CLIJxWeka2 trainWekaFromTable(ResultsTable arg1, String arg2, String arg3, double arg4, double arg5, double arg6) {
        if (doTimeTracing()) {recordMethodStart("TrainWekaFromTable");}
        CLIJxWeka2 result = TrainWekaFromTable.trainWekaFromTable(getCLIJ2(), arg1, arg2, arg3, new Double (arg4).intValue(), new Double (arg5).intValue(), new Double (arg6).intValue());
        if (doTimeTracing()) {recordMethodEnd("TrainWekaFromTable");}
        return result;
    }


    // net.haesleinhuepf.clijx.weka.TrainWekaModelWithOptions
    //----------------------------------------------------
    /**
     * Trains a Weka model using functionality of Fijis Trainable Weka Segmentation plugin. 
     * 
     * It takes a 3D feature stack (e.g. first plane original image, second plane blurred, third plane edge image)and trains a Weka model. This model will be saved to disc.
     * The given groundTruth image is supposed to be a label map where pixels with value 1 represent class 1, pixels with value 2 represent class 2 and so on. Pixels with value 0 will be ignored for training.
     * 
     * Default values for options are:
     * * trees = 200
     * * features = 2
     * * maxDepth = 0
     */
    default CLIJxWeka2 trainWekaModelWithOptions(ClearCLBuffer arg1, ClearCLBuffer arg2, String arg3, double arg4, double arg5, double arg6) {
        if (doTimeTracing()) {recordMethodStart("TrainWekaModelWithOptions");}
        CLIJxWeka2 result = TrainWekaModelWithOptions.trainWekaModelWithOptions(getCLIJ2(), arg1, arg2, arg3, new Double (arg4).intValue(), new Double (arg5).intValue(), new Double (arg6).intValue());
        if (doTimeTracing()) {recordMethodEnd("TrainWekaModelWithOptions");}
        return result;
    }


    // net.haesleinhuepf.clijx.plugins.StartContinuousWebcamAcquisition
    //----------------------------------------------------
    /**
     * Starts acquistion of images from a webcam.
     */
    default boolean startContinuousWebcamAcquisition(double arg1, double arg2, double arg3) {
        if (doTimeTracing()) {recordMethodStart("StartContinuousWebcamAcquisition");}
        boolean result = StartContinuousWebcamAcquisition.startContinuousWebcamAcquisition(getCLIJx(), new Double (arg1).intValue(), new Double (arg2).intValue(), new Double (arg3).intValue());
        if (doTimeTracing()) {recordMethodEnd("StartContinuousWebcamAcquisition");}
        return result;
    }


    // net.haesleinhuepf.clijx.plugins.StopContinuousWebcamAcquisition
    //----------------------------------------------------
    /**
     * Stops continous acquistion from a webcam.
     */
    default boolean stopContinuousWebcamAcquisition(double arg1) {
        if (doTimeTracing()) {recordMethodStart("StopContinuousWebcamAcquisition");}
        boolean result = StopContinuousWebcamAcquisition.stopContinuousWebcamAcquisition(getCLIJx(), new Double (arg1).intValue());
        if (doTimeTracing()) {recordMethodEnd("StopContinuousWebcamAcquisition");}
        return result;
    }


    // net.haesleinhuepf.clijx.plugins.CaptureWebcamImage
    //----------------------------------------------------
    /**
     * Acquires an image (in fact an RGB image stack with three slices) of given size using a webcam. 
     * 
     * It uses the webcam-capture library by Bartosz Firyn.https://github.com/sarxos/webcam-capture
     */
    default boolean captureWebcamImage(ClearCLBuffer arg1, double arg2, double arg3, double arg4) {
        if (doTimeTracing()) {recordMethodStart("CaptureWebcamImage");}
        boolean result = CaptureWebcamImage.captureWebcamImage(getCLIJx(), arg1, new Double (arg2).intValue(), new Double (arg3).intValue(), new Double (arg4).intValue());
        if (doTimeTracing()) {recordMethodEnd("CaptureWebcamImage");}
        return result;
    }


    // net.haesleinhuepf.clijx.plugins.ConvertRGBStackToGraySlice
    //----------------------------------------------------
    /**
     * Converts a three channel image (stack with three slices) to a single channel image (2D image) by multiplying with factors 0.299, 0.587, 0.114.
     */
    default boolean convertRGBStackToGraySlice(ClearCLBuffer stack_source, ClearCLBuffer slice_destination) {
        if (doTimeTracing()) {recordMethodStart("ConvertRGBStackToGraySlice");}
        boolean result = ConvertRGBStackToGraySlice.convertRGBStackToGraySlice(getCLIJx(), stack_source, slice_destination);
        if (doTimeTracing()) {recordMethodEnd("ConvertRGBStackToGraySlice");}
        return result;
    }

}
// 86 methods generated.
