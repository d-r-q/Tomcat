/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.ts_log;

import lxx.EnemySnapshotImpl;
import lxx.MySnapshotImpl;
import lxx.ts_log.attributes.Attribute;
import lxx.utils.LXXUtils;

import java.io.Serializable;

/**
 * User: jdev
 * Date: 10.03.2010
 */
public class TurnSnapshot implements Serializable {
    
    private final long time;
    private final int round;
    public final MySnapshotImpl mySnapshot;
    public final EnemySnapshotImpl enemySnapshot;

    // access optimisation
    public TurnSnapshot next;
    public final int roundTime;

    public TurnSnapshot(long time, int round, MySnapshotImpl mySnapshot, EnemySnapshotImpl enemySnapshot) {
        this.time = time;
        this.round = round;
        this.mySnapshot = mySnapshot;
        this.enemySnapshot = enemySnapshot;
        this.roundTime = LXXUtils.getRoundTime(time, round);
    }

    public double getAttrValue(Attribute a) {
        // todo: fix me!
        final double res = a.extractor.getAttributeValue(enemySnapshot, mySnapshot);
        a.actualRange.extend(res);
        return res;
    }

    public long getTime() {
        return time;
    }

    public int getRound() {
        return round;
    }

    public void setNext(TurnSnapshot next) {
        if (time + 1 != next.time) {
            throw new RuntimeException("Snapshot skipped");
        }
        this.next = next;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TurnSnapshot that = (TurnSnapshot) o;

        return roundTime == that.roundTime;

    }

    @Override
    public int hashCode() {
        return roundTime;
    }

}
