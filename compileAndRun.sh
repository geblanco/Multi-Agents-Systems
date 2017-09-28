#!/bin/sh
#
# Simple script to compile Mason code
#
# use example:
# ./compileAndRun.sh GameOfLife.java

# you need to change these three variables to suit your installation
MASON_DIR=./mason
JAVAC_CMD=javac
JAVA_CMD=java

# you shouldn't have to change the rest
CLASSPATH="$MASON_DIR:$CLASSPATH"
CLASSNAME=`basename $1 .java`
FILE=`basename $1`

CURR_PWD=`pwd`
cd `dirname $1`

$JAVAC_CMD -classpath "$CLASSPATH" "$FILE"
$JAVA_CMD -classpath "$CLASSPATH" "$CLASSNAME"

cd $CURR_PWD
