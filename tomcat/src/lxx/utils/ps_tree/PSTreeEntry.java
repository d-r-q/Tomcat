/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.utils.ps_tree;

import lxx.ts_log.TurnSnapshot;

import java.io.Serializable;

public class PSTreeEntry<T extends Serializable> implements Serializable {

    public TurnSnapshot predicate;

    public T result;

    public PSTreeEntry(TurnSnapshot predicate) {
        this.predicate = predicate;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PSTreeEntry)) return false;

        PSTreeEntry that = (PSTreeEntry) o;

        if (predicate != null ? !predicate.equals(that.predicate) : that.predicate != null) return false;

        return true;
    }

    public int hashCode() {
        return predicate != null ? predicate.hashCode() : 0;
    }
}
