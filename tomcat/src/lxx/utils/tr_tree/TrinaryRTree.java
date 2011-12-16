/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.utils.tr_tree;

import lxx.ts_log.attributes.Attribute;
import lxx.utils.IntervalDouble;

import java.util.Arrays;

public class TrinaryRTree<E extends TRTreeEntry> {

    private static final int LEFT = 0;
    private static final int CENTER = 1;
    private static final int RIGHT = 2;

    private final TrinaryRTree<E> parent;
    private final Attribute[] dimensions;
    private final IntervalDouble[] coveredRange;

    private E[] entries = (E[]) new TRTreeEntry[32];
    private int nextEntryIdx;

    private TrinaryRTree<E>[] children;
    private int nextChild = -1;

    private boolean singular = true;
    private int entryCount;

    public TrinaryRTree(Attribute[] dimensions) {
        this(null, dimensions);
    }

    private TrinaryRTree(TrinaryRTree<E> parent, Attribute[] dimensions) {
        this.parent = parent;
        this.dimensions = dimensions;
        coveredRange = new IntervalDouble[dimensions.length];
        for (int i = 0; i < coveredRange.length; i++) {
            coveredRange[i] = new IntervalDouble();
        }
    }

    public void insert(E entry) {
        entryCount++;
        for (int i = 0; i < coveredRange.length; i++) {
            coveredRange[i].extend(entry.location.toArray()[dimensions[i].id]);
            singular &= coveredRange[i].a == coveredRange[i].b;
        }
        if (children == null) {
            entries[nextEntryIdx++] = entry;
            if (nextEntryIdx == entries.length) {
                if (singular) {
                    E[] newEntries = (E[]) new TRTreeEntry[entries.length * 2];
                    System.arraycopy(entries, 0, newEntries, 0, entries.length);
                    entries = newEntries;
                } else {
                    split();
                }
            }

        } else {
            selectChild(entry).insert(entry);
        }
    }

    private void split() {
        final int splitDimensionIdx = getSplitDimensionIdx();
        final IntervalDouble splitDimensionRange = coveredRange[splitDimensionIdx];
        final double splitDimensionLength = splitDimensionRange.getLength();
        final IntervalDouble centerIval = new IntervalDouble(splitDimensionRange.a + splitDimensionLength * 0.33,
                splitDimensionRange.b - splitDimensionLength * 0.33);
        children = new TrinaryRTree[3];
        children[LEFT] = new TrinaryRTree<E>(this, dimensions);
        children[CENTER] = new TrinaryRTree<E>(this, dimensions);
        children[RIGHT] = new TrinaryRTree<E>(this, dimensions);

        for (int i = 0; i < entries.length; i++) {
            if (entries[i].location.toArray()[dimensions[splitDimensionIdx].id] <= centerIval.a) {
                children[LEFT].insert(entries[i]);
            } else if (entries[i].location.toArray()[dimensions[splitDimensionIdx].id] >= centerIval.b) {
                children[RIGHT].insert(entries[i]);
            } else {
                children[CENTER].insert(entries[i]);
            }
        }
        entries = null;
    }

    private int getSplitDimensionIdx() {
        int bestDimensionIdx = 0;
        for (int i = 1; i < dimensions.length; i++) {
            if (coveredRange[i].getLength() / dimensions[i].maxRange.getLength() >
                    coveredRange[bestDimensionIdx].getLength() / dimensions[bestDimensionIdx].maxRange.getLength()) {
                bestDimensionIdx = i;
            }
        }

        return bestDimensionIdx;
    }

    private TrinaryRTree<E> selectChild(TRTreeEntry entry) {
        TrinaryRTree<E> bestChild = children[0];
        double bestChildExtension = getExtension(children[0], entry);
        for (int i = 1; i < children.length; i++) {
            double ext = getExtension(children[i], entry);
            if (ext < bestChildExtension) {
                bestChildExtension = ext;
                bestChild = children[i];
            }
        }
        return bestChild;
    }

    private double getExtension(TrinaryRTree<E> child, TRTreeEntry entry) {
        double extension = 0;

        for (int i = 0; i < coveredRange.length; i++) {
            final double entryValue = entry.location.toArray()[dimensions[i].id];
            final IntervalDouble coveredRange = child.coveredRange[i];
            final double diff;
            if (coveredRange.a > entryValue) {
                diff = coveredRange.a - entryValue;
            } else if (coveredRange.b < entryValue) {
                diff = entryValue - coveredRange.b;
            } else {
                diff = 0;
            }
            extension += diff * diff / dimensions[i].maxRange.getLength();
        }

        return extension;
    }

    public TRTreeEntry[] rangeSearch(IntervalDouble[] range) {
        final TRTreeEntry[] res = new TRTreeEntry[entryCount];
        int len = rangeSearchImpl(range, res);
        return Arrays.copyOf(res, len);
    }

    private int rangeSearchImpl(IntervalDouble[] range, TRTreeEntry[] result) {
        TrinaryRTree<E> cursor = this;
        cursor.nextChild = 0;
        int resultIdx = 0;
        do {
            if (cursor.children == null) {
                for (int i = 0; i < cursor.nextEntryIdx; i++) {
                    boolean matches = true;
                    for (int j = 0; j < cursor.dimensions.length && matches; j++) {
                        matches = range[cursor.dimensions[j].id].contains(cursor.entries[i].location.toArray()[cursor.dimensions[j].id]);
                    }
                    if (matches) {
                        result[resultIdx++] = cursor.entries[i];
                    }
                }
                cursor = cursor.parent;
            } else {
                if (cursor.nextChild == cursor.children.length) {
                    cursor = cursor.parent;
                } else if (intersection(range, cursor.children[cursor.nextChild].coveredRange) != Intersection.NONE) {
                    cursor = cursor.children[cursor.nextChild++];
                    cursor.nextChild = 0;
                } else {
                    cursor.nextChild++;
                }
            }
        } while (cursor != null);
        return resultIdx;
    }

    private Intersection intersection(IntervalDouble[] range, IntervalDouble[] coveringRange) {
        Intersection intersection = Intersection.FULL;
        for (int i = 0; i < coveringRange.length && intersection != Intersection.NONE; i++) {
            if (range[dimensions[i].id].contains(coveringRange[i])) {
                intersection = Intersection.FULL;
            } else if (range[dimensions[i].id].intersection(coveringRange[i]) >= 0) {
                intersection = Intersection.PARTIALLY;
            } else {
                intersection = Intersection.NONE;
            }
        }

        return intersection;
    }

    private enum Intersection {
        NONE,
        PARTIALLY,
        FULL
    }

}
