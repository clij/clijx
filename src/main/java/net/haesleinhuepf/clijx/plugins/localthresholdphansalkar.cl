__const sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;

__kernel void localthresholdphansalkar (    IMAGE_src_TYPE  src,
                           					IMAGE_srcMean_TYPE  srcMean,
                           					IMAGE_srcSqrMean_TYPE  srcSqrMean,
                           					IMAGE_dst_TYPE  dst,
                           					const float k,
                           					const float r,
                           					const int whiteobjects,
                           					const int originalmode
                     					)
{

  const int x = get_global_id(0);
  const int y = get_global_id(1);

  const int2 pos = (int2){x,y};

  float value = (float)READ_IMAGE(src, sampler, pos).x;
  float mean = (float)READ_IMAGE(srcMean, sampler, pos).x;
  float sqrmean = (float)READ_IMAGE(srcSqrMean, sampler, pos).x;
  
  // t = mean * (1 + p * exp(-q * mean) + k * ((stddev / r) - 1))
  float stddev = sqrt(sqrmean - mean*mean)/255.0;
  
  // The character of the phansalkar threshold is slightly changed 
  // by the following modification (added by Peter Haub, 2021).
  if (!originalmode)
    stddev = sqrt(stddev);
  
  // note: 0.0392177 = 10.0/255.0
  float t = mean * (1.0f + 2.0f * exp(-0.0392177f * mean) + k * ((stddev / r ) - 1.0f));
  
  float res = 0.0f;
  if (value <= t)
  	res = 255.0f;
  	
  if (whiteobjects)
    res = 255.0 - res;
  	
  IMAGE_dst_PIXEL_TYPE out = CONVERT_dst_PIXEL_TYPE( res );
  WRITE_IMAGE(dst, pos, out);
}
