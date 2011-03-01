/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.fire_log;

import lxx.utils.DeltaVector;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * User: jdev
 * Date: 28.09.2010
 */
public class Pattern implements Serializable {

    private final List<DeltaVector> vectors = new ArrayList<DeltaVector>();

    public void add(DeltaVector deltaVector) {
        vectors.add(deltaVector);
    }

    public DeltaVector get(int i) {
        return vectors.get(i);
    }

    public int size() {
        return vectors.size();
    }
}
