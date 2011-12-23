/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.data_analysis.kd_tree;

import lxx.data_analysis.LocationFactory;
import lxx.data_analysis.LxxDataPoint;
import lxx.ts_log.TurnSnapshot;
import lxx.ts_log.attributes.Attribute;

public class GunKdTreeEntry extends LxxDataPoint<Object> {

    public double distance;
    public double normalWeightedDistance;

    public GunKdTreeEntry(TurnSnapshot ts, Attribute[] attrs) {
        super(LocationFactory.getNormalLocation(ts, attrs), ts, attrs);
    }

}
