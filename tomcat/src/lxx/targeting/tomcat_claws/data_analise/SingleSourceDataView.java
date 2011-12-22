/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.targeting.tomcat_claws.data_analise;

import ags.utils.KdTree;
import lxx.ts_log.TurnSnapshot;
import lxx.ts_log.attributes.Attribute;
import lxx.utils.IntervalDouble;
import lxx.utils.IntervalLong;
import lxx.utils.KdTreeAdapter;
import lxx.utils.KdTreeEntry;

import java.util.*;

import static java.lang.Math.sqrt;

/**
 * User: jdev
 * Date: 17.06.11
 */
public class SingleSourceDataView implements DataView {

    private static final DistTimeComparator distTimeComparator = new DistTimeComparator();

    private final KdTreeAdapter<KdTreeEntry> dataSource;

    private final double[] weights;

    public SingleSourceDataView(Attribute[] attributes, double[] weights) {
        this.weights = weights;
        dataSource = new KdTreeAdapter<KdTreeEntry>(attributes, 50000);
    }

    public Collection<TurnSnapshot> getDataSet(TurnSnapshot ts) {
        final List<KdTree.Entry<KdTreeEntry>> similarEntries = dataSource.getNearestNeighbours(ts);
        final IntervalLong timeInterval = new IntervalLong(Integer.MAX_VALUE, Integer.MIN_VALUE);
        final IntervalDouble distInterval = new IntervalDouble(Integer.MAX_VALUE, Integer.MIN_VALUE);
        for (KdTree.Entry<KdTreeEntry> entry : similarEntries) {
            final int timeDiff = ts.roundTime - entry.value.turnSnapshot.roundTime;
            if (timeDiff < 0) {
                throw new RuntimeException("Something wrong");
            }
            timeInterval.extend(timeDiff);
            distInterval.extend(entry.distance);
        }

        for (KdTree.Entry<KdTreeEntry> e : similarEntries) {
            final double timeDist = (e.value.turnSnapshot.roundTime - timeInterval.a) / (timeInterval.getLength()) * weights[0];
            final double locDist = (e.distance - distInterval.a) / (distInterval.getLength()) * weights[1];
            e.distance = sqrt(timeDist * timeDist + locDist * locDist);
        }
        Collections.sort(similarEntries, distTimeComparator);

        final LinkedList<IntervalLong> coveredTimeIntervals = new LinkedList<IntervalLong>();
        final List<TurnSnapshot> dataSet = new LinkedList<TurnSnapshot>();
        for (KdTree.Entry<KdTreeEntry> e : similarEntries) {
            boolean contained = false;
            final int eRoundTime = e.value.turnSnapshot.roundTime;
            for (IntervalLong ival : coveredTimeIntervals) {
                if (ival.contains(eRoundTime)) {
                    contained = true;
                    break;
                }
            }
            if (!contained) {
                dataSet.add(e.value.turnSnapshot);
                coveredTimeIntervals.add(new IntervalLong(eRoundTime - 10, eRoundTime + 10));
            }
            if (dataSet.size() > 10) {
                break;
            }
        }

        return dataSet;
    }

    public void addEntry(TurnSnapshot ts) {
        dataSource.addEntry(new KdTreeEntry(ts));
    }

    private static class DistTimeComparator implements Comparator<KdTree.Entry<KdTreeEntry>> {

        public int compare(KdTree.Entry<KdTreeEntry> o1, KdTree.Entry<KdTreeEntry> o2) {
            return Double.compare(o1.distance, o2.distance);
        }
    }

}
