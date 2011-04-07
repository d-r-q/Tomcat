/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.segmentation_tree;

import lxx.model.TurnSnapshot;
import lxx.model.attributes.Attribute;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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

    public T getClosestEntry(TurnSnapshot turnSnapshot) {
        final List<EntryMatch<T>> similarEntries = getSimilarEntries(turnSnapshot, 1);
        if (similarEntries.size() == 0) {
            return null;
        }

        return similarEntries.get(0).result;
    }
}
