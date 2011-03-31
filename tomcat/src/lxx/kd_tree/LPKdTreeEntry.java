/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.kd_tree;

import lxx.model.TurnSnapshot;

import java.io.Serializable;

public class LPKdTreeEntry<T extends Serializable> implements Serializable {

    public TurnSnapshot predicate;

    public T result;

    public LPKdTreeEntry(TurnSnapshot predicate) {
        this.predicate = predicate;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LPKdTreeEntry)) return false;

        LPKdTreeEntry that = (LPKdTreeEntry) o;

        if (predicate != null ? !predicate.equals(that.predicate) : that.predicate != null) return false;

        return true;
    }

    public int hashCode() {
        return predicate != null ? predicate.hashCode() : 0;
    }
}
