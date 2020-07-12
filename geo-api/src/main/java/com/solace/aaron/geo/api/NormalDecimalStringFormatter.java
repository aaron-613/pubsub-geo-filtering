/*
 * Boilerplate comment
 */

package com.solace.aaron.geo.api;

import com.solace.aaron.geo.api.RadixStringFormatter.Builder;
import com.sun.tools.jdeps.InverseDepsAnalyzer;

public class NormalDecimalStringFormatter implements GeoStringFormatter {

    
    public static class Builder {
        
        private int numDigitsLeftOfDecimal = 3;
        private int numDigitsRightOfDecimal = 5;
        
        // default constructor
        
        /**
         * Doesn't include the '-' sign.
         */
        public Builder numDigitsLeftOfDecimal(int numDigitsLeftOfDecimal) {
            if (numDigitsLeftOfDecimal < 0) {
                throw new IllegalArgumentException();
            }
            this.numDigitsLeftOfDecimal = numDigitsLeftOfDecimal;
            return this;
        }
        
        public Builder numDigitsRightOfDecimal(int numDigitsRightOfDecimal) {
            if (numDigitsRightOfDecimal < 0) {
                throw new IllegalArgumentException();
            }
            this.numDigitsRightOfDecimal = numDigitsRightOfDecimal;
            return this;
        }
        
//        public Builder supportUpToAbs(double maxValToSupport) {
//            if (numDigitsRightOfDecimal < 0) {
//                throw new IllegalArgumentException();
//            }
//            this.numDigitsRightOfDecimal = numDigitsRightOfDecimal;
//            return this;
//        }
        
        
        
        public NormalDecimalStringFormatter build() {
            return new NormalDecimalStringFormatter(numDigitsLeftOfDecimal,numDigitsRightOfDecimal);
        }
        
/*        String convert(double val) {
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
        */
    }
    // END OF HELPER /////////////////////////////////////////////////////////////////////

    
    
    
    private final int width;
    private final int scale;
    private final double multiplier;
//    private final int offset;
//    private final double offsetMultiplier;
    private int numDigitsLeftOfDecimal = 3;
    private int numDigitsRightOfDecimal = 5;
    
    
    /**
     * Rather than using the defined static methods, this allows you to instantiate an object that can be reused.
     * Typically within a system, the radix, factor, and width are fixed, so let's just lock them in for ease of use.
     * @param radix What base are we using? 2=binary, 4=base4, 10=decimal, etc.
     * @param scale How many positions is the "radix point" shifted over? E.g. if radix=10, and scale=2, then 1.0 == '100', and 12.34 == '1234'
     * @param width How many total characters long is the generated radix string? E.g. if radix=10, and scale=2, and width=5, then 12.34 == '01234'
     * @param offset If lowest value isn't 0, how much to shift by?  E.g. -180 for longitude, -90 for latitude 
     */
//    public DecimalStringFormatter(int width, int scale) {
    public NormalDecimalStringFormatter(int numDigitsLeftOfDecimal, int numDigitsRightOfDecimal) {
        
        //this.scale = scale;  // e.g. for lat (-90,90), scale==2, for lon: scale==3
        this.scale = numDigitsRightOfDecimal;
        this.multiplier = Math.pow(10, scale);
        //this.width = width;
        this.width = numDigitsLeftOfDecimal + numDigitsRightOfDecimal;
//        this.offset = offset;  // e.g. offset==-180 (longitude)
//        this.offsetMultiplier = -offset * Math.pow(10,scale);
        assert width > 0 : "width must be > 0, but width=="+width;
    }
    
    @Override
    public int getRadix() {
        return 10;
    }

    public int getWidth() {
        return width;
    }
    
    public int getScale() {
        return scale;
    }
    
    public int getOffset() {
        return 0;
    }

    @Override
    public String toString() {
        return String.format("+/-[%f..%f), radix=%d, width=%d, scale=%d, offset=%d",getInner(""),getOuter(""),10,width,scale,0);
    }

    /**
     * This will return the lowest
     */
    @Override
    public double getInner(final String decimalString) {
        if (decimalString.length() == 0) {
            throw new IllegalArgumentException("Cannot have empty string for DecimalFormatter, at least '-' or '0' required");
        }
        assert (decimalString.charAt(0)=='0' || decimalString.charAt(0)=='-');
        if (decimalString.length() == 1) {
        	return 0;
//        } else if (decimalString.length() > width+1) {  // +1 because we need space for '0' or '-'
        	// commented out b/c we also need space for a decimal place, so maybe 2 chars more than width
//            throw new IllegalArgumentException(String.format("decimalString '%s' has length %d, but max width==%d : %s",decimalString,decimalString.length(),width,toString()));
        }
        try {
            double valDouble = Double.parseDouble(decimalString);
            /* Examples: assume: 3.5   (longitude)
             *   '0123.4' -> 123.4
             *   '0123.' -> 123
             *   '0123' -> 123
             *   '012' -> 120  (need x10)
             *   '-02' -> -20  (ditto)
             *   '-1'  -> -100
             */
            if (decimalString.length() <= numDigitsLeftOfDecimal) {
                valDouble *= Math.pow(10,numDigitsLeftOfDecimal-(decimalString.length()-1));
            } else if (decimalString.length() > numDigitsLeftOfDecimal+1) {
                // there definitely needs to be a decimal point now
                assert decimalString.charAt(numDigitsLeftOfDecimal+1) == '.';
            }
            return valDouble;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Couldn't parse a Double, are you sure this string is correct?",e);
        }
    }

    
    /**
     * 
     */
    @Override
    public double getOuter(final String decimalString) {
        if (decimalString.length() == 0) {
            throw new IllegalArgumentException("Cannot have empty string for DecimalFormatter, at least '-' or '0' required");
        }
        assert (decimalString.charAt(0)=='0' || decimalString.charAt(0)=='-');
        int negModifier = decimalString.charAt(0) == '-' ? -1 : 1;
        if (decimalString.length() == 1) {
            return Math.pow(10,numDigitsLeftOfDecimal) * negModifier;
        }
//        } else if (decimalString.length() > width+1) {  // +1 because we need space for '0' or '-'
            // commented out b/c we also need space for a decimal place, so maybe 2 chars more than width
//            throw new IllegalArgumentException(String.format("decimalString '%s' has length %d, but max width==%d : %s",decimalString,decimalString.length(),width,toString()));

        try {
            double valDouble = Math.abs(Double.parseDouble(decimalString));
            /* Examples: assume: 3.5   (longitude)
             *   '0123.4' -> 123.5
             *   '0123.' -> 124  (length 5)
             *   '0123' -> 124   (length 4)
             *   '012' -> 130  (need x10)
             *   '-02' -> -30  (ditto)
             *   '-1'  -> -200
             */
            if (decimalString.length() <= numDigitsLeftOfDecimal) {  // e.g. 3 or less
                valDouble += 1;
                valDouble *= Math.pow(10,numDigitsLeftOfDecimal-(decimalString.length()-1));
            } else if (decimalString.length() == numDigitsLeftOfDecimal+1) {
                valDouble += 1;
            } else if (decimalString.length() == numDigitsLeftOfDecimal+2) {
                valDouble += 1;
                assert decimalString.charAt(numDigitsLeftOfDecimal+1) == '.';
            } else {
                // there definitely needs to be a decimal point now
                assert decimalString.charAt(numDigitsLeftOfDecimal+1) == '.';
                valDouble += RadixUtils.lookupInverseFactors(10,decimalString.length()-numDigitsLeftOfDecimal-2);
            }
            return valDouble * negModifier;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Couldn't parse a Double, are you sure this string is correct?",e);
        }

    }
    
    
    /**
     * 
     */
    public double getOuter2(final String decimalString) {
        if (decimalString.length() == 0) {
            throw new IllegalArgumentException("Cannot have empty string for DecimalFormatter, at least '-' or '0' required");
        }
        assert (decimalString.charAt(0)=='0' || decimalString.charAt(0)=='-');
        if (decimalString.length() > width+1) {
            throw new IllegalArgumentException(String.format("decimalString '%s' has length %d, but max width==%d : %s",decimalString,decimalString.length(),width,toString()));
        }
        int factor = width - decimalString.length()-1 - scale;  // the max width, less the scale (shift of decimal point), less num chars in string
        boolean isNegative = decimalString.charAt(0) == '-';
        if (decimalString.length() == 1) {
            return Math.pow(10,factor) + offset;  // empty string, then whatever the max is
        }
        try {
            long vall = Long.parseLong(decimalString,10) + 1;  // always positive
            long valo = (long)Math.pow(10,width-decimalString.length());  // width always > decimalString.length(), so positive
            return ((vall * valo) - offsetMultiplier) / multiplier;
        } catch (NumberFormatException e) {  // if there were weird chars in the decimalString
            throw new IllegalArgumentException("Couldn't parse a Double, are you sure this string is correct?",e);
        }
    }

    /**
     * This method returns a single character that 
     * @param digit
     * @return
     */
    public static char decimalCharConvert(int digit) {
        assert digit >= 0 : "digit can't be negative, but digit=="+digit;
        assert digit < 36 : "digit must be less than decimal 36, but digit=="+digit;
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