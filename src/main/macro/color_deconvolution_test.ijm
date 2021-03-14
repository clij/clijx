// IJ macro to demonstrate the usage of the CLIJx color deconvolution implementation
// in comparison with the IJ plugin ColorDeconvolution2
// Author: @phaub (Peter Haub) 03'2021


// To run this macro the IJ plugin ColorDeconvolution2 has to be installed.
// In Fiji, activate the ColorDeconvolution2 update site in the FIJI Updater.
// Additional information can be found here:
// https://blog.bham.ac.uk/intellimic/g-landini-software/colour-deconvolution-2/

// Open a test image
//RGB color image expected

//run("Colour Deconvolution", "vectors=[H DAB] show");

imgSrc = "RGBstack";
imgOut = "clij_ColorDeconvolution";
imgStainVectors = "StainVectors";


// RGB image expected
newImage("Untitled", "RGB noise", 1000, 1000, 1);
title = getTitle();

// Define Stain vectors
// Following vectors from ColorDeconvolution2 plugin, mode [H DAB], output as float32
// Stain 1 (HTX)
AR1=0.6500286;
AG1=0.704031;
AB1=0.2860126;
// Stain 2 (DAB)
AR2=0.26814753;
AG2=0.57031375;
AB2=0.77642715;
// Residual (vector perpendicular to stain 1 and 2)
AR3=0.6362142;
AG3=-0.7100268;
AB3=0.3018168;

// Create Stain vector matrix
// (see Supplementary Information to
//	Haub, P., Meckel, T. A Model based Survey of Colour Deconvolution in Diagnostic Brightfield Microscopy:
//	Error Estimation and Spectral Consideration. Sci Rep 5, 12096 (2015). https://doi.org/10.1038/srep12096	
// Color vectors matrix A 
//        AR1, AR2, AR3
//  A  =  AG1, AG2, AG3
//        AB1, AB2, AB3
// as float array {AR1, AR2, AR3, AG1, AG2, AG3, AB1, AB2, AB3}

matSV = newArray(AR1, AR2, AR3, AG1, AG2, AG3, AB1, AB2, AB3);

// Create Stain vector image
newImage(imgStainVectors, "32-bit black", 9, 1, 1);
for (i=0; i<matSV.length; i++)
	setPixel(i, 0, matSV[i]);

// create dublicate of original image and convert to RGB float stack (to force output as float)
selectWindow(title);
run("Duplicate...", "title="+imgSrc);
run("RGB Stack");
run("32-bit");


// color deconvolution on GPU

run("CLIJ2 Macro Extensions", "cl_device=");
// in my case
//run("CLIJ2 Macro Extensions", "cl_device=[GeForce GTX 1060]");

Ext.CLIJ2_clear();

time = getTime();

Ext.CLIJ2_push(imgSrc);
Ext.CLIJ2_push(imgStainVectors);

Ext.CLIJx_colorDeconvolution(imgSrc, imgStainVectors, imgOut);

Ext.CLIJ2_pull(imgOut);

deltaTime = (getTime() - time);
print("clijx GPU ColorDeconvolution took " + deltaTime + " ms");

Ext.CLIJ2_clear();


selectWindow(imgSrc);
close();
selectWindow(imgStainVectors);
close();


// ImageJ ColorDeconvolution2 plugin 

selectWindow(title);

time = getTime();

run("Colour Deconvolution2", "vectors=[H DAB] output=32bit_Absorbance cross show hide");

deltaTime = (getTime() - time);
print("IJ plugin ColorDeconvolution2 took " + deltaTime + " ms");


imgC1 = title + "-(Colour_1)A";
imgC2 = title + "-(Colour_2)A";
imgC3 = title + "-(Colour_3)A";

run("Concatenate...", "open image1="+imgC1+ " image2="+imgC2+ " image3="+imgC3+ " image4=[-- None --]");
rename("IJ_ColorDeconvolution2");

// Compare clij and IJ result
imageCalculator("Difference create stack", "clij_ColorDeconvolution","IJ_ColorDeconvolution2");
