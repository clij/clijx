// clean up first
run("Close All");
run("Clear Results");

// open the blobs exmple image
open("http://imagej.nih.gov/ij/images/blobs.gif");
run("Invert LUT");
/*
We now initialize the GPU and push the image to GPU memory under a given name "A"
*/
run("CLIJ2 Macro Extensions", "cl_device=");
Ext.CLIJ2_clear();

// push image to GPU memory
blobs = getTitle();
Ext.CLIJx_pushAs(blobs, "A");
/*
Then, we process the image "A".
*/
radius = 1;
Ext.CLIJ2_detectMaxima2DBox("A", maxima, radius, radius);
Ext.CLIJ2_pull(maxima);
