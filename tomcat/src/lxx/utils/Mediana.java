/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Mediana {

    private List<Double> values = new ArrayList<Double>();

    public Mediana() {
    }

    public void addValue(double value) {
        values.add(value);
        Collections.sort(values);
    }

    public double getMediana() {
        if (values.size() == 1) {
            return values.get(0);
        }
        int idx = values.size() / 2 - 1;
        return (values.get(idx) + values.get(idx + 1)) / 2;
    }

    public String toString() {
        return String.format("Mediana = %10.5f", getMediana());
    }
}
