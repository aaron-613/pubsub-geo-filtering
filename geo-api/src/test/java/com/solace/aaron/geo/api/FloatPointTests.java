package com.solace.aaron.geo.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

public class FloatPointTests {
    
    Logger logger = LogManager.getLogger(FloatPointTests.class);
    

    @Test
    public void someTests() {
    	logger.info("Some Tests");
    	
        for (int i=1;i<=10;i++) {
        	double d = i;
            System.out.println(d/10);
            System.out.println(d*0.1);
        }
    }
    
    @Test
    public void parseDoubleTests2() {
        float test = 123.456f;
        System.out.println(test);
        test = 173.4567f;
        System.out.println(test);
        test = 173.45670f;
        System.out.println(test);
        test = 173.45671f;
        System.out.println(test);
        test = 173.45672f;
        System.out.println(test);
        test = 173.45673f;
        System.out.println(test);
        test = 173.45674f;
        System.out.println(test);
        test = 173.45675f;
        System.out.println(test);
        test = 173.45676f;
        System.out.println(test);
        test = 173.45677f;
        System.out.println(test);
        test = 173.45678f;
        System.out.println(test);
        test = 173.45679f;
        System.out.println(test);

    }
    
    @Test
    public void parseDoubleTests() {
        // used in DecimalStringFormatter
        String ds;
        double d;
        
        ds= "12";
        d = Double.parseDouble(ds);
        System.out.println(ds+": "+d);
        
        ds= "-12";
        d = Double.parseDouble(ds);
        System.out.println(ds+": "+d);
        
        ds= "12.";
        d = Double.parseDouble(ds);
        System.out.println(ds+": "+d);

        ds= "-0012";
        d = Double.parseDouble(ds);
        System.out.println(ds+": "+d);

        ds= "-0012.";
        d = Double.parseDouble(ds);
        System.out.println(ds+": "+d);
    
        ds= "-0012.";
        d = Double.parseDouble(ds);
        System.out.println(ds+": "+d);
    
        ds= ".12";
        d = Double.parseDouble(ds);
        System.out.println(ds+": "+d);
    
        ds= "-.02";
        d = Double.parseDouble(ds);
        System.out.println(ds+": "+d);
    
        ds= "0.";
        d = Double.parseDouble(ds);
        System.out.println(ds+": "+d);
    
        ds= ".0";
        d = Double.parseDouble(ds);
        System.out.println(ds+": "+d);
    
        // just plain "." doesn't work
    }
    
}
