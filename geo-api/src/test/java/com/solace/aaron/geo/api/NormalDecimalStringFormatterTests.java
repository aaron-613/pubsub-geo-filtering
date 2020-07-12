package com.solace.aaron.geo.api;

import static org.junit.Assert.assertEquals;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

public class NormalDecimalStringFormatterTests {
    
    Logger logger = LogManager.getLogger(NormalDecimalStringFormatterTests.class);
    

    @Test
    public void innerTests() {
        System.out.println("Here are some inner tests");
        NormalDecimalStringFormatter f = new NormalDecimalStringFormatter(3,5);
        
        System.out.println(f.getInner("0"));
        System.out.println(f.getInner("-"));
        System.out.println(f.getInner("00"));
        System.out.println(f.getInner("-0"));
        System.out.println(f.getInner("000"));
        System.out.println(f.getInner("01"));
        System.out.println(f.getInner("-1"));
        System.out.println(f.getInner("001"));
        System.out.println(f.getInner("-01"));
        
        assertEquals(f.getInner("0"),0,0);
        assertEquals(f.getInner("-"),0,0);
        assertEquals(f.getInner("00"),0,0);
        assertEquals(f.getInner("-0"),0,0);
        assertEquals(f.getInner("000"),0,0);
        assertEquals(f.getInner("01"),100,0);
        assertEquals(f.getInner("-1"),-100,0);
        assertEquals(f.getInner("001"),10,0);
        assertEquals(f.getInner("-01"),-10,0);
        assertEquals(f.getInner("0010"),10,0);
        assertEquals(f.getInner("-010"),-10,0);
        assertEquals(f.getInner("0010.0"),10,0);
        assertEquals(f.getInner("-010.0"),-10,0);
        //f.getInner("00111");  // throws
        assertEquals(f.getInner("0123.0"),123,0);
        assertEquals(f.getInner("-123.0"),-123,0);
        
        
    }

    
    @Test
    public void outerTests() {
        System.out.println("Here are some outer tests");
        NormalDecimalStringFormatter f = new NormalDecimalStringFormatter(3,5);
        
        System.out.println(f.getOuter("0"));
        System.out.println(f.getOuter("-"));
        System.out.println(f.getOuter("00"));
        System.out.println(f.getOuter("-0"));
        System.out.println(f.getOuter("000"));
        System.out.println(f.getOuter("01"));
        System.out.println(f.getOuter("-1"));
        System.out.println(f.getOuter("001"));
        System.out.println(f.getOuter("-01"));
        System.out.println(f.getOuter("0001"));  // 2
        System.out.println(f.getOuter("-001"));  // -2
        System.out.println(f.getOuter("0001."));  // 2
        System.out.println(f.getOuter("-001."));  // -2
        System.out.println(f.getOuter("0001.0"));  // 1.1 
        System.out.println(f.getOuter("-001.0"));  // -1.1
        System.out.println(f.getOuter("0001.9"));  // 1.1 
        System.out.println(f.getOuter("-001.9"));  // -1.1
        
        assertEquals(1000,f.getOuter("0"),0);
        assertEquals(-1000,f.getOuter("-"),0);
        assertEquals(100,f.getOuter("00"),0);
        assertEquals(-100,f.getOuter("-0"),0);
        assertEquals(10,f.getOuter("000"),0);
        assertEquals(200,f.getOuter("01"),0);
        assertEquals(-200,f.getOuter("-1"),0);
        assertEquals(20,f.getOuter("001"),0);
        assertEquals(-20,f.getOuter("-01"),0);
        assertEquals(11,f.getOuter("0010"),0);
        assertEquals(-11,f.getOuter("-010"),0);
        assertEquals(10.1,f.getOuter("0010.0"),0);
        assertEquals(-10.1,f.getOuter("-010.0"),0);
        //f.getOuter("00111");  // throws
        assertEquals(123.1,f.getOuter("0123.0"),0);
        assertEquals(-123.1,f.getOuter("-123.0"),0);
        
        
    }

    
    
    @Test
    public void shiftingTest() {
        System.out.println("Looking to see how inverse factors work");
        System.out.println(RadixUtils.lookupInverseFactors(10,3));
        System.out.println(RadixUtils.lookupInverseFactors(10,2));
        System.out.println(RadixUtils.lookupInverseFactors(10,1));
        System.out.println(RadixUtils.lookupInverseFactors(10,0));
        System.out.println(RadixUtils.lookupInverseFactors(10,-1));
        System.out.println(RadixUtils.lookupInverseFactors(10,-2));
        System.out.println(RadixUtils.lookupInverseFactors(10,-3));

        
        
        
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
