// weka_segmentation.ijm
//
// This macro shows how to train and apply weka models
// to feature stacks made by CLIJ.
//
// Author: Robert Haase
//         December 2019
// ---------------------------------------------

run("Close All");

// Get test data
run("Blobs (25K)");
run("32-bit");
original = "original";
rename(original);
getDimensions(width, height, channels, slices, frames)

// generate partial segmentation
partialGroundTruth = "partialGroundTruth";
newImage(partialGroundTruth, "32-bit black", width, height, slices);
makeRectangle(21,51,17,13);
run("Add...", "value=2");
makeRectangle(101,37,20,16);
run("Add...", "value=1");

// init GPU
run("CLIJ2 Macro Extensions", "cl_device=");
Ext.CLIJ2_clear();

// push images to GPU
Ext.CLIJ2_push(original);
Ext.CLIJ2_push(partialGroundTruth);

// cleanup imagej
run("Close All");

featureStack = "featureStack";
Ext.CLIJ2_create3D(featureStack, width, height, 11, 32);

featureCount = 0;

Ext.CLIJ2_copySlice(original, featureStack, featureCount);
featureCount ++;

for (i = 0; i < 5; i++) {
    sigma = (i + 1);
    temp = "temp";
    Ext.CLIJ2_gaussianBlur2D(original, temp, sigma, sigma);

	temp2 = "temp2";
    Ext.CLIJ2_sobel(temp, temp2);

    Ext.CLIJ2_copySlice(temp, featureStack, featureCount);
    featureCount ++;
    Ext.CLIJ2_copySlice(temp2, featureStack, featureCount);
    featureCount ++;

}

Ext.CLIJ2_pull(original);
Ext.CLIJ2_pull(partialGroundTruth);
Ext.CLIJ2_pull(featureStack);

Ext.CLIJx_trainWekaModel(featureStack, partialGroundTruth, "test.model");

result = "result";
Ext.CLIJx_applyWekaModel(featureStack, result, "test.model");

Ext.CLIJ2_pull(result);


Ext.CLIJ2_clear();