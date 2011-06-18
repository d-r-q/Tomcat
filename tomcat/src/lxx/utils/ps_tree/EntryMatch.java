/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.utils.ps_tree;

import lxx.ts_log.TurnSnapshot;

public class EntryMatch<T> {

    public final TurnSnapshot predicate;
    public final T result;
    public final double match;

    public EntryMatch(T result, double match, TurnSnapshot predicate) {
        this.result = result;
        this.match = match;
        this.predicate = predicate;
    }
}
