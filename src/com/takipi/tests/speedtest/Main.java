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
                System.out.println("AWS upload speed - By Takipi");
                System.out.println("Usage: CREATE AWS_KEY AWS_SECRET PREFIX SUFFIX");
                System.out.println("Usage: DELETE AWS_KEY AWS_SECRET PREFIX SUFFIX");
                System.out.println("Usage: RUN AWS_KEY AWS_SECRET PREFIX SUFFIX ROUNDS SMALL|MEDIUM|BIG|HUGE SDK|PLAIN");
                System.out.println("PREFIX can be something like aws-speed-test-");
                System.out.println("SUFFIX can be something like -xx-xx-xxxx");
                return;
            }
		
        CredentialsManager.setup(args[1], args[2]);
        S3Manager.initBuckets(args[3], args[4]);
		
        if (args[0].equals("CREATE"))
            {
                S3Manager.initBuckets(true);
                return;
            }
        else if (args[0].equals("DELETE"))
            {
                S3Manager.initBuckets(false);
                S3Manager.removeBuckets();
                return;
            }
		
        int rounds = Integer.parseInt(args[5]);
        byte[] data = DataBytes.getData(Size.valueOf(args[6]));
		
        UploadTaskType uploadType = UploadTaskType.valueOf(args[7]);
		
        logger.debug("Starting test");
		
        SpeedTest speedTest = new SpeedTest(rounds, data, uploadType);
        speedTest.start();
		
        logger.debug("Test finished");

        logger.debug("Upload results");
        printResults(speedTest.getUploadTimings());
        logger.debug("Download results");
        printResults(speedTest.getDownloadTimings());
    }
	
    private static void printResults(Map<Region, List<Long>> timings)
    {
        String regionName = "";
        
        for (Region region : Region.values())
            {
                if (region.toString() != null) {
                    regionName = region.toString();
                } else {
                    regionName = "us-east-1";
                }
                logger.debug("RegionName: '{}'", regionName);
                if (regionName.equals("s3-us-gov-west-1") || regionName.equals("cn-north-1")) {
                    logger.debug("Skipping: Not authorized for region {}", regionName);
                    continue;
                }

                long sum = 0;
			
                List<Long> regionTimings = timings.get(region);

                if (regionTimings.size() == 0) {
                    logger.debug("Skipping: No results for region {}", regionName);
                    continue;
                }
			
                if (regionTimings.size() > 1)
                    {
                        Collections.sort(regionTimings);
                    }

                if (regionTimings.size() > 2)
                    {
                        regionTimings.remove(0);
                        regionTimings.remove(regionTimings.size() - 1);
                    }
			
                int timingsCount = regionTimings.size();
			
                for (Long time : timings.get(region))
                    {
                        sum += time.longValue();
                    }
			
                double avg = sum / (double)timingsCount;
                double median;
                    {
                        int middle = timingsCount / 2;
                        if (timingsCount == 1)
                            {
                                median = regionTimings.get(0);
                            }
                        else if (timingsCount % 2 == 1)
                            {
                                median = regionTimings.get(middle);
                            }
                        else
                        {
                            median = (regionTimings.get(middle - 1) + regionTimings.get(middle)) / 2.0;
                        }
                    }

                logger.info("Region {}: {} valid tasks. lowest: {} ms, highest: {} ms. Average: {} ms, median: {} ms.", region, timingsCount,
                            regionTimings.get(0), regionTimings.get(timingsCount - 1), avg, median);
            }
    }
}
