/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.fire_log;

import lxx.model.BattleSnapshot;
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
public class FireLogNode<T extends Serializable> {

    private final LinkedList<FireLogEntry<T>> entries = new LinkedList<FireLogEntry<T>>();
    private final List<FireLogNode<T>> children = new ArrayList<FireLogNode<T>>();

    private final int loadFactor;
    private final Interval interval;
    private final int attributeIdx;
    private final Attribute[] attributes;
    private final Interval range = new Interval(Integer.MAX_VALUE, Integer.MIN_VALUE);
    private final double maxIntervalLength;

    private Double mediana = null;
    private boolean isLoaded = false;

    public FireLogNode(int loadFactor, Interval interval, int attributeIdx, Attribute[] attributes,
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

    public List<FireLogNode<T>> addEntry(FireLogEntry<T> fireLogEntry) {
        if (isLoaded) {
            if (children.size() == 0) {
                children.add(new FireLogNode<T>(loadFactor, attributes[attributeIdx + 1].getRange(), attributeIdx + 1, attributes, maxIntervalLength));
            }
            final int attrValue = getAttrValue(fireLogEntry.predicate, attributes[attributeIdx + 1]);
            for (FireLogNode<T> n : children) {
                if (n.interval.contains(attrValue)) {
                    List<FireLogNode<T>> subRes = n.addEntry(fireLogEntry);
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
            mediana = (double) getAttrValue(fireLogEntry.predicate, attributes[attributeIdx]);
        } else {
            mediana = (mediana * entries.size() + getAttrValue(fireLogEntry.predicate, attributes[attributeIdx])) / (entries.size() + 1);
        }
        entries.addFirst(fireLogEntry);
        final int value = getAttrValue(fireLogEntry.predicate, attributes[attributeIdx]);
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

    private List<FireLogNode<T>> divideHor() {
        List<FireLogNode<T>> res = new ArrayList<FireLogNode<T>>();

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
        res.add(new FireLogNode<T>(loadFactor, i1, attributeIdx, attributes, maxIntervalLength));
        res.add(new FireLogNode<T>(loadFactor, i2, attributeIdx, attributes, maxIntervalLength));

        for (FireLogEntry<T> e : entries) {
            for (FireLogNode<T> n : res) {
                if (n.interval.contains(getAttrValue(e.predicate, attributes[attributeIdx]))) {
                    List<FireLogNode<T>> subRes = n.addEntry(e);
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
        children.add(new FireLogNode<T>(loadFactor, attributes[attributeIdx + 1].getRange(),
                attributeIdx + 1, attributes, maxIntervalLength));

        for (FireLogEntry<T> e : entries) {
            for (FireLogNode<T> n : children) {
                if (n.interval.contains(getAttrValue(e.predicate, attributes[attributeIdx + 1]))) {
                    List<FireLogNode<T>> subRes = n.addEntry(e);
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

    private int getAttrValue(BattleSnapshot bs, Attribute attrIdx) {
        return bs.getAttrValue(attrIdx);
    }

    public List<FireLogEntry<T>> getEntries(BattleSnapshot bs, int limit) {
        if (isLoaded) {
            if (attributeIdx == -1 && children.size() == 0) {
                return new ArrayList<FireLogEntry<T>>();
            }
            int idx = 0;
            final int value = getAttrValue(bs, attributes[attributeIdx + 1]);
            for (FireLogNode n : children) {
                if (n.interval.contains(value)) {
                    break;
                }
                idx++;
            }
            if (idx == children.size()) {
                idx--;
            }
            final List<FireLogEntry<T>> res = new ArrayList<FireLogEntry<T>>(children.get(idx).getEntries(bs, limit));
            int step = 1;
            while (res.size() < limit && (idx - step >= 0 || idx + step < children.size())) {
                final FireLogNode<T> n1 = idx - step >= 0 ?
                        children.get(idx - step)
                        : null;
                final FireLogNode<T> n2 = idx + step < children.size()
                        ? children.get(idx + step)
                        : null;

                try {
                    if (n1 != null && n1.mediana != null) {
                        if (n2 == null || n2.mediana == null) {
                            res.addAll(n1.getEntries(bs, limit));
                        } else if (abs(n1.mediana - value) < abs(n2.mediana - value)) {
                            res.addAll(n1.getEntries(bs, limit));
                        } else {
                            res.addAll(n2.getEntries(bs, limit));
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
