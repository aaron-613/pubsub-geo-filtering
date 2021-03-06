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
        
        logger.info(Arrays.toString(LatLonHelper.getLatLonCircleDimensions2(51, 100)));
        logger.info(Arrays.toString(LatLonHelper.getLatLonCircleDimensions2(51, 1000)));
        logger.info(Arrays.toString(LatLonHelper.getLatLonCircleDimensions2(51, 10000)));
        
    }



    @Test
    public void degreeConvertTests() {
        logger.info("degreeConvertTests");
        
        logger.info(LatLonHelper.getMetresPerDegreeLat(0));
        logger.info(LatLonHelper.getMetresPerDegreeLat(30));
        logger.info(LatLonHelper.getMetresPerDegreeLat(45));
        logger.info(LatLonHelper.getMetresPerDegreeLat(60));
        logger.info(LatLonHelper.getMetresPerDegreeLat(75));
        
        logger.info(LatLonHelper.getMetresPerDegreeLon(0));
        logger.info(LatLonHelper.getMetresPerDegreeLon(30));
        logger.info(LatLonHelper.getMetresPerDegreeLon(45));
        logger.info(LatLonHelper.getMetresPerDegreeLon(60));
        logger.info(LatLonHelper.getMetresPerDegreeLon(75));
        
    }

}
