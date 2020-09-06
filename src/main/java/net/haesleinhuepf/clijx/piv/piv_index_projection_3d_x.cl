

__kernel void index_projection_3d(
    IMAGE_index_src1_TYPE index_src1,
    IMAGE_index_map_src1_TYPE index_map_src1,
    IMAGE_dst_TYPE dst,
    int indexDimension,
    int fixed1,
    int fixedDimension1,
    int fixed2,
    int fixedDimension2
  )
{
    const sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;

    POS_index_src1_TYPE pos = POS_index_src1_INSTANCE(get_global_id(0), get_global_id(1), get_global_id(2), 0);
    float index = READ_IMAGE(index_src1, sampler, pos).x;

    POS_index_map_src1_TYPE indexPos = POS_index_map_src1_INSTANCE(0, 0, 0, 0);
    if (indexDimension == 0) {
        indexPos.x = index;
    } else if (indexDimension == 1) {
        indexPos.y = index;
    } else if (indexDimension == 2) {
        indexPos.z = index;
    }

    if (fixedDimension1 == 0) {
        indexPos.x = fixed1;
    } else if (fixedDimension1 == 1) {
        indexPos.y = fixed1;
    } else if (fixedDimension1 == 2) {
        indexPos.z = fixed1;
    }

    if (fixedDimension2 == 0) {
        indexPos.x = fixed2;
    } else if (fixedDimension2 == 1) {
        indexPos.y = fixed2;
    } else if (fixedDimension2 == 2) {
        indexPos.z = fixed2;
    }

    float value = READ_IMAGE(index_map_src1, sampler, indexPos).x;

    WRITE_IMAGE(dst, pos, CONVERT_dst_PIXEL_TYPE(value));
}


