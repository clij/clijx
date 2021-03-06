__const sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;

__kernel void local_threshold_phansalkar (    IMAGE_src_TYPE  src,
                           					IMAGE_srcMean_TYPE  srcMean,
                           					IMAGE_srcSqrMean_TYPE  srcSqrMean,
                           					IMAGE_dst_TYPE  dst,
                           					const float k,
                           					const float r
                     					)
{

  const int x = get_global_id(0);
  const int y = get_global_id(1);
  const int z = get_global_id(2);

  float value = (float)READ_IMAGE(src, sampler, POS_src_INSTANCE(x, y, z, 0)).x;
  float mean = (float)READ_IMAGE(srcMean, sampler, POS_srcMean_INSTANCE(x, y, z, 0)).x;
  float sqrmean = (float)READ_IMAGE(srcSqrMean, sampler, POS_srcSqrMean_INSTANCE(x, y, z, 0)).x;
  
  float stddev = sqrt(sqrmean - mean*mean);
  
  
  // [*  t = mean * (1 + p * exp(-q * mean) + k * ((stddev / r) - 1)) *]   
  float t = mean * (1.0 + 2.0 * exp(-10.0 * mean) + k * ((stddev / r ) - 1.0));
  
  float res = 1.0f;
  if (value <= t)
  	res = 0.0f;
  	  	
  IMAGE_dst_PIXEL_TYPE out = CONVERT_dst_PIXEL_TYPE( res );
  WRITE_IMAGE(dst, POS_dst_INSTANCE(x, y, z, 0), out);
}
