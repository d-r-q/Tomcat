/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.targeting.tomcat_claws.data_analise;

import lxx.ts_log.TurnSnapshot;
import lxx.ts_log.attributes.Attribute;
import lxx.utils.Interval;
import lxx.utils.LXXUtils;
import lxx.utils.sp_tree.SPTree;
import lxx.utils.sp_tree.SPTreeEntry;

import java.util.*;

import static java.lang.Math.round;

/**
 * User: jdev
 * Date: 17.06.11
 */
public class SingleSourceDataView implements DataView {

    private final SPTree<SPTreeEntry> dataSource;

    private final Map<Attribute, Integer> ranges;
    private final Attribute[] attributes;
    private final int roundsLimit;
    private final Map<Attribute, Interval> limits = new HashMap<Attribute, Interval>();

    public SingleSourceDataView(Attribute[] attributes, Map<Attribute, Integer> ranges, int roundsLimit) {
        this.ranges = ranges;
        this.attributes = attributes;
        this.roundsLimit = roundsLimit;

        dataSource = new SPTree<SPTreeEntry>(attributes);
    }

    public Collection<TurnSnapshot> getDataSet(TurnSnapshot ts) {
        final Collection<SPTreeEntry> similarEntries = dataSource.rangeSearch(ts, getLimits(ts), new ByTimeComparator());
        final List<TurnSnapshot> dataSet = new LinkedList<TurnSnapshot>();

        for (SPTreeEntry e : filterOutByTime(similarEntries, ts)) {
            dataSet.add(e.location);
        }

        return dataSet;
    }

    private List<SPTreeEntry> filterOutByTime(Collection<SPTreeEntry> entries, TurnSnapshot ts) {
        List<SPTreeEntry> similarSnapshots = new ArrayList<SPTreeEntry>(entries);
        for (int i = 0; i < similarSnapshots.size() - 1; i++) {
            final SPTreeEntry em1 = similarSnapshots.get(i);
            if (ts.getRound() - em1.location.getRound() > roundsLimit) {
                similarSnapshots.remove(i);
                i--;
                continue;
            }

            final SPTreeEntry em2 = similarSnapshots.get(i + 1);
            if (em1.location.getRound() == em2.location.getRound() &&
                    em1.location.getTime() + 5 > em2.location.getTime()) {
                if (em1.distance < em2.distance) {
                    similarSnapshots.remove(i + 1);
                } else {
                    similarSnapshots.remove(i);
                }
                i--;
            }
        }
        return similarSnapshots;
    }

    public void addEntry(TurnSnapshot ts) {
        dataSource.add(new SPTreeEntry(ts));
    }

    private Map<Attribute, Interval> getLimits(TurnSnapshot ts) {
        for (Attribute a : attributes) {
            Interval interval = new Interval(
                    (int) round(LXXUtils.limit(a, ts.getAttrValue(a) - ranges.get(a))),
                    (int) round(LXXUtils.limit(a, ts.getAttrValue(a) + ranges.get(a)))
            );
            limits.put(a, interval);
        }

        return limits;
    }

}
