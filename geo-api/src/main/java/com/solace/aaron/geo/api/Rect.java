package com.solace.aaron.geo.api;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class, just so I don't have to use a List<Double> everywhere. Yes, I know it's
 * not good to expose fields publicly, but super immutable class, and super simple so it's
 * just easier.
 * 
 * @author Aaron Lee
 */
public final class Rect {

    public final double x1;
    public final double y1;
    public final double x2;
    public final double y2;
    
    /** Will change order such that this.x1 = Math.min(x1,x2); etc. */
    public Rect(double x1, double y1, double x2, double y2) {
        this.x1 = Math.min(x1,x2);
        this.y1 = Math.min(y1,y2);
        this.x2 = Math.max(x1,x2);
        this.y2 = Math.max(y1,y2);
    }
    
    /**
     * Returns a List of Doubles: x1,y1,x2,y2
     */
    public List<Double> asList() {
        List<Double> returnList = new ArrayList<>();
        returnList.add(x1);
        returnList.add(y1);
        returnList.add(x2);
        returnList.add(y2);
        return returnList;
    }

    @Override
    public String toString() {
        return String.format("[%s,%s][%s,%s]",
                Double.toString(x1),Double.toString(y1),Double.toString(x2),Double.toString(y2));
    }
}
