package com.takipi.tests.speedtest.task;

public abstract class UploadTask implements Runnable
{
	protected final String bucket;
	protected final byte[] data;
	
	protected UploadTaskResult result;
	
	public static class UploadTaskResult
	{
		private final boolean 	success;
		private final long 		time;
		
		public UploadTaskResult(boolean success, long time)
		{
			this.success = success;
			this.time = time;
		}
		
		public boolean isSuccess()
		{
			return success;
		}

		public long getTime()
		{
			return time;
		}
	}
	
	public UploadTask(String bucket, byte[] data)
	{
		this.bucket = bucket;
		this.data 	= data;
	}
	
	
	public UploadTaskResult getResult()
	{
		return result;
	}
	
	@Override
	public abstract void run();
}
