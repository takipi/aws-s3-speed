package com.takipi.tests.speedtest.task;

import java.util.Arrays;
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
		
		long uploadTime = finish - start;

		if (!success) {
			result = new UploadTaskResult(success, uploadTime, 0);
			return;
		}

		start = System.currentTimeMillis();

		byte[] returnData = S3Manager.getBytes(region, bucket, key);

		finish = System.currentTimeMillis();

		long downloadTime = finish - start;
		
		logger.debug("Download task to {} finished in {} ms", bucket, downloadTime);

		result = new UploadTaskResult(success && returnData != null && Arrays.equals(data, returnData), uploadTime, downloadTime);

		S3Manager.deleteBytes(region, bucket, key);

		logger.debug("Delete task to {} finished", bucket);
	}
}
