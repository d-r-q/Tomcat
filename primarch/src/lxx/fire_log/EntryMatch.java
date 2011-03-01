/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.fire_log;

import lxx.model.BattleSnapshot;

public class EntryMatch<T> {

    public final BattleSnapshot predicate;
    public final T result;
    public final double match;

    public EntryMatch(T result, double match, BattleSnapshot predicate) {
        this.result = result;
        this.match = match;
        this.predicate = predicate;
    }
}
