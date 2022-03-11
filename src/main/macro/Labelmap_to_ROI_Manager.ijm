run("CLIJ2 Macro Extensions", "cl_device=");
Ext.CLIJ2_clear();

roiManager("Reset");
run("Clear Results");

//Prepare image and labelmap
run("Particles");
image = getTitle();
Ext.CLIJ2_push(image);
labelmap = "labelmap";
Ext.CLIJ2_connectedComponentsLabelingBox(image, labelmap);
Ext.CLIJ2_pull(labelmap);
run("glasbey_on_dark");

startTime = getTime();

//Determine the largest bounding box required to fit all the labels
Ext.CLIJ2_statisticsOfLabelledPixels(labelmap, labelmap);
boundingBox_X = Table.getColumn("BOUNDING_BOX_X", "Results");
boundingBox_Y = Table.getColumn("BOUNDING_BOX_Y", "Results");
boundingBox_width = Table.getColumn("BOUNDING_BOX_WIDTH", "Results");
boundingBox_height = Table.getColumn("BOUNDING_BOX_HEIGHT", "Results");
Array.getStatistics(boundingBox_width, min, boundingBoxMax_X, mean, stdDev);
Array.getStatistics(boundingBox_height, min, boundingBoxMax_Y, mean, stdDev);
print("Maximum boundingBox size: "+boundingBoxMax_X+", "+boundingBoxMax_Y);

//Crop labels, pull to ROI Manager and shift to the correct location
labels = Table.getColumn("IDENTIFIER", "Results");
for (i = 0; i < labels.length; i++) {
	Ext.CLIJ2_crop2D(labelmap, label_cropped, boundingBox_X[i], boundingBox_Y[i], boundingBoxMax_X, boundingBoxMax_Y);
	Ext.CLIJ2_labelToMask(label_cropped, mask_label, labels[i]);
	Ext.CLIJ2_getMaximumOfAllPixels(mask_label, maximum);
	Ext.CLIJ2_pullToROIManager(mask_label);
	roiManager("Select",i);
	Roi.move(boundingBox_X[i], boundingBox_Y[i]);
	roiManager("rename", "label_"+labels[i]);
	roiManager("update");
}

run("Select None");
roiManager("deselect");
Ext.CLIJ2_release(labelmap);
Ext.CLIJ2_release(mask_label);
Ext.CLIJ2_release(label_cropped);

print("Done in "+getTime() - startTime+ " ms.");
