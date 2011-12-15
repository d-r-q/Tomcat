/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.utils;

import lxx.ts_log.TurnSnapshot;

public class KdTreeEntry {

    public final TurnSnapshot turnSnapshot;

    public KdTreeEntry(TurnSnapshot turnSnapshot) {
        this.turnSnapshot = turnSnapshot;
    }
}
