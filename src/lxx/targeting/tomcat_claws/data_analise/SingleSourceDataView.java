/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.targeting.tomcat_claws.data_analise;

import lxx.data_analysis.LocationFactory;
import lxx.data_analysis.kd_tree.GunKdTreeEntry;
import lxx.data_analysis.kd_tree.KdTreeAdapter;
import lxx.ts_log.TurnSnapshot;
import lxx.ts_log.attributes.Attribute;
import lxx.ts_log.attributes.AttributesManager;
import lxx.utils.AvgValue;
import lxx.utils.IntervalDouble;
import lxx.utils.IntervalLong;

import java.io.Serializable;
import java.util.*;

import static java.lang.Math.sqrt;

/**
 * User: jdev
 * Date: 17.06.11
 */
public class SingleSourceDataView implements DataView {

    private final LinkedList<TurnSnapshot> buffer = new LinkedList<TurnSnapshot>();

    private final KdTreeAdapter<GunKdTreeEntry> dataSource;
    private final String name;
    private final TimeDependencyType timeDependencyType;
    private final int bufferLimit;

    public SingleSourceDataView(Attribute[] attributes, String name, TimeDependencyType timeDependencyType, int size) {
        this(attributes, name, timeDependencyType, size, 0);
    }

    public SingleSourceDataView(Attribute[] attributes, String name, TimeDependencyType timeDependencyType, int treeLimit, int bufferLimit) {
        this.name = name;
        this.timeDependencyType = timeDependencyType;

        if (this.timeDependencyType == TimeDependencyType.DIRECT_HITS) {
            Attribute[] newAttrs = new Attribute[attributes.length + 1];
            System.arraycopy(attributes, 0, newAttrs, 0, attributes.length);
            newAttrs[newAttrs.length - 1] = AttributesManager.enemyHitsCollected;
            attributes = newAttrs;
        } else if (timeDependencyType == TimeDependencyType.REVERCE_WAVES) {
            Attribute[] newAttrs = new Attribute[attributes.length + 1];
            System.arraycopy(attributes, 0, newAttrs, 0, attributes.length);
            newAttrs[newAttrs.length - 1] = AttributesManager.enemyWavesCollected;
            attributes = newAttrs;
        }

        dataSource = new KdTreeAdapter<GunKdTreeEntry>(attributes, treeLimit);
        this.bufferLimit = bufferLimit;
    }

    public Collection<TurnSnapshot> getDataSet(TurnSnapshot ts) {
        final double[] normalLocation = LocationFactory.getNormalLocation(ts, dataSource.getAttributes());
        if (timeDependencyType == TimeDependencyType.REVERCE_WAVES) {
            normalLocation[normalLocation.length - 1] = 0;
        }
        final GunKdTreeEntry[] similarEntries = dataSource.getNearestNeighbours(normalLocation);

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
                coveredTimeIntervals.add(new IntervalLong(eRoundTime - 7, eRoundTime + 7));
            }
        }

        return dataSet;
    }

    public void addEntry(TurnSnapshot ts) {
        if (bufferLimit == 0) {
            dataSource.addEntry(new GunKdTreeEntry(ts, dataSource.getAttributes()));
        } else {
            buffer.add(ts);
            if (buffer.size() == bufferLimit) {
                dataSource.addEntry(new GunKdTreeEntry(buffer.removeFirst(), dataSource.getAttributes()));
            }
        }
    }

    public String getName() {
        return name;
    }

    public enum TimeDependencyType {

        NO,
        DIRECT_HITS,
        REVERCE_WAVES

    }

}
