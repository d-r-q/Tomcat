/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.utils;

import lxx.ts_log.TurnSnapshot;

public class LoadedKdTreeEntry<T> extends KdTreeEntry {

    public final T result;

    public LoadedKdTreeEntry(TurnSnapshot turnSnapshot, T result) {
        super(turnSnapshot);
        this.result = result;
    }
}
