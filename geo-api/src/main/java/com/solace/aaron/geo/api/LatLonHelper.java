package com.solace.aaron.geo.api;

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
        double latOffset = convertMetresToDecimalDegree(radiusMetres);
        double lonOffset = latOffset / Math.cos(centerLat*Math.PI/180.0);
        return new double[] {2*latOffset,2*lonOffset};
    }
    
    /**
     * Bertter more complicated way to do this:
     * 
     * https://en.wikipedia.org/wiki/Geographic_coordinate_system
     * 
     * On the WGS84 spheroid, the length in meters of a degree of latitude at latitude phi (that is, the distance along a north-south line from latitude (phi - 0.5) degrees to (phi + 0.5) degrees) is about

{\displaystyle 111132.92-559.82\, &cosine; 2\varphi +1.175\,\cos 4\varphi -0.0023\,\cos 6\varphi } 111132.92-559.82\,\cos 2\varphi +1.175\,\cos 4\varphi -0.0023\,\cos 6\varphi [14]
Similarly, the length in meters of a degree of longitude can be calculated as

{\displaystyle 111412.84\,\cos \varphi -93.5\,\cos 3\varphi +0.118\,\cos 5\varphi } {\displaystyle 111412.84\,\cos \varphi -93.5\,\cos 3\varphi +0.118\,\cos 5\varphi }[14]


a better approximation of a longitudinal degree at latitude {\displaystyle \textstyle {\varphi }\,\!} {\displaystyle \textstyle {\varphi }\,\!} is

{\displaystyle {\frac {\pi }{180}}a\cos \beta \,\!} {\frac {\pi }{180}}a\cos \beta \,\!
where Earth's equatorial radius {\displaystyle a} a equals 6,378,137 m and {\displaystyle \textstyle {\tan \beta ={\frac {b}{a}}\tan \varphi }\,\!} {\displaystyle \textstyle {\tan \beta ={\frac {b}{a}}\tan \varphi }\,\!}; for the GRS80 and WGS84 spheroids, b/a calculates to be 0.99664719.

     */
    static double[] getLatLonCircleDimensions2(double centerLat, double centerLon, double radiusMetres) {
        //  better approximation of a longitudinal degree at latitude phi is Pi/180 * a cos(beta)
        //where Earth's equatorial radius a equals 6,378,137 m
        // and \tan \beta ==  b/a*tan(phi)
        // for the GRS80 and WGS84 spheroids, b/a calculates to be 0.99664719
        //long a = 6_378_137;
        //double b_a = 0.99664719;
        
        
        double latOffset = convertMetresToDecimalDegree(radiusMetres);
        double lonOffset = latOffset / Math.cos(centerLat*Math.PI/180.0);
        return new double[] {2*latOffset,2*lonOffset};
    }
    
    
    
    public static double convertMetresToDecimalDegree(double metres) {
        return metres / METRES_PER_DEGREE;
    }
    
    public static double convertDecimalDegreeToMetres(double degrees) {
        return degrees * METRES_PER_DEGREE;
    }
}
