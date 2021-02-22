package com.solace.aaron.geo.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

/**
 * Ok, this class was made because we now support multiple "targets" in the geo-filtering
 * algorithm.  It performs a particular operation (add, multiply, etc.) across a group of 
 * numbers.
 * 
 * @author AaronLee
 *
 */
final class ArrayMath {

    /**
     * Returns the sum of one array of numbers to another.
     * Asserts that the array lengths are the same
     */
    public static final double[] add(double[] da1, double[] da2) {
        assert da1.length == da2.length;
        double[] retArray = new double[da1.length];
        for (int i=0;i<da1.length;i++) {
            retArray[i] = da1[i] + da2[i];
        }
        return retArray;
    }

    /**
     * Returns the difference of one array of numbers to another.
     * Asserts that the array lengths are the same
     */
    public static final double[] subtract(double[] da1, double[] da2) {
        assert da1.length == da2.length;
        double[] retArray = new double[da1.length];
        for (int i=0;i<da1.length;i++) {
            retArray[i] = da1[i] - da2[i];
        }
        return retArray;
    }
    
    /**
     * Returns the product of one array of numbers to another.
     * Asserts that the array lengths are the same
     */
    private static final double[] multiply(double[] da1, double[] da2) {
        assert da1.length == da2.length;
        double[] retArray = new double[da1.length];
        for (int i=0;i<da1.length;i++) {
            retArray[i] = da1[i] * da2[i];
        }
        return retArray;
    }

    /**
     * Returns the product of an array by a particular number.
     */
    public static final double[] multiply(double[] da1, double val) {
        double[] retArray = new double[da1.length];
        for (int i=0;i<da1.length;i++) {
            retArray[i] = da1[i] * val;
        }
        return retArray;
    }

    /**
     * Returns the division of one array of numbers to another.
     * Asserts that the array lengths are the same
     */
    public static final double[] divide(double[] da1, double[] da2) {
        assert da1.length == da2.length;
        double[] retArray = new double[da1.length];
        for (int i=0;i<da1.length;i++) {
            retArray[i] = da1[i] / da2[i];
        }
        return retArray;
    }

    public static final double[] min(double[] da1, double val) {
        double[] retArray = new double[da1.length];
        for (int i=0;i<da1.length;i++) {
            retArray[i] = Math.min(da1[i],val);
        }
        return retArray;
    }

    public static final double[] calculateCombinedRatio(double[] over, double[] under) {
        assert over.length == under.length;
        double[] retArray = new double[over.length];
        for (int i=0;i<over.length;i++) {
            //retArray[i] = over[i] + (2 * under[i]);
            //retArray[i] = over[i] + ((1.0 / (1 - Math.pow(under[i],1))) - 1);
            retArray[i] = over[i] + (2.0 * ((1.0 / (1 - Math.pow(under[i],1))) - 1));
        }
        return retArray;
    }
    
    /**
     * Returns true if all elements of da are less than val.
     * @param da
     * @param val
     * @return true if all elements of da are less than val.
     */
    public static final boolean lessThan(double[] da, double val) {
        boolean allGood = true;
        for (int i=0;i<da.length;i++) {
            allGood = allGood & da[i] < val;
        }
        return allGood;
    }

    public static final boolean lessThanIgnore(double[] da, double val, Set<Integer> ignore) {
        boolean allGood = true;
        for (int i=0;i<da.length;i++) {
            allGood = allGood & (da[i] < val | ignore.contains(i));
        }
        return allGood;
    }
    
    /**
     * Returns a Set of all da indexes i, where da[i] &lt; val
     * @param da
     * @param val
     * @return
     */
    private static final Set<Integer> lessThanSet(double[] da, double val) {
        Set<Integer> retSet = new HashSet<>();
        for (int i=0;i<da.length;i++) {
            if (da[i] < val) {
                retSet.add(i);
            }
        }
        return retSet;
    }

    /**
     * Returns true if at least one value inside the array is greater than val.
     */
    private static final boolean moreThan(double[] da, double val) {
        boolean allGood = true;
        for (int i=0;i<da.length;i++) {
            allGood = allGood & da[i] > val;
        }
        return allGood;
    }

    /**
     * Looks through the passed array and compares each entry to the passed value,
     * and then returns a Set of 
     * @param da
     * @param val
     * @return
     */
    private static final Set<Integer> moreThanSet(double[] da, double val) {
        Set<Integer> retSet = new HashSet<>();
        for (int i=0;i<da.length;i++) {
            if (da[i] > val) {
                retSet.add(i);
            }
        }
        return retSet;
    }
    
    /**
     * Searches through an array and returns the index of the max value.
     * Will return -1 if the set is empty
     */
    public static int getMaxIndex(double[] da) {
        int index = -1;
        if (da != null && da.length > 0) {
            double curMax = da[0];
            index = 0;
            if (da.length > 1) {
                for (int i=1;i<da.length;i++) {
                    if (da[i] > curMax) {
                        curMax = da[i];
                        index = i;
                    }
                }
            }
        }
        return index;
    }

    /** 
     * This takes in an array [ 1.0, 3.5, 2.9, +Infinity ] and returns [ 3, 1, 2, 0 ]
     * that is, the indices of the sorted array of doubles.
     */
    public static int[] getArrayIndexReverseOrder(double[] da) {
        int[] sortedIndices = IntStream.range(0, da.length)
                .boxed().sorted((i, j) -> Double.compare(da[j], da[i]) )
                .mapToInt(ele -> ele).toArray();
        return sortedIndices;
    }

    
    private ArrayMath() {
        throw new AssertionError("Please don't instantiate this class!");
    }
}
