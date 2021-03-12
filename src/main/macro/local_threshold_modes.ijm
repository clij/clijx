// IJ macro to demonstrate the usage of the CLIJx local threshold implementations
// in comparison with the IJ implementations
// Author: @phaub (Peter Haub) 03'2021


// Current state:
// CLIJ and IJ implementation of the LocalThresholdContrast current produce very different results
// because of an error in the IJ implementation
// The current used math in the IJ implementation 
//		Math.abs((int)(a&0xff - b&0xff))
// must be exchanged by something like
//		Math.abs((int)(a&0xff) - (int)(b&0xff))



// Open a test image
// 8bit input image expected

//run("Blobs (25K)");
//open("C:/YourFolder/TestImage.tif");



// Select threshold mode
// 1-Bernsen 2-Contrast 3-Mean 4-Median 5-MidGrey 6-Niblack 7-Phansalkar 8-Sauvola   (Otsu currently not supported in clij)
n = 1



// Define parameter values
radius = 10;
par1 = 0.0;
par2 = 0.0;


// script

input = getTitle();
input32 = "32bit-"+input

run("Duplicate...", "title="+input32);
if (n == 7)  // Phansalkar (see note below)
	run("Enhance Contrast...", "saturated=0 normalize");
run("32-bit");

run("CLIJ2 Macro Extensions", "cl_device=[GeForce GTX 1060]");
run("CLIJ2 Macro Extensions", "cl_device=");
Ext.CLIJ2_clear();


// ****  Local threshold CLIJ implementation  ****
Ext.CLIJ2_push(input32);
imageCLIJout = "local_threshold_";
mode = "";

if (n == 1){   // Bernsen
  mode = "Bernsen";
  imageCLIJout += mode;
  Ext.CLIJx_localThresholdBernsen(input32, imageCLIJout, radius, par1);
}  
else if (n == 2){  // Contrast
  mode = "Contrast";
  imageCLIJout += mode;
  Ext.CLIJx_localThresholdContrast(input32, imageCLIJout, radius);
}  
else if (n == 3){  // Mean
  mode = "Mean";
  imageCLIJout += mode;
  Ext.CLIJx_localThresholdMean(input32, imageCLIJout, radius, par1);
}  
else if (n == 4){  // Median
  mode = "Median";
  imageCLIJout += mode;
  Ext.CLIJx_localThresholdMedian(input32, imageCLIJout, radius, par1);
}  
else if (n == 5){  // MidGrey
  mode = "MidGrey";
  imageCLIJout += mode;
  Ext.CLIJx_localThresholdMidGrey(input32, imageCLIJout, radius, par1);
}  
else if (n == 6){  // Niblack
  mode = "Niblack";
  imageCLIJout += mode;
  Ext.CLIJx_localThresholdNiblack(input32, imageCLIJout, radius, par1, par2);
}  
else if (n == 7){  // Phansalkar
  // NOTE: localThresholdPhansalkar need normalized image
  // To create clijx results similar to the IJ resuts, apply
  //       run("Enhance Contrast...", "saturated=0 normalize");
  // to the 8bit image before converting it to 32bit
  // (see above)
  mode = "Phansalkar";
  imageCLIJout += mode;
  Ext.CLIJx_localThresholdPhansalkarFast(input32, imageCLIJout, radius, par1, par2);
}  
else if (n == 8){  // Sauvola
  mode = "Sauvola";
  imageCLIJout += mode;
  Ext.CLIJx_localThresholdSauvola(input32, imageCLIJout, radius, par1, par2);
}  

Ext.CLIJ2_pull(imageCLIJout);
run("Multiply...", "value=255");
setOption("ScaleConversions", false);
run("8-bit");

selectWindow(input32);
close();


// ****  Local threshold ImageJ implementation  ****
selectWindow(input);

run("Auto Local Threshold", "method="+mode+ " radius="+radius+ " parameter_1="+par1+ " parameter_2="+par2+ " white");


// Compare clij and IJ results
imageCalculator("Difference create", input, imageCLIJout);
rename("Diff CLIJ<>IJ LocalThreshold mode: " + mode);
