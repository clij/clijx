run("Blobs (25K)");

run("CLIJ2 Macro Extensions", "cl_device=");
Ext.CLIJ2_clear();

// generate grey value cooccurrence matrix
image = getTitle();
Ext.CLIJ_push(image);

min_grey_value = 0.0;
max_grey_value = 255.0;
Ext.CLIJx_generateGreyValueCooccurrenceMatrixBox(image, grey_value_cooccurrence_matrix, min_grey_value, max_grey_value);

Ext.CLIJ2_pull(grey_value_cooccurrence_matrix);
