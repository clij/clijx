// CLIJ example macro: pullToROIManager.ijm
//
// This macro shows how to apply an automatic 
// threshold method and get an ROI into the 
// ROIManager from the GPU
//
// Author: Robert Haase
//         September 2019
// ---------------------------------------------


// Get test data
run("Blobs (25K)");
//open("C:/structure/data/blobs.gif");
input = getTitle();

mask = "mask";

// Init GPU
run("CLIJ Macro Extensions", "cl_device=");
Ext.CLIJ2_clear();

// push data to GPU
Ext.CLIJ2_push(input);

// create a mask using a threshold algorithm
Ext.CLIJ2_thresholdOtsu(input, mask);

Ext.CLIJ2_pullToROIManager(mask);
