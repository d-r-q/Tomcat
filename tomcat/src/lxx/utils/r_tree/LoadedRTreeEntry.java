/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.utils.r_tree;

import lxx.ts_log.TurnSnapshot;

public class LoadedRTreeEntry<E> extends RTreeEntry {

    public final E data;

    public LoadedRTreeEntry(TurnSnapshot location, E data) {
        super(location);
        this.data = data;
    }
}
