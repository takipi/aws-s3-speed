package com.takipi.tests.speedtest.data;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

import org.apache.commons.io.IOUtils;

public class DataBytes
{
	private final static String URL = "ipv4.download.thinkbroadband.com";
	private final static String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36";

	public static enum Size
	{
		SMALL, MEDIUM, BIG, HUGE
	}

	public static byte[] getData(Size size) throws Exception
	{
		switch (size)
		{
			case SMALL:
				return get1KBData();
			case MEDIUM:
				return get5MBData();
			case BIG:
				return get10MBData();
			case HUGE:
				return get100MBData();
		}
		
		return null;
	}

	private static byte[] get1KBData() throws Exception
	{
		byte[] bytes = new byte[1024];
		Random random = new Random();
		random.nextBytes(bytes);

		return bytes;
	}

	private static byte[] get5MBData() throws Exception
	{
		return download("5MB.zip");
	}

	private static byte[] get10MBData() throws Exception
	{
		return download("10MB.zip");
	}
	
	private static byte[] get100MBData() throws Exception
	{
		return download("100MB.zip");
	}

	private static byte[] download(String file) throws Exception
	{
		URL url = new URL("http", URL, "/" + file);
		HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
		httpConn.addRequestProperty("User-Agent", USER_AGENT);
		InputStream in = httpConn.getInputStream();
		return IOUtils.toByteArray(in);
	}
}
