/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.utils.ps_tree;

import lxx.ts_log.TurnSnapshot;
import lxx.ts_log.attributes.Attribute;
import lxx.utils.Interval;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * User: jdev
 * Date: 28.09.2010
 */
public class PSTreeNode<T extends Serializable> {

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
            if (interval.getLength() == 1) {
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

    public int getEntries(Interval[] limits, PSTreeEntry<T>[] res, int len) {
        if (children.size() == 0) {
            final PSTreeEntry<T>[] entriesArr = entries.toArray(new PSTreeEntry[entries.size()]);
            if (len == 0) {
                System.arraycopy(entriesArr, 0, res, 0, entriesArr.length);
            } else {
                final PSTreeEntry<T>[] resCopy = Arrays.copyOf(res, len);
                int i1 = 0;
                int i2 = 0;
                int resIdx = 0;
                while (i1 < entriesArr.length && i2 < len) {
                    if (entriesArr[i1].predicate.roundTime >= resCopy[i2].predicate.roundTime) {
                        res[resIdx++] = entriesArr[i1++];
                    } else {
                        res[resIdx++] = resCopy[i2++];
                    }
                }
                if (i1 < entriesArr.length) {
                    System.arraycopy(entriesArr, i1, res, resIdx, entriesArr.length - i1);
                } else if (i2 < len) {
                    System.arraycopy(resCopy, i2, res, resIdx, len - i2);
                }
            }
            return len + entriesArr.length;
        }

        final Interval limit = limits[attributes[attributeIdx + 1].id];
        int resSize = len;
        for (final PSTreeNode<T> child : children) {
            if (child.range.a > child.range.b || child.range.b < limit.a) {
                continue;
            } else if (child.range.a > limit.b) {
                break;
            } else {
                resSize = child.getEntries(limits, res, resSize);
            }
        }

        return resSize;
    }
}
