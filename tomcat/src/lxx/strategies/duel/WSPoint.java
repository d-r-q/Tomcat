/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.strategies.duel;

import lxx.utils.APoint;
import lxx.utils.LXXPoint;

import static java.lang.Math.signum;

class WSPoint extends LXXPoint implements Comparable<WSPoint> {

    public final PointDanger danger;
    public OrbitDirection orbitDirection;

    WSPoint(APoint point, PointDanger danger) {
        super(point);
        this.danger = danger;
    }

    public int compareTo(WSPoint o) {
        return (int) signum(danger.getDanger() - o.danger.getDanger());
    }
}
