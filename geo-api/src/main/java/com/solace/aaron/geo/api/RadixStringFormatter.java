/*
 * Boilerplate comment
 */

package com.solace.aaron.geo.api;

/**
 * <p>Let's define some terms used throughout these classes.</p>
 * <ul>
 * <li><b>width</b>: (int &gt; 0) this is the number of digits required to the <i>left</i> of the radix point.
 * It is determined by the total range of values required to be represented.  E.g.:
 * <ul>
 * <li>For decimal values in the range <code>[-180,+180]</code>: width == 3</li>
 * <li>For hex values in the range <code>[-7f,+ff]</code> (decimal <code>[-127,+255]</code>): width == 2</li>
 * <li>For base4 values in the range <code>[-12,+333]</code> (decimal <code>[-10,+63]</code>): width == 3</li>
 * <li>For binary values in the range <code>[0,11111111]</code> (decimal <code>[0,127]</code>): width = 8</li>
 * </ul>If the returned/computed value has less digits to the left of the radix point, additional zeros 0 will be added (padded) to the front of the number.</li>
 * <li><b>factor</b>: (integer) this is the number of digits required to the <i>right</i> of the radix point.
 * It is determined by the amount of precision that you want/need.
 * E.g. for minimum decimal precision of 0.01:
 * <ul>
 * <li>For decimal values with precision up to 0.01: factor == 2</li>
 * <li>For hex values: 0.01 in decimal ~= 0.028F in hex; so factor == 2</li>
 * <li>For octal values: 0.01 in decimal ~= 0.005075 in octal; so factor == 3</li>
 * <li>For base4 values: 0.01 in decimal ~= 0.00022033 in base4; so factor == 4</li>
 * <li>For binary values: 0.01 in decimal ~= 0.000000101 in binary; so factor == 7</li>
 * </ul>Looking at it from the other way:
 * <ul>
 * <li>For decimal values with factor == 2, minimum decimal precision == 0.01, i.e. 1/(10^2)</li>
 * <li>For hex values with factor == 2: minimum decimal precision == 0.00390625, i.e. 1/(16^2)</li>
 * <li>For octal values with factor == 2: minimum decimal precision == 0.015625, i.e. 1/(8^2)</li>
 * <li>For base4 values with factor == 2: minimum decimal precision == 0.0625, i.e. 1/(4^2)</li>
 * <li>For binary values with factor == 2: minimum decimal precision == 0.25, i.e.1/(2^2)</li>
 * </ul></li>
 * </ul>
 * <p>This is the next para. Need some concrete examples next!</p>
 * 
 * @author Aaron Lee
 *
 */
public class RadixStringFormatter implements GeoStringFormatter {

    
    
    
    /*
     * Just so that we're clear:
     * 1. if d = 123.456, with scale ==3 and width == 3
     *   a) factor == 0 --> 123
     *   b) factor == -2 --> 1
     *   c) factor == 2 --> 12345
     * 2. if d = 123.456,  with width == 5
     *   a) factor == 0 --> 00123
     *   b) factor == -2 --> 001
     *   c) factor == 2 --> 0012345
     */
    
    
    
    /*
     * So, 34.12:
     * If width == 2 and needNegs == false: 34.12
     * If width == 2 and needNegs == true: 034.12
     * 
     * 
     * 
     * 
     * 
     */

    
    /**
     * <p>Helper class when formating these radix strings... there's a lot of parameters,
     * and 3 ints in a row, so this makes it easier.</p>
     * <p>Default values:
     * <ul>
     * <li>val = 0</li>
     * <li>radix = 10</li>
     * <li>width = 1</li>
     * <li>factor = 0</li>
     * </ul>
     *
     */
    static class Builder {
        
        private int radix = 10;
        private int width = 10;
        private int scale = 4;
        private int offset = 0;
        
        // default constructor
        
        Builder radix(int radix) {
            if (radix <= 1 || radix > 36) {
                throw new IllegalArgumentException(String.format("Invalid value of radix (%d), must be in [2..36]",radix));
            }
            this.radix = radix;
            return this;
        }
        
        Builder width(int width) {
            if (width <= 0 || width > 64) {
                throw new IllegalArgumentException(String.format("Invalid value of width (%d), must be in [1..64]",width));
            }
            this.width = width;
            return this;
        }
        
        Builder scale(int scale) {
            this.scale = scale;
            return this;
        }

        Builder offset(int offset) {
            this.offset = offset;
            return this;
        }
        
        RadixStringFormatter build() {
            return new RadixStringFormatter(radix,width,scale,offset);
        }
        
        String convert(double val) {
            return new RadixStringFormatter(radix,width,scale,offset).convert(val);
        }
        
        public double getInner(String val) {
            return new RadixStringFormatter(radix,width,scale,offset).getInner(val);
        }

        public double getOuter(String val) {
            return new RadixStringFormatter(radix,width,scale,offset).getOuter(val);
        }

        @Override
        public String toString() {
            return build().toString();
        }
        
        String debugConvert(double val) {
            RadixStringFormatter formatter = build();
            return String.format("Convert %s,  val=%f ==> '%s'", formatter, val, formatter.convert(val));
        }

        String debugGetInner(String radixString) {
            return String.format("getInner %s,  radixString='%s' ==> %f", toString(), radixString, getInner(radixString));
        }

        String debugGetOuter(String radixString) {
            return String.format("getOuter %s,  radixString='%s' ==> %s", toString(), radixString, Double.toString(getOuter(radixString)));
        }
    }
    // END OF HELPER /////////////////////////////////////////////////////////////////////

    
    
    private final int radix;
    private final int width;
    private final int scale;
    private final int offset;
    private final double multiplier;
    private final double offsetMultiplier;
    
    /**
     * Rather than using the defined static methods, this allows you to instantiate an object that can be reused.
     * Typically within a system, the radix, factor, and width are fixed, so let's just lock them in for ease of use.
     * @param radix What base are we using? 2=binary, 4=base4, 10=decimal, etc.
     * @param scale How many positions is the "radix point" shifted over? E.g. if radix=10, and scale=2, then 1.0 == '100', and 12.34 == '1234'
     * @param width How many total characters long is the generated radix string? E.g. if radix=10, and scale=2, and width=5, then 12.34 == '01234'
     * @param offset If lowest value isn't 0, how much to shift by?  E.g. -180 for longitude, -90 for latitude 
     */
    private RadixStringFormatter(int radix, int width, int scale, int offset) {
        this.radix = radix;
        this.scale = scale;
        this.multiplier = Math.pow(radix, scale);
        this.width = width;
        this.offset = offset;  // e.g. offset==-180 (longitude)
        this.offsetMultiplier = -offset * Math.pow(radix,scale);
        assert width > 0 : "width must be > 0, but width=="+width;
        assert radix >= 2 && radix <= 36 : "radix must be in [2,36], but radix=="+radix;
    }
    
    @Override
    public int getRadix() {
        return radix;
    }
    
    public int getWidth() {
        return width;
    }
    
    public int getScale() {
        return scale;
    }
    
    public int getOffset() {
        return offset;
    }
    
    @Override
    public String toString() {
        return String.format("[%f..%f), radix=%d, width=%d, scale=%d, offset=%d",getInner(""),getOuter(""),radix,width,scale,offset);
    }

    /**
     * This will return the lowest
     */
    @Override
    public double getInner(final String radixString) {
        if (radixString.length() == 0) {
            return offset;
        } else if (radixString.length() > width) {
            throw new IllegalArgumentException(String.format("radixString '%s' has length %d, but max width==%d : %s",radixString,radixString.length(),width,toString()));
        }
        try {
            long valLong = Long.parseLong(radixString,radix);  // always positive
            assert valLong >= 0;
            long valShift = (long)Math.pow(radix,width-radixString.length());  // width always > radixString.length(), so positive
            return ((valLong * valShift) - offsetMultiplier) / multiplier;
        } catch (NumberFormatException e) {
            throw e;
        }
    }

    /**
     * 
     */
    @Override
    public double getOuter(final String radixString) {
        if (radixString.length() > width) {
            throw new IllegalArgumentException(String.format("radixString '%s' has length %d, but max width==%d : %s",radixString,radixString.length(),width,toString()));
        }
        int factor = width - radixString.length() - scale;  // the max width, less the scale (shift of radix point), less num chars in string
        if (radixString.length() == 0) {
            return Math.pow(radix,factor) + offset;  // empty string, then whatever the max is
        }
        try {
            long vall = Long.parseLong(radixString,radix) + 1;  // always positive
            long valo = (long)Math.pow(radix,width-radixString.length());  // width always > radixString.length(), so positive
            return ((vall * valo) - offsetMultiplier) / multiplier;
        } catch (NumberFormatException e) {  // if there were weird chars in the radixString
            throw e;
        }
    }

    /**
     * Pass in a value from 0->36, and this will return a char '0'..'9','A'..'Z'
     * @param digit in [0..36]
     * @return ['0'..'9','A'..'Z']
     */
    static char radixCharConvert(int digit) {
        assert digit >= 0 : "digit can't be negative, but digit=="+digit;
        assert digit < 36 : "digit must be less than radix 36, but digit=="+digit;
        if (digit < 10) {
            return (char)(digit+'0');
        } else {  // e.g. base 16, base 12, base 36
            return (char)(digit+'7');  // 7 in ASCII is 10 less than A
//            return (char)(digit-10+'A');
        }
    }
    
    /**
     * Will assert that the value of 'val' can be properly represented by this Formatter's configuration. 
     * @param val
     * @return
     */
    @Override
    public String convert(final double val) {
        final long shift = (long)(val*multiplier) + (long)offsetMultiplier;  // so, if radix=10, scale=3, multiplier=1000, offset=0, 123.456 --> 123456
        try {
            if (shift < 0) {
                String errMsg = String.format("%s underflow error: val=%f < MIN=%f (shift=%d) : %s",this.getClass().getSimpleName(),val,getInner(""),shift,this.toString());
                throw new IllegalArgumentException(errMsg);
            }
            String converted = Long.toString(shift,radix);
            if (converted.length() > width) {
                String errMsg = String.format("%s overflow error: val=%f >= MAX=%f (length=%d) : %s",this.getClass().getSimpleName(),val,getOuter(""),converted.length(),this.toString());
                throw new IllegalArgumentException(errMsg);
            }
            // ok, so now we make the String to return
            char[] a = new char[width];               // first, get a char array big enough
            converted.getChars(0,converted.length(),  // copy all of 'converted'
                    a,                                // into char[] a
                    width-converted.length());        // keeping it right-aligned
            if (converted.length() < a.length) {  // now pad the beginning with 0s if need be
                // ok, so width==5, length=3, need 2 extra 0's. Since there's an extra char for onlyAbs, start at index 1
                for (int i=0;i<a.length-converted.length();i++) {
                    a[i] = '0';
                }
            }
            return new String(a);
        } catch (Exception | Error e) {
            System.err.println(e);
            throw e;
        }
    }

}