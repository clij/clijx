__kernel void non_local_mean_3d(
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

  int pix0 = (int)(READ_IMAGE(input,sampler,(int4)(i,j,k,0)).x);

  float res = 0;
  float sum = 0;



  for(int i2 = -radiusX; i2 <= radiusX;i2++){
    for(int j2 = -radiusY; j2 <= radiusY;j2++){
  	  for(int k2 = -radiusZ; k2 <= radiusZ;k2++){

	    float dist = 0.f;

	    for(int i3 = -radiusX; i3 <= radiusX;i3++){
	      for(int j3 = -radiusY; j3 <= radiusY;j3++){
	    	for(int k3 = -radiusZ; k3 <= radiusZ;k3++){

	    	  float p1 = (float)(READ_IMAGE(input, sampler, (int4)(i+i3,j+j3,k+k3,0)).x);
	    	  float p2 = (float)(READ_IMAGE(local_mean, sampler, (int4)(i+i2+i3,j+j2+j3,k+k2+k3,0)).x);

	    	  dist += .1f * (p1 - p2) * (p1 - p2);
  	    	}
	      }
	    }


	    int pix1 = READ_IMAGE(local_mean,sampler,(int4)(i + i2, j + j2, k + k2, 0)).x;

	    float weight = exp(-1.f / sigma / sigma * dist);

	    res += pix1*weight;
	    sum += weight;
  	  }
    }
  }

  WRITE_IMAGE(output, (int4)(i,j,k,0), CONVERT_output_PIXEL_TYPE(res/sum));
}