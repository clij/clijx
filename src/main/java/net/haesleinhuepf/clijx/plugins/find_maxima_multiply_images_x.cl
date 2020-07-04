__constant sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;

__kernel void find_maxima_multiply_images(
    IMAGE_src_TYPE  src,
    IMAGE_src1_TYPE  src1,
    IMAGE_dst_TYPE  dst
)
{
  const int x = get_global_id(0);
  const int y = get_global_id(1);
  const int z = get_global_id(2);

  const POS_src_TYPE poss = POS_src_INSTANCE(x,y,z,0);
  const POS_src1_TYPE poss1 = POS_src1_INSTANCE(x,y,z,0);
  const POS_dst_TYPE posd = POS_dst_INSTANCE(x,y,z,0);

  const float value = (float)READ_src_IMAGE(src, sampler, poss).x * READ_src1_IMAGE(src1, sampler, poss1).x;

  WRITE_dst_IMAGE(dst, posd, CONVERT_dst_PIXEL_TYPE(value));
}
