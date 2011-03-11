/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Median {

    private final List<Integer> values = new ArrayList<Integer>();

    private int median;

    public void addValue(int value) {
        if (values.size() == 31) {
            if (value < median) {
                values.remove(0);
            } else {
                values.remove(values.size() - 1);
            }
        }
        if (value >= median) {
            values.add(value);
        } else {
            values.add(0, median);
        }
        Collections.sort(values);
        median = calculateMedian();
    }

    private int calculateMedian() {
        final int size = values.size();
        if (size % 2 == 1) {
            return values.get(size / 2);
        } else if (size == 0) {
            return 0;
        } else {
            int idx = size / 2 - 1;
            return (values.get(idx) + values.get(idx + 1)) / 2;
        }
    }

    public int getMedian() {
        return median;
    }

    public String toString() {
        return String.format("Median = %d", median);
    }

}
