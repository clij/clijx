
#define PIXEL(x, y) (READ_src_IMAGE(src, sampler,POS_src_INSTANCE((x),(y),0,0)).x)
#define WRITE_PIXEL(image, x, y, value) WRITE_ ## image ## _IMAGE(image,POS_ ## image ## _INSTANCE((x),(y),0,0), CONVERT_ ## image ## _PIXEL_TYPE(value))

__kernel void hessian_2d(
        IMAGE_src_TYPE src,
        IMAGE_small_eigenvalue_TYPE small_eigenvalue,
        IMAGE_large_eigenvalue_TYPE large_eigenvalue
) {
  const sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;

  const int x = get_global_id(0);
  const int y = get_global_id(1);

  float aa = PIXEL(x - 1, y - 1);
  float ab = PIXEL(x - 1, y);
  float ac = PIXEL(x - 1, y + 1);
  float ba = PIXEL(x, y - 1);
  float bb = PIXEL(x, y);
  float bc = PIXEL(x, y + 1);
  float ca = PIXEL(x + 1, y - 1);
  float cb = PIXEL(x + 1, y);
  float cc = PIXEL(x + 1, y + 1);
  float s_xx = ab - 2 * bb + cb;
  float s_yy = ba - 2 * bb + bc;
  float s_xy = (aa + cc - ac - ca) / 4;
  float trace = s_xx + s_yy;
  float l = (float) (trace / 2.0 + sqrt(4 * s_xy * s_xy + (s_xx - s_yy) * (s_xx - s_yy)) / 2.0);
  float s = (float) (trace / 2.0 - sqrt(4 * s_xy * s_xy + (s_xx - s_yy) * (s_xx - s_yy)) / 2.0);
  WRITE_PIXEL(small_eigenvalue, x, y, s);
  WRITE_PIXEL(large_eigenvalue, x, y, l);
}
