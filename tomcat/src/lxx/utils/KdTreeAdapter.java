/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.utils;

import ags.utils.KdTree;
import lxx.ts_log.TurnSnapshot;
import lxx.ts_log.attributes.Attribute;

import java.util.List;

import static java.lang.Math.sqrt;

public class KdTreeAdapter<T extends KdTreeEntry> {

    private final KdTree<T> delegate;
    private final Attribute[] attributes;

    public KdTreeAdapter(Attribute[] attributes, int sizeLimit) {
        this.attributes = attributes;
        delegate = new KdTree.SqrEuclid<T>(attributes.length, sizeLimit);
    }

    public void addEntry(T entry) {
        delegate.addPoint(getLocation(entry.turnSnapshot), entry);
    }

    public List<KdTree.Entry<T>> getNearestNeighbours(final TurnSnapshot ts) {
        return delegate.nearestNeighbor(getLocation(ts), (int) sqrt(delegate.size()), true);
    }

    private double[] getLocation(TurnSnapshot ts) {
        final double[] location = new double[attributes.length];

        for (int i = 0; i < attributes.length; i++) {
            location[i] = ts.getAttrValue(attributes[i]) / attributes[i].maxRange.getLength();
        }

        return location;
    }

}
