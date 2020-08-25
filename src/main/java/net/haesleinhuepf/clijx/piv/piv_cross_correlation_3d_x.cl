
__kernel void cross_correlation_3d(
    IMAGE_src1_TYPE src1,
    IMAGE_mean_src1_TYPE mean_src1,
    IMAGE_src2_TYPE src2,
    IMAGE_mean_src2_TYPE mean_src2,
    IMAGE_dst_TYPE dst,
    int radiusx,
    int radiusy,
    int radiusz,
    int ix,
    int iy,
    int iz)
{

    const sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;

    int4 pos = {get_global_id(0), get_global_id(1), get_global_id(2), 0};
    int4 deltaPos = {get_global_id(0), get_global_id(1), get_global_id(2), 0};

    int4 deltaPosI = {get_global_id(0), get_global_id(1), get_global_id(2), 0};


    float sum1 = 0;
    float sum2 = 0;
    float sum3 = 0;
    for(int kx = -radiusx; kx < radiusx + 1; kx++)
    {
        deltaPos.x = get_global_id(0) + kx;
        deltaPosI.x = get_global_id(0) + kx + ix;
        for(int ky = -radiusy; ky < radiusy + 1; ky++)
        {
            deltaPos.y = get_global_id(1) + ky;
            deltaPosI.y = get_global_id(1) + ky + iy;
            for(int kz = -radiusz; kz < radiusz + 1; kz++)
            {
                deltaPos.z = get_global_id(2) + kz;
                deltaPosI.z = get_global_id(2) + kz + iz;

                float Ia = READ_IMAGE(src1, sampler, deltaPos).x;
                float meanIa = READ_IMAGE(mean_src1, sampler, deltaPos).x;

                float Ib = READ_IMAGE(src2, sampler, deltaPosI).x;
                float meanIb = READ_IMAGE(mean_src2, sampler, deltaPosI).x;

                sum1 = sum1 + (Ia - meanIa) * (Ib - meanIb);
                sum2 = sum2 + pow((float)(Ia - meanIa), (float)2.0);
                sum3 = sum3 + pow((float)(Ib - meanIb), (float)2.0);
            }
        }
    }

    float result = sum1 / pow((float)(sum2 * sum3), (float)0.5);

    WRITE_IMAGE(dst, pos, CONVERT_dst_PIXEL_TYPE(result));
}

