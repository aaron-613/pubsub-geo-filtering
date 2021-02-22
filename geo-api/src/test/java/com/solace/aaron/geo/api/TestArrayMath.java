package com.solace.aaron.geo.api;

import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestArrayMath {
    
    private static final Logger logger = LogManager.getLogger(TestArrayMath.class);
    

    @Test
    public void someTests() {
    	logger.info("Some Tests");
    	
        double[] vals = new double[] { 1.0, 2.0, Double.POSITIVE_INFINITY, 1.5 };
        int[] response = new int[] { 2, 1, 3, 0 };
        Assert.assertTrue(Arrays.equals(response, ArrayMath.getArrayIndexReverseOrder(vals)));
    }
    

    @BeforeClass
    public static void before() {
        logger.info("This class will look to verify some of the ArrayMath funcitons.");
    }

     
}
