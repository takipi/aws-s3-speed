aws-s3-speed
============

A project we use in www.takipi.com to test upload speeds to Amazon Simple Storage Service (S3) regions.

Installation
------------
a. Fork / Get the code and run<br/>
```mvn clean compile assembly:single```<br/>
b. Download latest snapshot<br/>
https://s3.amazonaws.com/app-takipi-com/tests/aws-s3-speed-0.0.6-jar-with-dependencies.jar
  
Usage
-----
You will need your AWS access keys.<br/>
Make sure you have buckets in all AWS regions or use the CREATE mode.<br/>
The prefix and suffix are concatenated to the bucket name such as: aws-speed-test-eu-1-06-mar-2013<br/>
```java -jar aws-s3-speed-0.0.6-jar-with-dependencies.jar  AWS_KEY AWS_SECRET PREFIX SUFFIX```

Running the test:<br/>
Use the RUN mode (make sure AWS access keys, prefix and suffix are the same as when you run it with CREATE<br/>
The reason it's a two way process is because you should wait a few minutes before running the test to make sure the DNS servers will easily find all your buckets around the world and won't affect the first round of the test.

Cleanup buckets:<br/>
Use DELETE mode to remove buckets<br/>

ROUNDS: Number of rounds to test, usually 12 (since we remove the best and worst scores before averaging)<br/>
SIZE: SMALL=1KB, MEDIUM=5MB, BIG=10MB, HUGE=100MB<br/>
METHOD: <br/>SDK = Uses AWS SDK S3Client `putObject` (uses HttpClient).<br/>PLAIN = Sign a link and uses plain `HttpsURLConnection` output stream<br/>
```java -jar aws-s3-speed-0.0.6-jar-with-dependencies.jar RUN AWS_KEY AWS_SECRET PREFIX SUFFIX ROUNDS BIG SDK```
