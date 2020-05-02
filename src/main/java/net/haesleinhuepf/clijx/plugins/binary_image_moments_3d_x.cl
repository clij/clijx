

__kernel void binary_image_moments_3d(
    IMAGE_dst_TYPE dst,
    IMAGE_src_TYPE src,
    float center_x,
    float center_y,
    float center_z,
    float order_x,
    float order_y,
    float order_z
) {
  const sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;

  const int x = get_global_id(0);
  const int y = get_global_id(1);
  const int z = get_global_id(2);

  float value = READ_IMAGE(src, sampler, POS_src_INSTANCE(x, y, z, 0)).x;
  if (value != 0) {
    float temp = x - center_x;
    value = pow(temp, order_x);

    temp = y - center_y;
    value = value + pow(temp, order_y);

    temp = z - center_z;
    value = value + pow(temp, order_z);

    WRITE_IMAGE(dst, POS_dst_INSTANCE(x,y,z,0), CONVERT_dst_PIXEL_TYPE(value));
  } else {
    WRITE_IMAGE(dst, POS_dst_INSTANCE(x,y,z,0), CONVERT_dst_PIXEL_TYPE(0));
  }
}
