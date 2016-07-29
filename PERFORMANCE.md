# Performance

Computers are fast, and hardware is cheap. Still, it is nice to enjoy some performance. Especially because C and C++ are well known for that.

## History

In order to keep track of the performance of Cover we're using the [The Computer Language
Benchmarks Game](http://benchmarksgame.alioth.debian.org/). Currrently only the [mandelbrot](http://benchmarksgame.alioth.debian.org/u64q/performance.php?test=mandelbrot) benchmark runs.

| Date       | Mandelbrot | Change |
| ---        | ---        | --- |
| 2016-07-25 | 152.0      | First running version. |
| 2016-07-26 | 53.0       | Switch to faster implementation mandelbrot_gcc2_split.cover. |
| 2016-07-26 | 47.6       | Handle doubles unboxed if possible. |
| 2016-07-26 | 38.3       | Add unboxed long[] and double[] arrays & switch to 
mandelbrot_gcc9_modified.cover. | 
| 2016-07-27 | 34.2       | Variable scope + malloc builtin |
| 2016-07-28 | 38.1       | #include support |
| 2016-07-28 | 250.7 (!)  | Parse-time type system. |
| 2016-07-28 | 249.7 (!)  | All expressions are now typed. |

For comparison, on this machine (i7 870, 2.93 GHz) the fastest single core C implementation is gcc9, and it runs in 14.7 seconds. The Java1 implementation needs 24.1 seconds.
