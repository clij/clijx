
__kernel void find_maxima_determine_labels_to_exclude (
        IMAGE_intensity_TYPE intensity,
        IMAGE_src_labels_TYPE src_labels,
        IMAGE_dst_vector_TYPE dst_vector
)
{
    const sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;

    const int x = get_global_id(0);
    const int y = get_global_id(1);
    const int z = get_global_id(2);

    float value = READ_IMAGE(intensity, sampler, POS_intensity_INSTANCE(x, y, z, 0)).x;
    float label = READ_IMAGE(src_labels, sampler, POS_src_labels_INSTANCE(x, y, z, 0)).x;

    if (get_global_size(0) > 1) {
        float other_value = READ_IMAGE(intensity, sampler, POS_intensity_INSTANCE(x - 1, y, z, 0)).x;
        float other_label = READ_IMAGE(src_labels, sampler, POS_src_labels_INSTANCE(x - 1, y, z, 0)).x;

        if (other_value == value && other_label != label) {
            WRITE_dst_vector_IMAGE(dst_vector, POS_dst_vector_INSTANCE(label, 0, 0, 0), 1);
            return;
        }

        other_value = READ_IMAGE(intensity, sampler, POS_intensity_INSTANCE(x + 1, y, z, 0)).x;
        other_label = READ_IMAGE(src_labels, sampler, POS_src_labels_INSTANCE(x + 1, y, z, 0)).x;

        if (other_value == value && other_label != label) {
            WRITE_dst_vector_IMAGE(dst_vector, POS_dst_vector_INSTANCE(label, 0, 0, 0), 1);
            return;
        }
    }


    if (get_global_size(1) > 1) {
        float other_value = READ_IMAGE(intensity, sampler, POS_intensity_INSTANCE(x, y - 1, z, 0)).x;
        float other_label = READ_IMAGE(src_labels, sampler, POS_src_labels_INSTANCE(x, y - 1, z, 0)).x;

        if (other_value == value && other_label != label) {
            WRITE_dst_vector_IMAGE(dst_vector, POS_dst_vector_INSTANCE(label, 0, 0, 0), 1);
            return;
        }

        other_value = READ_IMAGE(intensity, sampler, POS_intensity_INSTANCE(x, y + 1, z, 0)).x;
        other_label = READ_IMAGE(src_labels, sampler, POS_src_labels_INSTANCE(x, y + 1, z, 0)).x;

        if (other_value == value && other_label != label) {
            WRITE_dst_vector_IMAGE(dst_vector, POS_dst_vector_INSTANCE(label, 0, 0, 0), 1);
            return;
        }
    }


    if (get_global_size(2) > 1) {
        float other_value = READ_IMAGE(intensity, sampler, POS_intensity_INSTANCE(x, y, z - 1, 0)).x;
        float other_label = READ_IMAGE(src_labels, sampler, POS_src_labels_INSTANCE(x, y, z - 1, 0)).x;

        if (other_value == value && other_label != label) {
            WRITE_dst_vector_IMAGE(dst_vector, POS_dst_vector_INSTANCE(label, 0, 0, 0), 1);
            return;
        }

        other_value = READ_IMAGE(intensity, sampler, POS_intensity_INSTANCE(x, y, z + 1, 0)).x;
        other_label = READ_IMAGE(src_labels, sampler, POS_src_labels_INSTANCE(x, y, z + 1, 0)).x;

        if (other_value == value && other_label != label) {
            WRITE_dst_vector_IMAGE(dst_vector, POS_dst_vector_INSTANCE(label, 0, 0, 0), 1);
            return;
        }
    }
    
}
