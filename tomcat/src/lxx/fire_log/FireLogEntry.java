/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.fire_log;

import lxx.model.BattleSnapshot;
import lxx.utils.APoint;

import java.io.Serializable;

public class FireLogEntry<T extends Serializable> implements Serializable {

    public APoint sourcePos;
    public APoint targetPos;
    public BattleSnapshot predicate;

    public T result;

    public FireLogEntry(APoint sourcePos, APoint targetPos, BattleSnapshot predicate) {
        this.sourcePos = sourcePos;
        this.targetPos = targetPos;
        this.predicate = predicate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FireLogEntry)) return false;

        FireLogEntry that = (FireLogEntry) o;

        if (predicate != null ? !predicate.equals(that.predicate) : that.predicate != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return predicate != null ? predicate.hashCode() : 0;
    }
}
