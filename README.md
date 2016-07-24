# Cover

Cover aims to implement a safe subset of C++ using the Truffle framework. The goal is to be able to run parts of your existing C++ code on the JVM in a safe way. It is aimed at algorithms or library functions that would take a lot of effort to replace. If you want full C++ support, but don't need memory safety, take a look at [Sulong](https://github.com/graalvm/sulong).

This project is at a very early stage. 

## Design

Cover aims to support the following C++ features:
* standard C++ syntax
* basic types, control flow
* objects and multiple inheritance
* virtual functions
* basic standard library
* basic preprocessor support

Does NOT support the following C++ features:
* delete: everything is garbage collected
* pointer arithmetic and casting to incompatible types
* exceptions

This repository is licensed under the permissive UPL licence.

## Prerequisites
* JDK 8
* maven3 

## Installation

* Clone the repository using
  `git clone https://github.com/TODO`
* Download Graal VM Development Kit from 
  http://www.oracle.com/technetwork/oracle-labs/program-languages/downloads
* Unpack the downloaded `graalvm_*.tar.gz` into `cover/graalvm`, or add a symlink.
* Verify that the file `cover/graalvm/bin/java` exists and is executable
* Execute `mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file -Dfile=lib/org.eclipse.cdt.core_6.0.0.201607151550.jar -DgroupId=org.eclipse.cdt -DartifactId=cdt-core -Dversion=6.0.0 -Dpackaging=jar`
* Execute `mvn package`

## IDE Setup 

### Eclipse
* Tested with Eclipse Mars SR2
* Open Eclipse with a new workspace
* Install `m2e` and `m2e-apt` plugins from the Eclipse marketplace (Help -> Eclipse Marketplace...)
* File -> Import... -> Existing Maven Projects -> Select `cover` folder -> Finish

### Netbeans
* Tested with Netbeans 8.1
* Open Netbeans
* File -> Open Project -> Select `cover` folder -> Open Project

### IntelliJ IDEA
* Tested with IntelliJ 2016.1.3 Community Edition
* Open IntelliJ IDEA
* File -> New -> Project from existing Sources -> Select `cover` folder -> Click next and keep everything default on several screens -> Finish

## Running

* Execute `./cover tests/HelloWorld.cover` to run a simple language source file.
* Execute `gcc -x c++ -o hello tests/HelloWorld.cover` and then `./hello` to verify that the cover file is also valid C++.
* Execute `./cover -disassemble tests/SumPrint.cover` to see assembly code for Truffle compiled functions.

## IGV

* Download the Ideal Graph Visualizer (IGV) from
  https://lafo.ssw.uni-linz.ac.at/pub/idealgraphvisualizer/
* Unpack the downloaded `.zip` file  
* Execute `bin/idealgraphvsiualizer` to start IGV
* Execute `./sl -dump tests/SumPrint.cover` to dump graphs to IGV.

## Debugging

* Execute `./cover -debug tests/HelloWorld.cpp`.
* Attach a Java remote debugger (like Eclipse) on port 8000.

## Further information

* [Truffle JavaDoc](http://lafo.ssw.uni-linz.ac.at/javadoc/truffle/latest/)
* [Truffle on Github](http://github.com/graalvm/truffle)
* [Graal on Github](http://github.com/graalvm/graal-core)
* [Truffle Tutorials and Presentations](https://wiki.openjdk.java.net/display/Graal/Publications+and+Presentations)
* [Truffle FAQ and Guidelines](https://wiki.openjdk.java.net/display/Graal/Truffle+FAQ+and+Guidelines)
* [Graal VM]( http://www.oracle.com/technetwork/oracle-labs/program-languages/overview) on the Oracle Technology Network
* [Papers on Truffle](http://ssw.jku.at/Research/Projects/JVM/Truffle.html)
* [Papers on Graal](http://ssw.jku.at/Research/Projects/JVM/Graal.html)
* [Sulong](https://github.com/graalvm/sulong)

## License

The Truffle framework is licensed under the [GPL 2 with Classpath exception](http://openjdk.java.net/legal/gplv2+ce.html).
The Cover language is licensed under the [Universal Permissive License (UPL)](http://opensource.org/licenses/UPL).
