__const sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;

__kernel void squaretest2_2d ( IMAGE_src_TYPE  src, IMAGE_dst_TYPE  dst )
{

  const int x = get_global_id(0);
  const int y = get_global_id(1);

  const int2 pos = (int2){x,y};

  float value = (float)READ_IMAGE(src, sampler, pos).x;
    	  
  float t1 = pow(value, 2);
  float t2 = pow(t1, 2);
  t1 = pow(t2, 2);
  t2 = pow(t1, 2);
  t1 = pow(t2, 2);
  t2 = pow(t1, 2);

  t1 = pow(t2, 2);
  t2 = pow(t1, 2);
  t1 = pow(t2, 2);
  t2 = pow(t1, 2);

  t1 = pow(t2, 2);
  t2 = pow(t1, 2);
  t1 = pow(t2, 2);
  t2 = pow(t1, 2);

  t1 = pow(t2, 2);
  t2 = pow(t1, 2);
  t1 = pow(t2, 2);
  t2 = pow(t1, 2);
    	
  IMAGE_dst_PIXEL_TYPE out = CONVERT_dst_PIXEL_TYPE( t2 );
  WRITE_IMAGE(dst, pos, out);
  
}
