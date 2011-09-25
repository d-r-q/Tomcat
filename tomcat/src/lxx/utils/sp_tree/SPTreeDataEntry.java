/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.utils.sp_tree;

import lxx.ts_log.TurnSnapshot;

/**
 * User: jdev
 * Date: 25.09.11
 */
public class SPTreeDataEntry<T> extends SPTreeEntry {

    public T data;

    public SPTreeDataEntry(TurnSnapshot location) {
        super(location);
    }
}
