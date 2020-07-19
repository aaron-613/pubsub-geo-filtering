package com.solace.aaron.geo.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

public class TestGeoPublisher {
    
    Logger logger = LogManager.getLogger(TestGeoPublisher.class);

    public static double normalizeLon(double lon) {
//        if (lon >= 360) {
//            while (lon >= 360) lon -= 360;
//        } else if (lon < 0) {
//            while (lon < 0) lon += 360;
//        }
        return lon;
    }

    @Test
    public void decimalPublisher() {
    	logger.info("This will pretend I'm publishing data for a regular decimal system");
    	GeoStringFormatter formatter = GeoStringFormatter.buildRegularDecimalFormatter(8,4);  // [-100,1000] use for both lat [90,90] and lon [0,360)
    	double lat;
    	double lon;
    	
    	lat = 45.12344567;
    	lon = normalizeLon(-105.87654);
        System.out.println(lat+", "+lon);
    	System.out.println(formatter.convert(lat)+", "+formatter.convert(lon));
        System.out.println(formatter.getDecimalString(formatter.convert(lat))+", "+formatter.getDecimalString(formatter.convert(lon)));
        System.out.println();
        lat = 45.123454;
        lon = normalizeLon(-105.876540);
        System.out.println(lat+", "+lon);
        System.out.println(formatter.convert(lat)+", "+formatter.convert(lon));
        System.out.println(formatter.getDecimalString(formatter.convert(lat))+", "+formatter.getDecimalString(formatter.convert(lon)));
        System.out.println();
        lat = 45.123455;
        lon = normalizeLon(-105.8765401);
        System.out.println(lat+", "+lon);
        System.out.println(formatter.convert(lat)+", "+formatter.convert(lon));
        System.out.println(formatter.getDecimalString(formatter.convert(lat))+", "+formatter.getDecimalString(formatter.convert(lon)));
        System.out.println();
        lat = 45.123456;
        lon = normalizeLon(-105.876542);
        System.out.println(lat+", "+lon);
        System.out.println(formatter.convert(lat)+", "+formatter.convert(lon));
        System.out.println(formatter.getDecimalString(formatter.convert(lat))+", "+formatter.getDecimalString(formatter.convert(lon)));
        System.out.println();
        lon = normalizeLon(-105.876545001);
        System.out.println(lat+", "+lon);
        System.out.println(formatter.convert(lat)+", "+formatter.convert(lon));
        System.out.println(formatter.getDecimalString(formatter.convert(lat))+", "+formatter.getDecimalString(formatter.convert(lon)));
        System.out.println();
    	
    	
    }
    
    
    
}
