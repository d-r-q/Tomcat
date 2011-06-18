package lxx.targeting.tomcat_claws.data_analise;

import lxx.ts_log.TurnSnapshot;

import java.util.*;

/**
 * User: jdev
 * Date: 18.06.11
 */
public class CompositeDataView implements DataView {

    private final List<DataView> underlyingDataViews;

    public CompositeDataView(DataView ... underlyingDataViews) {
        this.underlyingDataViews = Arrays.asList(underlyingDataViews);
    }

    public Set<TurnSnapshot> getDataSet(TurnSnapshot ts) {
        final Set<TurnSnapshot> dataSet = new HashSet<TurnSnapshot>();

        for (DataView dv : underlyingDataViews) {
            dataSet.addAll(dv.getDataSet(ts));
        }

        return dataSet;
    }

    public void addEntry(TurnSnapshot ts) {
        // data to underlying data views added by DataViewManager
    }
}
