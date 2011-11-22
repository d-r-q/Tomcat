/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.strategies.duel;

import lxx.utils.APoint;
import lxx.utils.LXXPoint;

class WSPoint extends LXXPoint {

    public final PointDanger danger;

    WSPoint(APoint point, PointDanger danger) {
        super(point);
        this.danger = danger;
    }
}
