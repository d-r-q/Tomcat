/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.segmentation_tree;

import lxx.model.TurnSnapshot;

import java.io.Serializable;

public class SegmentationTreeEntry<T extends Serializable> implements Serializable {

    public TurnSnapshot predicate;

    public T result;

    public SegmentationTreeEntry(TurnSnapshot predicate) {
        this.predicate = predicate;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SegmentationTreeEntry)) return false;

        SegmentationTreeEntry that = (SegmentationTreeEntry) o;

        if (predicate != null ? !predicate.equals(that.predicate) : that.predicate != null) return false;

        return true;
    }

    public int hashCode() {
        return predicate != null ? predicate.hashCode() : 0;
    }
}
