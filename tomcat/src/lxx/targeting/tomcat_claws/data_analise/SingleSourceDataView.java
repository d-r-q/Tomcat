/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.targeting.tomcat_claws.data_analise;

import lxx.ts_log.TurnSnapshot;
import lxx.ts_log.attributes.Attribute;
import lxx.ts_log.attributes.AttributesManager;
import lxx.utils.Interval;
import lxx.utils.LXXUtils;
import lxx.utils.ps_tree.PSTree;
import lxx.utils.ps_tree.PSTreeEntry;

import java.io.Serializable;
import java.util.*;

import static java.lang.Math.abs;
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
        final List<TurnSnapshot> dataSet = new LinkedList<TurnSnapshot>();

        List<PSTreeEntry<Serializable>> similarEntries = dataSource.getSimilarEntries(getLimits(ts));
        if (similarEntries.size() == 0) {
            return dataSet;
        }

        similarEntries = filterOutByTime(similarEntries, ts);

        for (PSTreeEntry e : similarEntries) {
            dataSet.add(e.predicate);
        }

        return dataSet;
    }

    private LinkedList<PSTreeEntry<Serializable>> filterOutByTime(List<PSTreeEntry<Serializable>> similarSnapshots, TurnSnapshot ts) {
        final LinkedList<PSTreeEntry<Serializable>> res = new LinkedList<PSTreeEntry<Serializable>>();
        Collections.sort(similarSnapshots, new ByTimeComparator());

        final int[] indexes = new int[attributes.length];
        final double[] factors = new double[AttributesManager.attributesCount()];
        for (int i = 0; i < indexes.length; i++) {
            indexes[i] = attributes[i].getId();
            factors[attributes[i].getId()] = 1D / attributes[i].getActualRange();
        }


        res.add(similarSnapshots.get(0));
        for (int i = 1; i < similarSnapshots.size(); i++) {
            final PSTreeEntry<Serializable> em1 = res.getLast();
            final PSTreeEntry<Serializable> em2 = similarSnapshots.get(i);
            if (ts.getRound() - em2.predicate.getRound() > roundsLimit) {
                break;
            }

            if (em1.predicate.getRound() == em2.predicate.getRound() &&
                    abs(em1.predicate.getTime() - em2.predicate.getTime()) < 5) {
                if (LXXUtils.factoredEuqDistance(indexes, em1.predicate.toArray(), ts.toArray(), factors) >
                        LXXUtils.factoredEuqDistance(indexes, em2.predicate.toArray(), ts.toArray(), factors)) {
                    res.removeLast();
                    res.add(em2);
                }
            } else {
                res.add(em2);
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
