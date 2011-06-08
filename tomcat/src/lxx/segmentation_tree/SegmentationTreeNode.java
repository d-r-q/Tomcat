/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.segmentation_tree;

import lxx.model.TurnSnapshot;
import lxx.model.attributes.Attribute;
import lxx.utils.Interval;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static java.lang.Math.abs;

/**
 * User: jdev
 * Date: 28.09.2010
 */
public class SegmentationTreeNode<T extends Serializable> {

    private LinkedList<SegmentationTreeEntry<T>> entries = new LinkedList<SegmentationTreeEntry<T>>();
    private final List<SegmentationTreeNode<T>> children = new ArrayList<SegmentationTreeNode<T>>();

    private final int loadFactor;
    private final Interval interval;
    private final int attributeIdx;
    private final Attribute[] attributes;
    private final Interval range = new Interval(Integer.MAX_VALUE, Integer.MIN_VALUE);
    private final double maxIntervalLength;

    private Double mediana = null;
    private boolean isLoaded = false;

    public SegmentationTreeNode(int loadFactor, Interval interval, int attributeIdx, Attribute[] attributes,
                                double maxIntervalLength) {
        this.loadFactor = loadFactor;
        this.interval = interval;
        this.attributeIdx = attributeIdx;
        this.attributes = attributes;
        this.maxIntervalLength = maxIntervalLength;

        if (attributeIdx == -1) {
            isLoaded = true;
        }
    }

    public List<SegmentationTreeNode<T>> addEntry(SegmentationTreeEntry<T> SegmentationTreeEntry) {
        if (isLoaded) {
            if (children.size() == 0) {
                children.add(new SegmentationTreeNode<T>(loadFactor, attributes[attributeIdx + 1].getRoundedRange(), attributeIdx + 1, attributes, maxIntervalLength));
            }
            final int attrValue = getAttrValue(SegmentationTreeEntry.predicate, attributes[attributeIdx + 1]);
            for (SegmentationTreeNode<T> n : children) {
                if (n.interval.contains(attrValue)) {
                    List<SegmentationTreeNode<T>> subRes = n.addEntry(SegmentationTreeEntry);
                    if (subRes != null) {
                        int idx = children.indexOf(n);
                        children.remove(idx);
                        children.addAll(idx, subRes);
                    }
                    break;
                }
            }
            return null;
        }
        if (mediana == null) {
            mediana = (double) getAttrValue(SegmentationTreeEntry.predicate, attributes[attributeIdx]);
        } else {
            mediana = (mediana * entries.size() + getAttrValue(SegmentationTreeEntry.predicate, attributes[attributeIdx])) / (entries.size() + 1);
        }
        entries.addFirst(SegmentationTreeEntry);
        final int value = getAttrValue(SegmentationTreeEntry.predicate, attributes[attributeIdx]);
        if (value < range.a) {
            range.a = value;
        }
        if (value > range.b) {
            range.b = value;
        }

        if (entries.size() == loadFactor) {
            if (interval.getLength() <= attributes[attributeIdx].getActualRange() * maxIntervalLength || interval.getLength() == 1) {
                if (attributeIdx < attributes.length - 1) {
                    divideVer();
                }
                return null;
            } else {
                return divideHor();
            }
        }
        return null;
    }

    private List<SegmentationTreeNode<T>> divideHor() {
        List<SegmentationTreeNode<T>> res = new ArrayList<SegmentationTreeNode<T>>();

        int med = mediana.intValue();
        Interval i1 = new Interval(interval.a, interval.getLength() > 2 ? med - 1 : interval.a - 1);
        Interval i2 = new Interval(interval.getLength() > 2 ? med : interval.b, interval.b);
        if (med == interval.a) {
            i1 = new Interval(interval.a, interval.a);
            i2 = new Interval(interval.a + 1, interval.b);
        } else if (med == interval.b) {
            i1 = new Interval(interval.a, interval.b - 1);
            i2 = new Interval(interval.b, interval.b);
        }
        res.add(new SegmentationTreeNode<T>(loadFactor, i1, attributeIdx, attributes, maxIntervalLength));
        res.add(new SegmentationTreeNode<T>(loadFactor, i2, attributeIdx, attributes, maxIntervalLength));

        for (SegmentationTreeEntry<T> e : entries) {
            for (SegmentationTreeNode<T> n : res) {
                if (n.interval.contains(getAttrValue(e.predicate, attributes[attributeIdx]))) {
                    List<SegmentationTreeNode<T>> subRes = n.addEntry(e);
                    if (subRes != null) {
                        int idx = res.indexOf(n);
                        res.remove(idx);
                        res.addAll(idx, subRes);
                    }
                    break;
                }
            }
        }
        entries = null;

        return res;
    }

    private void divideVer() {
        isLoaded = true;
        children.add(new SegmentationTreeNode<T>(loadFactor, attributes[attributeIdx + 1].getRoundedRange(),
                attributeIdx + 1, attributes, maxIntervalLength));

        for (SegmentationTreeEntry<T> e : entries) {
            for (SegmentationTreeNode<T> n : children) {
                if (n.interval.contains(getAttrValue(e.predicate, attributes[attributeIdx + 1]))) {
                    List<SegmentationTreeNode<T>> subRes = n.addEntry(e);
                    if (subRes != null) {
                        int idx = children.indexOf(n);
                        children.remove(idx);
                        children.addAll(idx, subRes);
                    }
                    break;
                }
            }
        }
        entries = null;
    }

    private int getAttrValue(TurnSnapshot bs, Attribute attrIdx) {
        return bs.getRoundedAttrValue(attrIdx);
    }

    public List<SegmentationTreeEntry<T>> getEntries(TurnSnapshot bs, int limit) {
        if (isLoaded) {
            if (attributeIdx == -1 && children.size() == 0) {
                return new ArrayList<SegmentationTreeEntry<T>>();
            }
            int idx = 0;
            final int value = getAttrValue(bs, attributes[attributeIdx + 1]);
            for (SegmentationTreeNode n : children) {
                if (n.interval.contains(value)) {
                    break;
                }
                idx++;
            }
            if (idx == children.size()) {
                idx--;
            }
            final List<SegmentationTreeEntry<T>> res = new ArrayList<SegmentationTreeEntry<T>>(children.get(idx).getEntries(bs, limit));
            int step = 1;
            while (res.size() < limit && (idx - step >= 0 || idx + step < children.size())) {
                final SegmentationTreeNode<T> n1 = idx - step >= 0 ?
                        children.get(idx - step)
                        : null;
                final SegmentationTreeNode<T> n2 = idx + step < children.size()
                        ? children.get(idx + step)
                        : null;

                try {
                    if (n1 != null && n1.mediana != null) {
                        if (n2 == null || n2.mediana == null) {
                            res.addAll(n1.getEntries(bs, limit));
                        } else if (abs(n1.mediana - value) < abs(n2.mediana - value)) {
                            res.addAll(n1.getEntries(bs, limit));
                            if (res.size() < limit) {
                                res.addAll(n2.getEntries(bs, limit));
                            }
                        } else {
                            res.addAll(n2.getEntries(bs, limit));
                            if (res.size() < limit) {
                                res.addAll(n1.getEntries(bs, limit));
                            }
                        }
                    } else {
                        if (n2 != null && n2.mediana != null) {
                            res.addAll(n2.getEntries(bs, limit));
                        }
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }

                step++;
            }
            return res;
        } else {
            return entries;
        }
    }

    public int getEntryCount() {
        int res = 0;
        if (entries != null) {
            res += entries.size();
        }

        for (SegmentationTreeNode child : children) {
            res += child.getEntryCount();
        }

        return res;
    }

    public List<SegmentationTreeEntry<T>> getEntries(Map<Attribute, Interval> limits) {
        if (children.size() == 0) {
            return entries;
        }

        int fromIdx = 0;
        int toIdx = 0;
        Interval limit = limits.get(attributes[attributeIdx + 1]);
        for (int i = 0; i < children.size(); i++) {
            if (children.get(i).interval.contains(limit.a)) {
                fromIdx = i;
            } else if (children.get(i).interval.contains(limit.b)) {
                toIdx = i;
                break;
            }
        }

        List<SegmentationTreeEntry<T>> entries = new ArrayList<SegmentationTreeEntry<T>>();
        for (int i = fromIdx; i <= toIdx; i++) {
            entries.addAll(children.get(i).getEntries(limits));
        }

        return entries;
    }
}
