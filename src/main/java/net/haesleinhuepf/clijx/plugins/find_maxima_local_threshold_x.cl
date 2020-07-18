__kernel void find_maxima_local_threshold (
    IMAGE_input_TYPE input,
    IMAGE_flag_TYPE flag,
    IMAGE_threshold_list_TYPE threshold_list,
    IMAGE_labelmap_src_TYPE labelmap_src,
    IMAGE_labelmap_dst_TYPE labelmap_dst
)
{
  const sampler_t sampler  = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_NONE | CLK_FILTER_NEAREST;

  const int x = get_global_id(0);
  const int y = get_global_id(1);
  const int z = get_global_id(2);

  float intensity = READ_IMAGE(input, sampler, POS_input_INSTANCE(x, y, z, 0)).x;
  int original_label = READ_IMAGE(labelmap_src, sampler, POS_labelmap_src_INSTANCE(x, y, z, 0)).x;

  int new_label = original_label;
  if (original_label == 0) {

    int other_label;

    float value = intensity;
    if (get_global_size(0) > 1) {
      other_label = READ_IMAGE(labelmap_src, sampler, POS_labelmap_src_INSTANCE(x - 1, y, z, 0)).x;
      if (other_label > 0) {
        float other_threshold = READ_IMAGE(threshold_list, sampler, POS_threshold_list_INSTANCE(other_label, 0, 0, 0)).x;
        if (other_threshold <= value) {
          new_label = other_label;
          value = other_threshold;
        }
      }

      other_label = READ_IMAGE(labelmap_src, sampler, POS_labelmap_src_INSTANCE(x + 1, y, z, 0)).x;
      if (other_label > 0) {
        float other_threshold = READ_IMAGE(threshold_list, sampler, POS_threshold_list_INSTANCE(other_label, 0, 0, 0)).x;
        if (other_threshold <= value) {
          new_label = other_label;
          value = other_threshold;
        }
      }
    }

    if (get_global_size(1) > 1) {
      other_label = READ_IMAGE(labelmap_src, sampler, POS_labelmap_src_INSTANCE(x, y - 1, z, 0)).x;
      if (other_label > 0) {
        float other_threshold = READ_IMAGE(threshold_list, sampler, POS_threshold_list_INSTANCE(other_label, 0, 0, 0)).x;
        if (other_threshold <= value) {
          new_label = other_label;
          value = other_threshold;
        }
      }

      other_label = READ_IMAGE(labelmap_src, sampler, POS_labelmap_src_INSTANCE(x, y + 1, z, 0)).x;
      if (other_label > 0) {
        float other_threshold = READ_IMAGE(threshold_list, sampler, POS_threshold_list_INSTANCE(other_label, 0, 0, 0)).x;
        if (other_threshold <= value) {
          new_label = other_label;
          value = other_threshold;
        }
      }
    }

    if (get_global_size(2) > 1) {
      other_label = READ_IMAGE(labelmap_src, sampler, POS_labelmap_src_INSTANCE(x, y, z - 1, 0)).x;
      if (other_label > 0) {
        float other_threshold = READ_IMAGE(threshold_list, sampler, POS_threshold_list_INSTANCE(other_label, 0, 0, 0)).x;
        if (other_threshold <= value) {
          new_label = other_label;
          value = other_threshold;
        }
      }

      other_label = READ_IMAGE(labelmap_src, sampler, POS_labelmap_src_INSTANCE(1, y, z + 1, 0)).x;
      if (other_label > 0) {
        float other_threshold = READ_IMAGE(threshold_list, sampler, POS_threshold_list_INSTANCE(other_label, 0, 0, 0)).x;
        if (other_threshold <= value) {
          new_label = other_label;
          value = other_threshold;
        }
      }
    }

    if (new_label != original_label) {
      WRITE_IMAGE(flag, POS_flag_INSTANCE(0, 0, 0, 0), 1);
    }
  }

  WRITE_IMAGE(labelmap_dst, POS_labelmap_dst_INSTANCE(x, y, z, 0), CONVERT_labelmap_dst_PIXEL_TYPE(new_label));

}
