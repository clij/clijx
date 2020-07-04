
__kernel void find_maxima_detect_maxima(
        IMAGE_src_TYPE src,
        IMAGE_dst_TYPE dst
)
{
    const sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;

    const int x = get_global_id(0);
    const int y = get_global_id(1);
    const int z = get_global_id(2);

    float value = READ_IMAGE(src, sampler, POS_src_INSTANCE(x, y, z, 0)).x;
    float other_value;
    int result = 1;
    
    POS_dst_TYPE dpos = POS_dst_INSTANCE(get_global_id(0), get_global_id(1), get_global_id(2), 0);

    if (get_global_size(0) > 1) {
        other_value = READ_IMAGE(src, sampler, POS_src_INSTANCE(x + 1, y, z, 0)).x;
        if (other_value > value) {
            WRITE_dst_IMAGE(dst, dpos, 0);
            return;
        }

        other_value = READ_IMAGE(src, sampler, POS_src_INSTANCE(x - 1, y, z, 0)).x;
        if (other_value > value) {
            WRITE_dst_IMAGE(dst, dpos, 0);
            return;
        }
    }


    if (get_global_size(1) > 1) {
        other_value = READ_IMAGE(src, sampler, POS_src_INSTANCE(x, y + 1, z, 0)).x;
        if (other_value > value) {
            WRITE_dst_IMAGE(dst, dpos, 0);
            return;
        }

        other_value = READ_IMAGE(src, sampler, POS_src_INSTANCE(x, y - 1, z, 0)).x;
        if (other_value > value) {
            WRITE_dst_IMAGE(dst, dpos, 0);
            return;
        }
    }


    if (get_global_size(2) > 1) {
        other_value = READ_IMAGE(src, sampler, POS_src_INSTANCE(x, y, z + 1, 0)).x;
        if (other_value > value) {
            WRITE_dst_IMAGE(dst, dpos, 0);
            return;
        }

        other_value = READ_IMAGE(src, sampler, POS_src_INSTANCE(x, y, z - 1, 0)).x;
        if (other_value > value) {
            WRITE_dst_IMAGE(dst, dpos, 0);
            return;
        }
    }
    
    WRITE_dst_IMAGE(dst, dpos, 1);
}
