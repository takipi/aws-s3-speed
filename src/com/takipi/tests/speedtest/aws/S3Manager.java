package com.takipi.tests.speedtest.aws;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.s3.model.S3Object;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.Region;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.RegionUtils;

public class S3Manager
{
    private static String BUCKET_PREFIX;// = "takipi-aws-speed-test-";
    private static String BUCKET_SUFFIX;// = "-05-mar-2013";
	
    private static final Logger logger = LoggerFactory.getLogger(S3Manager.class);

    private static final AmazonS3 s3client;

    public static final Map<Region, String> buckets; 
	
    static
    {
        s3client = new AmazonS3Client(CredentialsManager.getCreds());
        buckets = new HashMap<Region, String>();
    }
	
    public static void initBuckets(String prefix, String suffix)
    {
        BUCKET_PREFIX = prefix;
        BUCKET_SUFFIX = suffix;
    }
	
    public static Map<Region, String> getBuckets()
    {
        return buckets;
    }
	
    public static void initBuckets(boolean create)
    {
        String regionName = "";
        for (Region region : Region.values())
            {
                logger.debug("Region: '{}'", region);

                if (region.toString() != null) {
                    regionName = region.toString();
                } else {
                    regionName = "us-east-1";
                }
                logger.debug("RegionName: '{}'", regionName);
                    
                // need to skip this region because we're not authorized
                if (regionName.equals("s3-us-gov-west-1") || regionName.equals("cn-north-1")) {
                    logger.debug("Skipping: Not authorized for region {}", regionName);
                    continue;
                }
                    
                StringBuilder bucketNameBuilder = new StringBuilder();
                bucketNameBuilder.append(BUCKET_PREFIX);
                bucketNameBuilder.append(regionName);
                bucketNameBuilder.append(BUCKET_SUFFIX);
			
                String bucketName = bucketNameBuilder.toString().toLowerCase();
                    
                buckets.put(region, bucketName);
			
                if (create)
                    {
                        try {                            
                            // need to set the region for "eu-central-1" region to work
                            // this enables V4 signing
                            // careful, this is not thread-safe!
                            logger.debug("Setregion: {}", regionName);
                            s3client.setRegion(RegionUtils.getRegion(regionName)); 
                            if (! s3client.doesBucketExist(bucketName)) {
                                s3client.createBucket(bucketName, region);
                                logger.debug("Creating bucket {} in region {}", bucketName, region);
                            } else
                                logger.debug("Skipping: Bucket {} in region {} already exists.", bucketName, region);
                            
                        } catch (AmazonServiceException ase) {
                            logger.debug("Caught an AmazonServiceException, which " +
                                         "means your request made it " +
                                         "to Amazon S3, but was rejected with an error response" +
                                         " for some reason.");
                            logger.debug("Error Message:    " + ase.getMessage());
                            logger.debug("HTTP Status Code: " + ase.getStatusCode());
                            logger.debug("AWS Error Code:   " + ase.getErrorCode());
                            logger.debug("Error Type:       " + ase.getErrorType());
                            logger.debug("Request ID:       " + ase.getRequestId());
                        } catch (AmazonClientException ace) {
                            logger.debug("Caught an AmazonClientException, which " +
                                         "means the client encountered " +
                                         "an internal error while trying to " +
                                         "communicate with S3, " +
                                         "such as not being able to access the network.");
                            logger.debug("Error Message: " + ace.getMessage());
                        }
                    }

            }
    }
	
    public static void removeBuckets()
    {
        for (String bucketName : buckets.values())
            {
                s3client.deleteBucket(bucketName);
			
                logger.debug("Deleting bucket {}", bucketName);
            }
    }
	
    public static URL getSignedUrl(String bucket, String key, HttpMethod method)
    {
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucket, key, method);
		
        return s3client.generatePresignedUrl(request);

    }

    public static boolean putBytes(Region region, String bucket, String key, byte[] bytes)
    {
        ObjectMetadata metaData = new ObjectMetadata();
        metaData.setContentLength(bytes.length);

        return doPutObject(region, bucket, key, new ByteArrayInputStream(bytes), metaData);
    }

    public static byte[] getBytes(Region region, String bucket, String key)
    {
        return doGetObject(region, bucket, key);
    }

    public static void deleteBytes(Region region, String bucket, String key)
    {
        doDeleteObject(region, bucket, key);
    }

    private static boolean doPutObject(Region region, String bucket, String key, InputStream is, ObjectMetadata metaData)
    {
        try
            {
                String regionName = "";
                if (region.toString() != null) {
                    regionName = region.toString();
                } else {
                    regionName = "us-east-1";
                }
                logger.debug("Setregion: {}", regionName);
                // need to set the region for "eu-central-1" region to work
                // this enables V4 signing
                // careful, this is not thread-safe!
                s3client.setRegion(RegionUtils.getRegion(regionName));
                logger.debug("PUT object to S3 bucket: {}", bucket);
                s3client.putObject(bucket, key, is, metaData);
                return true;
            }
        catch (Exception e)
            {
                logger.error("Error putting object", e);
                return false;
            }
    }

    private static byte[] doGetObject(Region region, String bucket, String key)
    {
        try
        {
            String regionName = "";
            if (region.toString() != null) {
                regionName = region.toString();
            } else {
                regionName = "us-east-1";
            }
            logger.debug("Setregion: {}", regionName);
            // need to set the region for "eu-central-1" region to work
            // this enables V4 signing
            // careful, this is not thread-safe!
            s3client.setRegion(RegionUtils.getRegion(regionName));
            logger.debug("GET object from S3 bucket: {}", bucket);
            S3Object object = s3client.getObject(bucket, key);
            InputStream reader = new BufferedInputStream(object.getObjectContent());
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            OutputStream writer = new BufferedOutputStream(bytes);
            int read = -1;
            while ( ( read = reader.read() ) != -1 ) {
                writer.write(read);
            }
            writer.flush();
            writer.close();
            reader.close();
            object.close();
            return bytes.toByteArray();
        }
        catch (Exception e)
        {
            logger.error("Error getting object", e);
            return null;
        }
    }

    private static void doDeleteObject(Region region, String bucket, String key)
    {
        try
        {
            String regionName = "";
            if (region.toString() != null) {
                regionName = region.toString();
            } else {
                regionName = "us-east-1";
            }
            logger.debug("Setregion: {}", regionName);
            // need to set the region for "eu-central-1" region to work
            // this enables V4 signing
            // careful, this is not thread-safe!
            s3client.setRegion(RegionUtils.getRegion(regionName));
            logger.debug("DELETE object from S3 bucket: {}", bucket);
            s3client.deleteObject(bucket, key);
        }
        catch (Exception e)
        {
            logger.error("Error deleting object", e);
            return;
        }
    }
}
