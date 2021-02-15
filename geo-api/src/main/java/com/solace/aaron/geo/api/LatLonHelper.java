package com.solace.aaron.geo.api;

/**
 * This utility class is used to 
 * @author AaronLee
 *
 */
public class LatLonHelper {

    
    public static final double METRES_PER_DEGREE = 111319.9;
    
    /*
     * used for circle calculations -- figures out how high and wide in lat/lon this circle is
     * which would change at different latitudes... the further towards the poles you go, the more longitude degrees you
     * need to make x metres
     * @param centerLat
     * @param centerLon
     * @param radiusMetres
     * @return returns a array with length=2 of doubles, where [0]=width (lat) in decimal degrees, and [1]=height (lon) in decimal degrees
     */
/*    public static double[] getLatLonCircleDimensions(double centerLat, double centerLon, double radiusMetres) {
        //using a spherical earth, probably good enough for smallish distances
        double latOffset = convertMetresToDecimalDegree(radiusMetres);
        double lonOffset = latOffset / Math.cos(centerLat*Math.PI/180.0);
        return new double[] {2*latOffset,2*lonOffset};
    }
  */  
    /**
     * 
     * 
     * https://en.wikipedia.org/wiki/Geographic_coordinate_system
     * https://en.wikipedia.org/wiki/Latitude#Length_of_a_degree_of_latitude
     * https://en.wikipedia.org/wiki/Longitude#Length_of_a_degree_of_longitude
     */
    public static double[] getLatLonCircleDimensions2(double centerLat, double radiusMetres) {
        double latRadians = centerLat * Math.PI / 180;

        double metersPerDegreeLon = (111_412.84 * Math.cos(latRadians)) - (93.5 * Math.cos(3 * latRadians))+ (0.118 * Math.cos(5 * latRadians));
        double lonOffset = radiusMetres / metersPerDegreeLon;

        double metersPerDegreeLat = 111_132.954 - (559.822 * Math.cos(2 * latRadians)) + (1.175 * Math.cos(4 * latRadians)) - (0.0023 * Math.cos(6 * latRadians));
        double latOffset = radiusMetres / metersPerDegreeLat;

        return new double[] {2*latOffset,2*lonOffset};
    }

    public static double convertMetresToDecimalDegree(double metres) {
        return metres / METRES_PER_DEGREE;
    }

    public static double convertDecimalDegreeToMetres(double degrees) {
        return degrees * METRES_PER_DEGREE;
    }
}
