package com.takipi.tests.speedtest.data;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Random;

import org.apache.commons.io.IOUtils;

public class DataBytes
{
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
		InputStream in = new BufferedInputStream(new URL("http",
				"download.thinkbroadband.com", "/5MB.zip").openStream());
		return IOUtils.toByteArray(in);
	}

	private static byte[] get10MBData() throws Exception
	{
		InputStream in = new BufferedInputStream(new URL("http",
				"download.thinkbroadband.com", "/10MB.zip").openStream());
		return IOUtils.toByteArray(in);
	}
	
	private static byte[] get100MBData() throws Exception
	{
		InputStream in = new BufferedInputStream(new URL("http",
				"download.thinkbroadband.com", "/100MB.zip").openStream());
		return IOUtils.toByteArray(in);
	}
}
