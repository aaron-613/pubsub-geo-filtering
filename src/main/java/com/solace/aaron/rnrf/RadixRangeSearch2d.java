package com.solace.aaron.rnrf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;

/**
 * This class is a generalization of the original decimal-only (base 10) ranged filtering algorithm.
 * @author Aaron Lee
 *
 */
public class RadixRangeSearch2d {

    private static final double CUT_OFF_COVERAGE_RATIO = 0.995;  // no point in splitting if above this. don't use 1.0 in case of float round error
    private static final double PADDING_AMOUNT = 0.0000002;  // used to pad the squares (geometry) just a touch so when taking the union they will overlap properly

    private static final Logger logger = LogManager.getLogger(RadixRangeSearch2d.class);
    
    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    /**
     * Helps break up the 2D universe and keep track of which signs each quarter has
     * @author Aaron Lee
     *
     */
    enum Quadrant {
        PX_MY(1,-1),   // +x,-y   (or could think of this as +lat,-lon)
        PX_PY(1,1),    // +x,+y   (or +lat,+lon)
        MX_PY(-1,1),   // -x,+y   (or -lat,+lon)
        MX_MY(-1,-1),  // -x,-y   (or -lat,-lon)
        ;
        
        final int xNegativeModifier;
        final int yNegativeModifier;
        
        Quadrant(int xModifier, int yModifier) {
            this.xNegativeModifier = xModifier;
            this.yNegativeModifier = yModifier;
        }
        
        @Override
        public String toString() {
            return this.name();
        }
    }

    private final Geometry target;
    private final List<RadixGrid> quadGrids = new ArrayList<RadixGrid>();  // need 4 grids with different origins for each
    
    /** Tested with 2, 4, 8, 10, and 16 **/
    private final int radix;
    
    private final int xScale;
    private final int yScale;
    
    private final RadixStringFormatter xStringFormatter;
    private final RadixStringFormatter yStringFormatter;
    
    /** The list of all grids/squares that are considered for splitting.  This will be sorted each iteration, and the top one will be split. **/
    private final List<RadixGrid> splitContenders = new ArrayList<RadixGrid>();  // can't use a PriorityQueue as the sort value for items changes after they've been inserted

    /**
     * Builds a object for computing a 2-dimensional search 
     * @param radix
     * @param target
     */
    public RadixRangeSearch2d(int radix, int xPadding, int yPadding, int xScale, int yScale, boolean xNeedsNegs, boolean yNeedsNegs, Geometry target) {
        logger.fatal("### Starting create RadixRangeSearch2d");
        this.target = target;
        this.radix = radix;
        this.xScale = xScale;  // what is the offset, for the various later calculations?
        this.yScale = yScale;
        this.xStringFormatter = new RadixStringFormatter(radix, xScale, xPadding, xNeedsNegs);
        this.yStringFormatter = new RadixStringFormatter(radix, yScale, yPadding, yNeedsNegs);
        for (Quadrant quadrant : Quadrant.values()) {  // up to 4 possible combinations
            RadixGrid grid = new RadixGrid(Collections.singletonList(target),quadrant,0,0,xScale-xPadding,yScale-yPadding);
            if (grid.intersects()) {
                quadGrids.add(grid);  // only track ones we hit
                // for this huge grid, compute all the children
                splitContenders.addAll(grid.buildChildren());
            }
        }
        Collections.sort(splitContenders,new RadixGridRatioComparator());
    }
    
    public void splitOne() {  // split one, no matter what
        logger.debug(getSubs().toString());
        logger.debug("List of all "+splitContenders.size()+" Grids... splitting the first: "+splitContenders);
        if (splitContenders.isEmpty()) {
            logger.debug("No squares left to consider... stopping");
            return;
        }
        RadixGrid first = splitContenders.remove(0);
        logger.debug("SPLIT: "+first);
        List<RadixGrid> newGuys = first.split();
        logger.debug("Current ratio: "+getCurrentCoverageRatio());
        splitContenders.addAll(newGuys);
        Collections.sort(splitContenders,new RadixGridRatioComparator());
        for (int j=0;j<splitContenders.size();j++) {
            if (j<15 /*|| splitContenders.get(j).latFactor<=2*/) logger.debug("  "+(j+1)+") "+splitContenders.get(j));
        }
    }
    
    public void splitToRatio(final double completionRatio, final int maxSubs) {
        for (int i=0;i<Math.min(splitContenders.size(),10);i++) {
            logger.debug("  {}) {}",i+1,splitContenders.get(i));
        }
        int loop = 0;
        int curNumberSubs = getSubs().size();  // since getSubs() is expensive recursive call, keep track of the size using deltas
        final double targetArea = target.getArea();
        double curArea = getActualArea();
        long totalTime = System.nanoTime();
//        while (!(getCurrentCoverageRatio() >= completionRatio || getSubs().size() >= maxSubs) && !splitContenders.isEmpty()) {
        while (!((targetArea/curArea) >= completionRatio || curNumberSubs >= maxSubs) && !splitContenders.isEmpty()) {
            loop++;
            logger.debug("============= Loop {} ==========",loop);
            RadixGrid gridToSplit = splitContenders.remove(0);
//            if (this.getSubs().size()-1+first.getNumChildrenIfSplit() > maxSubs) {  // -1 because if we split one, we don't count that sub
            if ((curNumberSubs - 1 + gridToSplit.getNumChildrenIfSplit()) > maxSubs) {  // -1 because if we split one, we don't count that sub
                logger.debug("Throwing away: {}",gridToSplit);
                continue;  // throw out this guy, maybe the next one has the right amount of children
            }
            int splitSubCount = gridToSplit.getNumChildrenIfSplit();
            List<RadixGrid> newGridsToConsider = gridToSplit.split();
            curArea -= gridToSplit.getGridAreaDelta();  // change in size of the grid
            if (gridToSplit.getNumChildren()==radix && !gridToSplit.hasSplit) {
                // haven't actually split him yet, so total number of subs don't change
            } else {
                curNumberSubs += splitSubCount-1;  // else add the actual number of subs, less 1 since we removed the guy we're splitting
            }
            splitContenders.addAll(newGridsToConsider);
            Collections.sort(splitContenders,new RadixGridRatioComparator());
            for (int i=0;i<Math.min(splitContenders.size(),10);i++) {
                logger.debug("  {}) {}",i+1,splitContenders.get(i));
            }
        }
        logger.debug("$$$$ TOTAL TIME: {} total",(System.nanoTime()-totalTime)/1000000);
    }

    /**
     * Returns true if the target shape(s) intersects at least something within the "total universe"
     * of the range search.
     * @return
     */
    public boolean intersects() {
        return !quadGrids.isEmpty();
    }

    /**
     * Recursive call that walks through the all the grids and assembles as list of all the subscriptions
     * in the form 23.456*  / 12.789*  (without the spaces)
     * @return
     */
    public List<String> getSubs() {
        List<String> subs = new ArrayList<String>();
        for (RadixGrid grid : quadGrids) {
            subs.addAll(grid.getSubs());
        }
        return subs;
    }

    public List<String[]> getSquares() {
        List<String[]> squares = new ArrayList<String[]>();
        for (RadixGrid grid : quadGrids) {
            squares.addAll(grid.getSquares());
        }
        return squares;
    }
    
    public List<Polygon> getWktPolygons() {
        List<Polygon> polygons = new ArrayList<>();
        for (RadixGrid grid : quadGrids) {
        	polygons.addAll(grid.getPolygons());
        }
    	return polygons;
    }
    
    public double getCurrentCoverageRatio() {
        return target.getArea() / getActualArea();
    }
    
    private double getActualArea() {
        double area = 0;
        for (RadixGrid grid : quadGrids) {
            area += grid.getActualAreaRecursive(DepthOfCalc.ACTUAL);
        }
        return area;
    }

    /**
     * Returns the overall union of grids
     * @return
     */
    public Geometry getUnion() {
    	//Geometry union = null;
        Geometry union = GEOMETRY_FACTORY.createPolygon(new Coordinate[0]);
        for (RadixGrid grid : quadGrids) {
            /*if (union == null) union = grid.getUnion();
            else*/ union = union.union(grid.getUnion());
        }
        return union;
    }
    
    enum DepthOfCalc {
        ACTUAL,
        CHILDREN,
    }

    private enum StripeDirection {
        HORIZONTAL("Horiz slices"),
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

    static String[] staticBuildGridCoords(double innerX, double innerY, int radix, int xFactor, int yFactor, Quadrant quadrant) {
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
    
    /** Will return true for more vertical shapes */
    static StripeDirection staticWhichWayToSplit(Geometry intersectedTarget, Geometry square) {
        // what is the shape of this thing?
        Coordinate[] bbox = intersectedTarget.getEnvelope().getCoordinates();
        double width = Math.abs(bbox[0].x - bbox[2].x);
        double height = Math.abs(bbox[0].y - bbox[2].y);
        // is it taller or wider?  since doubles, won't be exactly equal, so check if within an epsilon
        double relError;
        if (Math.abs(height) > Math.abs(width)) relError = Math.abs((height-width)/height);
        else relError = Math.abs((height-width)/width);
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
    public class RadixGridRatioComparator implements Comparator<RadixGrid> {
        
        public double buildIntersectionRatio(RadixRangeSearch2d.RadixGrid grid) {
//            if (grid.getNumChildren() == 1 && grid.parent.hasSplit) return Double.POSITIVE_INFINITY;
            // makes it slightly less attractive to split if there are a lot of children (even moreso if this is a child of a 10 parent that hasn't been split yet
//            return (1-grid.staticCoverageRatio) * grid.getGridArea() / Math.pow(1.2,grid.getNumChildren()-1);  // so if num children == 1, then this does nothing.  if num children == 10, this weights it by an extra
            // new way...
//            return (1-grid.staticCoverageRatio) * grid.getGridArea() / (grid.getNumChildren()-1);  // so if num children == 1, makes it positive infinity... forces spitting
            return (1-grid.staticCoverageRatio) * grid.getGridArea() / (grid.getNumChildrenIfSplit()-1);  // so if num children == 1, makes it positive infinity... forces spitting
//            return (1-grid.staticCoverageRatio) * RangeUtils.lookupInverseFactors(Math.min(grid.xFactor,grid.yFactor)) / (grid.getNumChildrenIfSplit()-1);  // so if num children == 1, makes it positive infinity... forces spitting
        }

        @Override
        public int compare(RadixRangeSearch2d.RadixGrid a, RadixRangeSearch2d.RadixGrid b) {
            double ar = buildIntersectionRatio(a);
            double br = buildIntersectionRatio(b);
            if (ar > br) return -1;
            else if (ar < br) return 1;
            else return 0;
        }
    }
    
    class RadixGrid {

        /**
         * Helper function to put the whole subscription string together, including wildcard chars, but not trailing '/' char
         */ // e.g. -35.12*/_93.381*
        private String buildTopicSubscription() {
            StringBuilder sb = new StringBuilder();
            sb.append(xStringFormatter.convert(innerX,quadrant.xNegativeModifier<0,xScale-xFactor)).append("*/");
            sb.append(yStringFormatter.convert(innerY,quadrant.yNegativeModifier<0,yScale-yFactor)).append('*');
            return sb.toString();
        }
        
        /**
         * This is only used to draw the corresponding boxes on the map dashboard
         * @return
         */
        private String[] buildGridCoords() {
            return staticBuildGridCoords(innerX, innerY, radix, xFactor, yFactor, quadrant);
        }
        
        private Polygon buildGridPolygon(boolean slightlyInflate) {
            return staticBuildGridPolygon(innerX, innerY, radix, xFactor, yFactor, quadrant, slightlyInflate);
        }
        

        
        // VARIABLES //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        private RadixGrid parent = null;
        //private final Geometry target;
        private final List<Geometry> intersectedTargets = new ArrayList<>();
        private int biggestIntersectedTarget;
        private final Quadrant quadrant;
        private final double innerX;  // inner Coordinate (lat for geo), in decimal
        private final double innerY;
        private final int xFactor; // this is essentially the depth... level = 0 -> no decimal places, lev 1...
        private final int yFactor;
        private final Polygon gridPolygon;
        private final double staticCoverageRatio;  // this is the coverage ratio of this square, regardless of children/splits
        private StripeDirection stripeDirection = StripeDirection.TBD;
        private boolean hasSplit = false;  // if there are 10 (radix) whole children, no point in splitting right there as coverage ratio won't change
        private RadixGrid[] children = null;

        RadixGrid(RadixGrid parent, double innerX, double innerY, int xFactor, int yFactor) {
            this(parent.intersectedTargets,parent.quadrant,innerX,innerY,xFactor,yFactor);
            this.parent = parent;
        }
        
        /**
         * This constructor should only be used directly by parent GridContainer... everything else internally should use the one that includes 'parent' so it maintains a link
         */
        private RadixGrid(List<Geometry> parentsIntersectedTargets, Quadrant quadrant, double innerX, double innerY, int xFactor, int yFactor) {
            this.quadrant = quadrant;
            this.innerX = innerX;
            this.innerY = innerY;
            this.xFactor = xFactor;
            this.yFactor = yFactor;
            this.gridPolygon = buildGridPolygon(false);
            double maxCoverageRatio = 0;
            for (int i=0;i<parentsIntersectedTargets.size();i++) {
            	Geometry intersectedTarget = parentsIntersectedTargets.get(i).intersection(this.gridPolygon);
            	this.intersectedTargets.add(intersectedTarget);
            	if (intersectedTarget.getArea()/getGridArea() > maxCoverageRatio) {
            		maxCoverageRatio = intersectedTarget.getArea()/getGridArea();
            		biggestIntersectedTarget = i;
            	}
            }
           	this.staticCoverageRatio = maxCoverageRatio;
        }
        
        /** Will return true for more vertical shapes -- SHOULD! doesn't work well for L shapes */
        private StripeDirection whichWayToSplit() {  // this shouldn't be called very often..?
            return staticWhichWayToSplit(intersectedTargets.get(biggestIntersectedTarget), gridPolygon);  // used to be 'paddedSquare' for some reason?
        }
        

        /**
         * Returns a list of all the potential children grids that could be split further
         */
        private List<RadixGrid> buildChildren() {
            if (xFactor == xScale && yFactor == xScale) {  // can't split any further
                return Collections.emptyList();  // don't go down further after however many decimal places of accuracy!
            }
            // so both decimals can't be maxxed... is it either though?
            if (xFactor == xScale) {
                stripeDirection = StripeDirection.HORIZONTAL;
            } else if (yFactor == yScale) {
                stripeDirection = StripeDirection.VERTICAL;
            }
            // temp objects... used to potentially split both horizontally and vertically and see how it goes
            RadixGrid[] vertKids = new RadixGrid[radix];
            int vertKidCount = 0;
            int vertFullCoverageKidCount = 0;
            RadixGrid[] horizKids = new RadixGrid[radix];
            int horizKidCount = 0;
            int horizFullCoverageKidCount = 0;
            // which way do we split now?
            // let's do both ways and see which is better
            for (int i=0;i<radix;i++) {
                vertKids[i] = new RadixGrid(this,innerX+(i*RadixUtils.lookupInverseFactors(radix,xFactor+1)*quadrant.xNegativeModifier),innerY,xFactor+1,yFactor);
                if (vertKids[i].intersects()) { vertKidCount++; }
                if (vertKids[i].staticCoverageRatio >= CUT_OFF_COVERAGE_RATIO) { vertFullCoverageKidCount++; }
            }
            for (int i=0;i<radix;i++) {
                horizKids[i] = new RadixGrid(this,innerX,innerY+(i*RadixUtils.lookupInverseFactors(radix,yFactor+1)*quadrant.yNegativeModifier),xFactor,yFactor+1);
                if (horizKids[i].intersects()) { horizKidCount++; }
                if (horizKids[i].staticCoverageRatio >= CUT_OFF_COVERAGE_RATIO) { horizFullCoverageKidCount++; }
            }
            if (vertKidCount == horizKidCount) {  // dang, same number of kids
                if (vertFullCoverageKidCount == horizFullCoverageKidCount) {  // double dang, same number of full rows/cols
                    if (xFactor == yFactor) {  // argh, and the grids are the same dimension, would rather chop up a longer one
                        // let's decide this with geometry... whoever is more narrow, and then trust the centroid
                        stripeDirection = whichWayToSplit();
                    } else {
                        stripeDirection = xFactor > yFactor ? StripeDirection.HORIZONTAL : StripeDirection.VERTICAL;  // i.e. xFactor > yFactor == tall rect, slice horiz
                    }
                } else {  // who has more full coverage rows?
                    stripeDirection = vertFullCoverageKidCount > horizFullCoverageKidCount ? StripeDirection.VERTICAL : StripeDirection.HORIZONTAL;
                }
            } else {  // pick whichever way has less kids
                stripeDirection = vertKidCount < horizKidCount ? StripeDirection.VERTICAL : StripeDirection.HORIZONTAL;
            }
            this.children = new RadixGrid[radix];
            // now we've decided
            if (stripeDirection == StripeDirection.VERTICAL) {
                for (int i=0;i<radix;i++) {
                    if (vertKids[i].intersects()) {
                        children[i] = vertKids[i];
                    }
                }
            } else {
                for (int i=0;i<radix;i++) {
                    if (horizKids[i].intersects()) {
                        children[i] = horizKids[i];
                    }
                }
            }
            return Collections.singletonList(this);
        }
        

        /**
         * This method is called only by the containing Search class, and is used when looking at the terminating condition.
         * When considering a Grid to split, if the parent hasn't been split yet, if we split this guy then the parent gets split,
         * so we need to include that number of his kids as well
         * Note that if the parent hasn't split yet (e.g. it had 10 children, so deferred splitting) then
         * we should add that number of children as well
         */
        private int getNumChildrenIfSplit() {
            assert children != null;
            int childCount = 0;
            for (int i=0;i<radix;i++) {
                if (children[i] != null) {
                    childCount++;
                }
            }
            if (parent != null && !parent.hasSplit) {
                childCount += parent.getNumChildrenIfSplit()-1;  // a parent that hasn't split yet can only have 10/radix kids
                // but HIS parent might not have split either, so needs to be a recursive call
            }
            return childCount;
        }

        /**
         * Not a recursive function... simply walks the children array and sees how many are initialized.
         * @return
         */
        private int getNumChildren() {
            if (children == null) {
                return Integer.MIN_VALUE;  // this can happen when toString is printing out information about children... 
            }
            // else...
            int childCount = 0;
            for (int i=0;i<radix;i++) {
                if (children[i] != null) {
                    childCount++;
                }
            }
            return childCount;
        }
        
        @Override
        public String toString() {
            try {
                String shape;
                if (xFactor == yFactor) {
                    shape = "Square";
                } else if (xFactor < yFactor) {
                    shape = "Horiz Rect";
                } else {
                    shape = "Vert Rect";
                }
                String[] coords = buildGridCoords();
                return String.format("%s :  ([%s,%s]->[%s,%s]), xFactor=%d, yFactor=%d, Ratio=%.5f, Children=%d, Split? %b, Shape=%s, Slice Dir=%s",
                        buildTopicSubscription(),coords[0],coords[1],coords[2],coords[3],xFactor,yFactor,staticCoverageRatio,getNumChildren(),hasSplit,shape,stripeDirection);
            } catch (RuntimeException e) {
                return "toString() threw an error of some sort! "+e.toString();
            }
        }

        private boolean intersects() {
            return staticCoverageRatio > 0;
        }

        private String debugDraw() {
            StringBuilder sb = new StringBuilder(String.format("+--------------------+%n"));
            for (int y=radix-1;y>=0;y--) {
                sb.append('|');
                for (int x=0;x<radix;x++) {
                    RadixGrid temp = new RadixGrid(this,
                    		innerX+(x*RadixUtils.lookupInverseFactors(radix,xFactor-xScale+1)*quadrant.xNegativeModifier),
                            innerY+(y*RadixUtils.lookupInverseFactors(radix,yFactor-yScale+1)*quadrant.yNegativeModifier),
                            xFactor-xScale+1,yFactor-yScale+1);
                    // TODO 'radix' was == 1 on the y coordinate above. Intentional?
                    if (temp.intersects()) sb.append("##");
                    else sb.append("  ");
                }
                sb.append(String.format("|%n"));
            }
            return sb.append(String.format("+--------------------+%n")).toString();
        }

        private String getAncestors() {
            StringBuilder sb = new StringBuilder();
            if (parent != null) {  // recursive call
                sb.append(parent.getAncestors());
            }
            sb.append(String.format("> %s%n%s",toString(),debugDraw()));
            return sb.toString();
        }

        private void splitUnsplitParent() {
            logger.debug("Splitting an unsplit parent");
            logger.debug(getAncestors());
            assert parent != null;
            assert !parent.hasSplit;
            parent.hasSplit = true;
            if (parent.parent != null && !parent.parent.hasSplit) {  // wow 2 levels of unsplit parents. definitely possible!
                parent.splitUnsplitParent();
            }
        }

        private List<RadixGrid> split() {
            assert !hasSplit;  // should only be trying to split grids that I haven't yet
            assert children != null;  // these should have been defined a while ago
            if (getNumChildren() == radix) {
                // don't split!
                // if all 10 kids are present, don't actually split the square into 10 stripes until one of the children has to split
            } else {
                hasSplit = true;
                if (parent != null && !parent.hasSplit) {  // if the parent hasn't split yet
                    // this will occur due to lines above... if all 10 children present, don't split this square yet until one of the children gets chosen
                    splitUnsplitParent();
                }
            }
            List<RadixGrid> newSplitContenderGrids = new ArrayList<RadixGrid>();
            // now we have to build all of the kids' kids
            for (int i=0;i<radix;i++) {
                if (children[i] != null) {
                    if (children[i].staticCoverageRatio >= CUT_OFF_COVERAGE_RATIO) {  // no point!
                        // do nothing... no point it adding these to the list of contenders since splitting them will do nothing
                    } else {
                        newSplitContenderGrids.addAll(children[i].buildChildren());  // else go down a level
                    }
                }
            }
            return newSplitContenderGrids;
        }
        
        private List<String> getSubs() {
            if (!hasSplit) {
                //assert sub != null;
                return Collections.singletonList(buildTopicSubscription());
            }
            // else... we have split
            List<String> allSubs = new ArrayList<String>();
            for (int i=0;i<radix;i++) {
                if (children[i] != null) {
                    allSubs.addAll(children[i].getSubs());
                }
            }
            return allSubs;
        }

        private List<String[]> getSquares() {
            if (!hasSplit) {
                return Collections.singletonList(buildGridCoords());
            }
            // else...
            List<String[]> allSquares = new ArrayList<String[]>();
            for (int i=0;i<radix;i++) {
                if (children[i] != null) {
                    allSquares.addAll(children[i].getSquares());
                }
            }
            return allSquares;
        }

        private List<Polygon> getPolygons() {
            if (!hasSplit) return Collections.singletonList(gridPolygon);
            // else
            List<Polygon> polygons = new ArrayList<>();
            for (int i=0;i<radix;i++) {
                if (children[i] != null) {
                    polygons.addAll(children[i].getPolygons());
                }
            }
            return polygons;
        }

        /**
         * Need to use a slightly inflated square so that all the squares overlap properly, rather than just line up on the edge
         * @return
         */
        private Geometry getUnion() {
            if (!hasSplit) return buildGridPolygon(true);  // have to build these from scratch since inflated slightly
            // else
            Geometry union = GEOMETRY_FACTORY.createMultiPolygon(null);
            for (int i=0;i<radix;i++) {
                if (children[i] != null) {
                    union = union.union(children[i].getUnion());
                }
            }
            return union;
        }

        /**
         * Expensive recursive calculation, so can use the GridAreaDelta function below to track changes
         * @param depth
         * @return
         */
        private double getActualAreaRecursive(DepthOfCalc depth) {
            if ((depth == DepthOfCalc.ACTUAL && !hasSplit) || (depth == DepthOfCalc.CHILDREN && children==null)) {
                // i.e. this is the bottom node/grid.  Just return the size.  also equal to square.getArea()
                return getGridArea();
            }
            // else...
            double cumulativeArea = 0;
            for (int i=0;i<radix;i++) {
                if (children[i] != null) {
                    cumulativeArea += children[i].getActualAreaRecursive(depth);
                }
            }
            return cumulativeArea;
        }
        
        /**
         * Knowing the scale of this grid, easy to calculate using some math
         */
        private double getGridArea() {
            double area = RadixUtils.lookupInverseFactors(radix,xFactor+yFactor);
            //System.out.printf("factor calc: %f, area calc: %f%n",area,square.getArea());
            return area;
        }
        
        /**
         * If/when this grid is split, how much does the area change?
         * @return
         */
        private double getGridAreaDelta() {
            // so would be this square less, plus a grid for each of the children.  or, how many children are missing
            return (radix-getNumChildren()) * RadixUtils.lookupInverseFactors(radix,xFactor+yFactor+1);
        }
    }
}
