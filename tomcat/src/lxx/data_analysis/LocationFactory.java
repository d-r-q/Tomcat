/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.data_analysis;

import lxx.ts_log.TurnSnapshot;
import lxx.ts_log.attributes.Attribute;

public class LocationFactory {

    public static double[] getPlainLocation(TurnSnapshot ts, Attribute[] attrs) {
        return getLocationImpl(ts, attrs, false);
    }

    public static double[] getNormalLocation(TurnSnapshot ts, Attribute[] attrs) {
        return getLocationImpl(ts, attrs, true);
    }

    private static double[] getLocationImpl(TurnSnapshot ts, Attribute[] attrs, boolean normalise) {
        final double[] location = new double[attrs.length];

        for (int i = 0; i < attrs.length; i++) {
            location[i] = ts.getAttrValue(attrs[i]) / (normalise ? attrs[i].maxRange.getLength() : 1);
        }

        return location;
    }

}
