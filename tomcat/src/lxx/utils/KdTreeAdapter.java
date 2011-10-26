/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.utils;

import ags.utils.KdTree;
import lxx.ts_log.TurnSnapshot;
import lxx.ts_log.attributes.Attribute;

import java.util.*;

import static java.lang.Math.abs;
import static java.lang.Math.sqrt;

public class KdTreeAdapter<T extends KdTreeAdapter.KdTreeEntry> {

    private final DistTimeComparator distTimeComparator = new DistTimeComparator();
    private final KdTree<T> delegate;
    private final Attribute[] attributes;

    public KdTreeAdapter(Attribute[] attributes) {
        this.attributes = attributes;
        delegate = new KdTree.SqrEuclid<T>(attributes.length, 50000);
    }

    public void addEntry(T entry) {
        delegate.addPoint(getLocation(entry.turnSnapshot), entry);
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

        for (KdTree.Entry<T> e : entries) {
            final double timeDist = (e.value.turnSnapshot.roundTime - timeInterval.a) / (timeInterval.getLength()) * weights[0];
            final double locDist = (e.distance - distInterval.a) / (distInterval.getLength()) * weights[1];
            e.distance = sqrt(timeDist * timeDist + locDist * locDist);
        }
        Collections.sort(entries, distTimeComparator);

        KdTree.Entry<T> prev = null;
        for (Iterator<KdTree.Entry<T>> iter =entries.iterator(); iter.hasNext();) {
            final KdTree.Entry<T> cur = iter.next();
            if (prev != null && abs(prev.value.turnSnapshot.roundTime - cur.value.turnSnapshot.roundTime) < 5) {
                iter.remove();
            } else {
                prev = cur;
            }

        }

        return entries;
    }

    private double[] getLocation(TurnSnapshot ts) {
        final double[] location = new double[attributes.length];

        for (int i = 0; i < attributes.length; i++) {
            location[i] = ts.getAttrValue(attributes[i]) / attributes[i].maxRange.getLength();
        }

        return location;
    }

    public static class KdTreeEntry {

        public final TurnSnapshot turnSnapshot;

        public KdTreeEntry(TurnSnapshot turnSnapshot) {
            this.turnSnapshot = turnSnapshot;
        }
    }

    private class DistTimeComparator implements Comparator<KdTree.Entry<T>> {

        public int compare(KdTree.Entry<T> o1, KdTree.Entry<T> o2) {
            return Double.compare(o1.distance, o2.distance);
        }
    }
}
