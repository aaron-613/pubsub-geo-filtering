package com.solace.aaron.geo.api;

/**
 * This interface can be used for either radix string representation, or normal decimal.
 */
public interface GeoStringFormatter {

    public double getInner(String val);
    public double getOuter(String val);
    public int getRadix();
    public String convert(double val);
}
