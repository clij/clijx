
__kernel void generate_distance_matrix_along_axis(
    IMAGE_dst_matrix_TYPE dst_matrix,
    IMAGE_src_point_list1_TYPE src_point_list1,
    IMAGE_src_point_list2_TYPE src_point_list2,
    int axis
) {
  const sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;

  const int x = get_global_id(0);

  int n_dimensions = GET_IMAGE_HEIGHT(src_point_list1);
  int n_points = GET_IMAGE_WIDTH(src_point_list2);

  float position = READ_src_point_list1_IMAGE(src_point_list1, sampler, POS_src_point_list1_INSTANCE(x, axis, 0, 0)).x;

  for (int j = 0; j < GET_IMAGE_WIDTH(src_point_list2); j ++) {
      float out = position - (float)READ_src_point_list2_IMAGE(src_point_list2, sampler, POS_src_point_list2_INSTANCE(j, axis, 0, 0)).x;
      WRITE_dst_matrix_IMAGE(dst_matrix, POS_dst_matrix_INSTANCE(get_global_id(0)+1, j+1, 0, 0), CONVERT_dst_matrix_PIXEL_TYPE(out));
  }
}
