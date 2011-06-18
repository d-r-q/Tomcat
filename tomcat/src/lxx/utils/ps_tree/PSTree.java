/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.utils.ps_tree;

import lxx.ts_log.TurnSnapshot;
import lxx.ts_log.attributes.Attribute;
import lxx.ts_log.attributes.AttributesManager;
import lxx.utils.Interval;
import lxx.utils.LXXUtils;

import java.io.Serializable;
import java.util.*;

import static java.lang.Math.signum;

/**
 * User: jdev
 * Date: 28.09.2010
 */
public class PSTree<T extends Serializable> {

    private final List<PSTreeEntry<T>> allEntries = new ArrayList<PSTreeEntry<T>>();
    private final PSTreeNode<T> root;

    private int entryCount;

    public PSTree(Attribute[] splitAttributes, int loadFactor, double maxIntervalLength) {
        root = new PSTreeNode<T>(loadFactor, splitAttributes[0].getRoundedRange(), -1, splitAttributes, maxIntervalLength);
    }

    public void addEntry(PSTreeEntry<T> PSTreeEntry) {
        root.addEntry(PSTreeEntry);
        allEntries.add(PSTreeEntry);
        entryCount++;
    }

    public List<PSTreeEntry<T>> getEntries(TurnSnapshot bs, int limit) {
        return root.getEntries(bs, limit);
    }

    public List<EntryMatch<T>> getSimilarEntries(TurnSnapshot predicate, int limit) {
        final List<EntryMatch<T>> matches = new LinkedList<EntryMatch<T>>();

        int idx = 0;
        for (PSTreeEntry<T> e : getEntries(predicate, limit)) {
            matches.add(new EntryMatch<T>(e.result, idx++, e.predicate));
        }

        return matches;
    }

    public EntryMatch<T> getClosestEntry(TurnSnapshot turnSnapshot) {
        final List<EntryMatch<T>> similarEntries = getSimilarEntries(turnSnapshot, 1);
        if (similarEntries.size() == 0) {
            return null;
        }

        return similarEntries.get(0);
    }

    public List<PSTreeEntry<T>> getSimilarEntries(Map<Attribute, Interval> limits) {
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
        for (PSTreeEntry<T> entry : getSimilarEntries(limits)) {
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
