package com.takipi.tests.speedtest.task;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;

import com.amazonaws.HttpMethod;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.takipi.tests.speedtest.aws.S3Manager;
import com.amazonaws.services.s3.model.Region;

public class UploadWithPlainJavaTask extends UploadTask
{
	private static final Logger logger = LoggerFactory.getLogger(UploadWithPlainJavaTask.class);
	
    public UploadWithPlainJavaTask(Region region, String bucket, byte[] data)
	{
            super(region, bucket, data);
	}
	
	@Override
	public void run()
	{
		String key = UUID.randomUUID().toString();
		
		long startSign = System.currentTimeMillis();
		
		URL urlUpload = S3Manager.getSignedUrl(bucket, key, HttpMethod.PUT);
		URL urlDownload = S3Manager.getSignedUrl(bucket, key, HttpMethod.GET);
		URL urlDelete = S3Manager.getSignedUrl(bucket, key, HttpMethod.DELETE);
		
		long endSign = System.currentTimeMillis();
		
		long signTime = endSign - startSign;
		
		HttpsURLConnection httpCon = null;
		
		try
		{
			long start = System.currentTimeMillis();
			
			httpCon = (HttpsURLConnection)urlUpload.openConnection();
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

			if (!success) {
				result = new UploadTaskResult(success, uploadTime, 0);
				logger.warn("Failed to upload to {}, code {}", region, httpCon.getResponseCode());
				return;
			}

			start = System.currentTimeMillis();

			httpCon = (HttpsURLConnection)urlDownload.openConnection();
			httpCon.setDoOutput(false);
			httpCon.setRequestMethod("GET");
			httpCon.setUseCaches(false);

			InputStream input = httpCon.getInputStream();

			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			IOUtils.copy(input, bytes);

			success = (httpCon.getResponseCode() == HttpURLConnection.HTTP_OK) && Arrays.equals(data, bytes.toByteArray()); // Blocking until file download completed

			finish = System.currentTimeMillis();

			long downloadTime = finish - start;

			totalTime = downloadTime + uploadTime + signTime;

			logger.debug("Download task to {} finished in {} ms. (Sign: {}, Upload: {}, Download: {})", bucket, totalTime, signTime, uploadTime, downloadTime);

			httpCon = (HttpsURLConnection)urlDelete.openConnection();
			httpCon.setDoOutput(false);
			httpCon.setRequestMethod("DELETE");
			httpCon.setUseCaches(false);

			if (httpCon.getResponseCode() != HttpURLConnection.HTTP_OK && httpCon.getResponseCode() != HttpURLConnection.HTTP_NO_CONTENT) // Blocking until file delete completed
				{
					logger.warn("Failed to delete " + key + ", code " + httpCon.getResponseCode());
				}
			else
				{
					logger.debug("Delete task to {} finished", bucket);
				}
			
			result = new UploadTaskResult(success, uploadTime, downloadTime);
		}
		catch (IOException e)
		{
			logger.error("Problem with url " + urlUpload.toString() +
					" or url " + urlDownload +
					" or url " + urlDelete, e);
		}
	}
}
