/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Median {

    private List<Integer> values = new ArrayList<Integer>();

    public Median() {
    }

    public void addValue(int value) {
        values.add(value);
        Collections.sort(values);
    }

    public double getMediana() {
        if (values.size() == 0) {
            return 0;
        }
        if (values.size() == 1) {
            return values.get(0);
        }
        int idx = values.size() / 2 - 1;
        return (values.get(idx) + values.get(idx + 1)) / 2;
    }

    public String toString() {
        return String.format("Median = %10.5f", getMediana());
    }

    public Interval getRange(double width) {
        if (values.size() == 0) {
            return new Interval(0, 0);
        }
        if (values.size() == 1) {
            return new Interval(values.get(0), values.get(0));
        }
        if (values.size() == 2) {
            return new Interval(values.get(0), values.get(1));
        }
        int margin = (int) (values.size() * (1 - width) / 2);
        return new Interval(values.get(margin), values.get(values.size() - 1 - margin));
    }

}
