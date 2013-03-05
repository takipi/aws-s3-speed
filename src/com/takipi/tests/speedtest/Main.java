package com.takipi.tests.speedtest;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.s3.model.Region;
import com.takipi.tests.speedtest.aws.CredentialsManager;
import com.takipi.tests.speedtest.aws.S3Manager;
import com.takipi.tests.speedtest.data.DataBytes;
import com.takipi.tests.speedtest.data.DataBytes.Size;
import com.takipi.tests.speedtest.task.UploadTaskType;

public class Main
{
	private static final Logger logger = LoggerFactory.getLogger(Main.class);
	
	public static void main(String[] args) throws Exception
	{
		if (!((args.length == 5) || (args.length == 8)))
		{
			System.out.println("Usage: CREATE AWS_KEY AWS_SECRET PREFIX SUFFIX");
			System.out.println("Usage: RUN AWS_KEY AWS_SECRET PREFIX SUFFIX ROUNDS SMALL|MEDIUM|BIG|HUGE SDK|PLAIN");
			return;
		}
		
		CredentialsManager.setup(args[1], args[2]);
		
		S3Manager.initBuckets(args[3], args[4]);
		
		if (args.length == 5)
		{
			S3Manager.initBuckets(true);
			return;
		}
		
		int rounds = Integer.parseInt(args[5]);
		byte[] data = DataBytes.getData(Size.valueOf(args[6]));
		
		UploadTaskType uploadType = UploadTaskType.valueOf(args[7]);
		
		logger.debug("Starting test");
		
		SpeedTest speedTest = new SpeedTest(rounds, data, uploadType);
		speedTest.start();
		
		logger.debug("Test finished");
		
		printResults(speedTest.getTimings());
	}
	
	private static void printResults(Map<Region, List<Long>> timings)
	{
		for (Region region : Region.values())
		{
			long sum = 0;
			
			List<Long> regionTimings = timings.get(region);
			
			if (regionTimings.size() > 2)
			{
				Collections.sort(regionTimings);
				regionTimings.remove(0);
				regionTimings.remove(regionTimings.size() - 1);
			}
			
			int timingsCount = regionTimings.size();
			
			for (Long time : timings.get(region))
			{
				sum += time.longValue();
			}
			
			double avg = sum / (double)timingsCount;
			
			logger.info("Region {}: {} valid uploads. lowest: {} ms, highest: {} ms. Average: {} ms.", region, timingsCount,
					regionTimings.get(0), regionTimings.get(timingsCount - 1), avg);
		}
	}
}
