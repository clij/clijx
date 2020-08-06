// get example data
open("C:/structure/data/blobs.tif");
blobs = getTitle();

// initialize GPU
run("CLIJ2 Macro Extensions", "cl_device=");
Ext.CLIJ2_clear();

// push image to GPU
Ext.CLIJ2_push(blobs);

// generate matrix
Ext.CLIJx_generateIntegerGreyValueCooccurrenceCountMatrixHalfBox(blobs, matrix);

// show matrix
Ext.CLIJ2_pull(matrix);
