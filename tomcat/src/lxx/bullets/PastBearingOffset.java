/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.bullets;

import lxx.ts_log.TurnSnapshot;

/**
 * User: jdev
 * Date: 05.08.11
 */
public class PastBearingOffset {

    public final TurnSnapshot source;
    public final double bearingOffset;
    public final double danger;

    public PastBearingOffset(TurnSnapshot source, double bearingOffset, double danger) {
        this.source = source;
        this.bearingOffset = bearingOffset;
        this.danger = danger;
    }
}
