package com.takipi.tests.speedtest.aws;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.Region;

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
		for (Region region : Region.values())
		{
			StringBuilder bucketNameBuilder = new StringBuilder();
			bucketNameBuilder.append(BUCKET_PREFIX);
			bucketNameBuilder.append(region.toString());
			bucketNameBuilder.append(BUCKET_SUFFIX);
			
			String bucketName = bucketNameBuilder.toString().toLowerCase();
			
			buckets.put(region, bucketName);
			
			if (create)
			{
				s3client.createBucket(bucketName, region);
				logger.debug("Creating bucket {} in region {}", bucketName, region);
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
	
	public static URL getSignedUrl(String bucket, String key)
	{
		GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucket, key, HttpMethod.PUT);
		
		return s3client.generatePresignedUrl(request);
	}
	
	public static boolean putBytes(String bucket, String key, byte[] bytes)
	{
		ObjectMetadata metaData = new ObjectMetadata();
		metaData.setContentLength(bytes.length);
		
		return doPutObject(bucket, key, new ByteArrayInputStream(bytes), metaData);
	}

	private static boolean doPutObject(String bucket, String key, InputStream is, ObjectMetadata metaData)
	{
		try
		{
			s3client.putObject(bucket, key, is, metaData);
			return true;
		}
		catch (Exception e)
		{
			logger.error("Error putting object", e);
			return false;
		}
	}
}
