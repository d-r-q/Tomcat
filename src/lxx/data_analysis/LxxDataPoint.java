/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.data_analysis;

import lxx.ts_log.TurnSnapshot;
import lxx.ts_log.attributes.Attribute;

public class LxxDataPoint<T> extends DataPoint {

    public final TurnSnapshot ts;
    public final T payload;

    public LxxDataPoint(double[] location, TurnSnapshot ts, T payload) {
        super(location);
        this.ts = ts;
        this.payload = payload;
    }

    public static <T> LxxDataPoint createPlainPoint(TurnSnapshot ts, T payload, Attribute... attrs) {
        return new LxxDataPoint<T>(LocationFactory.getPlainLocation(ts, attrs), ts, payload);
    }

}
