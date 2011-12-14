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
import java.util.Iterator;
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
    private final PSTreeNode<T> parent;

    private Double mediana = null;
    private boolean isLoaded = false;
    private ArrayList<PSTreeEntry<T>> entries = null;
    private Iterator<PSTreeNode<T>> childIterator = null;
    private int entriesCount;

    public PSTreeNode(int loadFactor, Interval interval, int attributeIdx, Attribute[] attributes,
                      double maxIntervalLength, PSTreeNode<T> parent) {
        this.loadFactor = loadFactor;
        this.interval = interval;
        this.attributeIdx = attributeIdx;
        this.attributes = attributes;
        this.maxIntervalLength = maxIntervalLength;
        this.parent = parent;

        if (attributeIdx == -1) {
            isLoaded = true;
        }
    }

    public List<PSTreeNode<T>> addEntry(PSTreeEntry<T> PSTreeEntry) {
        if (isLoaded) {
            if (children.size() == 0) {
                children.add(new PSTreeNode<T>(loadFactor, attributes[attributeIdx + 1].getRoundedRange(), attributeIdx + 1, attributes, maxIntervalLength, this));
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
        if (entries == null) {
            entries = new ArrayList<PSTreeEntry<T>>();
        }
        entries.add(0, PSTreeEntry);
        entriesCount = entries.size();
        if (mediana == null) {
            mediana = (double) getAttrValue(PSTreeEntry.predicate, attributes[attributeIdx]);
        } else {
            mediana = (mediana * entriesCount + getAttrValue(PSTreeEntry.predicate, attributes[attributeIdx])) / (entries.size() + 1);
        }
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
        res.add(new PSTreeNode<T>(loadFactor, i1, attributeIdx, attributes, maxIntervalLength, parent));
        res.add(new PSTreeNode<T>(loadFactor, i2, attributeIdx, attributes, maxIntervalLength, parent));

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
                attributeIdx + 1, attributes, maxIntervalLength, this));

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

    public PSTreeEntry<T>[] getEntries(Interval[] limits, int totalEntriesCount) {
        PSTreeNode<T> cursor = this;
        PSTreeEntry<T>[] buffer1 = new PSTreeEntry[totalEntriesCount];
        PSTreeEntry<T>[] buffer2 = new PSTreeEntry[totalEntriesCount];
        PSTreeEntry<T>[] buffer3 = new PSTreeEntry[totalEntriesCount];
        PSTreeEntry<T>[] prevBuffer;
        PSTreeEntry<T>[] newBuffer = buffer2;
        int bufferLen = 0;
        int i = 0;
        do {
            if (cursor.entries != null) {
                if (i++ % 2 == 0) {
                    prevBuffer = buffer1;
                    newBuffer = buffer2;
                } else {
                    prevBuffer = buffer2;
                    newBuffer = buffer1;
                }
                final PSTreeEntry<T>[] entriesArr = cursor.entries.toArray(buffer3);
                int i1 = 0;
                int i2 = 0;
                int resIdx = 0;
                while (i1 < cursor.entriesCount && i2 < bufferLen) {
                    if (entriesArr[i1].predicate.roundTime >= prevBuffer[i2].predicate.roundTime) {
                        newBuffer[resIdx++] = entriesArr[i1++];
                    } else {
                        newBuffer[resIdx++] = prevBuffer[i2++];
                    }
                }
                if (i1 < cursor.entriesCount) {
                    System.arraycopy(entriesArr, i1, newBuffer, resIdx, cursor.entriesCount - i1);
                } else if (i2 < bufferLen) {
                    System.arraycopy(prevBuffer, i2, newBuffer, resIdx, bufferLen - i2);
                }
                bufferLen += cursor.entriesCount;
                cursor = cursor.parent;
            } else {
                final Interval limit = limits[cursor.attributes[cursor.attributeIdx + 1].id];
                if (cursor.childIterator == null) {
                    cursor.childIterator = cursor.children.iterator();
                }
                boolean isCursorChanged = false;
                while (cursor.childIterator.hasNext()) {
                    PSTreeNode<T> child = cursor.childIterator.next();
                    if (child.range.a > child.range.b || child.range.b < limit.a) {
                        continue;
                    } else if (child.range.a > limit.b) {
                        cursor.childIterator = null;
                        cursor = cursor.parent;
                        isCursorChanged = true;
                        break;
                    } else {
                        cursor = child;
                        isCursorChanged = true;
                        break;
                    }
                }
                if (!isCursorChanged) {
                    cursor.childIterator = null;
                    cursor = cursor.parent;
                }
            }

        } while (cursor != null);

        return Arrays.copyOf(newBuffer, bufferLen);
    }

}
