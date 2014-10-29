package com.takipi.tests.speedtest.task;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.takipi.tests.speedtest.aws.S3Manager;
import com.amazonaws.services.s3.model.Region;

public class UploadWithAwsSdkTask extends UploadTask
{
	private static final Logger logger = LoggerFactory.getLogger(UploadWithAwsSdkTask.class);
	
    public UploadWithAwsSdkTask(Region region, String bucket, byte[] data)
	{
            super(region, bucket, data);
	}
	
	@Override
	public void run()
	{
		String key = UUID.randomUUID().toString();
		
		long start = System.currentTimeMillis();
		
		boolean success = S3Manager.putBytes(region, bucket, key, data);
		
		long finish = System.currentTimeMillis();
		
		long time = finish - start;
		
		logger.debug("Upload task to {} finished in {} ms", bucket, time);
		
		result = new UploadTaskResult(success, time);
	}
}
