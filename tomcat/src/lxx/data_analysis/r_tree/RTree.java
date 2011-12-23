/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.data_analysis.r_tree;

import lxx.data_analysis.DataPoint;
import lxx.ts_log.attributes.Attribute;
import lxx.utils.IntervalDouble;

import java.util.Arrays;

public class RTree {

    private static final int CHILDREN_COUNT = 3;
    private static final int BUCKET_SIZE = 32;

    private final RTree parent;
    private final Attribute[] dimensions;
    private final IntervalDouble[] coveredRange;

    private DataPoint[] entries = new DataPoint[BUCKET_SIZE];
    private int nextEntryIdx;

    private RTree[] children;

    private int nextChild = -1;
    private Intersection intersection;

    private int splitDimensionIdx = -1;

    private boolean singular = true;
    private int entryCount;

    public RTree(Attribute[] dimensions) {
        this(null, dimensions);
    }

    private RTree(RTree parent, Attribute[] dimensions) {
        this.parent = parent;
        this.dimensions = dimensions;
        coveredRange = new IntervalDouble[dimensions.length];
        for (int i = 0; i < coveredRange.length; i++) {
            coveredRange[i] = new IntervalDouble();
        }
    }

    public void insert(DataPoint entry) {
        entryCount++;
        for (int i = 0; i < coveredRange.length; i++) {
            coveredRange[i].extend(entry.location[i]);
            singular &= coveredRange[i].a == coveredRange[i].b;
        }
        if (children == null) {
            entries[nextEntryIdx++] = entry;
            if (nextEntryIdx == entries.length) {
                if (singular) {
                    DataPoint[] newEntries = new DataPoint[entries.length * 2];
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
        splitDimensionIdx = getSplitDimensionIdx();
        children = new RTree[CHILDREN_COUNT];
        for (int i = 0; i < children.length; i++) {
            children[i] = new RTree(this, dimensions);
        }
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < entries.length; i++) {
            insert(entries[i]);
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

    private RTree selectChild(DataPoint entry) {
        final IntervalDouble splitDimensionRange = coveredRange[splitDimensionIdx];
        int idx = (int) ((entry.location[splitDimensionIdx] - splitDimensionRange.a) / (splitDimensionRange.getLength() * 1.05) * children.length);
        return children[idx];
    }

    public DataPoint[] rangeSearch(IntervalDouble[] range) {
        final DataPoint[] res = new DataPoint[entryCount];
        final int len = rangeSearchImpl(range, res);
        return Arrays.copyOf(res, len);
    }

    private int rangeSearchImpl(IntervalDouble[] range, DataPoint[] result) {
        RTree cursor = this;
        cursor.nextChild = 0;
        cursor.intersection = null;
        int resultIdx = 0;
        do {
            if (cursor.intersection == Intersection.NONE) {
                cursor = cursor.parent;
            } else if (cursor.children == null) {
                if (cursor.intersection == Intersection.FULL) {
                    System.arraycopy(cursor.entries, 0, result, resultIdx, cursor.nextEntryIdx);
                    resultIdx += cursor.nextEntryIdx;
                } else {
                    for (int i = 0; i < cursor.nextEntryIdx; i++) {
                        boolean matches = true;
                        for (int j = 0; j < cursor.dimensions.length && matches; j++) {
                            matches = range[j].contains(cursor.entries[i].location[j]);
                        }
                        if (matches) {
                            result[resultIdx++] = cursor.entries[i];
                        }
                    }
                }
                cursor = cursor.parent;
            } else {
                if (cursor.nextChild == cursor.children.length) {
                    cursor = cursor.parent;
                } else {
                    cursor = cursor.children[cursor.nextChild++];
                    cursor.nextChild = 0;
                    cursor.intersection = (cursor.parent.intersection == Intersection.FULL)
                            ? Intersection.FULL
                            : intersection(range, cursor.coveredRange);
                }
            }
        } while (cursor != null);
        return resultIdx;
    }

    private Intersection intersection(IntervalDouble[] range, IntervalDouble[] coveringRange) {
        Intersection intersectionType = Intersection.FULL;
        for (int i = 0; i < coveringRange.length && intersectionType != Intersection.NONE; i++) {
            double intersection = range[i].intersection(coveringRange[i]);
            if (intersection < 0) {
                intersectionType = Intersection.NONE;
            } else if (intersection != coveringRange[i].getLength()) {
                intersectionType = Intersection.PARTIALLY;
            }
        }

        return intersectionType;
    }

    private enum Intersection {
        NONE,
        PARTIALLY,
        FULL
    }

}
