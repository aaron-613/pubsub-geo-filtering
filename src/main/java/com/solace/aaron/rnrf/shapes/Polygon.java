package com.solace.aaron.rnrf.shapes;


public class Polygon {

	/*
	 *         		if (shapes.containsKey("polys")) {
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
        				GeometricShapeFactory gf = new GeometricShapeFactory();
        				double x = circle.getJsonArray("coords").getJsonNumber(0).doubleValue();
        				double y = circle.getJsonArray("coords").getJsonNumber(1).doubleValue();
        				double radius = circle.getJsonNumber("radius").doubleValue();
        				gf.setCentre(new Coordinate(x,y));
        				if (circle.containsKey("modifier") && circle.getJsonString("modifier").getString().equals("metresToLatLon")) {
                            gf.setWidth(getLatLonDimensions(x,y,radius)[0]);  // lat offset, even though it is width, b/c order matters
                            gf.setHeight(getLatLonDimensions(x,y,radius)[1]);
        				} else {
                            gf.setWidth(radius*2);  // I guess?  We haven't tested this yet.
                            gf.setHeight(radius*2);
        				}
                        gf.setNumPoints(72);
                        Geometry c = gf.createCircle();
                        target = target.union(c);
        			}
        		}

	 */
	
	
}
