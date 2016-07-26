# Performance

Computers are fast, and hardware is cheap. Still, it is nice to enjoy some performance. Especially because C and C++ are well known for that.

## Performance tips&trics

* Put the computationally expensive part (the inner loop, including the loop itself) in a separate function. This will make it easier for the optimizer to reason about the code.

## History

In order to keep track of the performance of Cover we're using the [The Computer Language
Benchmarks Game](http://benchmarksgame.alioth.debian.org/). Currrently only the [mandelbrot](http://benchmarksgame.alioth.debian.org/u64q/performance.php?test=mandelbrot) benchmark runs.

| Date       | Mandelbrot | Change |
| ---        | ---        | --- |
| 2016-07-25 | 152.0      | First running version. |
| 2016-07-26 | 53.0       | Switch to faster implementation mandelbrot_gcc2_split.cover. |
| 2016-07-26 | 47.6       | Handle doubles unboxed if possible. |
| 2016-07-26 | 38.3       | Add unboxed long[] and double[] arrays & switch to mandelbrot_gcc9_modified.cover. | 

For comparison, on this machine (i7 870, 2.93 GHz) the fastest single core C implementation is gcc9, and it runs in 14.7 seconds. The Java1 implementation needs 24.1 seconds.
