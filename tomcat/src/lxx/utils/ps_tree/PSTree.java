/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.utils.ps_tree;

import lxx.ts_log.attributes.Attribute;
import lxx.utils.Interval;

import java.io.Serializable;

/**
 * User: jdev
 * Date: 28.09.2010
 */
public class PSTree<T extends Serializable> {

    private final PSTreeNode<T> root;
    private int entriesCount = 0;

    public PSTree(Attribute[] splitAttributes, int loadFactor, double maxIntervalLength) {
        root = new PSTreeNode<T>(loadFactor, splitAttributes[0].getRoundedRange(), -1, splitAttributes, maxIntervalLength, null);
    }

    public void addEntry(PSTreeEntry<T> PSTreeEntry) {
        root.addEntry(PSTreeEntry);
        entriesCount++;
    }

    public PSTreeEntry<T>[] getSimilarEntries(Interval[] limits) {
        return root.getEntries0(limits, entriesCount);
    }
}
