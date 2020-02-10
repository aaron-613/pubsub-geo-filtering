package com.solace.aaron.geo.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;


/**
 * This class is a generalization of the original decimal-only (base 10) ranged filtering algorithm.
 * @author Aaron Lee
 *
 */
public class RadixRangeSearch1d {

    private static final double CUT_OFF_COVERAGE_RATIO = 0.995;  // no point in splitting if above this. don't use 1.0 in case of float round error
    private static final double PADDING_AMOUNT = 0.0002;  // used to pad the squares (geometry) just a touch so when taking the union they will overlap properly

    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(RadixRangeSearch1d.class);
    
    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    /**
     * Enum to define which sides of the number line will be included in the search:
     * <ul>
     * <li>Positive only</li>
     * <li>Negative only</li>
     * <li>Both</li>
     * </ul>
     */
    enum Hemisphere {
        PX(1),   // positive x
        MX(-1),  // minus x
        ;
        
        final int xNegativeModifier;
        
        private Hemisphere(int xModifier) {
            this.xNegativeModifier = xModifier;
        }
        
        @Override
        public String toString() {
            return this.name();
        }
    }

    private final Geometry target;
    private final List<RadixSegment> hemiSegments = new ArrayList<RadixSegment>();  // need 4 segments with different origins for each
    
    /** Tested with 2, 4, 8, 10, and 16 **/
    private final int radix;
    
    private final int xScale;
    
    private final RadixStringFormatter xStringFormatter;
    
    /** The list of all segments/squares that are considered for splitting.  This will be sorted each iteration, and the top one will be split. **/
    private final List<RadixSegment> splitContenders = new ArrayList<RadixSegment>();  // can't use a PriorityQueue as the sort value for items changes after they've been inserted

    /**
     * Builds a object for computing a 2-dimensional search 
     * @param radix
     * @param target
     */
    public RadixRangeSearch1d(int radix, int xPadding, int xScale, int offset, Geometry target) {
        logger.debug("Starting create RadixRangeSearch1d");
        this.target = target;
        this.radix = radix;
        this.xScale = xScale;  // what is the offset, for the various later calculations?
        this.xStringFormatter = new RadixStringFormatter(radix, xPadding, xScale, offset);
        for (Hemisphere hemi : Hemisphere.values()) {  // up to 4 possible combinations
            RadixSegment segment = new RadixSegment(Collections.singletonList(target),hemi,0,xScale-xPadding);
            if (segment.intersects()) {
                hemiSegments.add(segment);  // only track ones we hit
                // for this huge segment, compute all the children
                splitContenders.addAll(segment.buildChildren());
            }
        }
        Collections.sort(splitContenders,new RadixSegmentRatioComparator());
    }
    
    public void splitOne() {  // split one, no matter what
        logger.debug(getSubs().toString());
        logger.debug("List of all "+splitContenders.size()+" Segments... splitting the first: "+splitContenders);
        if (splitContenders.isEmpty()) {
            logger.debug("No squares left to consider... stopping");
            return;
        }
        RadixSegment first = splitContenders.remove(0);
        logger.debug("SPLIT: "+first);
        List<RadixSegment> newGuys = first.split();
        logger.debug("Current ratio: "+getCurrentCoverageRatio());
        splitContenders.addAll(newGuys);
        Collections.sort(splitContenders,new RadixSegmentRatioComparator());
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
        final double targetLength = target.getLength();
        double curLength = getActualLength();
        long totalTime = System.nanoTime();
//        while (!(getCurrentCoverageRatio() >= completionRatio || getSubs().size() >= maxSubs) && !splitContenders.isEmpty()) {
        while (!((targetLength/curLength) >= completionRatio || curNumberSubs >= maxSubs) && !splitContenders.isEmpty()) {
            loop++;
            logger.debug("============= Loop {} ==========",loop);
            RadixSegment segmentToSplit = splitContenders.remove(0);
//            if (this.getSubs().size()-1+first.getNumChildrenIfSplit() > maxSubs) {  // -1 because if we split one, we don't count that sub
            if ((curNumberSubs - 1 + segmentToSplit.getNumChildrenIfSplit()) > maxSubs) {  // -1 because if we split one, we don't count that sub
                logger.debug("Throwing away: {}",segmentToSplit);
                continue;  // throw out this guy, maybe the next one has the right amount of children
            }
            int splitSubCount = segmentToSplit.getNumChildrenIfSplit();
            List<RadixSegment> newSegmentsToConsider = segmentToSplit.split();
            curLength -= segmentToSplit.getSegmentLengthDelta();  // change in size of the segment
            if (segmentToSplit.getNumChildren()==radix && !segmentToSplit.hasSplit) {
                // haven't actually split him yet, so total number of subs don't change
            } else {
                curNumberSubs += splitSubCount-1;  // else add the actual number of subs, less 1 since we removed the guy we're splitting
            }
            splitContenders.addAll(newSegmentsToConsider);
            Collections.sort(splitContenders,new RadixSegmentRatioComparator());
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
        return !hemiSegments.isEmpty();
    }

    /**
     * Recursive call that walks through the all the segments and assembles as list of all the subscriptions
     * in the form 23.456*  / 12.789*  (without the spaces)
     * @return
     */
    public List<String> getSubs() {
        List<String> subs = new ArrayList<String>();
        for (RadixSegment segment : hemiSegments) {
            subs.addAll(segment.getSubs());
        }
        return subs;
    }

    public List<String[]> getSquares() {
        List<String[]> squares = new ArrayList<String[]>();
        for (RadixSegment segment : hemiSegments) {
            squares.addAll(segment.getSquares());
        }
        return squares;
    }
    
    public List<LineString> getWktLineStrings() {
        List<LineString> lineStrings = new ArrayList<>();
        for (RadixSegment segment : hemiSegments) {
            lineStrings.addAll(segment.getLineSegments());
        }
        return lineStrings;
    }
    
    public double getCurrentCoverageRatio() {
        return target.getLength() / getActualLength();
    }
    
    private double getActualLength() {
        double length = 0;
        for (RadixSegment segment : hemiSegments) {
            length += segment.getActualLengthRecursive(DepthOfCalc.ACTUAL);
        }
        return length;
    }

    /**
     * Returns the overall union of segments
     * @return
     */
    public Geometry getUnion() {
        //Geometry union = null;
        Geometry union = GEOMETRY_FACTORY.createLineString(new Coordinate[0]);
        for (RadixSegment segment : hemiSegments) {
            /*if (union == null) union = segment.getUnion();
            else*/ union = union.union(segment.getUnion());
        }
        return union;
    }
    
    enum DepthOfCalc {
        ACTUAL,
        CHILDREN,
    }
    
    /* HELPER FUNCTIONS ******************************************************************************/

    static String[] staticBuildSegmentCoords(double innerX, int radix, int xFactor, Hemisphere hemi) {
        double x1 = innerX;  // doesn't matter which 2 corners of square, as long as they are antipodal
        double x2 = innerX+(RadixUtils.lookupInverseFactors(radix,xFactor)*hemi.xNegativeModifier);
        String[] coords2 = {
                Double.toString(Math.min(x1,x2)),
                Double.toString(Math.max(x1,x2)),
        };
        return coords2;
    }

    // make a static version for easier testing... instance version just calls this
    static LineString staticBuildSegmentLineString(double innerX, int radix, int xFactor, Hemisphere hemi, boolean slightlyInflate) {
        double xStep = RadixUtils.lookupInverseFactors(radix,xFactor);  // based on the factor (depth), how far to the outside corner of the box
        double inflation = slightlyInflate ? PADDING_AMOUNT * xStep : 0;
        double outerX = innerX + ((xStep+inflation)*hemi.xNegativeModifier);  // just stretch it a bit with 'inflation' so it overlaps properly for the union
        return GEOMETRY_FACTORY.createLineString(new Coordinate[] {new Coordinate(Math.min(innerX,outerX),0),new Coordinate(Math.max(innerX,outerX),0)});
    }
    
    /**
     * This internal helper class is used to sort the various segments to determine which should be
     * split next.  There are a number of different ways to sort/weight the segment squares, and
     * by varying the algorithm it will change which segments will be split first.
     * @author Aaron Lee
     */
    public class RadixSegmentRatioComparator implements Comparator<RadixSegment> {
        
        public double buildIntersectionRatio(RadixRangeSearch1d.RadixSegment segment) {
            return (1-segment.staticCoverageRatio) * segment.getSegmentLength() / (segment.getNumChildrenIfSplit()-1);  // so if num children == 1, makes it positive infinity... forces spitting
        }

        @Override
        public int compare(RadixRangeSearch1d.RadixSegment a, RadixRangeSearch1d.RadixSegment b) {
            double ar = buildIntersectionRatio(a);
            double br = buildIntersectionRatio(b);
            if (ar > br) return -1;
            else if (ar < br) return 1;
            else return 0;
        }
    }
    
    class RadixSegment {

        private String buildTopicSubscription() {
            StringBuilder sb = new StringBuilder();
            sb.append(xStringFormatter.convert(innerX)).append("*");
            return sb.toString();
        }
        
        private String[] buildSegmentCoords() {
            return staticBuildSegmentCoords(innerX, radix, xFactor, hemi);
        }
        
        private LineString buildSegmentLineString(boolean slightlyInflate) {
            return staticBuildSegmentLineString(innerX, radix, xFactor, hemi, slightlyInflate);
        }
        

        
        // VARIABLES //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        private RadixSegment parent = null;
        //private final Geometry target;
        private final List<Geometry> intersectedTargets = new ArrayList<>();
        private int biggestIntersectedTarget;
        private final Hemisphere hemi;  // which Hemisphere is this in?  Could also just ask the parent's
        private final double innerX;  // inner Coordinate (lat for geo), in decimal
        private final int xFactor; // this is essentially the depth... level = 0 -> no decimal places, lev 1...
        private final LineString segmentLine;
        private final double staticCoverageRatio;  // this is the coverage ratio of this square, regardless of children/splits
        private boolean hasSplit = false;  // if there are 10 (radix) whole children, no point in splitting right there as coverage ratio won't change
        private RadixSegment[] children = null;

        RadixSegment(RadixSegment parent, double innerX, int xFactor) {
            this(parent.intersectedTargets,parent.hemi,innerX,xFactor);
            this.parent = parent;
        }
        
        /**
         * This constructor should only be used directly by parent SegmentContainer... everything else internally should use the one that includes 'parent' so it maintains a link
         */
        private RadixSegment(List<Geometry> parentsIntersectedTargets, Hemisphere hemi, double innerX, int xFactor) {
            this.hemi = hemi;
            this.innerX = innerX;
            this.xFactor = xFactor;
            this.segmentLine = buildSegmentLineString(false);
            double maxCoverageRatio = 0;
            for (int i=0;i<parentsIntersectedTargets.size();i++) {
                Geometry intersectedTarget = parentsIntersectedTargets.get(i).intersection(this.segmentLine);
                this.intersectedTargets.add(intersectedTarget);
                if (intersectedTarget.getLength()/getSegmentLength() > maxCoverageRatio) {
                    maxCoverageRatio = intersectedTarget.getLength()/getSegmentLength();
                    biggestIntersectedTarget = i;
                }
            }
               this.staticCoverageRatio = maxCoverageRatio;
        }
        
        /**
         * Returns a list of all the potential children segments that could be split further
         */
        private List<RadixSegment> buildChildren() {
            if (xFactor == xScale) {  // can't split any further
                return Collections.emptyList();  // don't go down further after however many decimal places of accuracy!
            }
            // temp objects... used to potentially split both horizontally and vertically and see how it goes
            RadixSegment[] kids = new RadixSegment[radix];
            for (int i=0;i<radix;i++) {
                kids[i] = new RadixSegment(this,innerX+(i*RadixUtils.lookupInverseFactors(radix,xFactor+1)*hemi.xNegativeModifier),xFactor+1);
            }
            this.children = new RadixSegment[radix];
            for (int i=0;i<radix;i++) {
                if (kids[i].intersects()) {
                    children[i] = kids[i];
                }
            }
            return Collections.singletonList(this);
        }
        

        /**
         * This method is called only by the containing Search class, and is used when looking at the terminating condition.
         * When considering a Segment to split, if the parent hasn't been split yet, if we split this guy then the parent gets split,
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
        private Integer getNumChildren() {
            if (children == null) {
                return null;  // this can happen when toString is printing out information about children... 
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
                String[] coords = buildSegmentCoords();
                return String.format("%s :  ([%s,%s]), xFactor=%d, Ratio=%.5f, Children=%d, Split? %b",
                        buildTopicSubscription(),coords[0],coords[1],xFactor,staticCoverageRatio,getNumChildren(),hasSplit);
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
                    RadixSegment temp = new RadixSegment(this,
                            innerX+(x*RadixUtils.lookupInverseFactors(radix,xFactor-xScale+1)*hemi.xNegativeModifier),
                            xFactor-xScale+1);
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

        private List<RadixSegment> split() {
            assert !hasSplit;  // should only be trying to split segments that I haven't yet
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
            List<RadixSegment> newSplitContenderSegments = new ArrayList<RadixSegment>();
            // now we have to build all of the kids' kids
            for (int i=0;i<radix;i++) {
                if (children[i] != null) {
                    if (children[i].staticCoverageRatio >= CUT_OFF_COVERAGE_RATIO) {  // no point!
                        // do nothing... no point it adding these to the list of contenders since splitting them will do nothing
                    } else {
                        newSplitContenderSegments.addAll(children[i].buildChildren());  // else go down a level
                    }
                }
            }
            return newSplitContenderSegments;
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
                return Collections.singletonList(buildSegmentCoords());
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

        private List<LineString> getLineSegments() {
            if (!hasSplit) return Collections.singletonList(segmentLine);
            // else
            List<LineString> lineStrings = new ArrayList<>();
            for (int i=0;i<radix;i++) {
                if (children[i] != null) {
                    lineStrings.addAll(children[i].getLineSegments());
                }
            }
            return lineStrings;
        }

        /**
         * Need to use a slightly inflated square so that all the squares overlap properly, rather than just line up on the edge
         * @return
         */
        private Geometry getUnion() {
            if (!hasSplit) return buildSegmentLineString(true);  // have to build these from scratch since inflated slightly
            // else
            Geometry union = GEOMETRY_FACTORY.createMultiLineString(null);
            for (int i=0;i<radix;i++) {
                if (children[i] != null) {
                    union = union.union(children[i].getUnion());
                }
            }
            return union;
        }

        /**
         * Expensive recursive calculation, so can use the SegmentLengthDelta function below to track changes
         * @param depth
         * @return
         */
        private double getActualLengthRecursive(DepthOfCalc depth) {
            if ((depth == DepthOfCalc.ACTUAL && !hasSplit) || (depth == DepthOfCalc.CHILDREN && children==null)) {
                // i.e. this is the bottom node/segment.  Just return the size.  also equal to segmentLine.getLength()
                return getSegmentLength();
            }
            // else...
            double cumulativeLength = 0;
            for (int i=0;i<radix;i++) {
                if (children[i] != null) {
                    cumulativeLength += children[i].getActualLengthRecursive(depth);
                }
            }
            return cumulativeLength;
        }
        
        /**
         * Knowing the scale of this segment, easy to calculate using some math
         */
        private double getSegmentLength() {
            double length = RadixUtils.lookupInverseFactors(radix,xFactor);
            return length;
        }
        
        /**
         * If/when this segment is split, how much does the length change?
         * @return
         */
        private double getSegmentLengthDelta() {
            // so would be this square less, plus a segment for each of the children.  or, how many children are missing
            return (radix-getNumChildren()) * RadixUtils.lookupInverseFactors(radix,xFactor+1);
        }
    }
}
