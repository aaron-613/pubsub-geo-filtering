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
public class Geo2dSearch {

    
    /** Tested with 2, 4, 8, 10, and 16 **/
    private final int radix;
    private final int xWidth;
    private final int scale;
    private final int yWidth;
    
    private final GeoStringFormatter xStringFormatter;
    private final GeoStringFormatter yStringFormatter;
    
    private static final Logger logger = LogManager.getLogger(Geo2dSearch.class);



    //public static Geo2dSearch buildGeo2dSearch
    
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
    public Geo2dSearch(int radix, int scale, int xWidth, int xOffset, int yWidth, int yOffset) {
        logger.debug("### Starting create {}",this.getClass().getSimpleName());
        this.radix = radix;
        this.scale = scale;  // what is the offset, for the various later calculations?
        this.xWidth = xWidth;
        this.yWidth = yWidth;
        this.xStringFormatter = new GeoStringFormatter.Builder().
            radix(this.radix).width(this.xWidth).scale(this.scale).offset(xOffset).build();
        this.yStringFormatter = new GeoStringFormatter.Builder().
            radix(this.radix).width(this.yWidth).scale(this.scale).offset(yOffset).build();
        logger.info("Initialized {} with Bounds {}",this.getClass().getSimpleName(),getBounds());
    }

    /**
     * Helper static method that returns a newly created Search object.
     * @param scale 
     * @param xWidth
     * @param yWidth
     * @return
     */
    public static Geo2dSearch buildDecimalGeo2dSearch(int scale, int xWidth, int yWidth) {
        return new Geo2dSearch(scale, xWidth, yWidth);
    }

    private Geo2dSearch(int scale, int xWidth, int yWidth) {
        this.radix = 10;
        this.scale = scale;
        this.xWidth = xWidth;
        this.yWidth = yWidth;
        this.xStringFormatter = GeoStringFormatter.buildRegularDecimalFormatter(xWidth, scale);
        this.yStringFormatter = GeoStringFormatter.buildRegularDecimalFormatter(yWidth, scale);
        logger.info("Initializing {} with Bounds {}",this.getClass().getSimpleName(),getBounds());
    }

    public Rect getBounds() {
        return new Rect(xStringFormatter.getMinValue(),yStringFormatter.getMinValue(),
                xStringFormatter.getMaxValue(),yStringFormatter.getMaxValue());
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
    
    int getXShift() {
        return xWidth-scale;
    }
    
    int getYShift() {
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
            targetCopy[i] = targets.get(i).copy();//.buffer(0.2);  // make a deep copy of the geometries so they can't be modified afterwards
        }
        Geo2dSearchEngine search = new Geo2dSearchEngine(this,targetCopy);
        return search.splitToRatio(completionRatio,maxSubs);
    }
    
    
}
