package lxx.bullets;

import lxx.ts_log.TurnSnapshot;

/**
 * User: jdev
 * Date: 05.08.11
 */
public class PastBearingOffset {

    public final TurnSnapshot source;
    public final double bearingOffset;

    public PastBearingOffset(TurnSnapshot source, double bearingOffset) {
        this.source = source;
        this.bearingOffset = bearingOffset;
    }
}
