/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.data_analysis.kd_tree;

import ags.utils.KdTree;
import lxx.data_analysis.LocationFactory;
import lxx.ts_log.TurnSnapshot;
import lxx.ts_log.attributes.Attribute;

import java.util.List;

import static java.lang.Math.min;
import static java.lang.Math.sqrt;

public class KdTreeAdapter<T extends GunKdTreeEntry> {

    private final KdTree<T> delegate;
    private final Attribute[] attributes;

    public KdTreeAdapter(Attribute[] attributes, int sizeLimit) {
        this.attributes = attributes;
        delegate = new KdTree.SqrEuclid<T>(attributes.length, sizeLimit);
    }

    public void addEntry(T entry) {
        delegate.addPoint(entry.location, entry);
    }

    public GunKdTreeEntry[] getNearestNeighbours(double[] location, int count) {
        final List<KdTree.Entry<T>> entries = delegate.nearestNeighbor(location, count, true);
        final GunKdTreeEntry[] res = new GunKdTreeEntry[entries.size()];

        int idx = res.length - 1;
        for (KdTree.Entry<T> e : entries) {
            res[idx] = e.value;
            res[idx--].distance = e.distance;
        }

        return res;
    }

    public GunKdTreeEntry[] getNearestNeighbours(double[] location) {
        return getNearestNeighbours(location, min((int) sqrt(delegate.size()), 100));
    }

    public Attribute[] getAttributes() {
        return attributes;
    }
}
