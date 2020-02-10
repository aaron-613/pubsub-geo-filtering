package com.solace.aaron.geo.api;

public interface GeoStringFormatter {

    public double getInner(String val);
    public double getOuter(String val);
    public int getRadix();
    public String convert(double val);
}
