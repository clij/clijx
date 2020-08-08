// init GPU
run("CLIJ2 Macro Extensions", "cl_device=[GeForce RTX 2080 Ti]");
Ext.CLIJ2_clear();

// open example image and push it to the GPU
run("Blobs (25K)");
input = getTitle();;
Ext.CLIJ_push(input);	

// define test parameters
min_sizes = newArray(10, 100, 1000);

for (i = 0; i < min_sizes.length; i++) {
	// grey level atttribute filtering
	number_of_bins = 256.0;
	minimum_pixel_count = min_sizes[i];
	Ext.CLIJx_greyLevelAtttributeFiltering(input, result, number_of_bins, minimum_pixel_count);
	Ext.CLIJ_pull(result);
	rename("Filtered with size > " + minimum_pixel_count);
}
