package com.solace.aaron.rnrf;

public class LatLonHelper {

	
	public static final double METRES_PER_DEGREE = 111319.9;
	
    /**
     * used for circle calculations -- figures out how high and wide in lat/lon this circle is
     * which would change at different latitudes... the further towards the poles you go, the more longitude degrees you
     * need to make x metres
     * @param centerLat
     * @param centerLon
     * @param radiusMetres
     * @return returns a array with length=2 of doubles, where [0]=width (lat) in decimal degrees, and [1]=height (lon) in decimal degrees
     */
	public static double[] getLatLonCircleDimensions(double centerLat, double centerLon, double radiusMetres) {
        double latOffset = convertMetresToDd(radiusMetres);
        double lonOffset = latOffset / Math.cos(centerLat*Math.PI/180.0);
        return new double[] {2*latOffset,2*lonOffset};
    }
	
	public static double convertMetresToDd(double metres) {
		return metres / METRES_PER_DEGREE;
	}
	
	public static double convertDdToMetres(double degrees) {
		return degrees * METRES_PER_DEGREE;
	}
}
