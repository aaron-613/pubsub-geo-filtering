package com.solace.aaron.geo.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.locationtech.jts.geom.Geometry;

import com.solace.aaron.geo.api.Geo2dSearch.RadixGrid;

/**
 * This class represents the result of performing a run with the geo[metric|spatial] algorithm.
 */
public class Geo2dSearchResult {

    private final List<Geometry> targets;
    private final RadixGrid rootNode;
    private final List<RadixGrid> orderedSplitGridList;
    
    // package visibility
    Geo2dSearchResult(Geometry[] targets, RadixGrid rootNode, List<RadixGrid> orderedSplitGridList) {
        this.targets = Arrays.asList(targets);
        this.rootNode = rootNode;
        this.orderedSplitGridList = orderedSplitGridList;
    }

    /**
     * Returns the original Geometry targets that were used
     */
    public List<Geometry> getTargets() {
        return targets;
    }

    /**
     * Returns the List of subscriptions, format <code>"xxxxx* /yyyyyy*"</code> (no space) for each target.
     */
    public List<List<String>> getSubs() {
        return rootNode.getSubs(false);
    }

    /**
     * Returns the List of subscriptions, format <code>"yyyyy* /xxxxx*"</code> (no space) for each target.
     * Returns the two levels in reversed order, useful for lat/lon vs. x/y support.
     */
    public List<List<String>> getSubsReversed() {
        return rootNode.getSubs(true);
    }

    /**
     * Returns a List of Rect objects representing the subscriptions for each target.
     */
    public List<List<Rect>> getSquares() {
        return rootNode.getSquares();
    }
    
//    public List<List<Polygon>> getWktPolygons() {
//        return rootNode.getPolygons();
//    }
    
    public List<Geometry> getUnion() {
        return rootNode.getUnion();
    }
    
    
    
    
    /**
     * Holy shit I can't remember what I was doing here..!
     */
    public static class IncrementalSearchResult {
        
        /**
         * A super nested list, defining when each was added, and for which target: [loop][target][childRect][coords]
         */
        public List<List<List<List<Double>>>> added = new ArrayList<>();  // each square that was added each loop, and for which target (loop, target, childRect)
        /**
         * For each loop, only a single rectangle can be removed (i.e. split), 
         */
        public List<List<Integer>> removed = new ArrayList<>();  // so for loop 0, nothing was removed, for loop 1 [x,0,0] was removed, for loop 2 [y,1,0] was removed
    }
    
    public IncrementalSearchResult getInc() {
        Map<RadixGrid,List<Integer>> addedWhen = new HashMap<>();  // used to find when a particular grid was added to the list of contenders
        
        IncrementalSearchResult result = new IncrementalSearchResult();
        // initialize
        {
            RadixGrid grid = orderedSplitGridList.get(0);  // get the first guy, the 'rootNode'
            result.added.add(new ArrayList<>());  // at index 0
            for (int i=0;i<rootNode.getNumTargets();i++) {
                result.added.get(0).add(new ArrayList<>());  // add a list for each target
            }
            result.added.get(0).get(grid.getBiggestIntersectedTarget()).add(grid.buildGridCoords().asList());  // start with the full covering grid
            result.removed.add(Collections.emptyList());  // and nothing was removed in loop 0  // this might need to be initialized further TODO
            List<Integer> when = new ArrayList<>();
            when.add(0);
            when.add(grid.getBiggestIntersectedTarget());
            when.add(0);
            addedWhen.put(grid,when);  // so, he was added at loop 0, index 0
        }
        // start
        int loop = 1;
        for (RadixGrid grid : orderedSplitGridList) {
            if (loop >= 12) {
                System.out.print("");
            }
            // ok, so this is the grid that we're splitting, so it's being removed
            List<Integer> when = addedWhen.get(grid);  // when was he added?  will be [loop][target][childIndex]
            result.removed.add(when);  // add that 'when' information to the removedIndex so we know when this grid was added
            List<List<List<Double>>> kids = new ArrayList<>();
            for (int i=0;i<rootNode.getNumTargets();i++) {
                kids.add(new ArrayList<>());
            }
            for (RadixGrid kid : grid.children) {
                List<Integer> childWhen = new ArrayList<>();
                childWhen.add(loop);
                childWhen.add(kid.getBiggestIntersectedTarget());
                childWhen.add(kids.get(kid.getBiggestIntersectedTarget()).size());  // not the index of the child, but the index of when we added to this target
                addedWhen.put(kid,childWhen);
                kids.get(kid.getBiggestIntersectedTarget()).add(kid.buildGridCoords().asList());
            }
            result.added.add(kids);
            loop++;
        }
        return result;
    }
}
