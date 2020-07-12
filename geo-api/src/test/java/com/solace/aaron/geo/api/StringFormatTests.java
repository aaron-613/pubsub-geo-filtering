package com.solace.aaron.geo.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

public class StringFormatTests {
    
    Logger logger = LogManager.getLogger(StringFormatTests.class);
    

    @Test
    public void someTests() {
        logger.info("Some Tests");
        double d = 12.3456789;
        System.out.printf("%09.4f%n",d);
        System.out.printf("%9.4f%n",d);

        d = -12.3456789;
        System.out.printf("%09.4f%n",d);
        System.out.printf("%9.4f%n",d);
        System.out.println();
        
        d = 123.456789;
        System.out.printf("%09.4f%n",d);
        System.out.printf("%9.4f%n",d);
    
        d = -123.456789;
        System.out.printf("%09.4f%n",d);
        System.out.printf("%9.4f%n",d);
        System.out.println();
        
        d = 1234.56789;
        System.out.printf("%09.4f%n",d);
        System.out.printf("%9.4f%n",d);
    
        d = -1234.56789;
        System.out.printf("%09.4f%n",d);
        System.out.printf("%9.4f%n",d);
        System.out.println();
    
    }   
}
