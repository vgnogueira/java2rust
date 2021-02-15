#!/bin/bash

export CLASSPATH=".:/usr/local/lib/antlr-4.8-complete.jar:$CLASSPATH"
alias antlr4='java -jar /usr/local/lib/antlr-4.8-complete.jar'
alias grun='java org.antlr.v4.gui.TestRig'

antlr4 Java9.g4 -o parser

cd parser

javac *.java

grun Java9 compilationUnit *.java

