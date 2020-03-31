package net.haesleinhuepf.clijx.utilities;
import net.haesleinhuepf.clij2.CLIJ2;
import net.haesleinhuepf.clijx.CLIJx;
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
import net.haesleinhuepf.clijx.plugins.SaveAsTIF;
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
// this is generated code. See src/test/java/net/haesleinhuepf/clijx/codegenerator for details
public abstract interface CLIJxOps {
   CLIJ getCLIJ();
   CLIJ2 getCLIJ2();
   CLIJx getCLIJx();
   

    // net.haesleinhuepf.clij.kernels.Kernels
    //----------------------------------------------------
    /**
     * 
     */
    default boolean differenceOfGaussianSliceBySlice(ClearCLImage arg1, ClearCLImage arg2, double arg3, double arg4, double arg5) {
        return Kernels.differenceOfGaussianSliceBySlice(getCLIJ(), arg1, arg2, new Double (arg3).intValue(), new Double (arg4).floatValue(), new Double (arg5).floatValue());
    }

    /**
     * 
     */
    default boolean convertToImageJBinary(ClearCLBuffer arg1, ClearCLBuffer arg2) {
        return Kernels.convertToImageJBinary(getCLIJ(), arg1, arg2);
    }

    /**
     * 
     */
    default boolean convertToImageJBinary(ClearCLImage arg1, ClearCLImage arg2) {
        return Kernels.convertToImageJBinary(getCLIJ(), arg1, arg2);
    }

    /**
     * Applies Gaussian blur to the input image twice with different sigma values resulting in two images which are then subtracted from each other.
     * 
     * It is recommended to apply this operation to images of type Float (32 bit) as results might be negative.
     */
    default boolean differenceOfGaussian(ClearCLImage arg1, ClearCLImage arg2, double arg3, double arg4, double arg5) {
        return Kernels.differenceOfGaussian(getCLIJ(), arg1, arg2, new Double (arg3).intValue(), new Double (arg4).floatValue(), new Double (arg5).floatValue());
    }

    /**
     * 
     */
    default boolean detectOptimaSliceBySlice(ClearCLImage arg1, ClearCLImage arg2, double arg3, boolean arg4) {
        return Kernels.detectOptimaSliceBySlice(getCLIJ(), arg1, arg2, new Double (arg3).intValue(), arg4);
    }

    /**
     * 
     */
    default boolean detectOptimaSliceBySlice(ClearCLBuffer arg1, ClearCLBuffer arg2, double arg3, boolean arg4) {
        return Kernels.detectOptimaSliceBySlice(getCLIJ(), arg1, arg2, new Double (arg3).intValue(), arg4);
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
        return Kernels.maximumXYZProjection(getCLIJ(), arg1, arg2, new Double (arg3).intValue(), new Double (arg4).intValue(), new Double (arg5).intValue());
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
        return Kernels.maximumXYZProjection(getCLIJ(), arg1, arg2, new Double (arg3).intValue(), new Double (arg4).intValue(), new Double (arg5).intValue());
    }

    /**
     * 
     */
    default boolean multiplySliceBySliceWithScalars(ClearCLImage arg1, ClearCLImage arg2, float[] arg3) {
        return Kernels.multiplySliceBySliceWithScalars(getCLIJ(), arg1, arg2, arg3);
    }

    /**
     * 
     */
    default boolean multiplySliceBySliceWithScalars(ClearCLBuffer arg1, ClearCLBuffer arg2, float[] arg3) {
        return Kernels.multiplySliceBySliceWithScalars(getCLIJ(), arg1, arg2, arg3);
    }

    /**
     * 
     */
    default double[] sumPixelsSliceBySlice(ClearCLImage arg1) {
        return Kernels.sumPixelsSliceBySlice(getCLIJ(), arg1);
    }

    /**
     * 
     */
    default double[] sumPixelsSliceBySlice(ClearCLBuffer arg1) {
        return Kernels.sumPixelsSliceBySlice(getCLIJ(), arg1);
    }

    /**
     * Deforms an image according to distances provided in the given vector images. It is recommended to use 32-bit images for input, output and vector images. 
     */
    default boolean applyVectorfield(ClearCLBuffer arg1, ClearCLBuffer arg2, ClearCLBuffer arg3, ClearCLBuffer arg4, ClearCLBuffer arg5) {
        return Kernels.applyVectorfield(getCLIJ(), arg1, arg2, arg3, arg4, arg5);
    }

    /**
     * Deforms an image according to distances provided in the given vector images. It is recommended to use 32-bit images for input, output and vector images. 
     */
    default boolean applyVectorfield(ClearCLImage source, ClearCLImage vectorX, ClearCLImage vectorY, ClearCLImage destination) {
        return Kernels.applyVectorfield(getCLIJ(), source, vectorX, vectorY, destination);
    }

    /**
     * Deforms an image according to distances provided in the given vector images. It is recommended to use 32-bit images for input, output and vector images. 
     */
    default boolean applyVectorfield(ClearCLBuffer source, ClearCLBuffer vectorX, ClearCLBuffer vectorY, ClearCLBuffer destination) {
        return Kernels.applyVectorfield(getCLIJ(), source, vectorX, vectorY, destination);
    }

    /**
     * Deforms an image according to distances provided in the given vector images. It is recommended to use 32-bit images for input, output and vector images. 
     */
    default boolean applyVectorfield(ClearCLImage arg1, ClearCLImage arg2, ClearCLImage arg3, ClearCLImage arg4, ClearCLImage arg5) {
        return Kernels.applyVectorfield(getCLIJ(), arg1, arg2, arg3, arg4, arg5);
    }

    /**
     * 
     */
    default boolean detectOptima(ClearCLImage arg1, ClearCLImage arg2, double arg3, boolean arg4) {
        return Kernels.detectOptima(getCLIJ(), arg1, arg2, new Double (arg3).intValue(), arg4);
    }

    /**
     * 
     */
    default boolean detectOptima(ClearCLBuffer arg1, ClearCLBuffer arg2, double arg3, boolean arg4) {
        return Kernels.detectOptima(getCLIJ(), arg1, arg2, new Double (arg3).intValue(), arg4);
    }


    // net.haesleinhuepf.clijx.plugins.CrossCorrelation
    //----------------------------------------------------
    /**
     * Performs cross correlation analysis between two images. The second image is shifted by deltaPos in the given dimension. The cross correlation coefficient is calculated for each pixel in a range around the given pixel with given radius in the given dimension. Together with the original images it is recommended to hand over mean filtered images using the same radius.  
     */
    default boolean crossCorrelation(ClearCLImage arg1, ClearCLImage arg2, ClearCLImage arg3, ClearCLImage arg4, ClearCLImage arg5, double arg6, double arg7, double arg8) {
        return CrossCorrelation.crossCorrelation(getCLIJ(), arg1, arg2, arg3, arg4, arg5, new Double (arg6).intValue(), new Double (arg7).intValue(), new Double (arg8).intValue());
    }

    /**
     * Performs cross correlation analysis between two images. The second image is shifted by deltaPos in the given dimension. The cross correlation coefficient is calculated for each pixel in a range around the given pixel with given radius in the given dimension. Together with the original images it is recommended to hand over mean filtered images using the same radius.  
     */
    default boolean crossCorrelation(ClearCLBuffer arg1, ClearCLBuffer arg2, ClearCLBuffer arg3, ClearCLBuffer arg4, ClearCLBuffer arg5, double arg6, double arg7, double arg8) {
        return CrossCorrelation.crossCorrelation(getCLIJ(), arg1, arg2, arg3, arg4, arg5, new Double (arg6).intValue(), new Double (arg7).intValue(), new Double (arg8).intValue());
    }


    // net.haesleinhuepf.clijx.plugins.Extrema
    //----------------------------------------------------
    /**
     * Returns an image with pixel values most distant from 0: f(x, y) = x if abs(x) > abs(y), y else.
     */
    default boolean extrema(ClearCLBuffer arg1, ClearCLBuffer arg2, ClearCLBuffer arg3) {
        return Extrema.extrema(getCLIJ(), arg1, arg2, arg3);
    }


    // net.haesleinhuepf.clijx.plugins.LocalExtremaBox
    //----------------------------------------------------
    /**
     * Applies a local minimum and maximum filter. Afterwards, the value is returned which is more far from zero.
     */
    default boolean localExtremaBox(ClearCLBuffer arg1, ClearCLBuffer arg2, double arg3, double arg4, double arg5) {
        return LocalExtremaBox.localExtremaBox(getCLIJ(), arg1, arg2, new Double (arg3).intValue(), new Double (arg4).intValue(), new Double (arg5).intValue());
    }


    // net.haesleinhuepf.clijx.plugins.LocalID
    //----------------------------------------------------
    /**
     * local id
     */
    default boolean localID(ClearCLBuffer arg1, ClearCLBuffer arg2) {
        return LocalID.localID(getCLIJ(), arg1, arg2);
    }


    // net.haesleinhuepf.clijx.plugins.Presign
    //----------------------------------------------------
    /**
     * Determines the extrema of pixel values: f(x) = x / abs(x).
     */
    default boolean presign(ClearCLBuffer arg1, ClearCLBuffer arg2) {
        return Presign.presign(getCLIJ(), arg1, arg2);
    }


    // net.haesleinhuepf.clijx.plugins.StackToTiles
    //----------------------------------------------------
    /**
     * Stack to tiles.
     */
    default boolean stackToTiles(ClearCLImageInterface arg1, ClearCLImageInterface arg2, double arg3, double arg4) {
        return StackToTiles.stackToTiles(getCLIJx(), arg1, arg2, new Double (arg3).intValue(), new Double (arg4).intValue());
    }


    // net.haesleinhuepf.clijx.plugins.SubtractBackground2D
    //----------------------------------------------------
    /**
     * Applies Gaussian blur to the input image and subtracts the result from the original image.
     */
    default boolean subtractBackground2D(ClearCLImageInterface arg1, ClearCLImageInterface arg2, double arg3, double arg4) {
        return SubtractBackground2D.subtractBackground2D(getCLIJx(), arg1, arg2, new Double (arg3).floatValue(), new Double (arg4).floatValue());
    }

    /**
     * Applies Gaussian blur to the input image and subtracts the result from the original image.
     */
    default boolean subtractBackground(ClearCLImageInterface arg1, ClearCLImageInterface arg2, double arg3, double arg4) {
        return SubtractBackground2D.subtractBackground(getCLIJx(), arg1, arg2, new Double (arg3).floatValue(), new Double (arg4).floatValue());
    }


    // net.haesleinhuepf.clijx.plugins.SubtractBackground3D
    //----------------------------------------------------
    /**
     * Applies Gaussian blur to the input image and subtracts the result from the original image.
     */
    default boolean subtractBackground3D(ClearCLImageInterface arg1, ClearCLImageInterface arg2, double arg3, double arg4, double arg5) {
        return SubtractBackground3D.subtractBackground3D(getCLIJx(), arg1, arg2, new Double (arg3).floatValue(), new Double (arg4).floatValue(), new Double (arg5).floatValue());
    }

    /**
     * Applies Gaussian blur to the input image and subtracts the result from the original image.
     */
    default boolean subtractBackground(ClearCLImageInterface arg1, ClearCLImageInterface arg2, double arg3, double arg4, double arg5) {
        return SubtractBackground3D.subtractBackground(getCLIJx(), arg1, arg2, new Double (arg3).floatValue(), new Double (arg4).floatValue(), new Double (arg5).floatValue());
    }


    // net.haesleinhuepf.clijx.piv.FastParticleImageVelocimetry
    //----------------------------------------------------
    /**
     * 
     */
    default boolean particleImageVelocimetry2D(ClearCLImage arg1, ClearCLImage arg2, ClearCLImage arg3, ClearCLImage arg4, double arg5) {
        return FastParticleImageVelocimetry.particleImageVelocimetry2D(getCLIJ(), arg1, arg2, arg3, arg4, new Double (arg5).intValue());
    }

    /**
     * 
     */
    default boolean particleImageVelocimetry2D(ClearCLBuffer arg1, ClearCLBuffer arg2, ClearCLBuffer arg3, ClearCLBuffer arg4, double arg5) {
        return FastParticleImageVelocimetry.particleImageVelocimetry2D(getCLIJ(), arg1, arg2, arg3, arg4, new Double (arg5).intValue());
    }


    // net.haesleinhuepf.clijx.piv.ParticleImageVelocimetry
    //----------------------------------------------------
    /**
     * For every pixel in source image 1, determine the pixel with the most similar intensity in 
     *  the local neighborhood with a given radius in source image 2. Write the distance in 
     * X and Y in the two corresponding destination images.
     */
    default boolean particleImageVelocimetry(ClearCLBuffer arg1, ClearCLBuffer arg2, ClearCLBuffer arg3, ClearCLBuffer arg4, ClearCLBuffer arg5, double arg6, double arg7, double arg8, boolean arg9) {
        return ParticleImageVelocimetry.particleImageVelocimetry(getCLIJ(), arg1, arg2, arg3, arg4, arg5, new Double (arg6).intValue(), new Double (arg7).intValue(), new Double (arg8).intValue(), arg9);
    }


    // net.haesleinhuepf.clijx.piv.ParticleImageVelocimetryTimelapse
    //----------------------------------------------------
    /**
     * Run particle image velocimetry on a 2D+t timelapse.
     */
    default boolean particleImageVelocimetryTimelapse(ClearCLBuffer arg1, ClearCLBuffer arg2, ClearCLBuffer arg3, ClearCLBuffer arg4, double arg5, double arg6, double arg7, boolean arg8) {
        return ParticleImageVelocimetryTimelapse.particleImageVelocimetryTimelapse(getCLIJ(), arg1, arg2, arg3, arg4, new Double (arg5).intValue(), new Double (arg6).intValue(), new Double (arg7).intValue(), arg8);
    }


    // net.haesleinhuepf.clijx.registration.DeformableRegistration2D
    //----------------------------------------------------
    /**
     * Applies particle image velocimetry to two images and registers them afterwards by warping input image 2 with a smoothed vector field.
     */
    default boolean deformableRegistration2D(ClearCLBuffer arg1, ClearCLBuffer arg2, ClearCLBuffer arg3, double arg4, double arg5) {
        return DeformableRegistration2D.deformableRegistration2D(getCLIJ(), arg1, arg2, arg3, new Double (arg4).intValue(), new Double (arg5).intValue());
    }


    // net.haesleinhuepf.clijx.registration.TranslationRegistration
    //----------------------------------------------------
    /**
     * Measures center of mass of thresholded objects in the two input images and translates the second image so that it better fits to the first image.
     */
    default boolean translationRegistration(ClearCLBuffer arg1, ClearCLBuffer arg2, ClearCLBuffer arg3) {
        return TranslationRegistration.translationRegistration(getCLIJ(), arg1, arg2, arg3);
    }

    /**
     * Measures center of mass of thresholded objects in the two input images and translates the second image so that it better fits to the first image.
     */
    default boolean translationRegistration(ClearCLBuffer arg1, ClearCLBuffer arg2, double[] arg3) {
        return TranslationRegistration.translationRegistration(getCLIJ(), arg1, arg2, arg3);
    }


    // net.haesleinhuepf.clijx.registration.TranslationTimelapseRegistration
    //----------------------------------------------------
    /**
     * Applies 2D translation registration to every pair of t, t+1 slices of a 2D+t image stack.
     */
    default boolean translationTimelapseRegistration(ClearCLBuffer arg1, ClearCLBuffer arg2) {
        return TranslationTimelapseRegistration.translationTimelapseRegistration(getCLIJ(), arg1, arg2);
    }


    // net.haesleinhuepf.clijx.io.ReadImageFromDisc
    //----------------------------------------------------
    /**
     * Read an image from disc.
     */
    default ClearCLBuffer readImageFromDisc(String arg1) {
        return ReadImageFromDisc.readImageFromDisc(getCLIJ(), arg1);
    }


    // net.haesleinhuepf.clijx.io.ReadRawImageFromDisc
    //----------------------------------------------------
    /**
     * Reads a raw file from disc and pushes it immediately to the GPU.
     */
    default ClearCLBuffer readRawImageFromDisc(String arg1, double arg2, double arg3, double arg4, double arg5) {
        return ReadRawImageFromDisc.readRawImageFromDisc(getCLIJ(), arg1, new Double (arg2).intValue(), new Double (arg3).intValue(), new Double (arg4).intValue(), new Double (arg5).intValue());
    }

    /**
     * Reads a raw file from disc and pushes it immediately to the GPU.
     */
    default boolean readRawImageFromDisc(ClearCLBuffer arg1, String arg2) {
        return ReadRawImageFromDisc.readRawImageFromDisc(getCLIJ(), arg1, arg2);
    }


    // net.haesleinhuepf.clijx.io.PreloadFromDisc
    //----------------------------------------------------
    /**
     * This plugin takes two image filenames and loads them into RAM. The first image is returned immediately, the second image is loaded in the background and  will be returned when the plugin is called again.
     * 
     *  It is assumed that all images have the same size. If this is not the case, call release(image) before  getting the second image.
     */
    default ClearCLBuffer preloadFromDisc(ClearCLBuffer destination, String filename, String nextFilename, String loaderId) {
        return PreloadFromDisc.preloadFromDisc(getCLIJ(), destination, filename, nextFilename, loaderId);
    }


    // net.haesleinhuepf.clijx.plugins.GaussJordan
    //----------------------------------------------------
    /**
     * Gauss Jordan elimination algorithm for solving linear equation systems. Ent the equation coefficients as an n*n sized image A and an n*1 sized image B:
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
    default boolean gaussJordan(ClearCLBuffer arg1, ClearCLBuffer arg2, ClearCLBuffer arg3) {
        return GaussJordan.gaussJordan(getCLIJ(), arg1, arg2, arg3);
    }


    // net.haesleinhuepf.clijx.plugins.StopWatch
    //----------------------------------------------------
    /**
     * Measures time and outputs delay to last call.
     */
    default boolean stopWatch(String text) {
        return StopWatch.stopWatch(getCLIJ(), text);
    }


    // net.haesleinhuepf.clijx.plugins.DrawTwoValueLine
    //----------------------------------------------------
    /**
     * Draws a line between two points with a given thickness. Pixels close to point 1 are set to value1. Pixels closer to point 2 are set to value2 All pixels other than on the line are untouched. Consider using clij.set(buffer, 0); in advance.
     */
    default boolean drawTwoValueLine(ClearCLBuffer arg1, double arg2, double arg3, double arg4, double arg5, double arg6, double arg7, double arg8, double arg9, double arg10) {
        return DrawTwoValueLine.drawTwoValueLine(getCLIJx(), arg1, new Double (arg2).floatValue(), new Double (arg3).floatValue(), new Double (arg4).floatValue(), new Double (arg5).floatValue(), new Double (arg6).floatValue(), new Double (arg7).floatValue(), new Double (arg8).floatValue(), new Double (arg9).floatValue(), new Double (arg10).floatValue());
    }


    // net.haesleinhuepf.clijx.plugins.SaveAsTIF
    //----------------------------------------------------
    /**
     * Pulls an image from the GPU memory and saves it as TIF to disc.
     */
    default boolean saveAsTIF(ClearCLBuffer input, String filename) {
        return SaveAsTIF.saveAsTIF(getCLIJ(), input, filename);
    }


    // net.haesleinhuepf.clijx.plugins.ConnectedComponentsLabelingInplace
    //----------------------------------------------------
    /**
     * Performs connected components analysis to a binary image and generates a label map.
     */
    default boolean connectedComponentsLabelingInplace(ClearCLBuffer binary_source_labeling_destination) {
        return ConnectedComponentsLabelingInplace.connectedComponentsLabelingInplace(getCLIJx(), binary_source_labeling_destination);
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
        return AutomaticThresholdInplace.automaticThresholdInplace(getCLIJx(), input_and_destination, method);
    }


    // net.haesleinhuepf.clijx.plugins.DifferenceOfGaussianInplace3D
    //----------------------------------------------------
    /**
     * Applies Gaussian blur to the input image twice with different sigma values resulting in two images which are then subtracted from each other.
     * 
     * It is recommended to apply this operation to images of type Float (32 bit) as results might be negative.
     */
    default boolean differenceOfGaussianInplace3D(ClearCLBuffer arg1, double arg2, double arg3, double arg4, double arg5, double arg6, double arg7) {
        return DifferenceOfGaussianInplace3D.differenceOfGaussianInplace3D(getCLIJ(), arg1, new Double (arg2).floatValue(), new Double (arg3).floatValue(), new Double (arg4).floatValue(), new Double (arg5).floatValue(), new Double (arg6).floatValue(), new Double (arg7).floatValue());
    }


    // net.haesleinhuepf.clijx.plugins.AbsoluteInplace
    //----------------------------------------------------
    /**
     * Computes the absolute value of every individual pixel x in a given image.
     * 
     * <pre>f(x) = |x| </pre>
     */
    default boolean absoluteInplace(ClearCLBuffer source_destination) {
        return AbsoluteInplace.absoluteInplace(getCLIJx(), source_destination);
    }


    // net.haesleinhuepf.clijx.plugins.Watershed
    //----------------------------------------------------
    /**
     * Apply a binary watershed to a binary image and introduces black pixels between objects.
     */
    default boolean watershed(ClearCLBuffer arg1, ClearCLBuffer arg2) {
        return Watershed.watershed(getCLIJx(), arg1, arg2);
    }


    // net.haesleinhuepf.clijx.plugins.ShowRGB
    //----------------------------------------------------
    /**
     * Visualises three 2D images as one RGB image
     */
    default boolean showRGB(ClearCLBuffer red, ClearCLBuffer green, ClearCLBuffer blue, String title) {
        return ShowRGB.showRGB(getCLIJ(), red, green, blue, title);
    }


    // net.haesleinhuepf.clijx.plugins.ShowGrey
    //----------------------------------------------------
    /**
     * Visualises a single 2D image.
     */
    default boolean showGrey(ClearCLBuffer input, String title) {
        return ShowGrey.showGrey(getCLIJ(), input, title);
    }


    // net.haesleinhuepf.clijx.gui.OrganiseWindows
    //----------------------------------------------------
    /**
     * Organises windows on screen.
     */
    default boolean organiseWindows(double arg1, double arg2, double arg3, double arg4, double arg5, double arg6) {
        return OrganiseWindows.organiseWindows(getCLIJ(), new Double (arg1).intValue(), new Double (arg2).intValue(), new Double (arg3).intValue(), new Double (arg4).intValue(), new Double (arg5).intValue(), new Double (arg6).intValue());
    }


    // net.haesleinhuepf.clijx.plugins.TopHatOctagon
    //----------------------------------------------------
    /**
     * Applies a minimum filter with kernel size 3x3 n times to an image iteratively. Odd iterations are done with box neighborhood, even iterations with a diamond. Thus, with n > 2, the filter shape is an octagon. The given number of iterations - 2 makes the filter result very similar to minimum sphere.
     */
    default boolean topHatOctagon(ClearCLBuffer arg1, ClearCLBuffer arg2, double arg3) {
        return TopHatOctagon.topHatOctagon(getCLIJx(), arg1, arg2, new Double (arg3).intValue());
    }


    // net.haesleinhuepf.clijx.plugins.ShowGlasbeyOnGrey
    //----------------------------------------------------
    /**
     * Visualises two 2D images as one RGB image. The first channel is shown in grey, the second with glasbey LUT.
     */
    default boolean showGlasbeyOnGrey(ClearCLBuffer red, ClearCLBuffer labelling, String title) {
        return ShowGlasbeyOnGrey.showGlasbeyOnGrey(getCLIJ(), red, labelling, title);
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
        return BlurSliceBySlice.blurSliceBySlice(getCLIJx(), arg1, arg2, new Double (arg3).floatValue(), new Double (arg4).floatValue());
    }

    /**
     * Computes the Gaussian blurred image of an image given two sigma values in X and Y. Thus, the filterkernel can have non-isotropic shape.
     * 
     * The Gaussian blur is applied slice by slice in 2D.
     */
    @Deprecated
    default boolean blurSliceBySlice(ClearCLImageInterface arg1, ClearCLImageInterface arg2, double arg3, double arg4, double arg5, double arg6) {
        return BlurSliceBySlice.blurSliceBySlice(getCLIJx(), arg1, arg2, new Double (arg3).intValue(), new Double (arg4).intValue(), new Double (arg5).floatValue(), new Double (arg6).floatValue());
    }


    // net.haesleinhuepf.clijx.plugins.splitstack.AbstractSplitStack
    //----------------------------------------------------

    // net.haesleinhuepf.clijx.plugins.TopHatOctagonSliceBySlice
    //----------------------------------------------------
    /**
     * Applies a minimum filter with kernel size 3x3 n times to an image iteratively. Odd iterations are done with box neighborhood, even iterations with a diamond. Thus, with n > 2, the filter shape is an octagon. The given number of iterations - 2 makes the filter result very similar to minimum sphere.
     */
    default boolean topHatOctagonSliceBySlice(ClearCLBuffer arg1, ClearCLBuffer arg2, double arg3) {
        return TopHatOctagonSliceBySlice.topHatOctagonSliceBySlice(getCLIJx(), arg1, arg2, new Double (arg3).intValue());
    }


    // net.haesleinhuepf.clijx.io.WriteVTKLineListToDisc
    //----------------------------------------------------
    /**
     * Takes a point list image representing n points (n*2 for 2D points, n*3 for 3D points) and a corresponding touch matrix , sized (n+1)*(n+1), and exports them in VTK format.
     */
    default boolean writeVTKLineListToDisc(ClearCLBuffer pointlist, ClearCLBuffer touch_matrix, String filename) {
        return WriteVTKLineListToDisc.writeVTKLineListToDisc(getCLIJx(), pointlist, touch_matrix, filename);
    }


    // net.haesleinhuepf.clijx.io.WriteXYZPointListToDisc
    //----------------------------------------------------
    /**
     * Takes a point list image representing n points (n*2 for 2D points, n*3 for 3D points) and exports them in XYZ format.
     */
    default boolean writeXYZPointListToDisc(ClearCLBuffer pointlist, String filename) {
        return WriteXYZPointListToDisc.writeXYZPointListToDisc(getCLIJx(), pointlist, filename);
    }


    // net.haesleinhuepf.clijx.plugins.AverageAngleBetweenAdjacentTriangles
    //----------------------------------------------------
    /**
     * Takes a pointlist and a touch matrix to determine the average angle of adjacent triangles in a surface mesh. For every point, the average angle of adjacent triangles is saved.
     */
    default boolean averageAngleBetweenAdjacentTriangles(ClearCLBuffer arg1, ClearCLBuffer arg2, ClearCLBuffer arg3) {
        return AverageAngleBetweenAdjacentTriangles.averageAngleBetweenAdjacentTriangles(getCLIJx(), arg1, arg2, arg3);
    }


    // net.haesleinhuepf.clijx.plugins.tenengradfusion.TenengradFusion
    //----------------------------------------------------
    /**
     * Fuses #n# image stacks using Tenengrads algorithm.
     */
    default boolean tenengradFusion(ClearCLBuffer arg1, ClearCLBuffer arg2, double arg3, double arg4, double arg5, double arg6, double arg7) {
        return TenengradFusion.tenengradFusion(getCLIJx(), arg1, arg2, new Double (arg3).intValue(), new Double (arg4).floatValue(), new Double (arg5).floatValue(), new Double (arg6).floatValue(), new Double (arg7).floatValue());
    }

    /**
     * Fuses #n# image stacks using Tenengrads algorithm.
     */
    default boolean tenengradFusion(ClearCLBuffer arg1, float[] arg2, float arg3, ClearCLBuffer[] arg4) {
        return TenengradFusion.tenengradFusion(getCLIJx(), arg1, arg2, arg3, arg4);
    }


    // net.haesleinhuepf.clijx.plugins.Skeletonize
    //----------------------------------------------------
    /**
     * Erodes a binary image until just its skeleton is left. The result is similar to Skeletonize3D in Fiji.
     */
    default boolean skeletonize(ClearCLBuffer arg1, ClearCLBuffer arg2) {
        return Skeletonize.skeletonize(getCLIJ2(), arg1, arg2);
    }


    // net.haesleinhuepf.clijx.plugins.PushTile
    //----------------------------------------------------
    /**
     * Copies a tile in an image specified by its name, position and size to GPU memory in order to process it there later.
     */
    default ClearCLBuffer pushTile(ClearCLBuffer arg1, int arg2, int arg3, int arg4, int arg5, int arg6, int arg7, int arg8, int arg9, int arg10) {
        return PushTile.pushTile(getCLIJ2(), arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10);
    }

    /**
     * Copies a tile in an image specified by its name, position and size to GPU memory in order to process it there later.
     */
    default void pushTile(ImagePlus arg1, String arg2, int arg3, int arg4, int arg5, int arg6, int arg7, int arg8, int arg9, int arg10, int arg11) {
        PushTile.pushTile(getCLIJ2(), arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11);
    }

    /**
     * Copies a tile in an image specified by its name, position and size to GPU memory in order to process it there later.
     */
    default ClearCLBuffer pushTile(ImagePlus arg1, int arg2, int arg3, int arg4, int arg5, int arg6, int arg7, int arg8, int arg9, int arg10) {
        return PushTile.pushTile(getCLIJ2(), arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10);
    }


    // net.haesleinhuepf.clijx.plugins.PullTile
    //----------------------------------------------------
    /**
     * Copies a tile in an image specified by its name, position and size to GPU memory in order to process it there later.
     */
    default void pullTile(ClearCLBuffer arg1, ClearCLBuffer arg2, int arg3, int arg4, int arg5, int arg6, int arg7, int arg8, int arg9, int arg10, int arg11) {
        PullTile.pullTile(getCLIJ2(), arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11);
    }

    /**
     * Copies a tile in an image specified by its name, position and size to GPU memory in order to process it there later.
     */
    default void pullTile(ImagePlus arg1, ClearCLBuffer arg2, int arg3, int arg4, int arg5, int arg6, int arg7, int arg8, int arg9, int arg10, int arg11) {
        PullTile.pullTile(getCLIJ2(), arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11);
    }

    /**
     * Copies a tile in an image specified by its name, position and size to GPU memory in order to process it there later.
     */
    default void pullTile(ImagePlus arg1, String arg2, int arg3, int arg4, int arg5, int arg6, int arg7, int arg8, int arg9, int arg10, int arg11) {
        PullTile.pullTile(getCLIJ2(), arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11);
    }


    // net.haesleinhuepf.clijx.weka.autocontext.ApplyAutoContextWekaModel
    //----------------------------------------------------
    /**
     * 
     */
    default boolean applyAutoContextWekaModelWithOptions(ClearCLBuffer arg1, ClearCLBuffer arg2, String arg3, String arg4, int arg5) {
        return ApplyAutoContextWekaModel.applyAutoContextWekaModelWithOptions(getCLIJ2(), arg1, arg2, arg3, arg4, arg5);
    }


    // net.haesleinhuepf.clijx.weka.autocontext.TrainAutoContextWekaModel
    //----------------------------------------------------
    /**
     * 
     */
    default boolean trainAutoContextWekaModelWithOptions(ClearCLBuffer arg1, ClearCLBuffer arg2, String arg3, String arg4, int arg5, double arg6, double arg7, double arg8) {
        return TrainAutoContextWekaModel.trainAutoContextWekaModelWithOptions(getCLIJ2(), arg1, arg2, arg3, arg4, arg5, new Double (arg6).intValue(), new Double (arg7).intValue(), new Double (arg8).intValue());
    }


    // net.haesleinhuepf.clijx.weka.ApplyWekaModel
    //----------------------------------------------------
    /**
     * Applies a Weka model using functionality of Fijis Trainable Weka Segmentation plugin.
     * It takes a 3D feature stack (e.g. first plane original image, second plane blurred, third plane edge image)and applies a pre-trained a Weka model. Take care that the feature stack has been generated in the sameway as for training the model!
     */
    default boolean applyWekaModel(ClearCLBuffer arg1, ClearCLBuffer arg2, CLIJxWeka2 arg3) {
        return ApplyWekaModel.applyWekaModel(getCLIJ2(), arg1, arg2, arg3);
    }

    /**
     * Applies a Weka model using functionality of Fijis Trainable Weka Segmentation plugin.
     * It takes a 3D feature stack (e.g. first plane original image, second plane blurred, third plane edge image)and applies a pre-trained a Weka model. Take care that the feature stack has been generated in the sameway as for training the model!
     */
    default CLIJxWeka2 applyWekaModel(ClearCLBuffer featureStack3D, ClearCLBuffer prediction2D_destination, String loadModelFilename) {
        return ApplyWekaModel.applyWekaModel(getCLIJ2(), featureStack3D, prediction2D_destination, loadModelFilename);
    }


    // net.haesleinhuepf.clijx.weka.ApplyWekaToTable
    //----------------------------------------------------
    /**
     * Applies a Weka model using functionality of Fijis Trainable Weka Segmentation plugin.
     * It takes a Results Table, sorts its columns by name alphabetically and uses it as extracted features (rows correspond to feature vectors) and applies a pre-trained a Weka model. Take care that the table has been generated in the sameway as for training the model!
     */
    default boolean applyWekaToTable(ResultsTable arg1, String arg2, CLIJxWeka2 arg3) {
        return ApplyWekaToTable.applyWekaToTable(getCLIJ2(), arg1, arg2, arg3);
    }

    /**
     * Applies a Weka model using functionality of Fijis Trainable Weka Segmentation plugin.
     * It takes a Results Table, sorts its columns by name alphabetically and uses it as extracted features (rows correspond to feature vectors) and applies a pre-trained a Weka model. Take care that the table has been generated in the sameway as for training the model!
     */
    default boolean applyWekaToTable(ResultsTable arg1, String arg2, String arg3) {
        return ApplyWekaToTable.applyWekaToTable(getCLIJ2(), arg1, arg2, arg3);
    }


    // net.haesleinhuepf.clijx.weka.GenerateFeatureStack
    //----------------------------------------------------
    /**
     * Generates a feature stack for Trainable Weka Segmentation. Use this terminology to specifiy which stacks should be generated:
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
        return GenerateFeatureStack.generateFeatureStack(getCLIJ2(), input, feature_stack_destination, feature_definitions);
    }

    /**
     * Generates a feature stack for Trainable Weka Segmentation. Use this terminology to specifiy which stacks should be generated:
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
        return GenerateFeatureStack.generateFeatureStack(getCLIJ2(), arg1, arg2);
    }


    // net.haesleinhuepf.clijx.weka.TrainWekaModel
    //----------------------------------------------------
    /**
     * Trains a Weka model using functionality of Fijis Trainable Weka Segmentation plugin.
     * It takes a 3D feature stack (e.g. first plane original image, second plane blurred, third plane edge image)and trains a Weka model. This model will be saved to disc.
     * The given groundTruth image is supposed to be a label map where pixels with value 1 represent class 1, pixels with value 2 represent class 2 and so on. Pixels with value 0 will be ignored for training.
     */
    default CLIJxWeka2 trainWekaModel(ClearCLBuffer featureStack3D, ClearCLBuffer groundTruth2D, String saveModelFilename) {
        return TrainWekaModel.trainWekaModel(getCLIJ2(), featureStack3D, groundTruth2D, saveModelFilename);
    }


    // net.haesleinhuepf.clijx.weka.TrainWekaFromTable
    //----------------------------------------------------
    /**
     * Trains a Weka model using functionality of Fijis Trainable Weka Segmentation plugin.
     * It takes the given Results Table, sorts its columns alphabetically as extracted features (rows correspond to feature vectors) and a given column name containing the ground truth to train a Weka model. This model will be saved to disc.
     * The given groundTruth column is supposed to be numeric with values 1 represent class 1,  value 2 represent class 2 and so on. Value 0 will be ignored for training.
     * 
     * Default values for options are:
     * * trees = 200
     * * features = 2
     * * maxDepth = 0
     */
    default CLIJxWeka2 trainWekaFromTable(ResultsTable arg1, String arg2, String arg3, double arg4, double arg5, double arg6) {
        return TrainWekaFromTable.trainWekaFromTable(getCLIJ2(), arg1, arg2, arg3, new Double (arg4).intValue(), new Double (arg5).intValue(), new Double (arg6).intValue());
    }

    /**
     * Trains a Weka model using functionality of Fijis Trainable Weka Segmentation plugin.
     * It takes the given Results Table, sorts its columns alphabetically as extracted features (rows correspond to feature vectors) and a given column name containing the ground truth to train a Weka model. This model will be saved to disc.
     * The given groundTruth column is supposed to be numeric with values 1 represent class 1,  value 2 represent class 2 and so on. Value 0 will be ignored for training.
     * 
     * Default values for options are:
     * * trees = 200
     * * features = 2
     * * maxDepth = 0
     */
    default CLIJxWeka2 trainWekaFromTable(ResultsTable arg1, String arg2, double arg3, double arg4, double arg5) {
        return TrainWekaFromTable.trainWekaFromTable(getCLIJ2(), arg1, arg2, new Double (arg3).intValue(), new Double (arg4).intValue(), new Double (arg5).intValue());
    }


    // net.haesleinhuepf.clijx.weka.TrainWekaModelWithOptions
    //----------------------------------------------------
    /**
     * Trains a Weka model using functionality of Fijis Trainable Weka Segmentation plugin.
     * It takes a 3D feature stack (e.g. first plane original image, second plane blurred, third plane edge image)and trains a Weka model. This model will be saved to disc.
     * The given groundTruth image is supposed to be a label map where pixels with value 1 represent class 1, pixels with value 2 represent class 2 and so on. Pixels with value 0 will be ignored for training.
     * 
     * Default values for options are:
     * * trees = 200
     * * features = 2
     * * maxDepth = 0
     */
    default CLIJxWeka2 trainWekaModelWithOptions(ClearCLBuffer arg1, ClearCLBuffer arg2, String arg3, double arg4, double arg5, double arg6) {
        return TrainWekaModelWithOptions.trainWekaModelWithOptions(getCLIJ2(), arg1, arg2, arg3, new Double (arg4).intValue(), new Double (arg5).intValue(), new Double (arg6).intValue());
    }

}
// 82 methods generated.
