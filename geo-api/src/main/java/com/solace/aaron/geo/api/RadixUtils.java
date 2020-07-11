package com.solace.aaron.geo.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class contains a number of internal utility 
 * @author AaronLee
 *
 */
public class RadixUtils {

    /**
     * <p>This function returns the number of places to the LEFT of the radix point needed to properly display the number based on range.
     * If maxRange is &lt; 0, then this will return 0.</p>
     * <ul>
     * <li>if maxRange = 120, radix = 10 --&gt; calcPadding() = 3</li>
     * <li>if maxRange = 0.050, radix = 10 --&gt; calcPadding() = 0</li>
     * <li>if maxRange = 120, radix = 16 (i.e. 0x78) --&gt; calcPadding() = 2</li>
     * </ul>
     * @param maxRange
     * @return
     */
    private static int calcPadding(double maxRange, int radix) {
        int factor =  calcFactor(Math.abs(maxRange),radix);
        if (factor >= 0) return 0;
        else return -factor;
    }
    
    /**
     * <p>This returns the number of places/columns to the RIGHT of the radix point needed for the desired range of numbers.</p>
     * <ul>
     * <li> if maxRange = 120, radix = 10 --&gt; calcFactor() = -3, i.e. &le; 999</li>
     * <li> if maxRange = 0.050, radix = 10 --&gt; calcFactor() = 1, i.e. &le; 0.1</li>
     * <li> if maxRange = 120, radix = 16 --&gt; calcFactor() = -2, i.e. &le; 999</li>
     * </ul>
     * TODO this doesn't make sense.
     * Note that abs() is taken, so will work just fine for negative numbers as well.
     * @param maxRange
     * @return
     */
    private static int calcFactor(double maxRange, int radix) {
        if (radix == 10) return -(int)Math.ceil(Math.log10(Math.abs(maxRange)));
        else return -(int)Math.ceil(Math.log10(Math.abs(maxRange))/Math.log10(radix));
    }
    
    /**
     * Helper function that returns the largest absolute of the two given values.
     * @param min
     * @param max
     * @return
     */
    private static double forRange(double min, double max) {
        return Math.max(Math.abs(min),Math.abs(max));
    }
    
    /**
     * If the range of possible values intersects the negative real numbers, return true.  This function is used
     * to determine if the topics/subscriptions need to be signed with minus signs, and padded with 0 if positive.
     * @param min
     * @param max
     * @return
     */
    private static boolean needNegsForRange(double min, double max) {
        return Math.min(min,max) < 0;
    }
    
    
    
    /*
     * This is constructs static "look-up tables" so that we don't have to compute the inverse factor using
     * division and Math.pow() constantly.  Saves on computation.
     */
    private static final Map<Integer,List<Double>> INVERSE_FACTORS = new HashMap<Integer, List<Double>>();
    private static final int MIN_FACTOR = -30;  // i.e. from 1/(2^-30) --&gt; 1/(2^50)
    private static final int MAX_FACTOR = 50;  // how big a range do we want to calculate?  because base2 needs a lot
    static {
        for (int base=2;base<=36;base++) {
            INVERSE_FACTORS.put(base, new ArrayList<Double>());
            for (int factor=MIN_FACTOR; factor<=MAX_FACTOR; factor++) {
                INVERSE_FACTORS.get(base).add(1.0/Math.pow(base, factor));
            }
        }
    }
    
    /**
     * This helper function just provides a look-up table to do some math with
     * hopefully speed things up... add 10 to the required factor to shift in the array.
     * This function is used to determine what the inverse of the factor is, which is used to 
     * 
     */
    public static double lookupInverseFactors(int radix, int factor) {
        try {
            return INVERSE_FACTORS.get(radix).get(factor-MIN_FACTOR);
        } catch (IndexOutOfBoundsException e) {
            System.err.println("(Not an error, just a note): RadixUtils.lookupInverseFactors is out of bounds: factor "+factor+", radix "+radix);
            return 1.0/Math.pow(radix,factor);
        } catch (NullPointerException e) {
            // this will happen when asking for any radix != 2,4,8,10,16
            //System.err.println("ASKING FOR A NON-EXISTENT FACTOR!  "+factor+", radix "+radix);
            return 1.0/Math.pow(radix,factor);  // just compute it regular
        }
    }
    
    /**
     * Returns the smallest value that a number can change and be "detected" or represented by
     * the 'factor' (i.e. num digits to the right of the decimal/radix point), at a given
     * radix.  For example, in base 10 with factor 3, this would return 0.001.  This should
     * be the equivalent to the inverse of getFactor().
     * @param radix
     * @param factor
     * @return
     */
    public static double lossOfPrecision(int radix, int factor) {
        return lookupInverseFactors(radix,factor);
    }
    
    /** This method will return the maximum possible decimal equivalent for a particular
     * radix and padding amount.  For example, in binary base 2 with padding == 8, this would
     * return 255.
     * 
     * @param radix
     * @param numPadding
     * @return
     */
    public static int maxDecEquivPossible(int radix, int numPadding) {
        return (int)Math.pow(radix,numPadding)-1;
    }
    
    /**
     * This method will specify how many digits are required to the left of the decimal/radix
     * point to ensure display the maximum decimal equivalent in a particular radix.  For
     * example, a value of 253 in radix 16 (hex) would require 2 digits. 
     * @param maxVal
     * @param radix
     * @return
     */
    public static int numPaddingNeeded(int radix, int maxVal) {
        int digitCount = 0;
        while (maxVal > 0) {
            maxVal /= radix;
            digitCount++;
        }
        return digitCount;
    }

    
    private RadixUtils() {
        throw new AssertionError("Please do not instantiate this helper class");
    }
}
