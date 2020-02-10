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
    
    public Rect(double x1, double y1, double x2, double y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }
    
    public List<Double> asList() {
        List<Double> returnList = new ArrayList<>();
        returnList.add(x1);
        returnList.add(y1);
        returnList.add(x2);
        returnList.add(y2);
        return returnList;
    }
}
