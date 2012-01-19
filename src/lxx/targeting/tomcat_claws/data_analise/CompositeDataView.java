/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.targeting.tomcat_claws.data_analise;

import lxx.ts_log.TurnSnapshot;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * User: jdev
 * Date: 18.06.11
 */
public class CompositeDataView implements DataView {

    private final List<DataView> underlyingDataViews;

    public CompositeDataView(DataView... underlyingDataViews) {
        this.underlyingDataViews = Arrays.asList(underlyingDataViews);
    }

    public Collection<TurnSnapshot> getDataSet(TurnSnapshot ts) {
        final List<TurnSnapshot> dataSet = new LinkedList<TurnSnapshot>();

        for (DataView dv : underlyingDataViews) {
            dataSet.addAll(dv.getDataSet(ts));
        }

        return dataSet;
    }

    public void addEntry(TurnSnapshot ts) {
        // data to underlying data views added by DataViewManager
    }
}
