// CLIJ example macro: parametric_watershed.ijm
//
// This macro shows how use parametric watershed 
// for splitting objects in a binary image
//
// Author: Robert Haase
// 		   July 2020
// ---------------------------------------------

// clean up first
run("Close All");

// init GPU
run("CLIJ2 Macro Extensions", "cl_device=");
Ext.CLIJ2_clear();

// load example data and push it to the GPU
run("Blobs (25K)");
image1 = getTitle();
Ext.CLIJ2_push(image1);

// define some settings
t_sigmas = newArray(5, 7, 9, 11, 13, 15, 11, 9, 7);
w_sigmas = newArray(1, 2, 3, 4, 5, 6, 4, 3, 2);

// create a stack where different results will be saved
Ext.CLIJ2_getDimensions(image1, width, height, depth);
Ext.CLIJ2_create3D(stack, width, height, lengthOf(t_sigmas) * lengthOf(w_sigmas), 32);

count = 0;

for (t = 0; t < lengthOf(t_sigmas); t++) {
	thresholding_sigma = t_sigmas[t];
	
	for (w = 0; w < lengthOf(w_sigmas); w++) {
		watershed_sigma = w_sigmas[w];

		// blur the image a bot
		Ext.CLIJ2_gaussianBlur2D(image1, image2, thresholding_sigma, thresholding_sigma);

		// make a binary mask out of it
		Ext.CLIJ2_thresholdOtsu(image2, image3);

		// apply a parametric watershed
		Ext.CLIJx_parametricWatershed(image3, image4, watershed_sigma, watershed_sigma, 0);

		// distinguish objects
		Ext.CLIJ2_connectedComponentsLabelingBox(image4, image4);

		// save result in a stack which becomes a video laters
		Ext.CLIJ2_copySlice(image4, stack, count);
		count++;

		//break;
	}
	//break;
}

// show resulting stack
Ext.CLIJ2_pull(stack);
run("Fire");
