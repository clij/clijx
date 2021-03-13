// March 2021, Peter Haub (@phaub)

__const sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;

__kernel void color_deconvolution (   	IMAGE_src_TYPE  src,
                           				IMAGE_dst_TYPE  dst,
                           				IMAGE_cv_TYPE cv,
                           				IMAGE_lognormx_TYPE lognormx,
                           				const float detA
                     				)
{

  // Color vectors matrix A (definition see below)
  
  const int x = get_global_id(0);
  const int y = get_global_id(1);

  // read RGB values from 8bit RGB stack input
  float aR = (float)READ_IMAGE(src, sampler, POS_src_INSTANCE(x, y, 0, 0)).x;
  float aG = (float)READ_IMAGE(src, sampler, POS_src_INSTANCE(x, y, 1, 0)).x;
  float aB = (float)READ_IMAGE(src, sampler, POS_src_INSTANCE(x, y, 2, 0)).x;
    
  // convert to absorbance values (using precalculated values)
  aR = lognormx[(int)aR];
  aG = lognormx[(int)aG];
  aB = lognormx[(int)aB];
  
  // solve linear equation
  float c1, c2, c3;
  
  if (detA == 0){
  	c1 = c2 = c3 = 0;
  }
  else{
  	// Color vectors matrix A 
  	//        AR1, AR2, AR3
  	//  A  =  AG1, AG2, AG3
  	//        AB1, AB2, AB3
  	//         0    1    2
  	//         3    4    5
  	//         6    7    8
  	// as float array {AR1, AR2, AR3, AG1, AG2, AG3, AB1, AB2, AB3}
  	//                  0,   1,   2,   3,   4,   5,   6,   7,   8
  	//  see Supplementary Information to
	//	Haub, P., Meckel, T. A Model based Survey of Colour Deconvolution in Diagnostic Brightfield Microscopy:
	//	Error Estimation and Spectral Consideration. Sci Rep 5, 12096 (2015). https://doi.org/10.1038/srep12096	
	  	
  	c1 = aR   *(cv[4]*cv[8] - cv[5]*cv[7]) - cv[1]*( aG*cv[8]   -   aB*cv[5] ) + cv[2]*( aG*cv[7]   -   aB*cv[4] );
  	c2 = cv[0]*( aG*cv[8]   -   aB*cv[5] ) - aR   *(cv[3]*cv[8] - cv[5]*cv[6]) + cv[2]*( aB*cv[3]   -   aG*cv[6] );
  	c3 = cv[0]*( aB*cv[4]   -   aG*cv[7] ) - cv[1]*( aB*cv[3]   -   aG*cv[6] ) + aR   *(cv[3]*cv[7] - cv[4]*cv[6]);
  	
  	c1 /= detA; c2 /= detA; c3 /= detA;
  }
    
  IMAGE_dst_PIXEL_TYPE out;
  out = CONVERT_dst_PIXEL_TYPE( c1 );
  WRITE_IMAGE(dst, POS_src_INSTANCE(x, y, 0, 0), out);
  
  out = CONVERT_dst_PIXEL_TYPE( c2 );
  WRITE_IMAGE(dst, POS_src_INSTANCE(x, y, 1, 0), out);
  
  out = CONVERT_dst_PIXEL_TYPE( c3 );
  WRITE_IMAGE(dst, POS_src_INSTANCE(x, y, 2, 0), out);
}
