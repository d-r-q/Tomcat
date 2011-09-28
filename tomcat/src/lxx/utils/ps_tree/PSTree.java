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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * User: jdev
 * Date: 28.09.2010
 */
public class PSTree<T extends Serializable> {

    private final PSTreeNode<T> root;

    public PSTree(Attribute[] splitAttributes, int loadFactor, double maxIntervalLength) {
        root = new PSTreeNode<T>(loadFactor, splitAttributes[0].getRoundedRange(), -1, splitAttributes, maxIntervalLength);
    }

    public void addEntry(PSTreeEntry<T> PSTreeEntry) {
        root.addEntry(PSTreeEntry);
    }

    public List<PSTreeEntry<T>> getEntries(TurnSnapshot bs, int limit) {
        return root.getEntries(bs, limit);
    }

    public List<PSTreeEntry<T>> getSimilarEntries(Map<Attribute, Interval> limits) {
        final List<PSTreeEntry<T>> res = new ArrayList<PSTreeEntry<T>>(10000);
        root.getEntries(limits, res);
        return res;
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

        return entries;
    }

}
