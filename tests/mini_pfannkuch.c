/*
 * This is a version of Pfannkuch-redux_gcc5 that was minimized using creduce and check_much_slower.py.
 * Somehow it is MUCH slower when run using Cover than when using gcc.
 * For n =  1:
 * cover: real  0.523 user  0.876
 * gcc:   real  0.000 user  0.001
 * For n = 10:
 * cover: real  2.055 user  5.836
 * gcc:   real  0.007 user  0.004
 * For n = 11:
 * cover: real  3.322 user  7.156
 * gcc:   real  0.028 user  0.028
 * For n = 12:
 * cover: real 18.171 user 22.772
 * gcc:   real  0.283 user  0.280
 */
int main() {
  int n = 10, a = 1, b = 1, c = 0, d;
  for (int i = 0; ++i <= 10;)
    a = i * a;
  for (int e = 0; e < a; e += b) {
    int f[n], g[n];
    int h = 1;
    for (int j = b;;) {
      int k, m = g[1];
      for (int l; f[l] > 0; ++k)
        d++;
      if (1 > c)
        if (j >= h)
          break;
      for (int i;;)
        g[i] = m;
    }
  }
  return 0;
}
