// CLIJ example macro: hessian.ijm
//
// This macro shows how to comput Hessian Eigenvalues 
// of an image in the GPU.
//
// ---------------------------------------------
run("Close All");

// Get test data
run("T1 Head (2.4M, 16-bits)");
input = getTitle();

// Init GPU
run("CLIJ2 Macro Extensions", "cl_device=");
Ext.CLIJ2_clear();

// push images to GPU
Ext.CLIJ2_push(input);

// cleanup ImageJ
run("Close All");

// compute Eigenvalues of a stack
Ext.CLIJx_hessianEigenvalues3D(input, small_eigenvalue, middle_eigenvalue, large_eigenvalue);

Ext.CLIJ2_pull(small_eigenvalue);
Ext.CLIJ2_pull(middle_eigenvalue);
Ext.CLIJ2_pull(large_eigenvalue);

// compute Eigenvalues of a slice
Ext.CLIJ2_copySlice(input, input_slice, 80);

Ext.CLIJx_hessianEigenvalues2D(input_slice, small_eigenvalue_slice, large_eigenvalue_slice);

Ext.CLIJ2_pull(small_eigenvalue_slice);
Ext.CLIJ2_pull(large_eigenvalue_slice);
