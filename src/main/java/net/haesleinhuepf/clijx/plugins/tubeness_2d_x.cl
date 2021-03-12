__constant sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;

__kernel void tubeness_2d
(
  IMAGE_dst_TYPE dst,
  IMAGE_src_large_eigenvalue_TYPE src_large_eigenvalue
)
{
  const int x = get_global_id(0);
  const int y = get_global_id(1);
  const int z = get_global_id(2);

  float large_eigenvalue = (float)READ_src_large_eigenvalue_IMAGE(src_large_eigenvalue, sampler, POS_src_large_eigenvalue_INSTANCE(x,y,z,0)).x;

  IMAGE_dst_PIXEL_TYPE res = 0;
  if (large_eigenvalue < 0) {
      // https://github.com/fiji/VIB-lib/blob/master/src/main/java/features/TubenessProcessor.java#L26
      res = CONVERT_dst_PIXEL_TYPE(-arge_eigenvalue);
  }


  WRITE_dst_IMAGE(dst, POS_dst_INSTANCE(x,y,z,0), res);
}
