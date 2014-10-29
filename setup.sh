#!/bin/sh
JAR='aws-s3-speed-0.0.5-SNAPSHOT-jar-with-dependencies.jar'
JAVA='/usr/bin/java'

AWS_KEY='AKIAIMBGWFS6Z4NVITMQ'
AWS_SECRET='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'

PREFIX='aws-speed-test-'
SUFFIX='-28-10-2014'

# run the command that prepares the bucket
$JAVA -jar $JAR CREATE $AWS_KEY $AWS_SECRET $PREFIX $SUFFIX

