/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.utils;

import ags.utils.KdTree;
import lxx.ts_log.TurnSnapshot;
import lxx.ts_log.attributes.Attribute;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static java.lang.Math.*;

public class KdTreeAdapter<T extends KdTreeAdapter.KdTreeEntry> {

    private final KdTree<T> delegate;
    private final Attribute[] attributes;

    public KdTreeAdapter(Attribute[] attributes) {
        this.attributes = attributes;
        delegate = new KdTree.SqrEuclid<T>(attributes.length, 50000);
    }

    public void addEntry(T entry) {
        delegate.addPoint(getLocation(entry.turnSnapshot), entry);
    }

    public List<KdTree.Entry<T>> getNearestNeighbours(TurnSnapshot ts) {
        return delegate.nearestNeighbor(getLocation(ts), (int) sqrt(delegate.size()), true);
    }

    public List<KdTree.Entry<T>> getNearestNeighboursS(final TurnSnapshot ts, final double[] weights) {
        final List<KdTree.Entry<T>> entries = delegate.nearestNeighbor(getLocation(ts), (int) sqrt(delegate.size()), true);
        final IntervalLong timeInterval = new IntervalLong(Integer.MAX_VALUE, Integer.MIN_VALUE);
        final IntervalDouble distInterval = new IntervalDouble(Integer.MAX_VALUE, Integer.MIN_VALUE);
        for (KdTree.Entry<T> entry : entries) {
            final int timeDiff = ts.roundTime - entry.value.turnSnapshot.roundTime;
            if (timeDiff < 0) {
                throw new RuntimeException("Something wrong");
            }
            timeInterval.extend(timeDiff);
            distInterval.extend(entry.distance);
        }

        Collections.sort(entries, new Comparator<KdTree.Entry<T>>() {
            public int compare(KdTree.Entry<T> o1, KdTree.Entry<T> o2) {
                final double timeDist1 = (o1.value.turnSnapshot.roundTime - timeInterval.a) / (timeInterval.getLength()) * weights[0];
                final double locDist1 = (o1.distance - distInterval.a) / (distInterval.getLength()) * weights[1];

                final double timeDist2 = (o2.value.turnSnapshot.roundTime - timeInterval.a) / (timeInterval.getLength()) * weights[0];
                final double locDist2 = (o2.distance - distInterval.a) / (distInterval.getLength()) * weights[1];

                return (int) signum(sqrt(timeDist1 * timeDist1 + locDist1 * locDist1) -
                        sqrt(timeDist2 * timeDist2 + locDist2 * locDist2));
            }
        });

        for (int i = 0; i < entries.size() - 1; i++) {
            if (abs(entries.get(i).value.turnSnapshot.roundTime -
                    entries.get(i + 1).value.turnSnapshot.roundTime) < 5) {
                if (entries.get(i).distance < entries.get(i + 1).distance) {
                    entries.remove(i + 1);
                } else {
                    entries.remove(i);
                }
                i--;
            }
        }

        return entries;
    }

    private double[] getLocation(TurnSnapshot ts) {
        final double[] location = new double[attributes.length];

        for (int i = 0; i < attributes.length; i++) {
            location[i] = ts.getAttrValue(attributes[i]) / attributes[i].getRange().getLength();
        }

        return location;
    }

    public static class KdTreeEntry {

        public final TurnSnapshot turnSnapshot;

        public KdTreeEntry(TurnSnapshot turnSnapshot) {
            this.turnSnapshot = turnSnapshot;
        }
    }

}
