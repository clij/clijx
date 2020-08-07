__kernel void bilateral_3d(
    IMAGE_input_TYPE input,
    IMAGE_output_TYPE output,
    const int radiusX,
    const int radiusY,
    const int radiusZ,
    const float sigma_intensity,
    const float sigma_space
  )
{


  const sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE |	CLK_ADDRESS_CLAMP_TO_EDGE |	CLK_FILTER_NEAREST ;

  uint i = get_global_id(0);
  uint j = get_global_id(1);
  uint k = get_global_id(2);

  double pix0 = (int)(READ_IMAGE(input,sampler,(int4)(i,j,k,0)).x);

  double res = 0;
  double sum = 0;



  for(int i2 = -radiusX; i2 <= radiusX;i2++){
    for(int j2 = -radiusY; j2 <= radiusY;j2++){
  	  for(int k2 = -radiusZ; k2 <= radiusZ;k2++){
        // source https://en.wikipedia.org/wiki/Bilateral_filter

        double p1 = (double)(READ_IMAGE(input, sampler,      (int4)(i + i2,j + j2,k + k2, 0)).x);
        double dist_intensity = (p1 - pix0) * (p1 - pix0);

        double dist_space = (i2 * i2 + j2 * j2 + k2 * k2);

        double weight = exp(- dist_intensity / sigma_intensity / sigma_intensity / 2.0f - dist_space / sigma_space / sigma_space / 2.0f );
	    res += p1 * weight;
        sum += weight;
  	  }
    }
  }

  WRITE_IMAGE(output, (int4)(i,j,k,0), CONVERT_output_PIXEL_TYPE((float)(res/sum)));
}