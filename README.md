# java2rust
java2rust is a naive attempt to produce a shortcut to translate Java code into Rust code.

It is the very first time that I used Antlr4.

I hope it can help someone in somehow.

# Folder antlr4-java-parser
It contains the original parser build using Java9.g4

# Folder j2r

It contains a Netbeans 11 project that actually converts Java code into Rust code.

Most converted code will need manual intervention to compile and run properly.

# Running j2r

At this time you have to modify Main class to compile your code.

There are 2 different methods todo so:

1. compileAT(String srcFileName, String dstFileName) which reads a single file and generate its Rust counterpar.

2. compileProject(String srcPath, String dstPath) which traverses a every folder and compiles Java ones.

