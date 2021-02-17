package com.solace.aaron.geo.api;

import static org.junit.Assert.assertTrue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import com.solace.aaron.geo.api.GeoStringFormatter.Builder;

public class GeoStringFormatterTest {
    
    Logger logger = LogManager.getLogger(GeoStringFormatterTest.class);
    
    public void factorTests() {
        boolean results = true;
        String converted;
        converted = new GeoStringFormatter.Builder().radix(10).scale(0).width(10).offset(0).convert(123.45678);
        results &= converted.equals("00000000123");

        assertTrue("Check that my conversion works properly", results);
    }

    @Test
    public void scaleTests() {
    	logger.info("Scale Tests");
        logger.info("Showing the effect of the scale element, effectively moves the radix point");
        logger.info(new GeoStringFormatter.Builder().radix(10).scale(0).width(10).offset(0).debugConvert(123.45678));
        logger.info(new GeoStringFormatter.Builder().radix(10).scale(1).width(10).offset(0).debugConvert(123.45678));
        logger.info(new GeoStringFormatter.Builder().radix(10).scale(2).width(10).offset(0).debugConvert(123.45678));
        logger.info(new GeoStringFormatter.Builder().radix(10).scale(3).width(10).offset(0).debugConvert(123.45678));
        logger.info(new GeoStringFormatter.Builder().radix(10).scale(4).width(10).offset(0).debugConvert(123.45678));
        logger.info(new GeoStringFormatter.Builder().radix(10).scale(5).width(10).offset(0).debugConvert(123.45678));
        logger.info(new GeoStringFormatter.Builder().radix(10).scale(6).width(10).offset(0).debugConvert(123.45678));
        logger.info(new GeoStringFormatter.Builder().radix(10).scale(-1).width(10).offset(0).debugConvert(123.45678));
        logger.info(new GeoStringFormatter.Builder().radix(10).scale(-2).width(10).offset(0).debugConvert(123.45678));
        logger.info(new GeoStringFormatter.Builder().radix(10).scale(-3).width(10).offset(0).debugConvert(123.45678));

        logger.info(new GeoStringFormatter.Builder().radix(8).scale(0).width(10).offset(0).debugConvert(123.45678));
        logger.info(new GeoStringFormatter.Builder().radix(8).scale(1).width(10).offset(0).debugConvert(123.45678));
        logger.info(new GeoStringFormatter.Builder().radix(8).scale(2).width(10).offset(0).debugConvert(123.45678));
        logger.info(new GeoStringFormatter.Builder().radix(8).scale(3).width(10).offset(0).debugConvert(123.45678));
        logger.info(new GeoStringFormatter.Builder().radix(8).scale(4).width(10).offset(0).debugConvert(123.45678));
        logger.info(new GeoStringFormatter.Builder().radix(8).scale(5).width(10).offset(0).debugConvert(123.45678));
        logger.info(new GeoStringFormatter.Builder().radix(8).scale(6).width(10).offset(0).debugConvert(123.45678));
        logger.info(new GeoStringFormatter.Builder().radix(8).scale(-1).width(10).offset(0).debugConvert(123.45678));
        logger.info(new GeoStringFormatter.Builder().radix(8).scale(-2).width(10).offset(0).debugConvert(123.45678));
        logger.info(new GeoStringFormatter.Builder().radix(8).scale(-3).width(10).offset(0).debugConvert(123.45678));

        logger.info(new GeoStringFormatter.Builder().radix(4).scale(0).width(10).offset(0).debugConvert(123.45678));
        logger.info(new GeoStringFormatter.Builder().radix(4).scale(1).width(10).offset(0).debugConvert(123.45678));
        logger.info(new GeoStringFormatter.Builder().radix(4).scale(2).width(10).offset(0).debugConvert(123.45678));
        logger.info(new GeoStringFormatter.Builder().radix(4).scale(3).width(10).offset(0).debugConvert(123.45678));
        logger.info(new GeoStringFormatter.Builder().radix(4).scale(4).width(10).offset(0).debugConvert(123.45678));
        logger.info(new GeoStringFormatter.Builder().radix(4).scale(5).width(10).offset(0).debugConvert(123.45678));
        logger.info(new GeoStringFormatter.Builder().radix(4).scale(6).width(10).offset(0).debugConvert(123.45678));
        logger.info(new GeoStringFormatter.Builder().radix(4).scale(-1).width(10).offset(0).debugConvert(123.45678));
        logger.info(new GeoStringFormatter.Builder().radix(4).scale(-2).width(10).offset(0).debugConvert(123.45678));
        logger.info(new GeoStringFormatter.Builder().radix(4).scale(-3).width(10).offset(0).debugConvert(123.45678));
    }
    
    @Test
    public void nothing() {
        boolean results = true;
        String converted;
        converted = new GeoStringFormatter.Builder().radix(10).scale(0).width(10).offset(0).convert(123.45678);
        results &= converted.equals("00000000123");
        converted = new GeoStringFormatter.Builder().radix(10).scale(1).width(10).offset(0).convert(123.45678);
        results &= converted.equals("00000001234");
        converted = new GeoStringFormatter.Builder().radix(10).scale(2).width(10).offset(0).convert(123.45678);
        results &= converted.equals("00000012345");
        converted = new GeoStringFormatter.Builder().radix(10).scale(3).width(10).offset(0).convert(123.45678);
        results &= converted.equals("00000123456");
        converted = new GeoStringFormatter.Builder().radix(10).scale(4).width(10).offset(0).convert(123.45678);
        results &= converted.equals("00001234567");
        converted = new GeoStringFormatter.Builder().radix(10).scale(5).width(10).offset(0).convert(123.45678);
        results &= converted.equals("00012345678");
        converted = new GeoStringFormatter.Builder().radix(10).scale(6).width(10).offset(0).convert(123.45678);
        results &= converted.equals("00123456780");
       
//        assertTrue("Check that my conversion works properly", results);
        
        logger.info("Showing the effect of the scale element, effectively moves the radix point");
        logger.info(new GeoStringFormatter.Builder().radix(10).scale(0).width(10).offset(0).debugConvert(123.45678));
        logger.info(new GeoStringFormatter.Builder().radix(10).scale(1).width(10).offset(0).debugConvert(123.45678));
        logger.info(new GeoStringFormatter.Builder().radix(10).scale(2).width(10).offset(0).debugConvert(123.45678));
        logger.info(new GeoStringFormatter.Builder().radix(10).scale(3).width(10).offset(0).debugConvert(123.45678));
        logger.info(new GeoStringFormatter.Builder().radix(10).scale(4).width(10).offset(0).debugConvert(123.45678));
        logger.info(new GeoStringFormatter.Builder().radix(10).scale(5).width(10).offset(0).debugConvert(123.45678));
        logger.info(new GeoStringFormatter.Builder().radix(10).scale(6).width(10).offset(0).debugConvert(123.45678));
        logger.info(new GeoStringFormatter.Builder().radix(10).scale(-1).width(10).offset(0).debugConvert(123.45678));
        logger.info(new GeoStringFormatter.Builder().radix(10).scale(-2).width(10).offset(0).debugConvert(123.45678));
        logger.info(new GeoStringFormatter.Builder().radix(10).scale(-3).width(10).offset(0).debugConvert(123.45678));

        logger.info(new GeoStringFormatter.Builder().radix(10).scale(5).width(8).offset(0).debugConvert(123));
        logger.info(new GeoStringFormatter.Builder().radix(10).scale(5).width(8).offset(0).debugConvert(123.1));
        logger.info(new GeoStringFormatter.Builder().radix(10).scale(5).width(8).offset(0).debugConvert(123.01));
        logger.info(new GeoStringFormatter.Builder().radix(10).scale(5).width(8).offset(0).debugConvert(123.001));
        logger.info(new GeoStringFormatter.Builder().radix(10).scale(5).width(8).offset(0).debugConvert(123.0001));
        logger.info(new GeoStringFormatter.Builder().radix(10).scale(5).width(8).offset(0).debugConvert(123.00001));
        logger.info(new GeoStringFormatter.Builder().radix(10).scale(5).width(8).offset(0).debugConvert(123.000001));
        logger.info(new GeoStringFormatter.Builder().radix(10).scale(5).width(8).offset(0).debugConvert(123.000004));
        logger.info(new GeoStringFormatter.Builder().radix(10).scale(5).width(8).offset(0).debugConvert(123.0000049));
        logger.info(new GeoStringFormatter.Builder().radix(10).scale(5).width(8).offset(0).debugConvert(123.000005));
        logger.info(new GeoStringFormatter.Builder().radix(10).scale(5).width(8).offset(0).debugConvert(123.000009));

        logger.info(new GeoStringFormatter.Builder().radix(10).scale(5).width(8).offset(-100).debugConvert(123.00001));
        logger.info(new GeoStringFormatter.Builder().radix(10).scale(5).width(8).offset(-500).debugConvert(123.00001));
        logger.info(new GeoStringFormatter.Builder().radix(8).scale(5).width(8).offset(0).debugConvert(123.00001));
        //logger.info(new RadixStringFormatter.Helper().radix(4).scale(5).width(8).offset(0).debugConvert(-1));
        logger.info(new GeoStringFormatter.Builder().radix(4).scale(5).width(8).offset(0).debugConvert(63.9));
        logger.info(new GeoStringFormatter.Builder().radix(4).scale(5).width(8).offset(0).debugConvert(63.99));
        logger.info(new GeoStringFormatter.Builder().radix(4).scale(5).width(8).offset(0).debugConvert(63.9901));
        logger.info(new GeoStringFormatter.Builder().radix(4).scale(5).width(8).offset(0).debugConvert(63.999));
        logger.info(new GeoStringFormatter.Builder().radix(4).scale(5).width(8).offset(0).debugConvert(63.9999));
        logger.info(new GeoStringFormatter.Builder().radix(4).scale(5).width(8).offset(0).debugConvert(63.99999));
        logger.info(new GeoStringFormatter.Builder().radix(4).scale(5).width(8).offset(0).debugConvert(63.999999));
 //       logger.info(new RadixStringFormatter.Builder().radix(4).scale(5).width(8).offset(0).debugConvert(123.00001));
    }

    @Test
    public void convertBackTests() {
        logger.info("Showing the effect of the scale element, effectively moves the radix point");
        logger.info(new GeoStringFormatter.Builder().radix(10).scale(0).width(11).offset(0).debugGetInner("00000000123"));
        logger.info(new GeoStringFormatter.Builder().radix(10).scale(1).width(11).offset(0).debugGetInner("00000001234"));
        logger.info(new GeoStringFormatter.Builder().radix(10).scale(2).width(11).offset(0).debugGetInner("00000012345"));
        logger.info(new GeoStringFormatter.Builder().radix(10).scale(3).width(11).offset(0).debugGetInner("00000123456"));
        logger.info(new GeoStringFormatter.Builder().radix(10).scale(4).width(11).offset(0).debugGetInner("00001234567"));
        logger.info(new GeoStringFormatter.Builder().radix(10).scale(5).width(11).offset(0).debugGetInner("00012345678"));
        logger.info(new GeoStringFormatter.Builder().radix(10).scale(6).width(11).offset(0).debugGetInner("00123456780"));
        logger.info(new GeoStringFormatter.Builder().radix(10).scale(-1).width(11).offset(0).debugGetInner("00000000012"));
        logger.info(new GeoStringFormatter.Builder().radix(10).scale(-2).width(11).offset(0).debugGetInner("00000000001"));
        logger.info(new GeoStringFormatter.Builder().radix(10).scale(-3).width(11).offset(0).debugGetInner("00000000000"));

        logger.info("Showing the effect of the factor element, effectively moves the radix point");
        logger.info(new GeoStringFormatter.Builder().radix(10).scale(0).width(11).offset(0).debugGetInner("00000000123"));
        logger.info(new GeoStringFormatter.Builder().radix(10).scale(0).width(11).offset(0).debugGetInner("0000000012"));
        logger.info(new GeoStringFormatter.Builder().radix(10).scale(0).width(11).offset(0).debugGetInner("000000001"));
        logger.info(new GeoStringFormatter.Builder().radix(10).scale(0).width(11).offset(0).debugGetInner("00000000"));
        logger.info(new GeoStringFormatter.Builder().radix(10).scale(0).width(11).offset(0).debugGetInner("0000000"));

    }
    
    @Test
    public void moreTests() {
        logger.info("Looking at how the range changes for different settings");
        logger.info(new GeoStringFormatter.Builder().radix(10).scale(5).width(8).offset(0).debugGetInner(""));
        logger.info(new GeoStringFormatter.Builder().radix(10).scale(5).width(8).offset(0).debugGetOuter(""));
        logger.info(new GeoStringFormatter.Builder().radix(10).scale(5).width(8).offset(0).debugGetInner("0"));
        logger.info(new GeoStringFormatter.Builder().radix(10).scale(5).width(8).offset(0).debugGetOuter("0"));
        logger.info(new GeoStringFormatter.Builder().radix(10).scale(5).width(8).offset(0).debugGetInner("1"));
        logger.info(new GeoStringFormatter.Builder().radix(10).scale(5).width(8).offset(0).debugGetOuter("1"));
        logger.info(new GeoStringFormatter.Builder().radix(10).scale(5).width(8).offset(0).debugGetInner("9"));
        logger.info(new GeoStringFormatter.Builder().radix(10).scale(5).width(8).offset(0).debugGetOuter("9"));
        logger.info(new GeoStringFormatter.Builder().radix(10).scale(5).width(8).offset(0).debugGetInner("00"));
        logger.info(new GeoStringFormatter.Builder().radix(10).scale(5).width(8).offset(0).debugGetOuter("00"));
        logger.info(new GeoStringFormatter.Builder().radix(10).scale(5).width(8).offset(0).debugGetInner("000"));
        logger.info(new GeoStringFormatter.Builder().radix(10).scale(5).width(8).offset(0).debugGetOuter("000"));
        logger.info(new GeoStringFormatter.Builder().radix(10).scale(5).width(8).offset(0).debugGetInner("0000"));
        logger.info(new GeoStringFormatter.Builder().radix(10).scale(5).width(8).offset(0).debugGetOuter("0000"));
        logger.info(new GeoStringFormatter.Builder().radix(10).scale(5).width(8).offset(0).debugGetInner("00000"));
        logger.info(new GeoStringFormatter.Builder().radix(10).scale(5).width(8).offset(0).debugGetOuter("00000"));
    }

    @Test
    public void newTests() {
        boolean results = true;
        logger.info("Trying to debug reverse conversion");
        Builder b = new GeoStringFormatter.Builder().radix(10).scale(4).width(7).offset(-50);
        logger.info(b.debugConvert(10));
        logger.info(b.debugGetInner(""));
        logger.info(b.debugGetOuter(""));
        logger.info(b.debugGetInner("-"));
        logger.info(b.debugGetOuter("-"));
        logger.info(b.debugGetInner("0"));
        logger.info(b.debugGetOuter("0"));
        logger.info(b.debugGetInner("1"));
        logger.info(b.debugGetOuter("1"));
        logger.info(b.debugGetInner("9"));
        logger.info(b.debugGetOuter("9"));
        logger.info(b.debugGetInner("-0"));
        logger.info(b.debugGetOuter("-0"));
        logger.info(b.debugGetInner("-1"));
        logger.info(b.debugGetOuter("-1"));
        logger.info(b.debugGetInner("-9"));
        logger.info(b.debugGetOuter("-9"));
        logger.info(b.debugGetInner("00"));
        logger.info(b.debugGetOuter("00"));
        logger.info(b.debugGetInner("01"));
        logger.info(b.debugGetOuter("01"));
        logger.info(b.debugGetInner("90"));
        logger.info(b.debugGetOuter("90"));
        b = b.radix(8);
        logger.info(b.debugGetInner(""));
        logger.info(b.debugGetOuter(""));
        logger.info(b.debugGetInner("-"));
        logger.info(b.debugGetOuter("-"));
        logger.info(b.debugGetInner("0"));
        logger.info(b.debugGetOuter("0"));
        logger.info(b.debugGetInner("1"));
        logger.info(b.debugGetOuter("1"));
        b = b.radix(2);
        logger.info(b.debugGetInner(""));
        logger.info(b.debugGetOuter(""));
        logger.info(b.debugGetInner("-"));
        logger.info(b.debugGetOuter("-"));
        logger.info(b.debugGetInner("0"));
        logger.info(b.debugGetOuter("0"));
        logger.info(b.debugGetInner("1"));
        logger.info(b.debugGetOuter("1"));
        
        b = b.radix(4);
        logger.info(b.debugGetInner("1"));
        logger.info(b.debugGetInner("11"));
        logger.info(b.debugGetInner("111"));
        logger.info(b.debugGetInner("1111"));
        logger.info(b.debugGetInner("11111"));
        logger.info(b.debugGetInner("111111"));
        logger.info(b.debugGetInner("1111111"));
 
        logger.info(b.debugGetOuter("1"));
        logger.info(b.debugGetOuter("11"));
        logger.info(b.debugGetOuter("111"));
        logger.info(b.debugGetOuter("1111"));
        logger.info(b.debugGetOuter("11111"));
        logger.info(b.debugGetOuter("111111"));
        logger.info(b.debugGetOuter("1111111"));
//        logger.info(b.debugGetOuter("11111111"));
//        logger.info(b.debugGetOuter("111111111"));

        logger.info(b.debugGetInner("1"));
        logger.info(b.debugGetInner("10"));
        logger.info(b.debugGetInner("100"));
        logger.info(b.debugGetInner("1000"));
        logger.info(b.debugGetInner("10000"));
        logger.info(b.debugGetInner("100000"));
        logger.info(b.debugGetInner("1000000"));
 
        logger.info(b.debugGetOuter("1"));
        logger.info(b.debugGetOuter("10"));
        logger.info(b.debugGetOuter("100"));
        logger.info(b.debugGetOuter("1000"));
        logger.info(b.debugGetOuter("10000"));
        logger.info(b.debugGetOuter("100000"));
        logger.info(b.debugGetOuter("1000000"));


    }

    @Test
    public void printouts() {
        boolean results = true;
        for (int i=2;i<=36;i++) {
            System.out.printf("%2da) %12s %12s %12s%n",i,
                    new GeoStringFormatter.Builder().radix(i).scale(0).width(10).offset(0).convert(123.45678),
                    new GeoStringFormatter.Builder().radix(i).scale(1).width(10).offset(0).convert(123.45678),
                    new GeoStringFormatter.Builder().radix(i).scale(2).width(10).offset(0).convert(123.45678),
                    new GeoStringFormatter.Builder().radix(i).scale(3).width(10).offset(0).convert(123.45678));
        }
        assertTrue("Check that my conversion works properly", results);
    }

    @Test
    public void printouts2() {
        boolean results = true;
        for (int i=2;i<=36;i++) {
            System.out.printf("%2db) %12s %11s %10s%n",
                    i,
                    new GeoStringFormatter.Builder().radix(i).scale(3).width(10).offset(0).convert(123.45678),
                    new GeoStringFormatter.Builder().radix(i).scale(3).width(10).offset(0).convert(123.45678),
                    new GeoStringFormatter.Builder().radix(i).scale(3).width(10).offset(0).convert(123.45678),
                    new GeoStringFormatter.Builder().radix(i).scale(3).width(10).offset(0).convert(123.45678));
        }
        assertTrue("Check that my conversion works properly", results);
    }

}
