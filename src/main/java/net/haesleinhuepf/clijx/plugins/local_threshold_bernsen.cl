__const sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;

__kernel void local_threshold_bernsen (   	IMAGE_src_TYPE  src,
                           					IMAGE_srcMin_TYPE  srcMin,
                           					IMAGE_srcMax_TYPE  srcMax,
                           					IMAGE_dst_TYPE  dst,
                           					const float tcontrast
                     				)
{

  const int x = get_global_id(0);
  const int y = get_global_id(1);
  const int z = get_global_id(2);

  float value = (float)READ_IMAGE(src, sampler, POS_src_INSTANCE(x, y, z, 0)).x;
  float min = (float)READ_IMAGE(srcMin, sampler, POS_src_INSTANCE(x, y, z, 0)).x;
  float max = (float)READ_IMAGE(srcMax, sampler, POS_src_INSTANCE(x, y, z, 0)).x;
  
  float local_contrast = max - min;
  float mid_grey = (max + min)/2.0;
  
  float res = 1.0f;
  if (local_contrast < tcontrast){
    if (mid_grey < 128.0f)
  	  res = 0.0f;  // Low contrast region
  }
  else{
    if (value < mid_grey)
  	  res = 0.0f;
  }
  
  	  	
  IMAGE_dst_PIXEL_TYPE out = CONVERT_dst_PIXEL_TYPE( res );
  WRITE_IMAGE(dst, POS_src_INSTANCE(x, y, z, 0), out);
}
