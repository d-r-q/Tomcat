/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.segmentation_tree;

import lxx.model.TurnSnapshot;
import lxx.model.attributes.Attribute;
import lxx.office.AttributesManager;
import lxx.utils.Interval;
import lxx.utils.LXXUtils;

import java.io.Serializable;
import java.util.*;

import static java.lang.Math.signum;

/**
 * User: jdev
 * Date: 28.09.2010
 */
public class SegmentationTree<T extends Serializable> {

    protected final List<SegmentationTreeEntry<T>> allEntries = new ArrayList<SegmentationTreeEntry<T>>();

    protected final SegmentationTreeNode<T> root;

    protected int entryCount;

    public SegmentationTree(Attribute[] splitAttributes, int loadFactor, double maxIntervalLength) {
        root = new SegmentationTreeNode<T>(loadFactor, splitAttributes[0].getRoundedRange(), -1, splitAttributes, maxIntervalLength);
    }

    public void addEntry(SegmentationTreeEntry<T> SegmentationTreeEntry) {
        root.addEntry(SegmentationTreeEntry);
        allEntries.add(SegmentationTreeEntry);
        entryCount++;
    }

    public List<SegmentationTreeEntry<T>> getEntries(TurnSnapshot bs, int limit) {
        return root.getEntries(bs, limit);
    }

    public List<EntryMatch<T>> getSimilarEntries(TurnSnapshot predicate, int limit) {
        final List<EntryMatch<T>> matches = new LinkedList<EntryMatch<T>>();

        int idx = 0;
        for (SegmentationTreeEntry<T> e : getEntries(predicate, limit)) {
            matches.add(new EntryMatch<T>(e.result, idx++, e.predicate));
        }

        return matches;
    }

    public T getClosestEntryResult(TurnSnapshot turnSnapshot) {
        final List<EntryMatch<T>> similarEntries = getSimilarEntries(turnSnapshot, 1);
        if (similarEntries.size() == 0) {
            return null;
        }

        return similarEntries.get(0).result;
    }

    public EntryMatch<T> getClosestEntry(TurnSnapshot turnSnapshot) {
        final List<EntryMatch<T>> similarEntries = getSimilarEntries(turnSnapshot, 1);
        if (similarEntries.size() == 0) {
            return null;
        }

        return similarEntries.get(0);
    }

    public List<SegmentationTreeEntry<T>> getSimilarEntries(Map<Attribute, Interval> limits) {
        return root.getEntries(limits);
    }

    public List<EntryMatch<T>> getSortedSimilarEntries(TurnSnapshot ts, Map<Attribute, Interval> limits) {
        final double[] weights = new double[AttributesManager.attributesCount()];
        final int[] indexes = new int[limits.size()];
        int idx = 0;
        for (Attribute a : limits.keySet()) {
            weights[a.getId()] = 100D / a.getActualRange();
            indexes[idx++] = a.getId();
        }
        final List<EntryMatch<T>> entries = new ArrayList<EntryMatch<T>>();
        for (SegmentationTreeEntry<T> entry : getSimilarEntries(limits)) {
            entries.add(new EntryMatch<T>(entry.result,
                    LXXUtils.factoredManhettanDistance(indexes, ts.toArray(), entry.predicate.toArray(), weights), entry.predicate));
        }
        Collections.sort(entries, new Comparator<EntryMatch>() {
            public int compare(EntryMatch o1, EntryMatch o2) {
                return (int) signum(o1.match - o2.match);
            }
        });

        return entries;
    }

}
