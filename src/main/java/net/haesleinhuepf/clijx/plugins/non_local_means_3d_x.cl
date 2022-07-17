__kernel void non_local_means_3d(
    IMAGE_input_TYPE input,
    IMAGE_local_mean_TYPE local_mean,
    IMAGE_output_TYPE output,
    const int radiusX,
    const int radiusY,
    const int radiusZ,
    const float sigma
  )
{


  const sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE |	CLK_ADDRESS_CLAMP_TO_EDGE |	CLK_FILTER_NEAREST ;

  uint i = get_global_id(0);
  uint j = get_global_id(1);
  uint k = get_global_id(2);

  //int pix0 = (int)(READ_IMAGE(input,sampler,(int4)(i,j,k,0)).x);

  double res = 0;
  double sum = 0;



  for(int i2 = -radiusX; i2 <= radiusX;i2++){
    for(int j2 = -radiusY; j2 <= radiusY;j2++){
  	  for(int k2 = -radiusZ; k2 <= radiusZ;k2++){

  	    // source https://en.wikipedia.org/wiki/Non-local_means
        double p1 = (double)(READ_IMAGE(local_mean, sampler, (int4)(i,j,k,0)).x);
        double p2 = (double)(READ_IMAGE(local_mean, sampler, (int4)(i+i2,j+j2,k+k2,0)).x);

        double dist = (p1 - p2) * (p1 - p2);

        double weight = exp(-1.f / sigma / sigma * dist);


	    double pix1 = READ_IMAGE(input,sampler,(int4)(i + i2, j + j2, k + k2, 0)).x;


	    res += pix1 * weight;
	    sum += weight;
  	  }
    }
  }

  WRITE_IMAGE(output, (int4)(i,j,k,0), CONVERT_output_PIXEL_TYPE((float)(res/sum)));
}