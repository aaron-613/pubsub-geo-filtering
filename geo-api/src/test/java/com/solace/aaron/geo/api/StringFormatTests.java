package com.solace.aaron.geo.api;

import static org.junit.Assert.assertEquals;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

public class StringFormatTests {
    
    Logger logger = LogManager.getLogger(StringFormatTests.class);
    

    @Test
    public void useJavaStringFormat() {
        logger.info("This is only useful if using a decimal GeoStringFormatter.");
        logger.info("Also, should really have the 0 padding, can't use spaces.");
        double d = 12.3456789;
        System.out.printf("%09.4f%n",d);
        assertEquals("0012.3457",String.format("%09.4f",d));
        System.out.printf("%9.4f%n",d);
        assertEquals("  12.3457",String.format("%9.4f",d));

        d = -12.3456789;
        System.out.printf("%09.4f%n",d);
        assertEquals("-012.3457",String.format("%09.4f",d));
        System.out.printf("%9.4f%n",d);
        assertEquals(" -12.3457",String.format("%9.4f",d));
        System.out.println();
        
        d = 123.456789;
        System.out.printf("%09.4f%n",d);
        assertEquals("0123.4568",String.format("%09.4f",d));
        System.out.printf("%9.4f%n",d);
        assertEquals(" 123.4568",String.format("%9.4f",d));
    
        d = -123.456789;
        System.out.printf("%09.4f%n",d);
        assertEquals("-123.4568",String.format("%09.4f",d));
        System.out.printf("%9.4f%n",d);
        assertEquals("-123.4568",String.format("%9.4f",d));
        System.out.println();
        
        d = 1234.56789;
        System.out.printf("%09.4f%n",d);
        assertEquals("1234.5679",String.format("%09.4f",d));
        System.out.printf("%9.4f%n",d);
        assertEquals("1234.5679",String.format("%9.4f",d));
    
        d = -1234.56789;
        System.out.printf("%09.4f%n",d);
        assertEquals("-1234.5679",String.format("%09.4f",d));
        System.out.printf("%9.4f%n",d);
        assertEquals("-1234.5679",String.format("%9.4f",d));
        System.out.println();
    
    }


    @Test
    public void latLonTest() {
        double lat = -12.345678;  // range -90..90
        double lon = 123.456789;  // range -180..180  or  0..360
        assertEquals("-12.34568",String.format("%09.5f",lat));
        assertEquals("0123.45679",String.format("%010.5f",lon));
    }
}
