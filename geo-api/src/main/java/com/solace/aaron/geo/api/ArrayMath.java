package com.solace.aaron.geo.api;

import java.util.HashSet;
import java.util.Set;

public final class ArrayMath {

    
    public static final double[] add(double[] da1, double[] da2) {
        assert da1.length == da2.length;
        double[] retArray = new double[da1.length];
        for (int i=0;i<da1.length;i++) {
            retArray[i] = da1[i] + da2[i];
        }
        return retArray;
    }

    public static final double[] subtract(double[] da1, double[] da2) {
        assert da1.length == da2.length;
        double[] retArray = new double[da1.length];
        for (int i=0;i<da1.length;i++) {
            retArray[i] = da1[i] - da2[i];
        }
        return retArray;
    }
    
    public static final double[] multiply(double[] da1, double[] da2) {
        assert da1.length == da2.length;
        double[] retArray = new double[da1.length];
        for (int i=0;i<da1.length;i++) {
            retArray[i] = da1[i] * da2[i];
        }
        return retArray;
    }

    public static final double[] multiply(double[] da1, double val) {
        double[] retArray = new double[da1.length];
        for (int i=0;i<da1.length;i++) {
            retArray[i] = da1[i] * val;
        }
        return retArray;
    }

    public static final double[] divide(double[] da1, double[] da2) {
        assert da1.length == da2.length;
        double[] retArray = new double[da1.length];
        for (int i=0;i<da1.length;i++) {
            retArray[i] = da1[i] / da2[i];
        }
        return retArray;
    }
    
    /**
     * Returns true if all elements of da are less than val.
     * @param da
     * @param val
     * @return true if all elements of da are less than val.
     */
    public static final boolean lessThan(double [] da, double val) {
        boolean allGood = true;
        for (int i=0;i<da.length;i++) {
            allGood = allGood & da[i] < val;
        }
        return allGood;
    }
    
    /**
     * Returns a Set of all da indexes i, where da[i] &lt; val
     * @param da
     * @param val
     * @return
     */
    public static final Set<Integer> lessThanSet(double [] da, double val) {
        Set<Integer> retSet = new HashSet<>();
        for (int i=0;i<da.length;i++) {
            if (da[i] < val) {
                retSet.add(i);
            }
        }
        return retSet;
    }

    public static final boolean moreThan(double [] da, double val) {
        boolean allGood = true;
        for (int i=0;i<da.length;i++) {
            allGood = allGood & da[i] > val;
        }
        return allGood;
    }

    public static final Set<Integer> moreThanSet(double [] da, double val) {
        Set<Integer> retSet = new HashSet<>();
        for (int i=0;i<da.length;i++) {
            if (da[i] > val) {
                retSet.add(i);
            }
        }
        return retSet;
    }
    
    /**
     * Will return -1 if the set is empty
     * @param da
     * @return
     */
    public static int getMaxIndex(double[] da) {
        int index = -1;
        if (da != null && da.length > 0) {
            double curMax = da[0];
            index = 0;
            if (da.length > 1) {
                for (int i=0;i<da.length;i++) {
                    if (da[i] > curMax) {
                        curMax = da[i];
                        index = i;
                    }
                }
            }
        }
        return index;
    }

    
    private ArrayMath() {
        throw new AssertionError("Please don't instantiate this class!");
    }
}
