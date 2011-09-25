/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.utils.sp_tree;

import lxx.ts_log.TurnSnapshot;

/**
 * User: jdev
 * Date: 24.09.11
 */
public class SPTreeEntry implements Comparable<SPTreeEntry> {

    public final TurnSnapshot location;
    public double distance;

    public SPTreeEntry(TurnSnapshot location) {
        this.location = location;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public int compareTo(SPTreeEntry o) {
        return distance != o.distance
                ? (distance < o.distance ? -1 : 1)
                : (o.location.roundTime - location.roundTime);
    }
}
