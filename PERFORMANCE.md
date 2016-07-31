# Performance

Computers are fast, and hardware is cheap. Still, it is nice to enjoy some performance. Especially because C and C++ are well known for that.

## History

In order to keep track of the performance of Cover we're using the [The Computer Language Benchmarks Game](http://benchmarksgame.alioth.debian.org/).

| Date       | Mandelbrot | Fannkuch | Change |
| ---        | ---        | ---      | --- |
| 2016-07-25 | 152.0      |          | First running version. |
| 2016-07-26 | 53.0       |          | Switch to faster implementation mandelbrot_gcc2_split.cover. |
| 2016-07-26 | 47.6       |          | Handle doubles unboxed if possible. |
| 2016-07-26 | 38.3       |          | Add unboxed long[] and double[] arrays & switch to mandelbrot_gcc9_modified.cover. |
| 2016-07-27 | 34.2       |          | Variable scope + malloc builtin |
| 2016-07-28 | 38.1       |          | #include support |
| 2016-07-28 | 250.7 (!)  |          | Parse-time type system. |
| 2016-07-28 | 249.7 (!)  |          | All expressions are now typed. |
| 2016-07-29 | 21.3       |          | Don't use CoverReferences at runtime, just emit the correct write nodes directly. |
| 2016-07-29 | 20.1       |          | Move local array definition out of the loop. |
| 2016-07-31 | 20.3       | 521.2    |  |

Benchmarks were run on a i7 870 (2.93 GHz).

### Comparison

For comparison, here are the fastest times for other implementations. OpenMP was *not* used, so these run single-core.

| Benchmark  | Time (s) | Implementation |
| ---        | ---      | --- |
| Mandelbrot | 14.7     | gcc9 |
| Mandelbrot | 24.1     | java1 |
| Fannkuch   | 24.5     | gcc5 |
