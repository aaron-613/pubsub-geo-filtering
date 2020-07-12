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
