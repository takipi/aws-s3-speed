package com.takipi.tests.speedtest.aws;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;

public class CredentialsManager
{
	private static String key;
	private static String secret;
	
	public static void setup(String newKey, String newSecret)
	{
		key = newKey;
		secret = newSecret;
	}
	
	public static AWSCredentials getCreds()
	{
		return new BasicAWSCredentials(key, secret);
	}
}
