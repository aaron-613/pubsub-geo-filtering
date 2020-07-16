package com.solace.aaron.geo.api;

import java.util.ArrayList;
import java.util.List;

/**
 * This utility class is used to represent a range of 
 *
 */
public class Range {

    private final GeoStringFormatter formatter;
    private final String val;
    private final double inner;
    private final double outer;
    
    public Range(GeoStringFormatter formatter) {
        this(formatter,"");
    }    
    
    public Range(GeoStringFormatter formatter, String val) {
        this.formatter = formatter;
        this.val = val;
        this.inner = formatter.getInner(val);
        this.outer = formatter.getOuter(val);
    }
    
    public int getWidth() {
        return val.length();
    }
    
    public String getVal() {
        return val;
    }

    public double getInner() {
        return inner;
    }

    public double getOuter() {
        return outer;
    }
    
    public List<Range> buildChildren() {
        final int radix = formatter.getRadix();
        List<Range> children = new ArrayList<>(radix);
        final int valLength = val.length();
        char[] childValChars = new char[valLength+1];  // reusable object for constructing children topic strings
        val.getChars(0, valLength, childValChars, 0);  // copy the current topic string into the new array
        for (int i=0;i<radix;i++) {
            childValChars[valLength] = GeoStringFormatter.radixCharConvert(i);  // overwrite the last char of the array with incrementing digits
            children.add(new Range(formatter,new String(childValChars)));
        }
        return children;
    }

}
