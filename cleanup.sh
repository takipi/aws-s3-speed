#!/bin/sh
. ./conf.sh

# run the command that cleanup the buckets
echo $JAVA -jar $JAR DELETE $AWS_KEY $AWS_SECRET $PREFIX $SUFFIX
$JAVA -jar $JAR DELETE $AWS_KEY $AWS_SECRET $PREFIX $SUFFIX
