package com.takipi.tests.speedtest.task;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.takipi.tests.speedtest.aws.S3Manager;

public class UploadWithPlainJavaTask extends UploadTask
{
	private static final Logger logger = LoggerFactory.getLogger(UploadWithPlainJavaTask.class);
	
	public UploadWithPlainJavaTask(String bucket, byte[] data)
	{
		super(bucket, data);
	}
	
	@Override
	public void run()
	{
		String key = UUID.randomUUID().toString();
		
		long startSign = System.currentTimeMillis();
		
		URL url = S3Manager.getSignedUrl(bucket, key);
		
		long endSign = System.currentTimeMillis();
		
		long signTime = endSign - startSign;
		
		HttpsURLConnection httpCon = null;
		
		try
		{
			long start = System.currentTimeMillis();
			
			httpCon = (HttpsURLConnection)url.openConnection();
			httpCon.setDoOutput(true);
			httpCon.setRequestMethod("PUT");
			httpCon.setUseCaches(false);
			
			OutputStream output = httpCon.getOutputStream();
			
			IOUtils.copy(new ByteArrayInputStream(data), output);
			
			boolean success = (httpCon.getResponseCode() == HttpURLConnection.HTTP_OK); // Blocking until file upload completed
			
			long finish = System.currentTimeMillis();
			
			long uploadTime = finish - start;
			
			long totalTime = uploadTime + signTime;
			
			logger.debug("Upload task to {} finished in {} ms. (Sign: {}, Upload: {})", bucket, totalTime, signTime, uploadTime);
			
			result = new UploadTaskResult(success, uploadTime);
		}
		catch (IOException e)
		{
			logger.error("Problem with url " + url.toString(), e);
		}
	}
}
