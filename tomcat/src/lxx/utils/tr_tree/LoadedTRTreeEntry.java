/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.utils.tr_tree;

import lxx.ts_log.TurnSnapshot;

public class LoadedTRTreeEntry<E> extends TRTreeEntry {

    public final E data;

    public LoadedTRTreeEntry(TurnSnapshot location, E data) {
        super(location);
        this.data = data;
    }
}
