/*
 * Boilerplate comment
 */

package com.solace.aaron.geo.api;


public class DecimalStringFormatter implements GeoStringFormatter {

    private final int width;
    private final int scale;
    private final double multiplier;
    private final int offset;
    private final double offsetMultiplier;
    
    /**
     * Rather than using the defined static methods, this allows you to instantiate an object that can be reused.
     * Typically within a system, the radix, factor, and width are fixed, so let's just lock them in for ease of use.
     * @param radix What base are we using? 2=binary, 4=base4, 10=decimal, etc.
     * @param scale How many positions is the "radix point" shifted over? E.g. if radix=10, and scale=2, then 1.0 == '100', and 12.34 == '1234'
     * @param width How many total characters long is the generated radix string? E.g. if radix=10, and scale=2, and width=5, then 12.34 == '01234'
     * @param offset If lowest value isn't 0, how much to shift by?  E.g. -180 for longitude, -90 for latitude 
     */
    public DecimalStringFormatter(int width, int scale, int offset) {
        this.scale = scale;
        this.multiplier = Math.pow(10, scale);
        this.width = width;
        this.offset = offset;  // e.g. offset==-180 (longitude)
        this.offsetMultiplier = -offset * Math.pow(10,scale);
        assert width > 0 : "width must be > 0, but width=="+width;
    }
    
    @Override
    public int getRadix() {
        return 10;
    }
    /*
    public int getWidth() {
        return width;
    }
    
    public int getScale() {
        return scale;
    }
    
    public int getOffset() {
        return offset;
    }
    */
    @Override
    public String toString() {
        return String.format("[%f..%f), radix=%d, width=%d, scale=%d, offset=%d",getInner(""),getOuter(""),10,width,scale,offset);
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
            long valLong = Long.parseLong(radixString,10);  // always positive
            assert valLong >= 0;
            long valShift = (long)Math.pow(10,width-radixString.length());  // width always > radixString.length(), so positive
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
            return Math.pow(10,factor) + offset;  // empty string, then whatever the max is
        }
        try {
            long vall = Long.parseLong(radixString,10) + 1;  // always positive
            long valo = (long)Math.pow(10,width-radixString.length());  // width always > radixString.length(), so positive
            return ((vall * valo) - offsetMultiplier) / multiplier;
        } catch (NumberFormatException e) {  // if there were weird chars in the radixString
            throw e;
        }
    }

    /**
     * This method returns a single character that 
     * @param digit
     * @return
     */
    public static char radixCharConvert(int digit) {
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
            String converted = Long.toString(shift,10);
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