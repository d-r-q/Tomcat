/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.utils.sp_tree;

import lxx.ts_log.TurnSnapshot;
import lxx.ts_log.attributes.Attribute;
import lxx.utils.Interval;

import java.util.*;

import static java.lang.Math.*;
import static java.lang.StrictMath.min;

/**
 * User: jdev
 * Date: 24.09.11
 */
public class SPTree<T extends SPTreeEntry> implements Comparable<Integer> {

    private final Attribute[] attributes;
    private final int attributeIdx;
    private final int attributeValue;

    private ArrayList<SPTree<T>> children;
    private LinkedList<T> entries;
    private int entriesCount = 0;

    public SPTree(Attribute... attributes) {
        final Attribute[] localCopy = Arrays.copyOf(attributes, attributes.length);
        this.attributes = localCopy;
        this.attributeIdx = -1;
        this.attributeValue = -1;

        Arrays.sort(localCopy, new Comparator<Attribute>() {
            public int compare(Attribute o1, Attribute o2) {
                return (int) signum(o1.getRange().getLength() - o2.getRange().getLength());
            }
        });

        children = new ArrayList<SPTree<T>>((int) attributes[attributeIdx + 1].getRange().getLength());
    }

    private SPTree(Attribute[] attributes, int attributeIdx, int attributeValue) {
        this.attributes = attributes;
        this.attributeIdx = attributeIdx;
        this.attributeValue = attributeValue;

        if (attributeIdx == attributes.length - 1) {
            entries = new LinkedList<T>();
        } else {
            children = new ArrayList<SPTree<T>>(min(10, (int) attributes[attributeIdx + 1].getRange().getLength()));
        }
    }

    public void add(T entry) {
        if (attributeIdx == attributes.length - 1) {
            entries.addLast(entry);
            return;
        } else if (attributeIdx == -1) {
            entriesCount++;
        }

        int idx = Collections.binarySearch(children, (int) round(entry.location.getAttrValue(attributes[attributeIdx + 1])));
        if (idx < 0) {
            idx = -idx - 1;
            final SPTree<T> child = new SPTree<T>(attributes, attributeIdx + 1, (int) round(entry.location.getAttrValue(attributes[attributeIdx + 1])));
            if (idx < children.size()) {
                children.add(idx, child);
            } else {
                children.add(child);
            }
        }

        children.get(idx).add(entry);
    }

    public Collection<T> rangeSearch(TurnSnapshot location, Map<Attribute, Interval> hypercube) {
        final double[] scales = new double[attributes.length];
        for (int i = 0; i < attributes.length; i++) {
            scales[i] = 100D / attributes[i].getRange().getLength();
        }

        final List<T> res = new LinkedList<T>();
        Interval[] hc = new Interval[hypercube.size()];
        for (int i = 0; i < attributes.length; i++) {
            hc[i] = hypercube.get(attributes[i]);
        }
        rangeSearch(location, hc, scales, 0, res);

        return res;
    }

    public Collection<T> rangeSearch(TurnSnapshot location, Map<Attribute, Interval> hypercube, Comparator<SPTreeEntry> cmp) {
        final double[] scales = new double[attributes.length];
        for (int i = 0; i < attributes.length; i++) {
            scales[i] = 100D / attributes[i].getRange().getLength();
        }

        final ArrayList<T> res = new ArrayList<T>(min(entriesCount, 200));
        Interval[] hc = new Interval[hypercube.size()];
        for (int i = 0; i < attributes.length; i++) {
            hc[i] = hypercube.get(attributes[i]);
        }
        rangeSearch(location, hc, scales, 0, res);
        Collections.sort(res, cmp);

        return res;
    }

    private void rangeSearch(TurnSnapshot location, Interval[] hypercube, double[] scales, double accumulatedDist,
                             Collection<T> result) {
        if (attributeIdx == attributes.length - 1) {
            for (SPTreeEntry ts : entries) {
                ts.distance = accumulatedDist;
            }
            result.addAll(entries);
            return;
        }

        int idx = Collections.binarySearch(children, hypercube[attributeIdx + 1].a);
        if (idx < 0) {
            idx = max(0, -idx - 1);
        }

        final int size = children.size();
        for (; idx < size; idx++) {
            final SPTree<T> child = children.get(idx);
            if (child.attributeValue > hypercube[attributeIdx + 1].b) {
                break;
            }

            final double dist = child.attributeValue - location.getAttrValue(attributes[attributeIdx + 1]);
            child.rangeSearch(location, hypercube, scales, accumulatedDist + dist * dist * scales[attributeIdx + 1], result);
        }
    }

    public int compareTo(Integer attrValue) {
        return attributeValue - attrValue;
    }
}
