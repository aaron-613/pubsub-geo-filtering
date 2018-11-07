/*
 * Boilerplate comment
 */

package com.solace.aaron.geo.api;

/**
 * <p>Let's define some terms used throughout these classes.</p>
 * <ul>
 * <li><b>padding</b>: (int &gt; 0) this is the number of digits required to the <i>left</i> of the radix point.
 * It is determined by the total range of values required to be represented.  E.g.:
 * <ul>
 * <li>For decimal values in the range <code>[-180,+180]</code>: padding == 3</li>
 * <li>For hex values in the range <code>[-7f,+ff]</code> (decimal <code>[-127,+255]</code>): padding == 2</li>
 * <li>For base4 values in the range <code>[-12,+333]</code> (decimal <code>[-10,+63]</code>): padding == 3</li>
 * <li>For binary values in the range <code>[0,11111111]</code> (decimal <code>[0,127]</code>): padding = 8</li>
 * </ul>If the returned/computed value has less digits to the left of the radix point, additional zeros 0 will be added (padded) to the front of the number.</li>
 * <li><b>onlyAbs</b>: (boolean) whether the computed radix string requires an additional character to indicate if negative values are expected.
 * If the total range of values includes negative numbers, then a '-' or '0' will be added to the front of the returned radix string. E.g.:
 * <ul>
 * <li>For decimal range <code>[-180,+180]</code>: onlyAbs == true</li>
 * <li>For hex values in the range <code>[-7f,+ff]</code> (decimal <code>[-127,+255]</code>): onlyAbs == true</li>
 * <li>For binary values in the range <code>[0,11111111]</code> (decimal <code>[0,127]</code>): onlyAbs == false</li>
 * </ul></li>
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
public class RadixStringFormatter {

    
    
    
    /*
     * Just so that we're clear:
     * 1. if d = 123.456, with scale ==3 and padding == 3
     *   a) factor == 0 --> 123
     *   b) factor == -2 --> 1
     *   c) factor == 2 --> 12345
     * 2. if d = 123.456,  with padding == 5
     *   a) factor == 0 --> 00123
     *   b) factor == -2 --> 001
     *   c) factor == 2 --> 0012345
     */
    
    
    
    /*
     * So, 34.12:
     * If padding == 2 and needNegs == false: 34.12
     * If padding == 2 and needNegs == true: 034.12
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
     * <li>padding = 1</li>
     * <li>factor = 0</li>
     * <li>onlyAbs = false</li>
     * </ul>
     * @author Aaron Lee
     *
     */
    public static class Helper {
        
        private int radix = 10;
        private int padding = 1;
        private int scale = 0;
        private int offset = 0;
        
        public Helper radix(int radix) {
            if (radix <= 1 || radix > 36) {
                throw new IllegalArgumentException(String.format("Invalid value of radix (%d), must be in [2..36]",radix));
            }
            this.radix = radix;
            return this;
        }
        
        public Helper padding(int padding) {
            if (padding <= 0 || padding > 64) {
                throw new IllegalArgumentException(String.format("Invalid value of padding (%d), must be in [1..64]",padding));
            }
            this.padding = padding;
            return this;
        }
        
        public Helper scale(int scale) {
            this.scale = scale;
            return this;
        }

        public Helper offset(int offset) {
            this.offset = offset;
            return this;
        }
        
        
        public String convert(double val) {
            return new RadixStringFormatter(radix,scale,padding,offset).convert(val);
        }
        
        public double getInner(String val) {
            return new RadixStringFormatter(radix,scale,padding,offset).getInner(val);
        }
        
        @Override
        public String toString() {
            return String.format("radix=%d, scale=%d, padding=%d",radix,scale,padding);
        }
        
        public String debugConvert(double val) {
            return String.format("%s,  val=%f ==> '%s'", toString(), val, convert(val));
        }

        public String debugConvertBack(String val) {
            return String.format("%s,  val='%s' ==> %f", toString(), val, getInner(val));
        }
    }
    
    private final int radix;
    private final int scale;
    private final double multiplier;
    private final int padding;
    private final int offset;
    private final double offsetMultiplier;
    
    /**
     * Rather than using the defined static methods, this allows you to instantiate an object that can be reused.
     * Typically within a system, the radix, factor, and padding are fixed, so let's just lock them in for ease of use.
     * @param radix
     * @param scale
     * @param padding
     * @param onlyAbs
     */
    public RadixStringFormatter(int radix, int scale, int padding, int offset) {
        this.radix = radix;
        this.scale = scale;
        this.multiplier = Math.pow(radix, scale);
        this.padding = padding;
        this.offset = offset;
        this.offsetMultiplier = offset * Math.pow(radix,scale);
        assert padding > 0 : "padding must be > 0, but padding=="+padding;
        assert radix >= 2 && radix <= 36 : "radix must be in [2,36], but radix=="+radix;
    }

    double getInner(final String val) {
        if (val.length() == 0) {
            return -offset;
        }
        long vall = Long.parseLong(val,radix);  // always positive
        long valo = (long)Math.pow(radix,padding-val.length());  // padding always > val.length(), so positive
        //long off = offset * (long)multiplier;
        return ((vall * valo) - offsetMultiplier) / multiplier;
    }

    double getOuter(final String val) {
        int factor = padding - val.length() - scale;
        if (val.length() == 0) {
            return Math.pow(radix,factor) - offset;
        }
        long vall = Long.parseLong(val,radix) + 1;  // always positive
        long valo = (long)Math.pow(radix,padding-val.length());  // padding always > val.length(), so positive
        //long off = offset*(long)multiplier;
        return ((vall * valo) - offsetMultiplier) / multiplier;
    }

    public static char radixConvert(int digit) {
        assert digit >= 0 : "digit can't be negative, but digit=="+digit;
        assert digit <= 36 : "digit must be less than radix 36, but digit=="+digit;
        if (digit < 10) {
            return (char)(digit+'0');
        } else {  // e.g. base 16
            return (char)(digit-10+'A');
        }        
    }
    
    public String convert(final double val) {
        final long shift = (long)((val)*multiplier) + (long)offsetMultiplier;  // so, if radix=10, scale=3, multiplier=1000, offset=0, 123.456 --> 123456
        assert shift >= 0 : "shift is negative="+shift+", val="+val+" (means offset wasn't enough), "+toString();
        return format(shift,radix,padding);
    }

    private static String format(final long shift, final int radix, final int padding) {
        String converted;
        try {
            converted = Long.toString(shift,radix);
            assert converted.length() <= padding : "converted length >= padding";
            char[] a = new char[padding];  // plus one for the (possible) negative sign or 0 pad
            converted.getChars(0,converted.length(),  // copy all of 'converted'
                    a,                                // into char[] a
                    a.length-converted.length());     // keeping it right-aligned
            if (converted.length() < a.length) {  // now pad the beginning with 0s if need be
                // ok, so padding==5, length=3, need 2 extra 0's. Since there's an extra char for onlyAbs, start at index 1
                for (int i=0;i<a.length-converted.length();i++) {
                    a[i] = '0';
                }
            }
            return new String(a);
        } catch (Exception | Error e) {
            System.err.println(e);
            System.err.printf(">>> shift=%d, radix=%d, padding=%d%n%n",shift,radix,padding);
            throw e;
        }
    }
}
