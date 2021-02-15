package com.solace.aaron.geo.submgr;

import javax.json.JsonArray;
import javax.json.JsonObject;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.util.GeometricShapeFactory;

import com.solace.aaron.geo.api.LatLonHelper;

public class Search {

    /** The minimum number of subscriptions that may be requested */
    public static final int MIN_SUBS = 4;
    
    /** The maximum number of subscriptions that may be requested */
    public static final int MAX_SUBS = 2000;
    
    /** Minimum accuracy of 10%. */
    public static final double MIN_ACC = 0.10;
    
    /** Maximum accuracy of 99% */
    public static final double MAX_ACC = 0.99;
    
    Geometry target = new GeometryFactory().createMultiPolygon(null);
    double accuracy = 0.8;  // default
    int numSubs = 200;  // default
    int radix = 10;  // default
    String topicPrefix = "geo"+radix+"/*/";
    String topicSuffix = "/>";
    
    public Search(JsonObject request) throws IllegalArgumentException {
        if (request.containsKey("accuracy")) {
            try {
                double acc = request.getJsonNumber("accuracy").doubleValue();
                if (acc >= MIN_ACC && acc <= MAX_ACC) this.accuracy = acc;
            } catch (NumberFormatException e) {
                System.out.printf("That didn't work for accuracy '%s': %s%n",request.get("accuracy"),e);
               
            } catch (ClassCastException e) {
                System.out.printf("That didn't work for accuracy '%s': %s%n",request.get("accuracy"),e);
            }
        }
        if (request.containsKey("numSubs")) {
            try {
                 int num = request.getJsonNumber("numSubs").intValue();
                if (num >= MIN_SUBS && num <= MAX_SUBS) this.numSubs = num;
            } catch (NumberFormatException e) {
                System.out.printf("That didn't work for numSubs '%s': %s%n",request.get("numSubs"),e);
            } catch (ClassCastException e) {
                System.out.printf("That didn't work for numSubs '%s': %s%n",request.get("numSubs"),e);
            }
        }
        if (request.containsKey("radix")) {
            radix = request.getJsonNumber("radix").intValue();
            topicPrefix = "geo"+radix+"/*/";
        }
        if (request.containsKey("topicPrefix")) {
            topicPrefix = request.getJsonString("topicPrefix").getString();
        }
        if (request.containsKey("topicSuffix")) {
            topicSuffix = request.getJsonString("topicSuffix").getString();
        }
        if (request.containsKey("shapes")) {
            JsonObject shapes = request.getJsonObject("shapes");
            if (shapes.containsKey("polys")) {
                JsonArray polys = shapes.getJsonArray("polys");
                for (int i=0;i<polys.size();i++) {
                    Polygon poly = new GeometryFactory().createPolygon(getCoords(polys.getJsonObject(i).getJsonArray("coords")));
                    target = target.union(poly.isSimple() ? poly : poly.convexHull());
                }
            }
            if (shapes.containsKey("circles")) {
                JsonArray circles = shapes.getJsonArray("circles");
                for (int i=0;i<circles.size();i++) {
                    JsonObject circle = circles.getJsonObject(i);
                    GeometricShapeFactory factory = new GeometricShapeFactory();
                    double x = circle.getJsonArray("coords").getJsonNumber(0).doubleValue();
                    double y = circle.getJsonArray("coords").getJsonNumber(1).doubleValue();
                    double radius = circle.getJsonNumber("radius").doubleValue();
                    factory.setCentre(new Coordinate(x,y));
                    if (circle.containsKey("modifier") && circle.getJsonString("modifier").getString().equals("metresToLatLon")) {
                        factory.setWidth(LatLonHelper.getLatLonCircleDimensions2(x,radius)[0]);  // lat offset, even though it is width, b/c order matters
                        factory.setHeight(LatLonHelper.getLatLonCircleDimensions2(x,radius)[1]);
                    } else {
                        factory.setWidth(radius*2);  // I guess?  We haven't tested this yet.
                        factory.setHeight(radius*2);
                    }
                    factory.setNumPoints(72);
                    Geometry c = factory.createCircle();
                    target = target.union(c);
                }
            }
        }
    }
    
    private static Coordinate[] getCoords(JsonArray coords) {
        Coordinate[] ret = new Coordinate[coords.size()+1];
        for (int i=0;i<coords.size();i++) {
            JsonArray pair = coords.getJsonArray(i);
            ret[i] = new Coordinate(pair.getJsonNumber(0).doubleValue(), pair.getJsonNumber(1).doubleValue());
        }
        ret[ret.length-1] = new Coordinate(ret[0]);  // close the shape
        return ret;
    }

}
