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
        GeoStringFormatter f = GeoStringFormatter.buildRegularDecimalFormatter(8,5);

        System.out.println(f.getInner(""));
        System.out.println(f.getInner("0"));
        System.out.println(f.getInner("-"));
        System.out.println(f.getInner("9"));
        System.out.println(f.getInner("00"));
        System.out.println(f.getInner("-0"));
        System.out.println(f.getInner("000"));
        System.out.println(f.getInner("01"));
        System.out.println(f.getInner("-1"));
        System.out.println(f.getInner("001"));
        System.out.println(f.getInner("-01"));
    }        
        
    @Test
    public void outerTests() {
        System.out.println("Here are some outer tests formatted for Decimal(8,5)");
        GeoStringFormatter f = GeoStringFormatter.buildRegularDecimalFormatter(8,5);
        
        System.out.println(f.getOuter(""));
        System.out.println(f.getOuter("0"));
        System.out.println(f.getOuter("-"));
        System.out.println(f.getOuter("9"));
        System.out.println(f.getOuter("00"));
        System.out.println(f.getOuter("-0"));
        System.out.println(f.getOuter("000"));
        System.out.println(f.getOuter("01"));
        System.out.println(f.getOuter("-1"));
        System.out.println(f.getOuter("001"));
        System.out.println(f.getOuter("-01"));
        

    }        
    
    
    @Test
    public void decimalTests() {
        System.out.println("Here are some decimal tests");
        GeoStringFormatter f = GeoStringFormatter.buildRegularDecimalFormatter(8,5);
        
        System.out.println(f.convertDecimalString("01234"));
        System.out.println(f.convertDecimalString("-12345"));
        System.out.println(f.convertDecimalString("0012345"));
        System.out.println(f.convertDecimalString("-01324"));
        System.out.println(f.convertDecimalString("0006142"));
        System.out.println(f.convertDecimalString("011235"));
        System.out.println(f.convertDecimalString("-1762"));
        System.out.println(f.convertDecimalString("0012222"));
        System.out.println(f.convertDecimalString("-0155555"));
        
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
