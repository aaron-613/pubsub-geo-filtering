package com.solace.aaron.geo.api;

import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

public class ArrayMathTest {
    
    Logger logger = LogManager.getLogger(ArrayMathTest.class);
    

    @Test
    public void someTests() {
    	logger.info("Some Tests");
    	
        double[] vals = new double[] { 1.0, 2.0, Double.POSITIVE_INFINITY, 1.5 };
        System.out.println(Arrays.toString(vals));
        System.out.println(Arrays.toString(ArrayMath.getArrayIndexReverseOrder(vals)));
    }
    
     
}
