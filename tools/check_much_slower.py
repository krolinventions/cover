#!/usr/bin/env python3

# returns 0 if the file is much slower when run under cover than when compiled using gcc
import subprocess
import sys
import time
import os
cwd = os.getcwd()

filename = 'test.c'
aout = './a.out'
acceptable_speed = 0.5
min_runtime = 1.5 # seconds
max_gcc_runtime = 0.5
cover_allowed_startup_time = 1

args = ['gcc','-Wall','-Werror',filename,'-o'+aout]+'-O3 -fomit-frame-pointer -march=native -std=c99 -D_GNU_SOURCE -mfpmath=sse -msse2'.split(' ')
print(args)
e = subprocess.run(args)
if e.returncode != 0:
    print("gcc failed with error code " + str(e.returncode))
    sys.exit(1)

start = time.time()
try:
    e = subprocess.run([aout], timeout=max_gcc_runtime)
except subprocess.TimeoutExpired:
    print("gcc_timeout!")
    sys.exit(1)
gcc_runtime = time.time() - start

# now run using cover
max_cover_time = max(gcc_runtime/acceptable_speed + cover_allowed_startup_time, min_runtime)

start = time.time()
try:
    e = subprocess.run(['/home/gerard/projects/truffle/cover/cover', cwd+'/'+filename], cwd='/home/gerard/projects/truffle/cover', timeout=max_cover_time)
except subprocess.TimeoutExpired:
    print("timeout!") # either much slower than gcc or runtime > 1 second
    sys.exit(0) # interesting!
cover_runtime = time.time() - start
if e.returncode != 0:
    print("cover returned with an error")
    sys.exit(1) # not interesting

if cover_runtime < min_runtime:
    print("cover executed the code too fast: " + str(cover_runtime))
    sys.exit(1) # not interesting

print("cover_runtime" + str(cover_runtime))
print("gcc_runtime" + str(gcc_runtime))

if cover_runtime < max_cover_time:
    print("not interesting (too fast)")
    sys.exit(1) # too fast!
else:
    print("interesting!")
    sys.exit(0);
