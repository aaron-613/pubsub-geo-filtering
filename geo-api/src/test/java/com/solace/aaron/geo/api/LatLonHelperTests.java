package com.solace.aaron.geo.api;

import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

public class LatLonHelperTests {
    
    Logger logger = LogManager.getLogger(LatLonHelperTests.class);
    

    @Test
    public void compareOldNewWays() {
        double[] lats = new double[] {0,15,30,45,60,75,90};
        //double lat = 45;
        double lon = 0;
        double radiusMeters = 10000;
        for (double lat : lats) {
            //logger.info(Arrays.toString(LatLonHelper.getLatLonCircleDimensions(lat, lon, radiusMeters)));
            logger.info(Arrays.toString(LatLonHelper.getLatLonCircleDimensions2(lat, radiusMeters)));
        }
        
    }

    @Test
    public void someTests() {
        logger.info("Some Tests");
        // logger.info(LatLonHelper.convertDecimalDegreeToMetres(1.123));
        // logger.info(LatLonHelper.convertDecimalDegreeToMetres(0.123));
        // logger.info(LatLonHelper.convertDecimalDegreeToMetres(0.0123));
        // logger.info(LatLonHelper.convertDecimalDegreeToMetres(0.00123));
        
        // logger.info(Arrays.toString(LatLonHelper.getLatLonCircleDimensions(51, 103, 100)));
        // logger.info(Arrays.toString(LatLonHelper.getLatLonCircleDimensions(51, 103, 1000)));
        // logger.info(Arrays.toString(LatLonHelper.getLatLonCircleDimensions(51, 103, 10000)));
        
    }
}
