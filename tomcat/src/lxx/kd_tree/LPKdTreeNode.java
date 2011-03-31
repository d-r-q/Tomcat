/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.kd_tree;

import lxx.model.TurnSnapshot;
import lxx.model.attributes.Attribute;
import lxx.utils.Interval;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static java.lang.Math.abs;

/**
 * User: jdev
 * Date: 28.09.2010
 */
public class LPKdTreeNode<T extends Serializable> {

    private final LinkedList<LPKdTreeEntry<T>> entries = new LinkedList<LPKdTreeEntry<T>>();
    private final List<LPKdTreeNode<T>> children = new ArrayList<LPKdTreeNode<T>>();

    private final int loadFactor;
    private final Interval interval;
    private final int attributeIdx;
    private final Attribute[] attributes;
    private final Interval range = new Interval(Integer.MAX_VALUE, Integer.MIN_VALUE);
    private final double maxIntervalLength;

    private Double mediana = null;
    private boolean isLoaded = false;

    public LPKdTreeNode(int loadFactor, Interval interval, int attributeIdx, Attribute[] attributes,
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

    public List<LPKdTreeNode<T>> addEntry(LPKdTreeEntry<T> LPKdTreeEntry) {
        if (isLoaded) {
            if (children.size() == 0) {
                children.add(new LPKdTreeNode<T>(loadFactor, attributes[attributeIdx + 1].getRange(), attributeIdx + 1, attributes, maxIntervalLength));
            }
            final int attrValue = getAttrValue(LPKdTreeEntry.predicate, attributes[attributeIdx + 1]);
            for (LPKdTreeNode<T> n : children) {
                if (n.interval.contains(attrValue)) {
                    List<LPKdTreeNode<T>> subRes = n.addEntry(LPKdTreeEntry);
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
            mediana = (double) getAttrValue(LPKdTreeEntry.predicate, attributes[attributeIdx]);
        } else {
            mediana = (mediana * entries.size() + getAttrValue(LPKdTreeEntry.predicate, attributes[attributeIdx])) / (entries.size() + 1);
        }
        entries.addFirst(LPKdTreeEntry);
        final int value = getAttrValue(LPKdTreeEntry.predicate, attributes[attributeIdx]);
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

    private List<LPKdTreeNode<T>> divideHor() {
        List<LPKdTreeNode<T>> res = new ArrayList<LPKdTreeNode<T>>();

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
        res.add(new LPKdTreeNode<T>(loadFactor, i1, attributeIdx, attributes, maxIntervalLength));
        res.add(new LPKdTreeNode<T>(loadFactor, i2, attributeIdx, attributes, maxIntervalLength));

        for (LPKdTreeEntry<T> e : entries) {
            for (LPKdTreeNode<T> n : res) {
                if (n.interval.contains(getAttrValue(e.predicate, attributes[attributeIdx]))) {
                    List<LPKdTreeNode<T>> subRes = n.addEntry(e);
                    if (subRes != null) {
                        int idx = res.indexOf(n);
                        res.remove(idx);
                        res.addAll(idx, subRes);
                    }
                    break;
                }
            }
        }

        return res;
    }

    private void divideVer() {
        isLoaded = true;
        children.add(new LPKdTreeNode<T>(loadFactor, attributes[attributeIdx + 1].getRange(),
                attributeIdx + 1, attributes, maxIntervalLength));

        for (LPKdTreeEntry<T> e : entries) {
            for (LPKdTreeNode<T> n : children) {
                if (n.interval.contains(getAttrValue(e.predicate, attributes[attributeIdx + 1]))) {
                    List<LPKdTreeNode<T>> subRes = n.addEntry(e);
                    if (subRes != null) {
                        int idx = children.indexOf(n);
                        children.remove(idx);
                        children.addAll(idx, subRes);
                    }
                    break;
                }
            }
        }
    }

    private int getAttrValue(TurnSnapshot bs, Attribute attrIdx) {
        return bs.getAttrValue(attrIdx);
    }

    public List<LPKdTreeEntry<T>> getEntries(TurnSnapshot bs, int limit) {
        if (isLoaded) {
            if (attributeIdx == -1 && children.size() == 0) {
                return new ArrayList<LPKdTreeEntry<T>>();
            }
            int idx = 0;
            final int value = getAttrValue(bs, attributes[attributeIdx + 1]);
            for (LPKdTreeNode n : children) {
                if (n.interval.contains(value)) {
                    break;
                }
                idx++;
            }
            if (idx == children.size()) {
                idx--;
            }
            final List<LPKdTreeEntry<T>> res = new ArrayList<LPKdTreeEntry<T>>(children.get(idx).getEntries(bs, limit));
            int step = 1;
            while (res.size() < limit && (idx - step >= 0 || idx + step < children.size())) {
                final LPKdTreeNode<T> n1 = idx - step >= 0 ?
                        children.get(idx - step)
                        : null;
                final LPKdTreeNode<T> n2 = idx + step < children.size()
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

}
