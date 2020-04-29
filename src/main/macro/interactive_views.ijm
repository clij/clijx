/*
# Interactive views
Author: Robert Haase
        April 2020
*/

run("CLIJ2 Macro Extensions", "cl_device=[GeForce RTX 2060 SUPER]");
Ext.CLIJ2_clear();

run("Close All");
time = getTime();

/*
## Load a data set
The dataset is available [online](https://git.mpi-cbg.de/rhaase/neubias_academy_clij2/blob/master/data/lund1051_resampled.tif).
It shows a Tribolium castaneum embryo imaged using a custom light sheet microscope using a wavelength of 488nm (Imaging credits: Daniela Vorkel, Myers lab, MPI CBG). 
The data set has been resampled to a voxel size of 1x1x1 microns. The embryo expresses nuclei-GFP. We will use it for detecting nuclei and generating an estimated cell-segmentation first.

All processing steps are performed in 3D, for visualisation purposes, we're looking at maximum intensity projections in Z: 
*/
open("C:/structure/teaching/neubias_academy_clij2/data/lund1051_resampled.tif");
input = getTitle();

print("Loading took " + (getTime() - time) + " msec");


Ext.CLIJ2_push(input);
run("Close All");

// gaussian blur
sigma = 2.0;
Ext.CLIJ2_gaussianBlur3D(input, blurred, sigma, sigma, sigma);

// detect maxima
radius = 2.0;
Ext.CLIJ2_detectMaximaBox(blurred, detected_maxima, radius);

// threshold
threshold = 300.0;
Ext.CLIJ2_threshold(blurred, thresholded, threshold);

// mask
Ext.CLIJ2_mask(detected_maxima, thresholded, masked_spots);

// label spots
Ext.CLIJ2_labelSpots(masked_spots, flip);

// labelmap closing
number_of_dilations = 10;
number_of_erosions = 4;
for (i = 0; i < number_of_dilations; i++) {
	Ext.CLIJ2_onlyzeroOverwriteMaximumBox(flip, flop);
	Ext.CLIJ2_onlyzeroOverwriteMaximumDiamond(flop, flip);
}
Ext.CLIJ2_threshold(flip, flap, 1);
for (i = 0; i < number_of_erosions; i++) {
	Ext.CLIJ2_erodeBox(flap, flop);
	Ext.CLIJ2_erodeBox(flop, flap);
}

Ext.CLIJ2_mask(flip, flap, flop);

//Ext.CLIJ2_pull(flop);
labels = flop;
spots = masked_spots;

Ext.CLIJx_show3DWithTipTilt(input, "Input", true);
Ext.CLIJx_show3DWithTipTilt(labels, "Labels", true);
run("glasbey_on_dark");

Ext.CLIJ2_labelSpots(spots, labelled_spots);
Ext.CLIJ2_labelledSpotsToPointList(labelled_spots, pointlist);

Ext.CLIJ2_generateDistanceMatrix(pointlist, pointlist, distance_matrix);
Ext.CLIJ2_generateTouchMatrix(labels, touch_matrix);

// we set the first column in the touch matrix to zero because we want to ignore that spots touch the background (background label 0, first column)
Ext.CLIJ2_setColumn(touch_matrix, 0, 0);

Ext.CLIJ2_multiplyImages(touch_matrix, distance_matrix, touch_matrix_with_distances);

Ext.CLIJ2_getDimensions(input, width, height, depth);
Ext.CLIJ2_create3D(mesh, width, height, depth, 32);
Ext.CLIJ2_touchMatrixToMesh(pointlist, touch_matrix_with_distances, mesh);

Ext.CLIJx_show3DWithTipTilt(mesh, "distances", true);
run("Green Fire Blue");
setMinAndMax(0, 50);

Ext.CLIJ2_labelSpots(spots, labelled_spots);
Ext.CLIJ2_labelledSpotsToPointList(labelled_spots, pointlist);

Ext.CLIJ2_generateDistanceMatrix(pointlist, pointlist, distance_matrix);
Ext.CLIJ2_generateTouchMatrix(labels, touch_matrix);

// we set the first column in the touch matrix to zero because we want to ignore that spots touch the background (background label 0, first column)
Ext.CLIJ2_setColumn(touch_matrix, 0, 0);

distance_map = "distance_map";
Ext.CLIJ2_averageDistanceOfTouchingNeighbors(distance_matrix, touch_matrix, distances_vector);
Ext.CLIJ2_replaceIntensities(labels, distances_vector, distance_map);
Ext.CLIJx_show3DWithTipTilt(distance_map, "Mean distances", true);
run("Fire");
setMinAndMax(0, 50);

Ext.CLIJ2_meanOfTouchingNeighbors(distances_vector, touch_matrix, local_minimum_distances_vector);
Ext.CLIJ2_replaceIntensities(labels, local_minimum_distances_vector, local_minimum_pixel_count_map);
Ext.CLIJx_show3DWithTipTilt(local_minimum_pixel_count_map, "Mean of mean distances", true);
run("Fire");
setMinAndMax(0, 50);






print("The whole workflow took " + (getTime() - time) + " msec");
Ext.CLIJx_organiseWindows(0, 150, 5, 1, 380, 1500);

Ext.CLIJ2_reportMemory();

