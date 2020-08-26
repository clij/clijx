
__kernel void cross_correlation_3d(
    IMAGE_src1_TYPE src1,
    IMAGE_mean_src1_TYPE mean_src1,
    IMAGE_src2_TYPE src2,
    IMAGE_mean_src2_TYPE mean_src2,
    IMAGE_dst_TYPE dst,
    int radius,
    int i,
    int dimension)
{

    const sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;

    POS_src1_TYPE pos = POS_src1_INSTANCE(get_global_id(0), get_global_id(1), get_global_id(2), 0);
    POS_src1_TYPE deltaPos = POS_src1_INSTANCE(get_global_id(0), get_global_id(1), get_global_id(2), 0);


    float sum1 = 0;
    float sum2 = 0;
    float sum3 = 0;
    for(int k = -radius; k < radius + 1; k++)
    {
        if (dimension == 0) {
            deltaPos.x = get_global_id(dimension) + k;
        } else if (dimension == 1) {
            deltaPos.y = get_global_id(dimension) + k;
        } else if (dimension == 2) {
            deltaPos.z = get_global_id(dimension) + k;
        }
        float Ia = READ_IMAGE(src1, sampler, deltaPos).x;
        float meanIa = READ_IMAGE(mean_src1, sampler, deltaPos).x;
        //deltaPos[dimension] = get_global_id(dimension) + k + i;
        if (dimension == 0) {
            deltaPos.x = get_global_id(dimension) + k + i;
        } else if (dimension == 1) {
            deltaPos.y = get_global_id(dimension) + k + i;
        } else if (dimension == 2) {
            deltaPos.z = get_global_id(dimension) + k + i;
        }
        float Ib = READ_IMAGE(src2, sampler, deltaPos).x;
        float meanIb = READ_IMAGE(mean_src2, sampler, deltaPos).x;

        sum1 = sum1 + (Ia - meanIa) * (Ib - meanIb);
        sum2 = sum2 + pow((float)(Ia - meanIa), (float)2.0);
        sum3 = sum3 + pow((float)(Ib - meanIb), (float)2.0);
    }

    float result = sum1 / pow((float)(sum2 * sum3), (float)0.5);

    WRITE_IMAGE(dst, pos, CONVERT_dst_PIXEL_TYPE(result));
}
