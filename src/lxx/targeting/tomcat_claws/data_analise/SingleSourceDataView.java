/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.targeting.tomcat_claws.data_analise;

import lxx.data_analysis.kd_tree.GunKdTreeEntry;
import lxx.data_analysis.kd_tree.KdTreeAdapter;
import lxx.ts_log.TurnSnapshot;
import lxx.ts_log.attributes.Attribute;
import lxx.utils.IntervalDouble;
import lxx.utils.IntervalLong;

import java.util.*;

import static java.lang.Math.sqrt;

/**
 * User: jdev
 * Date: 17.06.11
 */
public class SingleSourceDataView implements DataView {

    private static final DistTimeComparator distTimeComparator = new DistTimeComparator();

    private final KdTreeAdapter<GunKdTreeEntry> dataSource;

    private final double[] weights;

    public SingleSourceDataView(Attribute[] attributes, double[] weights) {
        this.weights = weights;
        dataSource = new KdTreeAdapter<GunKdTreeEntry>(attributes, 50000);
    }

    public Collection<TurnSnapshot> getDataSet(TurnSnapshot ts) {
        final GunKdTreeEntry[] similarEntries = dataSource.getNearestNeighbours(ts);
        final IntervalLong timeInterval = new IntervalLong(Integer.MAX_VALUE, Integer.MIN_VALUE);
        final IntervalDouble distInterval = new IntervalDouble(Integer.MAX_VALUE, Integer.MIN_VALUE);
        for (GunKdTreeEntry entry : similarEntries) {
            final int timeDiff = ts.roundTime - entry.ts.roundTime;
            timeInterval.extend(timeDiff);
            distInterval.extend(entry.distance);
        }

        for (GunKdTreeEntry e : similarEntries) {
            final double timeDist = (e.ts.roundTime - timeInterval.a) / (timeInterval.getLength()) * weights[0];
            final double locDist = (e.distance - distInterval.a) / (distInterval.getLength()) * weights[1];
            e.normalWeightedDistance = sqrt(timeDist * timeDist + locDist * locDist);
        }
        Arrays.sort(similarEntries, distTimeComparator);

        final LinkedList<IntervalLong> coveredTimeIntervals = new LinkedList<IntervalLong>();
        final List<TurnSnapshot> dataSet = new LinkedList<TurnSnapshot>();
        for (GunKdTreeEntry e : similarEntries) {
            boolean contained = false;
            final int eRoundTime = e.ts.roundTime;
            for (IntervalLong ival : coveredTimeIntervals) {
                if (ival.contains(eRoundTime)) {
                    contained = true;
                    break;
                }
            }
            if (!contained) {
                dataSet.add(e.ts);
                coveredTimeIntervals.add(new IntervalLong(eRoundTime - 10, eRoundTime + 10));
            }
            if (dataSet.size() > 10) {
                break;
            }
        }

        return dataSet;
    }

    public void addEntry(TurnSnapshot ts) {
        dataSource.addEntry(new GunKdTreeEntry(ts, dataSource.getAttributes()));
    }

    private static class DistTimeComparator implements Comparator<GunKdTreeEntry> {

        public int compare(GunKdTreeEntry o1, GunKdTreeEntry o2) {
            return Double.compare(o1.normalWeightedDistance, o2.normalWeightedDistance);
        }
    }

}
