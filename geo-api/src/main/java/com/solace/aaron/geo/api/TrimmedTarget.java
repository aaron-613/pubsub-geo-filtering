package com.solace.aaron.geo.api;

import org.locationtech.jts.geom.Geometry;

public class TrimmedTarget {

    private final Geometry geometry;
    private final double area;
    private final double overCoverageArea;
    
    public TrimmedTarget(Geometry geometry) {
        this.geometry = geometry;
        this.area = geometry.getArea();
        this.overCoverageArea = 1;
    }
}
