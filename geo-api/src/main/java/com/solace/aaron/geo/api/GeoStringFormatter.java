/*
 * Boilerplate comment
 */

package com.solace.aaron.geo.api;

import java.util.ArrayList;
import java.util.List;

/**
 * This class provides the important methods for converting between floating point numbers and the GeoString
 * representation (in different bases) used to perform the filtering by the algorithm.
 * 
 * <b>scale</b>: think of this as the negative exponent when writing out the as "long value" x 10^-scale
 * 
 * 
 * 
 * <p>Let's define some terms used throughout these classes.</p>
 * 
 * 
 * <ul>
 * <li><b>width</b>: (int &gt; 0) this is the total number of digit places.
 * It is determined by: a) the total range of values required to be represented; and
 * b) the required precision. E.g.:
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
public final class GeoStringFormatter {

    
    
    /**
     * <p>Helper class when formating these radix strings... there's a lot of parameters,
     * and 3 ints in a row, so this makes it easier.</p>
     * <p>Default values:
     * <ul>
     * <li>radix = 10</li>
     * <li>width = 9</li>
     * <li>scale = 5</li>
     * <li>offset = 0</li>
     * </ul>
     * which will give you a range of (-100,1000)
     */
    public static class Builder {
        
        private int radix = 10;
        private int width = 9;
        private int scale = 5;
        private int offset = 0;
        
        // default constructor
        
        public Builder radix(int radix) {
            if (radix <= 1 || radix > 36) {
                throw new IllegalArgumentException(String.format("Invalid value of radix '%d', must be in [2..36]",radix));
            }
            this.radix = radix;
            return this;
        }
        
        public Builder width(int width) {
            if (width <= 0 || width > 64) {
                throw new IllegalArgumentException(String.format("Invalid value of width (%d), must be in [1..64]",width));
            }
            this.width = width;
            return this;
        }
        
        public Builder scale(int scale) {
            this.scale = scale;
            return this;
        }

        /** E.g. if wanting to represent a range -180..180, and no negative numbers in representation, then use offset -180. */
        public Builder offset(int offset) {
            this.offset = offset;
            return this;
        }
        
        public GeoStringFormatter build() {
            return new GeoStringFormatter(radix,width,scale,offset,false);
        }
        
        String convert(double val) {
            return new GeoStringFormatter(radix,width,scale,offset,false).convert(val);
        }
        
        public double getInner(String val) {
            return new GeoStringFormatter(radix,width,scale,offset,false).getInner(val);
        }

        public double getOuter(String val) {
            return new GeoStringFormatter(radix,width,scale,offset,false).getOuter(val);
        }

        @Override
        public String toString() {
            return build().toString();
        }
        
        String debugConvert(double val) {
            GeoStringFormatter formatter = build();
            return String.format("%s, convert(%f) ==> '%s'", formatter, val, formatter.convert(val));
        }

        String debugGetInner(String radixString) {
            return String.format("%s, getInner('%s') ==> %s", toString(), radixString, Double.toString(getInner(radixString)));
        }

        String debugGetOuter(String radixString) {
            return String.format("%s, getOuter('%s') ==> %s", toString(), radixString, Double.toString(getOuter(radixString)));
        }
    }
    // END OF HELPER /////////////////////////////////////////////////////////////////////

    
    
    
    
    private final int radix;
    private final int width;
    private final int scale;
    private final int offset;  // how much do we "shift" the values up or down?
    private final double multiplier;  // derived from the scale... if base10, scale=3, then multiplier==1000
    private final double offsetMultiplier;  // since we work in "shifted" values for accuracy, need to shift the offset too
    private final boolean includeDecimal;  // only used in regular base10 notation, although could still use radix=10 to disable
    private final int digitsLeftOfDecimal;  // derived, used when building the "actual" decimal representation in getDecimalString()
    private final double minValue;
    private final double maxValue;

    /**
     * Rather than using the defined static methods, this allows you to instantiate an object that can be reused.
     * Typically within a system, the radix, factor, and width are fixed, so let's just lock them in for ease of use.
     * @param radix What base are we using? 2=binary, 4=base4, 10=decimal, etc.
     * @param scale How many positions is the "radix point" shifted over? E.g. if radix=10, and scale=2, then 1.0 == '100', and 12.34 == '1234'
     * @param width How many total characters long is the generated radix string? E.g. if radix=10, and scale=2, and width=5, then 12.34 == '01234'
     * @param offset If lowest value isn't 0, how much to shift by?  E.g. -180 for longitude, -90 for latitude 
     */
    private GeoStringFormatter(int radix, int width, int scale, int offset, boolean includeDecimal) {
        this.radix = radix;
        this.scale = scale;
        this.multiplier = Math.pow(radix, scale);
        this.width = width;
        this.offset = offset;  // e.g. offset == 180 (for longitude [-180..180])
        this.offsetMultiplier = -offset * Math.pow(radix,scale);
        assert width > 0 : "width must be > 0, but width=="+width;
        assert radix >= 2 && radix <= 36 : "radix must be in [2,36], but radix=="+radix;
        this.includeDecimal = true;//includeDecimal;
        if (this.includeDecimal) {
            //assert radix == 10;
        }
        digitsLeftOfDecimal = width-scale;
        this.minValue = getInner("");
        this.maxValue = getOuter("");
    }
    
    public static GeoStringFormatter buildRegularDecimalFormatter(int width, int scale) {
        return new GeoStringFormatter(10,width,scale,0,true);
    }
    
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
    
    public boolean isDecimal() {
        return includeDecimal;
    }
    
    public double getMinValue() {
        return minValue;
    }
    
    public double getMaxValue() {
        return maxValue;
    }

    public void contains(final double realValue) {
        if (realValue < minValue || realValue > maxValue) throw new IllegalArgumentException("OUT OF BOUNDS");
    }

    public void covers(final double realValue) {
        if (realValue == minValue || realValue == maxValue) throw new IllegalArgumentException("ON THE BOUNDEARY");
        contains(realValue);
    }
    
    @Override
    public String toString() {
        return String.format("(%f..%f), radix=%d, width=%d, scale=%d, offset=%d, with '.'?=%b",getInner(""),getOuter(""),radix,width,scale,offset,includeDecimal);
    }

    /**
     * This will return the lowest
     */
    public double getInner(final String radixString) {
        if (radixString.isEmpty()) {  // special case
            // Since allowing negative numbers, this must cover the full range.
            // we need to return the outer for the 1st level.
            return getOuter("-");
        } else if (radixString.length() > width) {
            throw new IllegalArgumentException(String.format("radixString '%s' has length %d, but max width==%d : %s",radixString,radixString.length(),width,toString()));
        } else if (radixString.length() == 1 && radixString.charAt(0) == '-') {  // another special case, can't parse with parseLong()
            return offset;  // usually just 0
        }
        try {
            long valLong = Long.parseLong(radixString,radix);
            long valShift = (long)Math.pow(radix,width-radixString.length());  // width always > radixString.length(), so positive
            return ((valLong * valShift) - offsetMultiplier) / multiplier;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Couldn't parse a Long, are you sure this string is correct?",e);
        }
    }

    /**
     * 
     */
    public double getOuter(final String radixString) {
        if (radixString.length() > width) {
            throw new IllegalArgumentException(String.format("radixString '%s' has length %d, but max width==%d : %s",radixString,radixString.length(),width,toString()));
        }
        int factor = width - radixString.length() - scale;  // the max width, less the scale (shift of radix point), less num chars in string
        if (radixString.isEmpty()) {
            return Math.pow(radix,factor) + offset;  // empty string, then whatever the max is
        } else if (radixString.length() == 1 && radixString.charAt(0) == '-') {  // can't parse with parseLong()
            return (-1 * (getOuter("0") - offset)) + offset;
        }
        try {
            long vall = Math.abs(Long.parseLong(radixString,radix)) + 1;
            long valo = (long)Math.pow(radix,width-radixString.length());  // width always > radixString.length(), so positive
            if (radixString.charAt(0) == '-') {  // is negative
                // return -1 * (((vall * valo) - offsetMultiplier) / multiplier);
                return (-1 * ((((vall * valo) - offsetMultiplier) / multiplier) - offset)) + offset;
            } else {
                return ((vall * valo) - offsetMultiplier) / multiplier;
            }
        } catch (NumberFormatException e) {  // if there were weird chars in the radixString
            throw new IllegalArgumentException("Couldn't parse a Long, are you sure this string is correct?",e);
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
     * <p>Use this method, or if scale >= 0 you can use String.format();
     * e.g. String.format("%09.5f");  // for longitude</p>
     * 
     {@code
     formatter.convertDecimal(123.45);
     result == "0123.45000";
     }
     * 
     * @param realValue
     * @return this is a return
     * 
     */
    public String convertDecimal(final double realValue) {
        //return convertDecimalString(convert(realValue));
        return String.format(String.format("%%0%d.%df",width+1,scale),realValue);
    }

    /**
     * Will provide the formatted GeoString by converting the double 'realValue'.
     * The length of the returned string will exaclty the 'width'.
     * @param realValue a double hopefully within the range of the formatter
     * @return
     */
    public String convert(final double realValue) {
        assert realValue <= getMaxValue();
        assert realValue >= getMinValue();
        // final long shift = (long)Math.round(realValue*multiplier) + (long)offsetMultiplier;  // so, if radix=10, scale=3, multiplier=1000, offset=0, 123.456 --> 123456
        // cannot use round, because values close to the edge won't work
        final long shift = (long)(realValue*multiplier) + (long)offsetMultiplier;  // so, if radix=10, scale=3, multiplier=1000, offset=0, 123.456 --> 123456
        try {
            String converted = Long.toString(shift,radix);
            if (converted.length() > width) {
                System.out.println(converted);
                String errMsg = String.format("%s convert() length overflow error: val=%f -> \"%s\" >= length=%d : %s",this.getClass().getSimpleName(),realValue,converted,converted.length(),this.toString());
                throw new IllegalArgumentException(errMsg);
            }
            // ok, so now we make the String to return
            char[] charArray = new char[width];               // first, get a char array big enough
            if (shift < 0) {  // negative, special case... need to put the minus sign at the front
                charArray[0] = '-';
                // then, continue copying as before. might end up with extra - sign that we'll overwrite later
            }
            converted.getChars(0,converted.length(),  // copy all of 'converted' val String
                    charArray,                        // into char[] charArray
                    width-converted.length());        // keeping it right-aligned
            if (converted.length() < charArray.length) {  // now pad the beginning with 0s if need be
                // ok, so width==5, converted.length=3, need 2 extra 0's.
                int negativeModifier = shift < 0 ? 1 : 0;  // where to start adding the extra 0's
                // so if positive, start adding from 0
                // but if negative, already have minus in pos[0], so add 0's from 1, but one less length
                for (int i=negativeModifier; i<charArray.length-converted.length()+negativeModifier; i++) {
                    charArray[i] = '0';
                }
            }
            return new String(charArray);
        } catch (Exception | Error e) {
            System.err.println(e);
            throw e;
        }
    }
    
    /**
     * This method is called by both the Range toString(), and the convert(double) method.
     * @param geoString
     * @return
     */
    String convertDecimalString(final String geoString) {
        assert radix == 10;
        assert offset == 0;
        assert includeDecimal == true;
        // where does the '.' go??
        // width=8, scale=5: xxx.xxxxx
        if (geoString.length() <= digitsLeftOfDecimal) {  // too short for a decimal!
            return geoString;
        }
        // else!
        char[] charArray = new char[geoString.length()+1];  // first, get a char array big enough
        geoString.getChars(0,digitsLeftOfDecimal,           // copy the (e.g.) first 3 chars
                charArray,                                  // into char[] charArray
                0);                                         // keeping it left-aligned
        charArray[digitsLeftOfDecimal] = '.';               // add the decimal
        geoString.getChars(digitsLeftOfDecimal,geoString.length(),  // put the mantissa
                charArray,                                  // into char[] charArray
                digitsLeftOfDecimal+1);                     // after the decimal
        return new String(charArray);
    }

    Range buildStartingRange() {
        return new Range();
    }
    
    Range buildDebugRange(String val) {
        return new Range(val);
    }
    
    /**
     * The Range is the actual object/class that holds the range of values on the inner and outer
     * side of the subscription.  
     */
    class Range {

        private final String val;
        private final double inner;
        private final double outer;
        
        private Range() {
            this("");
        }    
        
        private Range(String val) {
            this.val = val;
            this.inner = GeoStringFormatter.this.getInner(val);
            this.outer = GeoStringFormatter.this.getOuter(val);
        }
        
        /**
         * The width (length, actually) of the String `val` that defines this range.
         * @return
         */
        int getWidth() {
            return val.length();
        }
        
        String getVal() {
            return val;
        }

        @Override
        public String toString() {
            if (includeDecimal) {
                return convertDecimalString(val);
            } else {
                return val;
            }
        }

        double getInner() {
            return inner;
        }

        double getOuter() {
            return outer;
        }

        /** This one should only be used by the VERY first time you create the search grid, b/c it has +1 children for the - sign. */
        List<Range> buildInitialChildren() {
            assert val.isEmpty();
            List<Range> children = new ArrayList<>(radix+1);
            children.add(new Range("-"));
            for (int i=0;i<radix;i++) {
                children.add(new Range(new String(new char[] {radixCharConvert(i)})));
            }
            return children;
        }
        
        /**
         * This build all the sub-ranges, using the exact number of the radix.
         * @return
         */
        List<Range> buildChildren() {
            List<Range> children = new ArrayList<>(radix);
            final int valLength = val.length();
            char[] childValChars = new char[valLength+1];  // reusable object for constructing children topic strings
            val.getChars(0, valLength, childValChars, 0);  // copy the current topic string into the new array
            for (int i=0;i<radix;i++) {
                childValChars[valLength] = radixCharConvert(i);  // overwrite the last char of the array with incrementing digits
                children.add(new Range(new String(childValChars)));
            }
            return children;
        }
    };
}