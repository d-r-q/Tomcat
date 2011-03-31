/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.kd_tree;

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
public class LimitedPriorityKdTree<T extends Serializable> {

    protected final List<LPKdTreeEntry<T>> allEntries = new ArrayList<LPKdTreeEntry<T>>();

    protected final LPKdTreeNode<T> root;

    protected int entryCount;

    public LimitedPriorityKdTree(Attribute[] splitAttributes, int loadFactor, double maxIntervalLength) {
        root = new LPKdTreeNode<T>(loadFactor, splitAttributes[0].getRange(), -1, splitAttributes, maxIntervalLength);
    }

    public void addEntry(LPKdTreeEntry<T> LPKdTreeEntry) {
        root.addEntry(LPKdTreeEntry);
        allEntries.add(LPKdTreeEntry);
        entryCount++;
    }

    public List<LPKdTreeEntry<T>> getEntries(TurnSnapshot bs, int limit) {
        return root.getEntries(bs, limit);
    }

    public List<EntryMatch<T>> getSimilarEntries(TurnSnapshot predicate, int limit) {
        final List<EntryMatch<T>> matches = new LinkedList<EntryMatch<T>>();

        int idx = 0;
        for (LPKdTreeEntry<T> e : getEntries(predicate, limit)) {
            matches.add(new EntryMatch<T>(e.result, idx++, e.predicate));
        }

        return matches;
    }

    public int getEntryCount() {
        return entryCount;
    }
}
