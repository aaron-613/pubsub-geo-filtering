package com.solace.aaron.geo.api;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.util.GeometricShapeFactory;

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
    /*
     * used for circle calculations -- figures out how high and wide in lat/lon this circle is
     * which would change at different latitudes... the further towards the poles you go, the more longitude degrees you
     * need to make x metres
     * @param centerLat
     * @param radiusMetres
     * @return returns a array with length=2 of doubles, where [0]=x width (lon) in decimal degrees, and [1]=height (lat) in decimal degrees
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

    public static Geometry buildLatLonCircleGeometry(double lat, double lon, double radiusMetres) {
        GeometricShapeFactory shapeFactory = new GeometricShapeFactory(Geo2dSearchEngine.GEOMETRY_FACTORY);
        shapeFactory.setCentre(new Coordinate(lon,lat));
        shapeFactory.setWidth(getLatLonCircleDimensions2(lon,radiusMetres)[0]);  // lat offset, even though it is width, b/c order matters
        shapeFactory.setHeight(getLatLonCircleDimensions2(lon,radiusMetres)[1]);
        shapeFactory.setNumPoints(72);
        return shapeFactory.createCircle();
    }

    public static Geometry buildCircleGeometry(double x, double y, double radius) {
        GeometricShapeFactory shapeFactory = new GeometricShapeFactory(Geo2dSearchEngine.GEOMETRY_FACTORY);
        shapeFactory.setCentre(new Coordinate(x,y));
        shapeFactory.setWidth(radius*2);  // I guess?  We haven't tested this yet.
        shapeFactory.setHeight(radius*2);
        shapeFactory.setNumPoints(72);
        return shapeFactory.createCircle();
    }

}
