package com.solace.aaron.rnrf;

import static org.junit.Assert.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.io.WKTWriter;

public class RadixRangeSerachTests {

	

    

    @Test
    public void testSearchWorks() {

    	

    	WKTWriter wktWriter = new WKTWriter();
    	GeometryFactory factory = new GeometryFactory();
        
        Coordinate[] coords1 = {
                new Coordinate(-20000,0),
                new Coordinate(0,-20000),
                new Coordinate(-20000,-20000),
                new Coordinate(-20000,0),
        };
        Coordinate[] coords2 = {
                new Coordinate(0.89,0),
                new Coordinate(0,0.179),
                new Coordinate(0.89,0.179),
                new Coordinate(0.89,0),
        };
        
/*        Coordinate[] coords2 = {
                new Coordinate(89,0),
                new Coordinate(0,179),
                new Coordinate(89,179),
                new Coordinate(89,0),
        };
*/
        Geometry target = factory.createPolygon(coords2);

        RadixRangeSearch2d rrs = new RadixRangeSearch2d(10,6,7,4,4,true,true,target);
//        com.solace.aaron.rnrf2.RadixRangeSearch2d2 rrs = new RadixRangeSearch2d(2,19,20,12,12,true,true,target);
//        com.solace.aaron.rnrf2.RadixRangeSearch2d2 rrs = new RadixRangeSearch2d(16,5,5,3,3,true,true,target);
        rrs.splitToRatio(0.95,1500);
    	System.out.println(rrs.getSubs().toString());
    	System.out.println(rrs.getSubs().size());
    	System.out.printf("%.4f%%%n",rrs.getCurrentCoverageRatio()*100);
    	System.out.println(rrs.getUnion().intersects(target));
    	System.out.println(rrs.getUnion().intersection(target).getArea() / rrs.getUnion().getArea());
        
        assertTrue("Check that my conversion works properly", rrs.getSubs().size() > 1);
        assertEquals("Within range tolerances", rrs.getCurrentCoverageRatio(), rrs.getUnion().intersection(target).getArea() / rrs.getUnion().getArea(), 0.001);
        
        System.out.println();
        LineString lineString = factory.createLineString(new Coordinate[] {new Coordinate(45,0),new Coordinate(135,0)});
        //LineString ls2 = factory.createLineString(new Coordinate[] {new Coordinate(3,0),new Coordinate(4,0)});
        //Geometry asdf = lineString.union(ls2);

        //System.out.println(asdf.getLength());
        
        RadixRangeSearch1d rs = new RadixRangeSearch1d(2, 5, -4, false, lineString);
        rs.splitToRatio(0.97, 30);
    	System.out.println(rs.getSubs().toString());
    	System.out.println(rs.getSubs().size());
    	System.out.printf("%.4f%%%n",rs.getCurrentCoverageRatio()*100);
        rs = new RadixRangeSearch1d(2, 6, -3, false, lineString);
        rs.splitToRatio(0.97, 30);
    	System.out.println(rs.getSubs().toString());
    	System.out.println(rs.getSubs().size());
    	System.out.printf("%.4f%%%n",rs.getCurrentCoverageRatio()*100);
        rs = new RadixRangeSearch1d(19, 1, -1, false, lineString);
        rs.splitToRatio(0.97, 30);
    	System.out.println(rs.getSubs().toString());
    	System.out.println(rs.getSubs().size());
    	System.out.printf("%.4f%%%n",rs.getCurrentCoverageRatio()*100);
        rs = new RadixRangeSearch1d(19, 2, 0, false, lineString);
        rs.splitToRatio(0.97, 30);
    	System.out.println(rs.getSubs().toString());
    	System.out.println(rs.getSubs().size());
    	System.out.printf("%.4f%%%n",rs.getCurrentCoverageRatio()*100);
        rs = new RadixRangeSearch1d(4, 4, -1, false, lineString);
        rs.splitToRatio(0.97, 30);
    	System.out.println(rs.getSubs().toString());
    	System.out.println(rs.getSubs().size());
    	System.out.printf("%.4f%%%n",rs.getCurrentCoverageRatio()*100);
        rs = new RadixRangeSearch1d(4, 5, 0, false, lineString);
        rs.splitToRatio(0.97, 30);
    	System.out.println(rs.getSubs().toString());
    	System.out.println(rs.getSubs().size());
    	System.out.printf("%.4f%%%n",rs.getCurrentCoverageRatio()*100);
        rs = new RadixRangeSearch1d(3, 4, -2, false, lineString);
        rs.splitToRatio(0.97, 30);
    	System.out.println(rs.getSubs().toString());
    	System.out.println(rs.getSubs().size());
    	System.out.printf("%.4f%%%n",rs.getCurrentCoverageRatio()*100);
    	
    	Logger logger = LogManager.getLogger(RadixRangeSerachTests.class);
    	
        logger.debug("This is a debug message");
        logger.info("This is an info message");
        logger.warn("This is a warn message");
        logger.error("This is an error message");
        logger.fatal("This is a fatal message");
    }
    
    
    
    
    
}
