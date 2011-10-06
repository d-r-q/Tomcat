/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.targeting.tomcat_claws.data_analise;

import ags.utils.KdTree;
import lxx.ts_log.TurnSnapshot;
import lxx.ts_log.attributes.Attribute;
import lxx.utils.KdTreeAdapter;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * User: jdev
 * Date: 17.06.11
 */
public class SingleSourceDataView implements DataView {

    private final KdTreeAdapter<KdTreeAdapter.KdTreeEntry> dataSource;

    private final double[] weights;

    public SingleSourceDataView(Attribute[] attributes, double[] weights) {
        this.weights = weights;
        dataSource = new KdTreeAdapter<KdTreeAdapter.KdTreeEntry>(attributes);
    }

    public Collection<TurnSnapshot> getDataSet(TurnSnapshot ts) {
        final List<KdTree.Entry<KdTreeAdapter.KdTreeEntry>> similarEntries = dataSource.getNearestNeighboursS(ts, weights);
        final List<TurnSnapshot> dataSet = new LinkedList<TurnSnapshot>();

        for (KdTree.Entry<KdTreeAdapter.KdTreeEntry> e : similarEntries) {
            dataSet.add(e.value.turnSnapshot);
            if (dataSet.size() > 10) {
                break;
            }
        }

        return dataSet;
    }

    public void addEntry(TurnSnapshot ts) {
        dataSource.addEntry(new KdTreeAdapter.KdTreeEntry(ts));
    }

}
