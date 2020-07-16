package com.solace.aaron.geo.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.GeometryFilter;
import org.locationtech.jts.geom.Polygon;

/**
 * This class is a generalization of the original decimal-only (base 10) ranged filtering algorithm.
 * @author Aaron Lee
 *
 */
public class Geo2dSearch {

    private static final double CUT_OFF_COVERAGE_RATIO = 0.9995;  // no point in splitting if above this. don't use 1.0 in case of float round error

    private static final Logger logger = LogManager.getLogger(Geo2dSearch.class);
    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    private final RadixGridRatioComparator SORT_COMPARATOR = new RadixGridRatioComparator();
    
    private final Geo2dSearchEngine engine;  // the Engine we're using, preconfigured with the radix, dimensions, scale, etc.
    private final Geometry[] targets;        // an array of targets that we are using
    private final RadixGrid rootNode;        // the absolute root node of the whole RadixGrid search, which will be split
    private int curNumberSubs;               // running total for the current number of subscriptions, rather than recalculating recursively each loop

    final double[] targetAreas;  // immutable total area of each of the target areas
    double[] curAreas;        // actual current area of each of 
//    double[] curAreas2;
    double[] overAreas;       // total area of the subs outside the region of the target (i.e. subs are sticking out over the target)
    double[] overRatios;      // ratio/percentage of area outside the target
    double[] underAreas;      // total area where subs are "inside" the target area (i.e. not covered)
    double[] underRatios;     // ratio of underArea vs. targetArea
    double[] combinedRatios;  // a combination of overRatios and underRatios.  underRatios are given a x2 multiplier to decrease the amount the subs can be "under" the target.
    int worstIndex;           // which target is the "worst", has the worst combinedRatio. Recalculated after each loop.
    
    public Geo2dSearch(Geo2dSearchEngine engine, Geometry[] targets) {
        this.engine = engine;
        this.targets = targets;
        
        // pre-processing
        for (int i=0;i<targets.length-1;i++) {
            for (int j=i+1;j<targets.length;j++) {
                Geometry intersection = targets[i].intersection(targets[j]);
                if (intersection.getArea() > 0) {
                    System.err.println(intersection);
                    targets[i] = targets[i].difference(targets[j]);
//                    throw new AssertionError("input target shapes are overlapping. can only be touching at vertices/edges.");
                }
            }
            for (Coordinate coord : targets[i].getCoordinates()) {
                // this will check if anything is outside our bounds defined by the range of the formatter
                engine.getXStringFormatter().convert(coord.x);
                engine.getYStringFormatter().convert(coord.y);
//                logger.debug("Coord X: {} --> '{}'",coord.x,engine.getXStringFormatter().convert(coord.x));
//                logger.debug("Coord Y: {} --> '{}'",coord.y,engine.getYStringFormatter().convert(coord.y));
            }
        }
        
        // start with the "global" grid
        RadixGrid startNode = new RadixGrid(targets,
                new Range(engine.getXStringFormatter()),
                new Range(engine.getYStringFormatter())
//                engine.getXFixedScale()-engine.getXFixedWidth(),
//                engine.getYFixedScale()-engine.getYFixedWidth()
                );
        startNode.buildChildren();
        while (startNode.children.size() == 1) {
            startNode = startNode.split().get(0);
        }
        startNode.parent = null;  // force this guy to be the root
        this.rootNode = startNode;
        curNumberSubs = 1;
        targetAreas = new double[targets.length];
        for (int i=0;i<targets.length;i++) {
            targetAreas[i] = targets[i].getArea();
        }
        logger.info("Starting with {}",rootNode.toString());
        System.out.println(Arrays.toString(targetAreas));
        curAreas = startNode.getActualAreaRecursive(DepthOfCalc.ACTUAL);
//        curAreas2 = startNode.getActualAreaRecursive(DepthOfCalc.ACTUAL);
        overAreas = startNode.getOverCoverageAreas(Depth.SINGLE);
        underAreas = startNode.getUnderCoverageAreas(Depth.SINGLE);
        crunchRatios();
    }
    
    private void crunchRatios() {
        overRatios = ArrayMath.divide(overAreas,targetAreas);
        underRatios = ArrayMath.divide(underAreas,targetAreas);
        combinedRatios = ArrayMath.add(overRatios,ArrayMath.multiply(underRatios,2));
        worstIndex = ArrayMath.getMaxIndex(combinedRatios);
    }
    
    private boolean doesWorstTargetTouch(RadixGrid grid) {
        int worstTargetIndex = ArrayMath.getMaxIndex(combinedRatios);
        for (com.solace.aaron.geo.api.Geo2dSearch.RadixGrid.TargetObject t : grid.sortedTargets) {
            if (t.index == worstTargetIndex) return true;
        }
        return false;
    }
    
    public Geo2dSearchResult splitToRatio(final double completionRatio, final int maxSubs) {
        /** The list of all grids/squares that are considered for splitting.  This will be sorted each iteration, and the top one will be split. **/
        int loop = 0;
        Set<RadixGrid> orderedSplitGrids = new LinkedHashSet<>();  // the order of how we split things
        LinkedList<RadixGrid> gridList = new LinkedList<>();  // an ordered of contenders to split
        gridList.add(rootNode);  // add the one and only first "global" grid
        ListIterator<RadixGrid> li;
        long totalTime = System.nanoTime();
        while (!gridList.isEmpty() && ((!ArrayMath.lessThan(combinedRatios,1-completionRatio) && curNumberSubs < maxSubs))) {
            loop++;
            logger.debug("============= Loop {} ==========",loop);
            logger.debug("Should be splitting the first grid for Target {}",worstIndex);
            RadixGrid gridToSplit = null;
            li = gridList.listIterator();
            // we only want to split a grid for the target that is currently has the worst ratio
            while (li.hasNext() && gridToSplit == null) {  // while the list has more elements, or the grid hasn't been chosen yet
                RadixGrid grid = li.next();
                if (doesWorstTargetTouch(grid)) {  // for the worst-ratio target, does this grid touch it?
                    // if so, remove it, and let's split it!
                    li.remove();
                    gridToSplit = grid;
                } else if (grid.children.size()==1) {  // or maybe split this guy since he has exactly 1 child, and so #subs == same
                    if (!grid.parent.hasSplit) {  // but the parent hasn't split yet (indicating parent has #radix children)
                        // ignore it!
                    } else {
                        // parent has split, so this is a free split (replacing 'this' with single child)
                        li.remove();
                        gridToSplit = grid;
                    }
                }
            }
            if (gridToSplit == null) {
                gridToSplit = gridList.removeFirst();
            }
            if (loop == 15) {
                System.out.print("");
                //break;
            }
            if (gridToSplit.xRange.getInner() == 1 && gridToSplit.yRange.getInner() == 0) {
                System.out.print("");
            }
            if (gridToSplit.done) {
                logger.fatal("Throwing away, has already been processed: {}",gridToSplit);
                break;
//                continue;
            }
            logger.debug("SPLITTING: {}",gridToSplit);
            int numberNewSubs = gridToSplit.getNumChildrenIfSplit();
            if ((curNumberSubs - 1 + numberNewSubs) > maxSubs) {  // -1 because if we split one, we don't count that sub
                logger.debug("Throwing away - too many children: {}",gridToSplit);
                continue;  // throw out this guy, maybe the next one has the right amount of children
            }
            
            List<RadixGrid> newGridsToConsider = gridToSplit.split();  // DO IT!!
            Collections.sort(newGridsToConsider,SORT_COMPARATOR);  // this will be the grandchildren of the gridToSplit
            // this next block is how we build the animation showing the algorithm
            if ("show only when actually split".equals("show only when actually split")) {
                // use this block of code to show the grids splitting only when it actually splits
                if (!gridToSplit.hasSplit) {
                    // don't add it if it hasn't split
                } else {
                    // so we've split for sure
                    Deque<RadixGrid> gridsToAdd = new LinkedList<>();
                    gridsToAdd.add(gridToSplit);
                    // check if this guy's ancestors had been added previously
                    RadixGrid parent = gridToSplit.parent;
                    while (parent != null && !orderedSplitGrids.contains(parent)) {
                        gridsToAdd.addFirst(parent);
                        parent = parent.parent;  // walk up the tree
                    }
                    orderedSplitGrids.addAll(gridsToAdd);
                }
            } else {  // this sticks it in, event if it hasn't technically split yet
                orderedSplitGrids.add(gridToSplit);
            }
            
            curAreas[gridToSplit.getBiggestIntersectedTarget()] -= gridToSplit.getGridArea();
            double[] areaDeltas = gridToSplit.getGridAreaDelta();
            curAreas = ArrayMath.add(curAreas,areaDeltas);
            //curAreas2 = rootNode.getActualAreaRecursive(DepthOfCalc.ACTUAL);  // as a test
//            logger.debug("CurArea deltas: "+Arrays.toString(ArrayMath.subtract(curAreas2,curAreas)));
            overAreas = ArrayMath.add(overAreas,gridToSplit.getOverCoverageAreas(Depth.RECURSIVE));
            underAreas = ArrayMath.add(underAreas,gridToSplit.getUnderCoverageAreas(Depth.RECURSIVE));
            crunchRatios();

            logger.debug("OverRatio: "+Arrays.toString(overRatios));
            logger.debug("UnderRatio: "+Arrays.toString(underRatios));
            logger.debug("CombinedRatio: "+Arrays.toString(combinedRatios));
            //System.out.println("Targets still not in range: "+ArrayMath.moreThanSet(combinedRatios,COMBINED_RATIO));

            if (gridToSplit.hasSplit) {
                curNumberSubs += numberNewSubs-1;  // only change the sub count once we've actually split else add the actual number of subs, less 1 since we removed the guy we're splitting
            }
            // this section of code is to insert the new grids to consider into the main list in proper order
            // we don't use Collections.sort() anymore
            int leadGuy = 0;  // this will be the index of something
            li = gridList.listIterator();  // all the grids to consider
            while (li.hasNext() && leadGuy < newGridsToConsider.size()) {  // while there's still more and 
                RadixGrid next = li.next();  // grab the next guy
                li.previous();  // back up the iterator
                while (leadGuy < newGridsToConsider.size() &&
                        SORT_COMPARATOR.buildIntersectionRatio(newGridsToConsider.get(leadGuy)) > SORT_COMPARATOR.buildIntersectionRatio(next)) {
                    li.add(newGridsToConsider.get(leadGuy++));
                }
                li.next();
            }
            // finally, stick the leftover guys at the back of the list
            gridList.addAll(newGridsToConsider.subList(leadGuy,newGridsToConsider.size()));
        }
        logger.info("$$$$ TOTAL TIME: {}ms, LOOPS: {}",(System.nanoTime()-totalTime)/1000000,loop);
        int tot = 0;
        for (int i=0;i<targets.length;i++) {
            logger.info("Total number of subs {}: {}",i,rootNode.getSubs().get(i).size());
            tot += rootNode.getSubs().get(i).size();
        }
        logger.info("TOTAL number of subs: {}",tot);
        logger.info(" (and by my count: {})",curNumberSubs);
//        try {
//            for (int i=0;i<targets.length;i++) {
        //                logger.info("outside ratio {}  {}",i,(rootNode.getAreaOutside()[i]/targets[i].getArea()));
//                logger.info("coverage ratio {} {}",i,targets[i].getArea()/rootNode.getActualAreaRecursive(DepthOfCalc.ACTUAL)[i]);
//            }
//        } catch (RuntimeException e) {
//            logger.warn(e.toString());
//        }
        
        if (rootNode.getUnion() instanceof GeometryCollection) {
            throw new AssertionError("Union is not a single object");
        }
        logger.info("OverRatio: "+Arrays.toString(overRatios));
        logger.info("UnderRatio: "+Arrays.toString(underRatios));
        logger.info("Combined: "+Arrays.toString(combinedRatios));
        double avg = 0;
        for (double ratio : combinedRatios) {
            avg += ratio;
        }
        avg /= combinedRatios.length;
        logger.info("Average Combined: "+(Math.floor((1-avg)*10000)/100)+"%");
//        logger.info("CurArea deltas: "+Arrays.toString(ArrayMath.subtract(curAreas2,curAreas)));
//        logger.info("For target #4: {}",(rootNode.getUnion().get(3).getArea() - targetAreas[3]) / targetAreas[3]);
        logger.info(rootNode.getSubs());
        
        return new Geo2dSearchResult(targets,rootNode,new ArrayList<RadixGrid>(orderedSplitGrids));
    }

    public static List<Double> getCurrentCoverageRatio(Geometry target, RadixGrid rootNode) {
        // I know, I know!  Mixing arrays and Lists.  Trying to use arrays in my inner class so it's faster
        double[] areas = rootNode.getActualAreaRecursive(DepthOfCalc.ACTUAL);
        List<Double> ratios = new ArrayList<>(areas.length);
        for (int i=0;i<areas.length;i++) {
            ratios.add(rootNode.trimmedTargets[i].getArea() / areas[i]);
        }
        return ratios;
    }
    
//    private static double getActualArea(RadixGrid rootNode) {
//        return rootNode.getActualAreaRecursive(DepthOfCalc.ACTUAL);
//    }

    
    /**
     * This is used by helper functions below to determine if a method should recurse
     */
    enum DepthOfCalc {
        ACTUAL,
        CHILDREN,
    }
    
    enum Depth {
        RECURSIVE,
        SINGLE,
    }

    private enum StripeDirection {
        /** Horizontal slices **/
        HORIZONTAL("Horiz slices"),
        /** Vertical slices **/
        VERTICAL("Vert slices"),
        TBD("TBD");

        final String text;
        
        StripeDirection(String text) {
            this.text = text;
        }
        
        @Override
        public String toString() {
            return text;
        }
    }
    
    /* HELPER FUNCTIONS ******************************************************************************/

/*    static String[] staticBuildGridCoords(double innerX, double innerY, int radix, int xFactor, int yFactor, Quadrant quadrant) {
        double x1 = innerX;  // doesn't matter which 2 corners of square, as long as they are antipodal
        double y1 = innerY;
        double x2 = innerX+(RadixUtils.lookupInverseFactors(radix,xFactor)*quadrant.xNegativeModifier);
        double y2 = innerY+(RadixUtils.lookupInverseFactors(radix,yFactor)*quadrant.yNegativeModifier);
        String[] coords2 = {
                Double.toString(Math.min(x1,x2)),
                Double.toString(Math.min(y1,y2)),
                Double.toString(Math.max(x1,x2)),
                Double.toString(Math.max(y1,y2))
        };
        return coords2;
    }

    // make a static version for easier testing... instance version just calls this
    static Polygon staticBuildGridPolygon(double innerX, double innerY, int radix, int xFactor, int yFactor, Quadrant quadrant, boolean slightlyInflate) {
        double xStep = RadixUtils.lookupInverseFactors(radix,xFactor);  // based on the factor (depth), how far to the outside corner of the box
        double yStep = RadixUtils.lookupInverseFactors(radix,yFactor);
        double inflation = slightlyInflate ? PADDING_AMOUNT * xStep : 0;
        double outerX = innerX + ((xStep+inflation)*quadrant.xNegativeModifier);  // just stretch it a bit with 'inflation' so it overlaps properly for the union
        double outerY = innerY + ((yStep+inflation)*quadrant.yNegativeModifier);
        Coordinate[] square = new Coordinate[5];
        square[0] = new Coordinate(innerX,innerY);
        square[1] = new Coordinate(innerX,outerY);
        square[2] = new Coordinate(outerX,outerY);
        square[3] = new Coordinate(outerX,innerY);
        square[4] = new Coordinate(innerX,innerY);  // repeat coord 0
        return GEOMETRY_FACTORY.createPolygon(square);
    }
*/    
    /** Will return true for more vertical shapes */
    static StripeDirection staticWhichWayToSplit(Geometry intersectedTarget, Geometry square) {
        // what is the shape of this thing?
        Coordinate[] bbox = intersectedTarget.getEnvelope().getCoordinates();
        double width = Math.abs(bbox[0].x - bbox[2].x);
        double height = Math.abs(bbox[0].y - bbox[2].y);
        // is it taller or wider?  since doubles, won't be exactly equal, so check if within an epsilon
        double relError;
        if (Math.abs(height) > Math.abs(width)) {
            relError = Math.abs((height-width)/height);
        } else {
            relError = Math.abs((height-width)/width);
        }
        if (relError < 0.0001) {  // won't be calling this unless the current grid is square, so the factors don't matter
            // means this box is square, so could be an L shape, or a solid box with a diagonal slice.  Calculate the centroid
            Coordinate centroid = intersectedTarget.getCentroid().getCoordinate();
            // is it closer to the left/right, or top/bottom?
            double xDistance = Math.min(Math.abs(square.getCoordinates()[2].x-centroid.x),Math.abs(square.getCoordinates()[0].x-centroid.x));
            double yDistance = Math.min(Math.abs(square.getCoordinates()[2].y-centroid.y),Math.abs(square.getCoordinates()[0].y-centroid.y));
            // if closer to the top/bottom (i.e. yDistance is smaller) then we should split horizontally
//            return yDistance < xDistance;
            if (yDistance < xDistance) return StripeDirection.HORIZONTAL;
            else return StripeDirection.VERTICAL;
        } else {
            // so it's not square
            if (height > width) return StripeDirection.VERTICAL;
            else return StripeDirection.HORIZONTAL;
//            if (height > width) return false;  // taller, so vertical shape, so split vertically
//            else return true;  // wider, so split horiz
        }
    }
    
    /**
     * This internal helper class is used to sort the various grids to determine which should be
     * split next.  There are a number of different ways to sort/weight the grid squares, and
     * by varying the algorithm it will change which grids will be split first.
     * @author Aaron Lee
     */
    class RadixGridRatioComparator implements Comparator<RadixGrid> {
        
        public double buildIntersectionRatio(Geo2dSearch.RadixGrid grid) {
            return ((1-grid.staticCoverageRatio) * grid.getGridArea() * Math.pow(engine.getRadix(),Math.abs(grid.getXFactor()-grid.getYFactor())/2)) / (grid.getNumChildrenIfSplit()-1);  // so if num children == 1, makes it positive infinity... forces spitting
//            return ((1-grid.staticCoverageRatio) * grid.getGridArea() * Math.pow(engine.getRadix(),Math.abs(grid.xFactor-grid.yFactor)/2)) / (grid.getNumChildrenIfSplit()-1);  // so if num children == 1, makes it positive infinity... forces spitting
//            return (1-grid.staticCoverageRatio) * grid.getGridArea() / (grid.getNumChildrenIfSplit()-1);  // so if num children == 1, makes it positive infinity... forces spitting
//            return (1-grid.staticCoverageRatio) * grid.getGridArea() / (grid.getNumChildren()-1);  // so if num children == 1, makes it positive infinity... forces spitting
            //double ret =  ((1-grid.staticCoverageRatio) * grid.getGridArea() / (grid.getNumChildrenIfSplit()-1)) * (grid.getBiggestIntersectedTarget() == ArrayMath.getMaxIndex(combinedRatios) ? 1000 : 0);  // so if num children == 1, makes it positive infinity... forces spitting
            //return ((1-grid.staticCoverageRatio) * grid.getGridArea() / (grid.getNumChildrenIfSplit()-1)) * (doesWorstTargetTouch(grid) ? 1000 : 0);  // so if num children == 1, makes it positive infinity... forces spitting
        }

        @Override
        public int compare(Geo2dSearch.RadixGrid a, Geo2dSearch.RadixGrid b) {
            double ar = buildIntersectionRatio(a);
            double br = buildIntersectionRatio(b);
            if (ar > br) return -1;
            else if (ar < br) return 1;
            else return 0;
        }
    }
    ////////////////////////////////////////////////////////


    class RadixGrid {

        /**
         * Helper function to put the whole subscription string together, including wildcard chars, but not trailing '/' char
         */ // e.g. "-35.12*/_93.381*"
        private String buildTopicSubscription() {
            StringBuilder sb = new StringBuilder();
//            sb.append(xStringFormatter.convert(innerX,quadrant.xNegativeModifier<0,xFixedScale-xFactor)).append("*/");
//            sb.append(yStringFormatter.convert(innerY,quadrant.yNegativeModifier<0,engine.getyFixedScale()-yFactor)).append('*');
            sb.append(xRange.getVal()).append("*/").append(yRange.getVal()).append("*");
            return sb.toString();
        }

        
        
        /**
         * This is only used to draw the corresponding boxes on the map dashboard
         * @return
         */
        Rect buildGridCoords() {
            Rect grid = new Rect(Math.min(xRange.getInner(),xRange.getOuter()),Math.min(yRange.getInner(),yRange.getOuter()),Math.max(xRange.getInner(),xRange.getOuter()),Math.max(yRange.getInner(),yRange.getOuter()));
            return grid;
        }
        
        Polygon buildGridPolygon() {
            Coordinate[] square = new Coordinate[5];
            square[0] = new Coordinate(xRange.getInner(),yRange.getInner());
            square[1] = new Coordinate(xRange.getInner(),yRange.getOuter());
            square[2] = new Coordinate(xRange.getOuter(),yRange.getOuter());
            square[3] = new Coordinate(xRange.getOuter(),yRange.getInner());
            square[4] = new Coordinate(xRange.getInner(),yRange.getInner());  // repeat coord 0
            return GEOMETRY_FACTORY.createPolygon(square);
        }
        
        // VARIABLES //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        private RadixGrid parent = null;
        private final Geometry[] trimmedTargets = new Geometry[Geo2dSearch.this.targets.length];
        private final Range xRange;
        private final Range yRange;
//        private final int xFactor; // this is essentially the depth... level = 0 -> no decimal places, lev 1...
//        private final int yFactor;
        private final Polygon gridPolygon;
        private final double staticCoverageRatio;  // this is the coverage ratio of this square, regardless of children/splits
        private StripeDirection stripeDirection = StripeDirection.TBD;
        private boolean hasSplit = false;  // if there are 10 (radix) whole children, no point in splitting right there as coverage ratio won't change
        private boolean done = false;
        List<RadixGrid> children = null;
        private List<TargetObject> sortedTargets = new ArrayList<>();


        private RadixGrid(RadixGrid parent, Range xRange, Range yRange) { //, int xFactor, int yFactor) {
            this(parent.trimmedTargets,xRange,yRange);//,xFactor,yFactor);
            this.parent = parent;
        }

        // only the outer Search class should call this method
//        private RadixGrid(Geometry[] parentsIntersectedTargets, int xFactor, int yFactor) {
//            this(parentsIntersectedTargets,new "","",xFactor,yFactor);
//        }

        /**
         * This internal class is used specifically for calculating intersected shapes.
         */
        private class TargetObject implements Comparable<TargetObject> {
            
//            private final Geometry trimmedTarget;
            private int index;
            private final double coverageRatio;
            
            private TargetObject(Geometry target, int index, double coverageRatio) {
//                this.trimmedTarget = target;
                this.index = index;
                this.coverageRatio = coverageRatio;
            }

/*            private TargetObject(Geometry parentTarget, int index) {
                this.trimmedTarget = parentTarget.intersection(RadixGrid.this.gridPolygon);
                if (trimmedTarget instanceof GeometryCollection) {
                    System.out.println(trimmedTarget);
                }
                this.index = index;
                this.coverageRatio = trimmedTarget.getArea()/getGridArea();
            }
*/
            @Override
            public int compareTo(TargetObject t) {
                if (this.coverageRatio < t.coverageRatio) return 1;
                else if (this.coverageRatio > t.coverageRatio) return -1;
                else {
                    double thisArea = targets[this.index].getArea();
                    double tArea = targets[t.index].getArea();
                    if (thisArea < tArea) return -1;
                    else if (thisArea > tArea) return 1;
                    else {
                        return this.index < t.index ? 1 : this.index > t.index ? -1 : 0;
                    }
                }
            }
            
            @Override
            public String toString() {
                return String.format("Target %d, %f%%",index,coverageRatio*100);
            }
        }
        
        private Geometry keepOnlyPolygons(Geometry geom) {
            if ("MultiPolygon".equals(geom.getGeometryType())) {
                return geom;
            } else {  // points and lines need to be stripped out
                final List<Polygon> polygons = new LinkedList<Polygon>();
                geom.apply(new GeometryFilter() {
                    @Override
                    public void filter(Geometry geom) {
                        if (geom instanceof Polygon) {
                            polygons.add((Polygon)geom);
                        }
                    }
                });
                if (polygons.size() > 1) {
                    return GEOMETRY_FACTORY.createMultiPolygon(polygons.toArray(new Polygon[0]));
                } else if (polygons.size() == 1) {
                    return polygons.get(0);
                } else {  // must be empty!
                    return GEOMETRY_FACTORY.createPolygon();
                }
            }
        }
        

        /**
         * This constructor should only be used directly by parent GridContainer... everything else internally should use the one that includes 'parent' so it maintains a link
         */
        private RadixGrid(Geometry[] parentsIntersectedTargets, Range xRange, Range yRange) {
            this.xRange = xRange;
            this.yRange = yRange;
            this.gridPolygon = buildGridPolygon();
            // this is for debugging
            if (xRange.getInner() == 2 && yRange.getInner() == 3.75) {
                System.out.print("");
            }
            // first, let's trim all the targets to this new grid
            for (int i=0;i<parentsIntersectedTargets.length;i++) {
                if (parentsIntersectedTargets[i] != null) {
                    Geometry intersectedTarget = parentsIntersectedTargets[i].intersection(this.gridPolygon);
                    intersectedTarget = keepOnlyPolygons(intersectedTarget);  // get rid of any lines or points
                    trimmedTargets[i] = intersectedTarget;
                    double targetCoverageRatio = intersectedTarget.getArea()/getGridArea();
                    // if the trimmed target has a non-trivial intersection, then add it to the list of targets for tracking
                    if (intersectedTarget.getArea() > 0) {
//                        if (targetCoverageRatio > 1E-15) {  // rounding error in geometry lib
                        sortedTargets.add(new TargetObject(intersectedTarget,i,targetCoverageRatio));
                    }
                }
            }
            double computedRatio = 0;
            if (sortedTargets.size() > 0) {  // at least one thing intersects
                Collections.sort(sortedTargets);  // sort by ratio
                computedRatio = sortedTargets.get(0).coverageRatio;  // get the biggest guy
                if (sortedTargets.size() > 1) {  // then, if there is more...
                    for (int i=1;i<sortedTargets.size();i++) {
                        computedRatio -= sortedTargets.get(i).coverageRatio;  // subtract the cov.ratio of all smaller guys
                    }
                }
            }
              this.staticCoverageRatio = computedRatio;
        }
        
        int getBiggestIntersectedTarget() {
            return sortedTargets.get(0).index;
        }
        
        StripeDirection whichWayToSplit() {
            // what is the shape of the biggest target?
            Geometry union = trimmedTargets[getBiggestIntersectedTarget()];
            Coordinate[] bbox = union.getEnvelope().getCoordinates();
            // relative width and height
            double width = Math.abs((bbox[0].x - bbox[2].x) / (xRange.getOuter() - xRange.getInner()));
            double height = Math.abs((bbox[0].y - bbox[2].y) / (yRange.getOuter() - yRange.getInner()));
            // is it taller or wider?  since doubles, won't be exactly equal, so check if within an epsilon
            double relError;
            if (Math.abs(height) > Math.abs(width)) {
                relError = Math.abs((height-width)/height);
            } else {
                relError = Math.abs((height-width)/width);
            }
            if (relError < 0.0001) {  // won't be calling this unless the current grid is square, so the factors don't matter
                // means this box is square, so could be an L shape, or a solid box with a diagonal slice.  Calculate the centroid
                Coordinate centroid = union.getCentroid().getCoordinate();
                // is it closer to the left/right, or top/bottom?
                double xDistance = Math.min(Math.abs(xRange.getOuter()-centroid.x),Math.abs(xRange.getInner()-centroid.x));
                double yDistance = Math.min(Math.abs(yRange.getOuter()-centroid.y),Math.abs(yRange.getInner()-centroid.y));
                // if closer to the top/bottom (i.e. yDistance is smaller) then we should split horizontally
//                return yDistance < xDistance;
                if (yDistance < xDistance) return StripeDirection.HORIZONTAL;
                else return StripeDirection.VERTICAL;
            } else {
                // so it's not square
                if (height > width) return StripeDirection.VERTICAL;
                else return StripeDirection.HORIZONTAL;
//                if (height > width) return false;  // taller, so vertical shape, so split vertically
//                else return true;  // wider, so split horiz
            }
        }

        
        private void buildChildren() {
            if (xRange.getWidth() == engine.getXMaxWidth() && yRange.getWidth() == engine.getYMaxWidth()) {  // can't split any further
                return;  // don't go down further after however many decimal places of accuracy!
            }
            // so both ranges can't be maxed out... is it either though?
            if (xRange.getWidth() == engine.getXMaxWidth()) {
                stripeDirection = StripeDirection.HORIZONTAL;
            } else if (yRange.getWidth() == engine.getYMaxWidth()) {
                stripeDirection = StripeDirection.VERTICAL;
            }
            
            // temp objects... used to potentially split both horizontally and vertically and see how it goes
            List<RadixGrid> vertKids = new ArrayList<>(engine.getRadix());
            double vertCoverageSum = 0;
            int vertFullCoverageKidCount = 0;
            Set<Integer> vertIndexes = new HashSet<>();
            List<RadixGrid> horizKids = new ArrayList<>(engine.getRadix());
            double horizCoverageSum = 0;
            int horizFullCoverageKidCount = 0;
            Set<Integer> horizIndexes = new HashSet<>();
            if (stripeDirection != StripeDirection.HORIZONTAL) {  // so, either TBD (usual) or vertical
                for (Range child : xRange.buildChildren()) {
                    RadixGrid kid = new RadixGrid(this,child,yRange);
                    if (kid.intersects()) {
                        vertKids.add(kid);
                        if (kid.staticCoverageRatio >= CUT_OFF_COVERAGE_RATIO && kid.sortedTargets.size() == 1) {
                            vertFullCoverageKidCount++;
                        }
                        vertIndexes.add(kid.getBiggestIntersectedTarget());
                        vertCoverageSum += kid.staticCoverageRatio;
                    }
                }
            }
            if (stripeDirection != StripeDirection.VERTICAL) {  // either TBD or horizontal
                for (Range child : yRange.buildChildren()) {
                    RadixGrid kid = new RadixGrid(this,xRange,child);
                    if (kid.intersects()) {
                        horizKids.add(kid);
                        if (kid.staticCoverageRatio >= CUT_OFF_COVERAGE_RATIO && kid.sortedTargets.size() == 1) {
                            horizFullCoverageKidCount++;
                        }
                        horizIndexes.add(kid.getBiggestIntersectedTarget());
                        horizCoverageSum += kid.staticCoverageRatio;
                    }
                }
            }
            // now... which way do we split??
            if (stripeDirection == StripeDirection.TBD) {  // haven't decided yet...
                assert vertKids.size() > 0;  // there has to be at least one intersection
                assert horizKids.size() > 0;
                if (RadixUtils.lookupInverseFactors(engine.getRadix(),Math.abs(getXFactor()-getYFactor())) >= 0.02) {  // NEW i.e. not too skinny
                    if (vertKids.size() == horizKids.size()) {  // dang, same number of kids
                        if (vertFullCoverageKidCount == horizFullCoverageKidCount) {  // double dang, same number of full rows/cols
//                        if (RadixUtils.lookupInverseFactors(engine.getRadix(),Math.abs(xFactor-yFactor)) >= 0.1) {  // NEW i.e. not too skinny
                            if (Math.abs(horizCoverageSum - vertCoverageSum) < 1E-6) {  // rounding error, do we get better coverage one way?
                                if (vertIndexes.size() == horizIndexes.size()) {  // is one direction have a better split between multiple targets?
                                    stripeDirection = whichWayToSplit();
                                } else {
                                    stripeDirection = vertIndexes.size() > horizIndexes.size() ? StripeDirection.HORIZONTAL : StripeDirection.VERTICAL;
                                }
                            } else {
                                stripeDirection = horizCoverageSum > vertCoverageSum ? StripeDirection.HORIZONTAL : StripeDirection.VERTICAL;
                            }
                        } else {  // who has more full coverage rows?
                            stripeDirection = vertFullCoverageKidCount > horizFullCoverageKidCount ? StripeDirection.VERTICAL : StripeDirection.HORIZONTAL;
                        }
                    } else {  // pick whichever way has less kids
                        stripeDirection = vertKids.size() < horizKids.size() ? StripeDirection.VERTICAL : StripeDirection.HORIZONTAL;
                    }
                } else {
                    stripeDirection = getXFactor() > getYFactor() ? StripeDirection.HORIZONTAL : StripeDirection.VERTICAL;
                }
            }
            // now that we've decided...
            children = stripeDirection == StripeDirection.VERTICAL ? vertKids : horizKids;
        }
        

        /**
         * This method is called only by the containing Search class, and is used when looking at the terminating condition.
         * When considering a Grid to split, if the parent hasn't been split yet, if we split this guy then the parent gets split,
         * so we need to include that number of his kids as well
         * Note that if the parent hasn't split yet (e.g. it had #radix children, so deferred splitting) then
         * we should add that number of children as well
         */
        private int getNumChildrenIfSplit() {
//            assert children != null;  // since toString() calls this method when children==null, don't do this (even though it should never be null during normal operation)
            if (children == null) return Integer.MAX_VALUE;
            int childCount = children.size();
            if (parent != null && !parent.hasSplit) {
                childCount += parent.getNumChildrenIfSplit()-1;  // a parent that hasn't split yet can only have #radix kids
                // but HIS parent might not have split either, so needs to be a recursive call
            }
            return childCount;
        }

        /**
         * Returns the number of children this Grid has
         * @return
         */
        private int getNumChildren() {
            if (children == null) {
                return -1;  // this can happen when toString is printing out information about children... 
//                return Integer.MIN_VALUE;  // this can happen when toString is printing out information about children... 
//                return -engine.getRadix();  // this can happen when toString is printing out information about children... 
            }
            // else...
            return children.size();
        }
        
        private int getXFactor() {
            return xRange.getWidth() - engine.getXShift();
        }
        
        private int getYFactor() {
            return yRange.getWidth() - engine.getYShift();
        }

        @Override
        public String toString() {
            try {
                String shape;
                if (getXFactor() == getYFactor()) {
                    shape = "Square";
                } else if (getXFactor() < getYFactor()) {
                    shape = "Horiz Rect";
                } else {
                    shape = "Vert Rect";
                }
                Rect coords = buildGridCoords();
                return String.format("%s :  ([%f,%f]->[%f,%f]), xFactor=%d, yFactor=%d, COMP=%.8f, Targets=%s, Ratio=%.6f, Children=%d, Split? %b, Shape=%s, Slice Dir=%s",
                        buildTopicSubscription(),coords.x1,coords.y1,coords.x2,coords.y2,getXFactor(),getYFactor(),SORT_COMPARATOR.buildIntersectionRatio(this),sortedTargets,staticCoverageRatio,getNumChildren(),hasSplit,shape,stripeDirection);
            } catch (RuntimeException e) {
                return "toString() threw an error of some sort! "+e.toString();
            }
        }
        
        /**
         * The topic string will be unique for each grid
         */
        @Override
        public int hashCode() {
            return buildTopicSubscription().hashCode();
        }

        private boolean intersects() {
            return sortedTargets.size() > 0;
//            return staticCoverageRatio > 0;
        }

        String debugDraw() {
            StringBuilder sb = new StringBuilder("+");
            for (int i=0;i<engine.getRadix();i++) {
                sb.append("--");
            }
            sb.append(String.format("+%n"));
            for (int y=engine.getRadix()-1;y>=0;y--) {
                sb.append('|');
                for (int x=0;x<engine.getRadix();x++) {
                    RadixGrid temp = new RadixGrid(this,
                            new Range(engine.getXStringFormatter(),xRange.getVal()+GeoStringFormatter.radixCharConvert(x)),
                            new Range(engine.getYStringFormatter(),yRange.getVal()+GeoStringFormatter.radixCharConvert(y)));
                    if (temp.intersects()) sb.append("()");
                    else sb.append("  ");
                }
                sb.append(String.format("|%n"));
            }
            sb.append("+");
            for (int i=0;i<engine.getRadix();i++) {
                sb.append("--");
            }
            sb.append(String.format("+%n"));
            return sb.toString();
        }

        private String getAncestors() {
            StringBuilder sb = new StringBuilder();
            if (parent != null && !parent.hasSplit) {  // recursive call
                sb.append(parent.getAncestors());
            }
            sb.append(String.format("> %s%n%s",toString(),debugDraw()));
            return sb.toString();
        }

        private void splitUnsplitParent() {
            logger.debug("Splitting an unsplit parent");
//            if (logger.isDebugEnabled()) {
//                logger.debug(getAncestors());
//            }
            assert parent != null;
            assert !parent.hasSplit;
            parent.hasSplit = true;
            if (parent.parent != null && !parent.parent.hasSplit) {  // wow 2 levels of unsplit parents. definitely possible!
                parent.splitUnsplitParent();  // recursive call
            }
        }

        private List<RadixGrid> split() {
            assert !hasSplit;  // should only be trying to split grids that I haven't yet
            assert !done;
            assert children != null;  // these should have been defined a while ago
            boolean shouldSplit = true;
            if (getNumChildren() == engine.getRadix()) {// && numIntersectedTargets == 1) {
                // only split if all the children are owned by the same target
                Set<Integer> targetIndexes = new HashSet<>();
                for (RadixGrid child : children) {
                    targetIndexes.add(child.getBiggestIntersectedTarget());
                }
                if (targetIndexes.size() == 1) {
                    // don't split!
                    // if all n kids are present, don't actually split the square into n stripes until one of the children has to split
                    shouldSplit = false;
                }
            }
            done = true;  // i have been processed... don't consider me again
            if (shouldSplit) {  // yup, I guess we should split then!
                hasSplit = true;
                if (parent != null && !parent.hasSplit) {  // if the parent hasn't split yet
                    // this will occur due to lines above... if all #radix children present, don't split this square yet until one of the children gets chosen
                    splitUnsplitParent();
                }
            }
            List<RadixGrid> newSplitContenderGrids = new ArrayList<RadixGrid>();
            // now we have to build all of the kids' kids
            for (RadixGrid child : children) {
                if (child.staticCoverageRatio >= CUT_OFF_COVERAGE_RATIO) {  // no point!
                    // do nothing... no point it adding these to the list of contenders since splitting them will do (almost) nothing
                } else {
                    child.buildChildren();  // i.e. grandchildren of 'this'
                    if (child.children != null) {  // if they're null, then child must be at maximum factor and can't split anymore, so don't add this guy to the list of contenders
                        newSplitContenderGrids.add(child);  // else go down a level
                    }
                }
            }
            return newSplitContenderGrids;
        }
        
        List<List<String>> getSubs() {
            List<List<String>> returnList = new ArrayList<>(trimmedTargets.length);
            if (!hasSplit) {
                for (int i=0;i<trimmedTargets.length;i++) {
                    if (i == this.getBiggestIntersectedTarget()) {
                        returnList.add(Collections.singletonList(buildTopicSubscription()));
                    } else {
                        returnList.add(Collections.emptyList());
                    }
                }
            } else {
                for (int i=0;i<trimmedTargets.length;i++) {
                    returnList.add(new ArrayList<>());
                }
                for (RadixGrid child : children) {
                    List<List<String>> childSubs = child.getSubs();
                    for (int i=0;i<trimmedTargets.length;i++) {
                        returnList.get(i).addAll(childSubs.get(i));
                    }
                }
                
            }
            return returnList;
        }
        
        int getNumTargets() {
            return trimmedTargets.length;
        }

        List<List<Rect>> getSquares() {
            List<List<Rect>> returnList = new ArrayList<>(trimmedTargets.length);
            if (!hasSplit) {
                for (int i=0;i<trimmedTargets.length;i++) {
                    if (i == this.getBiggestIntersectedTarget()) {
                        returnList.add(Collections.singletonList(buildGridCoords()));
                    } else {
                        returnList.add(Collections.emptyList());
                    }
                }
            } else {
                for (int i=0;i<trimmedTargets.length;i++) {
                    returnList.add(new ArrayList<>());
                }
                for (RadixGrid child : children) {
                    List<List<Rect>> childSquares = child.getSquares();
                    for (int i=0;i<trimmedTargets.length;i++) {
                        returnList.get(i).addAll(childSquares.get(i));
                    }
                }
            }
            return returnList;
        }


        /**
         * Computes the union of all grids
         * @return
         */
        List<Geometry> getUnion() {
            List<Geometry> returnList = new ArrayList<>(trimmedTargets.length);
            // populate with empty geometries
            for (int i=0;i<trimmedTargets.length;i++) {
                returnList.add(GEOMETRY_FACTORY.createPolygon());
            }
            if (!hasSplit) {
                returnList.set(getBiggestIntersectedTarget(),gridPolygon);
            } else {
                for (RadixGrid child : children) {
                    List<Geometry> childUnions = child.getUnion();
                    for (int i=0;i<trimmedTargets.length;i++) {
                        returnList.set(i,returnList.get(i).union(childUnions.get(i)));  // pull him out, do a union, then put back in
                    }
                }
            }
            return returnList;
        }

        /**
         * The over-coverage can only apply to the "owner" of this grid.  How much it is sticking out over. Doesn't look if we've split.
         * @return
         */
        private double[] getOverCoverageAreas(Depth depth) {
            double[] over = new double[trimmedTargets.length];
            if (depth == Depth.SINGLE) {
                over[getBiggestIntersectedTarget()] = getGridArea() - trimmedTargets[getBiggestIntersectedTarget()].getArea();
            } else {  // calculating the delta for when we split
                over[getBiggestIntersectedTarget()] -= getGridArea() - trimmedTargets[getBiggestIntersectedTarget()].getArea();
                for (RadixGrid child : children) {
                    over = ArrayMath.add(over,child.getOverCoverageAreas(Depth.SINGLE));
                }
            }
            return over;
        }

        private double[] getUnderCoverageAreas(Depth depth) {
            double[] under = new double[trimmedTargets.length];
            if (depth == Depth.SINGLE) {
                for (int i=0;i<trimmedTargets.length;i++) {
                    if (i != getBiggestIntersectedTarget() ) {
                        under[i] = trimmedTargets[i].getArea();
                    }
                }
            } else {  // dpeth == Depth.RECURSIVE
                for (RadixGrid child : children) {
                    under = ArrayMath.add(under,child.getUnderCoverageAreas(Depth.SINGLE));
                }
                under = ArrayMath.subtract(under,getUnderCoverageAreas(Depth.SINGLE));
            }
            return under;
        }


        /**
         * Expensive recursive calculation, so can use the GridAreaDelta function below to track changes
         * @param depth
         * @return
         */
        private double[] getActualAreaRecursive(DepthOfCalc depth) {
            double[] areas = new double[trimmedTargets.length];
            Arrays.fill(areas,0);
            if ((depth == DepthOfCalc.ACTUAL && !hasSplit) || (depth == DepthOfCalc.CHILDREN && children==null)) {
                // i.e. this is the bottom node/grid.  Just return the size.  also equal to square.getArea()
                areas[getBiggestIntersectedTarget()] = getGridArea();
            } else {
                for (RadixGrid child : children) {
                    areas = ArrayMath.add(areas,child.getActualAreaRecursive(depth));
                }
            }
            return areas;
        }
        
        /**
         * Knowing the scale of this grid, easy to calculate using some math
         */
        private double getGridArea() {
            int areaFactor = getXFactor() + getYFactor();
            double area = RadixUtils.lookupInverseFactors(engine.getRadix(),areaFactor);
            return area;
        }
        
        /**
         * If/when this grid is split, how much does the area change?
         * @return
         */
        private double[] getGridAreaDelta() {
            double[] areas = new double[trimmedTargets.length];
            Arrays.fill(areas,0);
            final int childAreaFactor = getXFactor() + getYFactor() + 1;
            for (RadixGrid child : children) {
                areas[child.getBiggestIntersectedTarget()] += RadixUtils.lookupInverseFactors(engine.getRadix(),childAreaFactor);
//                areas[child.getBiggestIntersectedTarget()] += RadixUtils.lookupInverseFactors(engine.getRadix(),xFactor+yFactor+1);
            }
            // so would be this square less, plus a grid for each of the children.  or, how many children are missing
//            return (engine.getRadix()-getNumChildren()) * RadixUtils.lookupInverseFactors(engine.getRadix(),xFactor+yFactor+1);
            return areas;
        }
    }
}
