package com.solace.aaron.geo.api;

import org.junit.Test;

import com.solace.aaron.geo.api.RadixStringFormatter;
import com.solace.aaron.geo.api.RadixStringFormatter.Helper;

import static org.junit.Assert.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RadixStringFormatterTest {
    
    Logger logger = LogManager.getLogger(RadixStringFormatterTest.class);
    
    public void factorTests() {
        boolean results = true;
        String converted;
        converted = new RadixStringFormatter.Helper().radix(10).scale(0).padding(10).offset(0).convert(123.45678);
        results &= converted.equals("00000000123");

        assertTrue("Check that my conversion works properly", results);
    }
    
    public void scaleTests() {
        boolean results = true;
        String converted;
        converted = new RadixStringFormatter.Helper().radix(10).scale(0).padding(10).offset(0).convert(123.45678);
        results &= converted.equals("00000000123");
        converted = new RadixStringFormatter.Helper().radix(10).scale(1).padding(10).offset(0).convert(123.45678);
        results &= converted.equals("00000001234");
        converted = new RadixStringFormatter.Helper().radix(10).scale(2).padding(10).offset(0).convert(123.45678);
        results &= converted.equals("00000012345");
        converted = new RadixStringFormatter.Helper().radix(10).scale(3).padding(10).offset(0).convert(123.45678);
        results &= converted.equals("00000123456");
        converted = new RadixStringFormatter.Helper().radix(10).scale(4).padding(10).offset(0).convert(123.45678);
        results &= converted.equals("00001234567");
        converted = new RadixStringFormatter.Helper().radix(10).scale(5).padding(10).offset(0).convert(123.45678);
        results &= converted.equals("00012345678");
        converted = new RadixStringFormatter.Helper().radix(10).scale(6).padding(10).offset(0).convert(123.45678);
        results &= converted.equals("00123456780");
       
        assertTrue("Check that my conversion works properly", results);
        
        logger.info("Showing the effect of the scale element, effectively moves the radix point");
        logger.info(new RadixStringFormatter.Helper().radix(10).scale(0).padding(10).offset(0).debugConvert(123.45678));
        logger.info(new RadixStringFormatter.Helper().radix(10).scale(1).padding(10).offset(0).debugConvert(123.45678));
        logger.info(new RadixStringFormatter.Helper().radix(10).scale(2).padding(10).offset(0).debugConvert(123.45678));
        logger.info(new RadixStringFormatter.Helper().radix(10).scale(3).padding(10).offset(0).debugConvert(123.45678));
        logger.info(new RadixStringFormatter.Helper().radix(10).scale(4).padding(10).offset(0).debugConvert(123.45678));
        logger.info(new RadixStringFormatter.Helper().radix(10).scale(5).padding(10).offset(0).debugConvert(123.45678));
        logger.info(new RadixStringFormatter.Helper().radix(10).scale(6).padding(10).offset(0).debugConvert(123.45678));
        logger.info(new RadixStringFormatter.Helper().radix(10).scale(-1).padding(10).offset(0).debugConvert(123.45678));
        logger.info(new RadixStringFormatter.Helper().radix(10).scale(-2).padding(10).offset(0).debugConvert(123.45678));
        logger.info(new RadixStringFormatter.Helper().radix(10).scale(-3).padding(10).offset(0).debugConvert(123.45678));

    }

    @Test
    public void convertBackTests() {
        logger.info("Showing the effect of the scale element, effectively moves the radix point");
        logger.info(new RadixStringFormatter.Helper().radix(10).scale(0).padding(10).offset(0).debugConvertBack("00000000123"));
        logger.info(new RadixStringFormatter.Helper().radix(10).scale(1).padding(10).offset(0).debugConvertBack("00000001234"));
        logger.info(new RadixStringFormatter.Helper().radix(10).scale(2).padding(10).offset(0).debugConvertBack("00000012345"));
        logger.info(new RadixStringFormatter.Helper().radix(10).scale(3).padding(10).offset(0).debugConvertBack("00000123456"));
        logger.info(new RadixStringFormatter.Helper().radix(10).scale(4).padding(10).offset(0).debugConvertBack("00001234567"));
        logger.info(new RadixStringFormatter.Helper().radix(10).scale(5).padding(10).offset(0).debugConvertBack("00012345678"));
        logger.info(new RadixStringFormatter.Helper().radix(10).scale(6).padding(10).offset(0).debugConvertBack("00123456780"));
        logger.info(new RadixStringFormatter.Helper().radix(10).scale(-1).padding(10).offset(0).debugConvertBack("00000000012"));
        logger.info(new RadixStringFormatter.Helper().radix(10).scale(-2).padding(10).offset(0).debugConvertBack("00000000001"));
        logger.info(new RadixStringFormatter.Helper().radix(10).scale(-3).padding(10).offset(0).debugConvertBack("00000000000"));

        logger.info("Showing the effect of the factor element, effectively moves the radix point");
        logger.info(new RadixStringFormatter.Helper().radix(10).scale(0).padding(10).offset(0).debugConvertBack("00000000123"));
        logger.info(new RadixStringFormatter.Helper().radix(10).scale(0).padding(10).offset(0).debugConvertBack("0000000012"));
        logger.info(new RadixStringFormatter.Helper().radix(10).scale(0).padding(10).offset(0).debugConvertBack("000000001"));
        logger.info(new RadixStringFormatter.Helper().radix(10).scale(0).padding(10).offset(0).debugConvertBack("00000000"));
        logger.info(new RadixStringFormatter.Helper().radix(10).scale(0).padding(10).offset(0).debugConvertBack("0000000"));

    }

    @Test
    public void newTests() {
        boolean results = true;
        logger.info("Trying to debug reverse conversion");
        Helper h = new RadixStringFormatter.Helper().radix(10).scale(4).padding(6).offset(0);
        logger.info(h.debugConvert(10));
        logger.info(h.debugConvert(10));
        logger.info(h.debugConvert(10));
        

    }

    public void printouts() {
        boolean results = true;
        for (int i=2;i<=36;i++) {
            System.out.printf("%2da) %12s %12s %12s%n",i,
                    new RadixStringFormatter.Helper().radix(i).scale(0).padding(10).offset(0).convert(123.45678),
                    new RadixStringFormatter.Helper().radix(i).scale(1).padding(10).offset(0).convert(123.45678),
                    new RadixStringFormatter.Helper().radix(i).scale(2).padding(10).offset(0).convert(123.45678),
                    new RadixStringFormatter.Helper().radix(i).scale(3).padding(10).offset(0).convert(123.45678));
        }
        assertTrue("Check that my conversion works properly", results);
    }

    public void printouts2() {
        boolean results = true;
        for (int i=2;i<=36;i++) {
            System.out.printf("%2db) %12s %11s %10s%n",
                    i,
                    new RadixStringFormatter.Helper().radix(i).scale(3).padding(10).offset(0).convert(123.45678),
                    new RadixStringFormatter.Helper().radix(i).scale(3).padding(10).offset(0).convert(123.45678),
                    new RadixStringFormatter.Helper().radix(i).scale(3).padding(10).offset(0).convert(123.45678),
                    new RadixStringFormatter.Helper().radix(i).scale(3).padding(10).offset(0).convert(123.45678));
        }
        assertTrue("Check that my conversion works properly", results);
    }

}
