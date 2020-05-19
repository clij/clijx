__kernel void bilateral_2d(
    IMAGE_input_TYPE input,
    IMAGE_output_TYPE output,
    const int radiusX,
    const int radiusY,
    const float sigma
  )
{


  const sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE |	CLK_ADDRESS_CLAMP_TO_EDGE |	CLK_FILTER_NEAREST ;

  uint i = get_global_id(0);
  uint j = get_global_id(1);

  double pix0 = (int)(READ_IMAGE(input,sampler,(int2)(i,j)).x);

  double res = 0;
  double sum = 0;

  for(int i2 = -radiusX; i2 <= radiusX;i2++){
    for(int j2 = -radiusY; j2 <= radiusY;j2++){
        double p1 = (double)(READ_IMAGE(input, sampler,      (int2)(i+i2,j+j2)).x);
        double temp = (i2 * i2 + j2 * j2);
        double dist = (p1 - pix0) * (p1 - pix0) * sqrt(temp);
        double weight = exp(-1.f / sigma / sigma * dist);
	    double pix1 = READ_IMAGE(input,sampler,(int2)(i + i2, j + j2)).x;
	    res += pix1 * weight;
        sum += weight;
    }
  }

  WRITE_IMAGE(output, (int2)(i,j), CONVERT_output_PIXEL_TYPE((float)(res/sum)));
}