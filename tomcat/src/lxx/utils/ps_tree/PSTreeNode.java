/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.utils.ps_tree;

import lxx.ts_log.TurnSnapshot;
import lxx.ts_log.attributes.Attribute;
import lxx.utils.Interval;

import java.io.Serializable;
import java.util.*;

import static java.lang.Math.*;

/**
 * User: jdev
 * Date: 28.09.2010
 */
public class PSTreeNode<T extends Serializable> {

    private static final LinkedList<PSTreeEntry> EMPTY_LIST = new LinkedList<PSTreeEntry>();
    private final List<PSTreeNode<T>> children = new ArrayList<PSTreeNode<T>>();

    private final int loadFactor;
    private final Interval interval;
    private final int attributeIdx;
    private final Attribute[] attributes;
    private final Interval range = new Interval(Integer.MAX_VALUE, Integer.MIN_VALUE);
    private final double maxIntervalLength;

    private Double mediana = null;
    private boolean isLoaded = false;
    private ArrayList<PSTreeEntry<T>> entries = new ArrayList<PSTreeEntry<T>>();

    public PSTreeNode(int loadFactor, Interval interval, int attributeIdx, Attribute[] attributes,
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

    public List<PSTreeNode<T>> addEntry(PSTreeEntry<T> PSTreeEntry) {
        if (isLoaded) {
            if (children.size() == 0) {
                children.add(new PSTreeNode<T>(loadFactor, attributes[attributeIdx + 1].getRoundedRange(), attributeIdx + 1, attributes, maxIntervalLength));
            }
            final int attrValue = getAttrValue(PSTreeEntry.predicate, attributes[attributeIdx + 1]);
            if (attributeIdx > -1) {
                final int value = getAttrValue(PSTreeEntry.predicate, attributes[attributeIdx]);
                if (value < range.a) {
                    range.a = value;
                }
                if (value > range.b) {
                    range.b = value;
                }
            }
            int left = 0;
            int right = children.size();
            int medin;
            while (left < right) {
                medin = (left + right) / 2;
                PSTreeNode<T> n = children.get(medin);
                if (n.interval.contains(attrValue)) {
                    List<PSTreeNode<T>> subRes = n.addEntry(PSTreeEntry);
                    if (subRes != null) {
                        int idx = children.indexOf(n);
                        children.remove(idx);
                        children.addAll(idx, subRes);
                    }
                    break;
                } else if (attrValue < n.interval.a) {
                    right = medin;
                } else {
                    left = medin;
                }
            }
            return null;
        }
        if (mediana == null) {
            mediana = (double) getAttrValue(PSTreeEntry.predicate, attributes[attributeIdx]);
        } else {
            mediana = (mediana * entries.size() + getAttrValue(PSTreeEntry.predicate, attributes[attributeIdx])) / (entries.size() + 1);
        }
        entries.add(0, PSTreeEntry);
        final int value = getAttrValue(PSTreeEntry.predicate, attributes[attributeIdx]);
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

    private List<PSTreeNode<T>> divideHor() {
        List<PSTreeNode<T>> res = new ArrayList<PSTreeNode<T>>();

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
        res.add(new PSTreeNode<T>(loadFactor, i1, attributeIdx, attributes, maxIntervalLength));
        res.add(new PSTreeNode<T>(loadFactor, i2, attributeIdx, attributes, maxIntervalLength));

        for (PSTreeEntry<T> e : entries) {
            for (PSTreeNode<T> n : res) {
                if (n.interval.contains(getAttrValue(e.predicate, attributes[attributeIdx]))) {
                    List<PSTreeNode<T>> subRes = n.addEntry(e);
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
        children.add(new PSTreeNode<T>(loadFactor, attributes[attributeIdx + 1].getRoundedRange(),
                attributeIdx + 1, attributes, maxIntervalLength));

        for (PSTreeEntry<T> e : entries) {
            for (PSTreeNode<T> n : children) {
                if (n.interval.contains(getAttrValue(e.predicate, attributes[attributeIdx + 1]))) {
                    List<PSTreeNode<T>> subRes = n.addEntry(e);
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

    public List<PSTreeEntry<T>> getEntries(TurnSnapshot bs, int limit) {
        if (isLoaded) {
            if (attributeIdx == -1 && children.size() == 0) {
                return new ArrayList<PSTreeEntry<T>>();
            }
            int idx = 0;
            final int value = getAttrValue(bs, attributes[attributeIdx + 1]);
            for (PSTreeNode n : children) {
                if (n.interval.contains(value)) {
                    break;
                }
                idx++;
            }
            if (idx == children.size()) {
                idx--;
            }
            final List<PSTreeEntry<T>> res = new ArrayList<PSTreeEntry<T>>(children.get(idx).getEntries(bs, limit));
            int step = 1;
            while (res.size() < limit && (idx - step >= 0 || idx + step < children.size())) {
                final PSTreeNode<T> n1 = idx - step >= 0 ?
                        children.get(idx - step)
                        : null;
                final PSTreeNode<T> n2 = idx + step < children.size()
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

        for (PSTreeNode child : children) {
            res += child.getEntryCount();
        }

        return res;
    }

    public LinkedList<PSTreeEntry<T>> getEntries(Map<Attribute, Interval> limits) {
        if (children.size() == 0) {
            return entries;
        }

        int fromIdx = Integer.MAX_VALUE;
        int toIdx = Integer.MIN_VALUE;
        Interval limit = limits.get(attributes[attributeIdx + 1]);
        for (int i = 0; i < children.size(); i++) {
            if (children.get(i).range.intersects(limit)) {
                fromIdx = min(fromIdx, i);
                toIdx = max(toIdx, i);
            }
        }

        if (fromIdx == Integer.MAX_VALUE) {
            return EMPTY_LIST;
        }

        int medin = (fromIdx + toIdx) / 2;
        LinkedList res = children.get(medin).getEntries(limits);

        for (int delta = 1; delta <= limit.getLength(); delta++) {
            boolean isUpdate = false;
            if (medin - delta >= fromIdx) {
                res = merge(res, children.get(medin - delta).getEntries(limits), new Cmp1());
                isUpdate = true;
            }
            if (medin + delta <= toIdx) {
                children.get(medin + delta).getEntries(limits);
                isUpdate = true;
            }

            if (!isUpdate) {
                break;
            }
        }
    }

    private LinkedList merge(LinkedList lst1, LinkedList lst2, Comparator cmp) {
        LinkedList res = new LinkedList();

        while (lst1.size() > 0 || lst2.size() > 0) {
            if (lst1.size() == 0) {
                res.add(lst2.removeFirst());
            }
            if (lst2.size() == 0) {
                res.add(lst1.removeFirst());
            }
            if (cmp.compare(lst1.getFirst(), lst2.getFirst()) < 0) {
                res.addLast(lst1.removeFirst());
            }
        }

        return res;
    }

    private static class Cmp1 implements Comparator<PSTreeEntry> {

        @Override
        public int compare(PSTreeEntry o1, PSTreeEntry o2) {
            return 0;  //To change body of implemented methods use File | Settings | File Templates.
        }
    }
}