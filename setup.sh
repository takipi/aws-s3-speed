#!/bin/sh
. ./conf.sh

# run the command that prepares the bucket
echo $JAVA -jar $JAR CREATE $AWS_KEY $AWS_SECRET $PREFIX $SUFFIX
$JAVA -jar $JAR CREATE $AWS_KEY $AWS_SECRET $PREFIX $SUFFIX
