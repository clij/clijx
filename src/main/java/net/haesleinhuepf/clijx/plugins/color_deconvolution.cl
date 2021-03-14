// 03'2021, Peter Haub (@phaub)

__const sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;

__kernel void color_deconvolution (   	IMAGE_src_TYPE  src,
                           				IMAGE_dst_TYPE  dst,
                           				IMAGE_rotmat_TYPE rotmat,
                           				IMAGE_lognormx_TYPE lognormx
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
  
  // Calculation concentrations (with color vectors matrix A transformed to rotation matrix):
  float c1 = aR*rotmat[0] + aG*rotmat[1] + aB*rotmat[2]   ;
  float c2 = aR*rotmat[3] + aG*rotmat[4] + aB*rotmat[5]   ;
  float c3 = aR*rotmat[6] + aG*rotmat[7] + aB*rotmat[8]   ;
  	
  IMAGE_dst_PIXEL_TYPE out;
  out = CONVERT_dst_PIXEL_TYPE( c1 );
  WRITE_IMAGE(dst, POS_src_INSTANCE(x, y, 0, 0), out);
  
  out = CONVERT_dst_PIXEL_TYPE( c2 );
  WRITE_IMAGE(dst, POS_src_INSTANCE(x, y, 1, 0), out);
  
  out = CONVERT_dst_PIXEL_TYPE( c3 );
  WRITE_IMAGE(dst, POS_src_INSTANCE(x, y, 2, 0), out);
}
