package com.solace.aaron.geo.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

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

    	RadixStringFormatter rsf = new RadixStringFormatter(10, 4, 7, 90);
//        System.out.println("0.3");
//    	System.out.println(rsf.convert(0.3999999));
//    	System.out.println("0003");
//    	System.out.println(rsf.getInner("0003"));
//        System.out.println(rsf.getOuter2("0003"));
//        System.out.println("00030");
//        System.out.println(rsf.getInner("00030"));
//        System.out.println(rsf.getOuter2("00030"));
//        System.out.println("000300");
//        System.out.println(rsf.getInner("000300"));
//        System.out.println(rsf.getOuter2("000300"));
        System.out.println("0903");
        System.out.println(rsf.convert(0.3));
        System.out.println(rsf.getInner("0903"));
        System.out.println(rsf.getOuter("0903"));
        System.out.println("1801");
        System.out.println(rsf.getInner("1801"));
        System.out.println(rsf.getOuter("1801"));
        
        double d3 = 90.3;
        System.out.println(d3);
        System.out.println(d3 - 90);
        
//        System.exit(0);
    	for (int i=0;i<10;i++) {
    	    double d = Math.random()*90;
    	    double dd = rsf.getInner(rsf.convert(d));
    	    System.out.printf("%15.6f --> %s --> %9f  %f%n",d,rsf.convert(d),dd,d-dd);
    	}
    	rsf.convert(7.955848);
    	
//        System.exit(0);

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
                new Coordinate(-89,0),
                new Coordinate(0,-179),
                new Coordinate(-89,-179),
                new Coordinate(-89,0),
        };
*/
        Geometry target = factory.createPolygon(coords2);

        RadixRangeSearch2d rrs = new RadixRangeSearch2d(10,7,7,4,4,90,180,target);
//        com.solace.aaron.rnrf2.RadixRangeSearch2d2 rrs = new RadixRangeSearch2d(2,19,20,12,12,true,true,target);
//        com.solace.aaron.rnrf2.RadixRangeSearch2d2 rrs = new RadixRangeSearch2d(16,5,5,3,3,true,true,target);
        for (int i=0;i<20;i++) {
            System.out.printf("%d) #subs %d, #squares %d, cov%%=%f, |union|=%f%n",
                    i,rrs.getSubs().size(),rrs.getSquares().size(),rrs.getCurrentCoverageRatio(),rrs.getUnion().getArea());
            rrs.splitOne();
        }
        
        
        rrs.splitToRatio(0.95,1500);
        System.out.println(rrs.getSubs().toString());
        System.out.println(rrs.getSubs().size());
        System.out.printf("%.4f%%%n",rrs.getCurrentCoverageRatio()*100);
        List<String[]> squares = rrs.getSquares();
        for (String[] ar : squares) {
            System.out.println(Arrays.toString(ar));
        }
        System.out.println(rrs.getUnion().intersects(target));
        System.out.println(rrs.getUnion().intersection(target).getArea() / rrs.getUnion().getArea());
        System.out.println(rrs.getUnion());
        
        assertTrue("Check that my conversion works properly", rrs.getSubs().size() > 1);
        assertEquals("Within range tolerances", rrs.getCurrentCoverageRatio(), rrs.getUnion().intersection(target).getArea() / rrs.getUnion().getArea(), 0.001);
        
        System.out.println();
        LineString lineString = factory.createLineString(new Coordinate[] {new Coordinate(45,0),new Coordinate(135,0)});
        //LineString ls2 = factory.createLineString(new Coordinate[] {new Coordinate(3,0),new Coordinate(4,0)});
        //Geometry asdf = lineString.union(ls2);

        //System.out.println(asdf.getLength());
        // circle 360 degree tests
        RadixRangeSearch1d rs = new RadixRangeSearch1d(2, 5, -4, 0, lineString);
        rs.splitToRatio(0.97, 30);
        System.out.println(rs.getSubs().toString());
        System.out.println(rs.getSubs().size());
        System.out.printf("%.4f%%%n",rs.getCurrentCoverageRatio()*100);
        rs = new RadixRangeSearch1d(2, 6, -3, 0, lineString);
        rs.splitToRatio(0.97, 30);
        System.out.println(rs.getSubs().toString());
        System.out.println(rs.getSubs().size());
        System.out.printf("%.4f%%%n",rs.getCurrentCoverageRatio()*100);
        rs = new RadixRangeSearch1d(19, 1, -1, 0, lineString);
        rs.splitToRatio(0.97, 30);
        System.out.println(rs.getSubs().toString());
        System.out.println(rs.getSubs().size());
        System.out.printf("%.4f%%%n",rs.getCurrentCoverageRatio()*100);
        rs = new RadixRangeSearch1d(19, 2, 0, 0, lineString);
        rs.splitToRatio(0.97, 30);
        System.out.println(rs.getSubs().toString());
        System.out.println(rs.getSubs().size());
        System.out.printf("%.4f%%%n",rs.getCurrentCoverageRatio()*100);
        rs = new RadixRangeSearch1d(4, 4, -1, 0, lineString);
        rs.splitToRatio(0.97, 30);
        System.out.println(rs.getSubs().toString());
        System.out.println(rs.getSubs().size());
        System.out.printf("%.4f%%%n",rs.getCurrentCoverageRatio()*100);
        rs = new RadixRangeSearch1d(4, 5, 0, 0, lineString);
        rs.splitToRatio(0.97, 30);
        System.out.println(rs.getSubs().toString());
        System.out.println(rs.getSubs().size());
        System.out.printf("%.4f%%%n",rs.getCurrentCoverageRatio()*100);
        rs = new RadixRangeSearch1d(3, 4, -2, 0, lineString);
        rs.splitToRatio(0.97, 30);
        System.out.println(rs.getSubs().toString());
        System.out.println(rs.getSubs().size());
        System.out.printf("%.4f%%%n",rs.getCurrentCoverageRatio()*100);
        
        Logger logger = LogManager.getLogger(RadixRangeSerachTests.class);
        
//        logger.debug("This is a debug message");
//        logger.info("This is an info message");
//        logger.warn("This is a warn message");
//        logger.error("This is an error message");
//        logger.fatal("This is a fatal message");
        
        // altitiude, between -1000 and +100000
        lineString = factory.createLineString(new Coordinate[] {new Coordinate(11500,0),new Coordinate(32000,0)});
        rs = new RadixRangeSearch1d(10, 5, -2, 1000, lineString);
        rs.splitToRatio(0.97, 30);
        System.out.println(rs.getSubs().toString());
        System.out.println(rs.getSubs().size());
        System.out.printf("%.4f%%%n",rs.getCurrentCoverageRatio()*100);
        
        
        Coordinate[] coords3 = {
                new Coordinate(18293,11973),
                new Coordinate(35627,12098),
                new Coordinate(63412,25891),
                new Coordinate(6812,39360),
                new Coordinate(18293,11973),
        };
        
        target = factory.createPolygon(coords3);

//        rrs = new RadixRangeSearch2d(2,11,11,-6,-6,1024,1024,target);
//        rrs = new RadixRangeSearch2d(4,6,6,-3,-3,1024,1024,target);
        rrs = new RadixRangeSearch2d(6,4,4,-3,-3,1188,1188,target);
//        rrs = new RadixRangeSearch2d(8,4,4,-2,-2,4096,4096,target);
//        rrs = new RadixRangeSearch2d(10,4,4,-2,-2,1000,1000,target);
//        com.solace.aaron.rnrf2.RadixRangeSearch2d2 rrs = new RadixRangeSearch2d(2,19,20,12,12,true,true,target);
//        com.solace.aaron.rnrf2.RadixRangeSearch2d2 rrs = new RadixRangeSearch2d(16,5,5,3,3,true,true,target);
        for (int i=0;i<20;i++) {
            System.out.printf("%d) #subs %d, #squares %d, cov%%=%f, |union|=%f%n",
                    i,rrs.getSubs().size(),rrs.getSquares().size(),rrs.getCurrentCoverageRatio(),rrs.getUnion().getArea());
            rrs.splitOne();
        }
        
        
        rrs.splitToRatio(0.90,1500);
        System.out.println(rrs.getSubs().toString());
        System.out.println(rrs.getSubs().size());
        System.out.printf("%.4f%%%n",rrs.getCurrentCoverageRatio()*100);
        squares = rrs.getSquares();
        for (String[] ar : squares) {
            System.out.println(Arrays.toString(ar));
        }
        System.out.println(rrs.getUnion().intersects(target));
        System.out.println(rrs.getUnion().intersection(target).getArea() / rrs.getUnion().getArea());
        System.out.println(rrs.getUnion());

    }
    
    
    
    
    
}
