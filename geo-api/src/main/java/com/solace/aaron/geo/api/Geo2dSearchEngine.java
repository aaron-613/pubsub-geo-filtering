package com.solace.aaron.geo.api;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.locationtech.jts.geom.Geometry;

/**
 * This simple class defines the parameters used by the geospactial search. It is a generisized version
 * of the standard decimal placement.
 * 
 */
public class Geo2dSearchEngine {

    
    /** Tested with 2, 4, 8, 10, and 16 **/
    private final int radix;
    private final int xWidth;
    private final int scale;
    private final int yWidth;
    
    private final GeoStringFormatter xStringFormatter;
    private final GeoStringFormatter yStringFormatter;
    
    private static final Logger logger = LogManager.getLogger(Geo2dSearchEngine.class);

    
    /**
     * 
     * @param radix the radix, obviously!  2=binary, 10=decimal, etc.
     * @param xWidth the total size of the whole string?  in the radix.  So, binary radix=2, xWidth=10 == 0b10 1010 10110 == 0..1023 decimal 
     * @param yWidth
     * @param xScale how many "radix points" the value is shifted over.  E.g. for decimal radix=10, scale 2 ==> *100, for binary scale=3 --> *8
     * @param yScale
     * @param xOffset this is how much to shift the numbers "to the left" or negative... i want to change this
     * @param yOffset
     */
    public Geo2dSearchEngine(int radix, int scale, int xWidth, int xOffset, int yWidth, int yOffset) {
        logger.info("### Starting create Geo2dSearchEngine");
        this.radix = radix;
        this.scale = scale;  // what is the offset, for the various later calculations?
        this.xWidth = xWidth;
        this.yWidth = yWidth;
        this.xStringFormatter = new RadixStringFormatter(radix, xWidth, scale, xOffset);
        this.yStringFormatter = new RadixStringFormatter(radix, yWidth, scale, yOffset);
    }
    
    public int getRadix() {
        return radix;
    }

    public int getXMaxWidth() {
        return xWidth;
    }

    public int getYMaxWidth() {
        return yWidth;
    }

    public int getScale() {
        return scale;
    }
    
    public int getXShift() {
        return xWidth-scale;
    }
    
    public int getYShift() {
        return yWidth-scale;
    }

    public GeoStringFormatter getXStringFormatter() {
        return xStringFormatter;
    }
    
    public GeoStringFormatter getYStringFormatter() {
        return yStringFormatter;
    }
    

    public Geo2dSearchResult splitToRatio(final List<Geometry> targets, final double completionRatio, final int maxSubs) {
        Geometry[] targetCopy = new Geometry[targets.size()];
        for (int i=0;i<targets.size();i++) {
            targetCopy[i] = targets.get(i).copy();  // make a deep copy of the geometries so they can't be modified afterwards
        }
        Geo2dSearch search = new Geo2dSearch(this,targetCopy);
        return search.splitToRatio(completionRatio,maxSubs);
    }
    
    
}
