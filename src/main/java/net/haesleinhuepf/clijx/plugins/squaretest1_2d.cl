__const sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;

__kernel void squaretest1_2d ( IMAGE_src_TYPE  src, IMAGE_dst_TYPE  dst )
{

  const int x = get_global_id(0);
  const int y = get_global_id(1);

  const int2 pos = (int2){x,y};

  float value = (float)READ_IMAGE(src, sampler, pos).x;

  float t1 = value * value;
  float t2 = t1 * t1;
  t1 = t2 * t2;
  t2 = t1 * t1;
  t1 = t2 * t2;
  t2 = t1 * t1;

  t1 = t2 * t2;
  t2 = t1 * t1;
  t1 = t2 * t2;
  t2 = t1 * t1;

  t1 = t2 * t2;
  t2 = t1 * t1;
  t1 = t2 * t2;
  t2 = t1 * t1;

  t1 = t2 * t2;
  t2 = t1 * t1;
  t1 = t2 * t2;
  t2 = t1 * t1;
    	
  IMAGE_dst_PIXEL_TYPE out = CONVERT_dst_PIXEL_TYPE( t2 );
  WRITE_IMAGE(dst, pos, out);
  
}
