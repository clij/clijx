__constant sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;


__kernel void split_12_stacks(IMAGE_src_TYPE src,
                                IMAGE_dst0_TYPE dst0,
                                IMAGE_dst1_TYPE dst1,
                                IMAGE_dst2_TYPE dst2,
                                IMAGE_dst3_TYPE dst3,
                                IMAGE_dst4_TYPE dst4,
                                IMAGE_dst5_TYPE dst5,
                                IMAGE_dst6_TYPE dst6,
                                IMAGE_dst7_TYPE dst7,
                                IMAGE_dst8_TYPE dst8,
                                IMAGE_dst9_TYPE dst9,
                                IMAGE_dst10_TYPE dst10,
                                IMAGE_dst11_TYPE dst11
                            ){

   const int i = get_global_id(0), j = get_global_id(1), k = get_global_id(2);

   WRITE_dst0_IMAGE(dst0,(int4)(i,j,k,0),CONVERT_dst0_PIXEL_TYPE(READ_src_IMAGE(src,sampler,(const int4)(i,j,12*k,0)).x));
   WRITE_dst1_IMAGE(dst1,(int4)(i,j,k,0),CONVERT_dst1_PIXEL_TYPE(READ_src_IMAGE(src,sampler,(int4)(i,j,12*k+1,0)).x));
   WRITE_dst2_IMAGE(dst2,(int4)(i,j,k,0),CONVERT_dst2_PIXEL_TYPE(READ_src_IMAGE(src,sampler,(int4)(i,j,12*k+2,0)).x));
   WRITE_dst3_IMAGE(dst3,(int4)(i,j,k,0),CONVERT_dst3_PIXEL_TYPE(READ_src_IMAGE(src,sampler,(int4)(i,j,12*k+3,0)).x));
   WRITE_dst4_IMAGE(dst4,(int4)(i,j,k,0),CONVERT_dst4_PIXEL_TYPE(READ_src_IMAGE(src,sampler,(int4)(i,j,12*k+4,0)).x));
   WRITE_dst5_IMAGE(dst5,(int4)(i,j,k,0),CONVERT_dst5_PIXEL_TYPE(READ_src_IMAGE(src,sampler,(int4)(i,j,12*k+5,0)).x));
   WRITE_dst6_IMAGE(dst6,(int4)(i,j,k,0),CONVERT_dst6_PIXEL_TYPE(READ_src_IMAGE(src,sampler,(int4)(i,j,12*k+6,0)).x));
   WRITE_dst7_IMAGE(dst7,(int4)(i,j,k,0),CONVERT_dst7_PIXEL_TYPE(READ_src_IMAGE(src,sampler,(int4)(i,j,12*k+7,0)).x));
   WRITE_dst8_IMAGE(dst8,(int4)(i,j,k,0),CONVERT_dst8_PIXEL_TYPE(READ_src_IMAGE(src,sampler,(int4)(i,j,12*k+8,0)).x));
   WRITE_dst9_IMAGE(dst9,(int4)(i,j,k,0),CONVERT_dst9_PIXEL_TYPE(READ_src_IMAGE(src,sampler,(int4)(i,j,12*k+9,0)).x));
   WRITE_dst10_IMAGE(dst10,(int4)(i,j,k,0),CONVERT_dst10_PIXEL_TYPE(READ_src_IMAGE(src,sampler,(int4)(i,j,12*k+10,0)).x));
   WRITE_dst11_IMAGE(dst11,(int4)(i,j,k,0),CONVERT_dst11_PIXEL_TYPE(READ_src_IMAGE(src,sampler,(int4)(i,j,12*k+11,0)).x));
}
