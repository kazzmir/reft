#!/bin/sh

jar=$(ls *.jar | tail -n 1)
echo "Running ${jar}"
java -Xmx512m -jar $jar $*
