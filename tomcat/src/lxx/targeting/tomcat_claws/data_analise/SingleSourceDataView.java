/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.targeting.tomcat_claws.data_analise;

import lxx.ts_log.TurnSnapshot;
import lxx.ts_log.attributes.Attribute;
import lxx.utils.Interval;
import lxx.utils.LXXUtils;
import lxx.utils.ps_tree.EntryMatch;
import lxx.utils.ps_tree.PSTree;
import lxx.utils.ps_tree.PSTreeEntry;

import java.io.Serializable;
import java.util.*;

import static java.lang.Math.round;

/**
 * User: jdev
 * Date: 17.06.11
 */
public class SingleSourceDataView implements DataView {

    private final PSTree<Serializable> dataSource;

    private final Map<Attribute, Integer> ranges;
    private final Attribute[] attributes;
    private final int roundsLimit;

    public SingleSourceDataView(Attribute[] attributes, Map<Attribute, Integer> ranges, int roundsLimit) {
        this.ranges = ranges;
        this.attributes = attributes;
        this.roundsLimit = roundsLimit;

        dataSource = new PSTree<Serializable>(attributes, 2, 0.0001);
    }

    public Collection<TurnSnapshot> getDataSet(TurnSnapshot ts) {
        List<EntryMatch<Serializable>> similarEntries = dataSource.getSimilarEntries(getLimits(ts), ts);
        similarEntries = filterOutByTime(similarEntries, ts);
        final List<TurnSnapshot> dataSet = new LinkedList<TurnSnapshot>();

        for (EntryMatch e : similarEntries) {
            dataSet.add(e.predicate);
        }

        return dataSet;
    }

    private List<EntryMatch<Serializable>> filterOutByTime(List<EntryMatch<Serializable>> similarSnapshots, TurnSnapshot ts) {
        final LinkedList<EntryMatch<Serializable>> res = new LinkedList<EntryMatch<Serializable>>();

        for (EntryMatch<Serializable> s : similarSnapshots) {
            if (ts.getRound() - s.predicate.getRound() > roundsLimit) {
                break;
            }
            res.add(s);

            if (s.predicate.getRound() == res.getLast().predicate.getRound() &&
                    s.predicate.getTime() + 5 > res.getLast().predicate.getTime()) {
                if (s.match < res.getLast().match) {
                    res.removeLast();
                    res.addLast(s);
                }
            }
        }

        return res;
    }

    public void addEntry(TurnSnapshot ts) {
        dataSource.addEntry(new PSTreeEntry<Serializable>(ts));
    }

    private Map<Attribute, Interval> getLimits(TurnSnapshot ts) {
        Map<Attribute, Interval> limits = new HashMap<Attribute, Interval>();

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
