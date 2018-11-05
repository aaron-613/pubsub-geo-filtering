package com.solace.aaron.geo.api;

import org.junit.Test;

import com.solace.aaron.geo.api.RadixStringFormatter;

import static org.junit.Assert.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RadixStringFormatterTest {
	
	Logger logger = LogManager.getLogger(RadixStringFormatterTest.class);
	
    @Test
    public void factorTests() {
    	boolean results = true;
    	String converted;
    	converted = new RadixStringFormatter.Helper().radix(10).scale(0).padding(10).paddingForNegs(true).convert(123.45678,0);
    	results &= converted.equals("00000000123");
    	converted = new RadixStringFormatter.Helper().radix(10).scale(0).padding(10).paddingForNegs(true).convert(123.45678,1);
    	results &= converted.equals("0000000012");
    	converted = new RadixStringFormatter.Helper().radix(10).scale(0).padding(10).paddingForNegs(true).convert(123.45678,2);
    	results &= converted.equals("000000001");
    	converted = new RadixStringFormatter.Helper().radix(10).scale(0).padding(10).paddingForNegs(true).convert(123.45678,3);
    	results &= converted.equals("00000000");
    	converted = new RadixStringFormatter.Helper().radix(10).scale(0).padding(10).paddingForNegs(true).convert(123.45678,4);
    	results &= converted.equals("0000000");
    	converted = new RadixStringFormatter.Helper().radix(10).scale(0).padding(10).paddingForNegs(true).convert(123.45678,5);
    	results &= converted.equals("000000");
    	converted = new RadixStringFormatter.Helper().radix(10).scale(0).padding(10).paddingForNegs(true).convert(123.45678,6);
    	results &= converted.equals("00000");

    	System.out.println(new RadixStringFormatter.Helper().radix(10).scale(0).padding(10).paddingForNegs(true).debugConvert(123.45678,0));
    	System.out.println(new RadixStringFormatter.Helper().radix(10).scale(0).padding(10).paddingForNegs(true).debugConvert(123.45678,1));
    	System.out.println(new RadixStringFormatter.Helper().radix(10).scale(0).padding(10).paddingForNegs(true).debugConvert(123.45678,2));
    	System.out.println(new RadixStringFormatter.Helper().radix(10).scale(0).padding(10).paddingForNegs(true).debugConvert(123.45678,3));
    	System.out.println(new RadixStringFormatter.Helper().radix(10).scale(0).padding(10).paddingForNegs(true).debugConvert(123.45678,4));
    	System.out.println(new RadixStringFormatter.Helper().radix(10).scale(0).padding(10).paddingForNegs(true).debugConvert(123.45678,5));
    	System.out.println(new RadixStringFormatter.Helper().radix(10).scale(0).padding(10).paddingForNegs(true).debugConvert(123.45678,6));
    	
        assertTrue("Check that my conversion works properly", results);
    }
    
    @Test
    public void scaleTests() {
    	boolean results = true;
    	String converted;
    	converted = new RadixStringFormatter.Helper().radix(10).scale(0).padding(10).paddingForNegs(true).convert(123.45678,0);
    	results &= converted.equals("00000000123");
    	converted = new RadixStringFormatter.Helper().radix(10).scale(1).padding(10).paddingForNegs(true).convert(123.45678,0);
    	results &= converted.equals("00000001234");
    	converted = new RadixStringFormatter.Helper().radix(10).scale(2).padding(10).paddingForNegs(true).convert(123.45678,0);
    	results &= converted.equals("00000012345");
    	converted = new RadixStringFormatter.Helper().radix(10).scale(3).padding(10).paddingForNegs(true).convert(123.45678,0);
    	results &= converted.equals("00000123456");
    	converted = new RadixStringFormatter.Helper().radix(10).scale(4).padding(10).paddingForNegs(true).convert(123.45678,0);
    	results &= converted.equals("00001234567");
    	converted = new RadixStringFormatter.Helper().radix(10).scale(5).padding(10).paddingForNegs(true).convert(123.45678,0);
    	results &= converted.equals("00012345678");
    	converted = new RadixStringFormatter.Helper().radix(10).scale(6).padding(10).paddingForNegs(true).convert(123.45678,0);
    	results &= converted.equals("00123456780");
   	
        assertTrue("Check that my conversion works properly", results);
        
        logger.info("Showing the effect of the scale element, effectively moves the radix point");
    	logger.info(new RadixStringFormatter.Helper().radix(10).scale(0).padding(10).paddingForNegs(true).debugConvert(123.45678,0));
    	logger.info(new RadixStringFormatter.Helper().radix(10).scale(1).padding(10).paddingForNegs(true).debugConvert(123.45678,0));
    	logger.info(new RadixStringFormatter.Helper().radix(10).scale(2).padding(10).paddingForNegs(true).debugConvert(123.45678,0));
    	logger.info(new RadixStringFormatter.Helper().radix(10).scale(3).padding(10).paddingForNegs(true).debugConvert(123.45678,0));
    	logger.info(new RadixStringFormatter.Helper().radix(10).scale(4).padding(10).paddingForNegs(true).debugConvert(123.45678,0));
    	logger.info(new RadixStringFormatter.Helper().radix(10).scale(5).padding(10).paddingForNegs(true).debugConvert(123.45678,0));
    	logger.info(new RadixStringFormatter.Helper().radix(10).scale(6).padding(10).paddingForNegs(true).debugConvert(123.45678,0));
    	logger.info(new RadixStringFormatter.Helper().radix(10).scale(-1).padding(10).paddingForNegs(true).debugConvert(123.45678,0));
    	logger.info(new RadixStringFormatter.Helper().radix(10).scale(-2).padding(10).paddingForNegs(true).debugConvert(123.45678,0));
    	logger.info(new RadixStringFormatter.Helper().radix(10).scale(-3).padding(10).paddingForNegs(true).debugConvert(123.45678,0));

    }
    
    @Test
    public void printouts() {
    	boolean results = true;
    	for (int i=2;i<=36;i++) {
        	System.out.printf("%2da) %12s %12s %12s%n",i,
        			new RadixStringFormatter.Helper().radix(i).scale(0).padding(10).paddingForNegs(true).convert(123.45678,0),
        			new RadixStringFormatter.Helper().radix(i).scale(1).padding(10).paddingForNegs(true).convert(123.45678,0),
        			new RadixStringFormatter.Helper().radix(i).scale(2).padding(10).paddingForNegs(true).convert(123.45678,0),
        			new RadixStringFormatter.Helper().radix(i).scale(3).padding(10).paddingForNegs(true).convert(123.45678,0));
    	}
        assertTrue("Check that my conversion works properly", results);
    }

    @Test
    public void printouts2() {
    	boolean results = true;
    	for (int i=2;i<=36;i++) {
        	System.out.printf("%2db) %12s %11s %10s%n",
        			i,
        			new RadixStringFormatter.Helper().radix(i).scale(3).padding(10).paddingForNegs(true).convert(123.45678,0),
        			new RadixStringFormatter.Helper().radix(i).scale(3).padding(10).paddingForNegs(true).convert(123.45678,1),
        			new RadixStringFormatter.Helper().radix(i).scale(3).padding(10).paddingForNegs(true).convert(123.45678,2),
        			new RadixStringFormatter.Helper().radix(i).scale(3).padding(10).paddingForNegs(true).convert(123.45678,3));
    	}
        assertTrue("Check that my conversion works properly", results);
    }

}
