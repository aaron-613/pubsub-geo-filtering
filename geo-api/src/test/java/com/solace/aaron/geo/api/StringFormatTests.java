package com.solace.aaron.geo.api;

import static org.junit.Assert.assertEquals;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

public class StringFormatTests {
    
    Logger logger = LogManager.getLogger(StringFormatTests.class);
    

    @Test
    public void useJavaStringFormat() {
        logger.info("This is only useful if using a decimal GeoStringFormatter");
        double d = 12.3456789;
        System.out.printf("%09.4f%n",d);
        assertEquals(String.format("%09.4f",d),"0012.3457");
        System.out.printf("%9.4f%n",d);
        assertEquals(String.format("%9.4f",d),"  12.3457");

        d = -12.3456789;
        System.out.printf("%09.4f%n",d);
        assertEquals(String.format("%09.4f",d),"-012.3457");
        System.out.printf("%9.4f%n",d);
        assertEquals(String.format("%9.4f",d)," -12.3457");
        System.out.println();
        
        d = 123.456789;
        System.out.printf("%09.4f%n",d);
        assertEquals(String.format("%09.4f",d),"0123.4568");
        System.out.printf("%9.4f%n",d);
        assertEquals(String.format("%9.4f",d)," 123.4568");
    
        d = -123.456789;
        System.out.printf("%09.4f%n",d);
        assertEquals(String.format("%09.4f",d),"-123.4568");
        System.out.printf("%9.4f%n",d);
        assertEquals(String.format("%9.4f",d),"-123.4568");
        System.out.println();
        
        d = 1234.56789;
        System.out.printf("%09.4f%n",d);
        assertEquals(String.format("%09.4f",d),"1234.5679");
        System.out.printf("%9.4f%n",d);
        assertEquals(String.format("%9.4f",d),"1234.5679");
    
        d = -1234.56789;
        System.out.printf("%09.4f%n",d);
        assertEquals(String.format("%09.4f",d),"-1234.5679");
        System.out.printf("%9.4f%n",d);
        assertEquals(String.format("%9.4f",d),"-1234.5679");
        System.out.println();
    
    }


    @Test
    public void latLonTest() {
        double lat = -12.345678;  // range -90..90
        double lon = 123.456789;  // range -180..180  or  0..360
        assertEquals(String.format("%09.5f",lat),"-12.34568");
        assertEquals(String.format("%010.5f",lon),"0123.45679");
    }
}
