/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.utils;

import java.util.HashMap;
import java.util.Map;

public class MostUsedValue {

    private final Map<Integer, IntWrapper> valuesUsage = new HashMap<Integer, IntWrapper>();

    private int mostUsedValue = 0;
    private int maxUsage = 0;

    public void addUsage(int value) {
        IntWrapper iw = valuesUsage.get(value);
        if (iw == null) {
            iw = new IntWrapper();
            valuesUsage.put(value, iw);
        }

        iw.value++;
        if (iw.value > maxUsage) {
            maxUsage = iw.value;
            mostUsedValue = value;
        }
    }

    public int getMostUsedValue() {
        return mostUsedValue;
    }

    public String toString() {
        return String.format("Most used value = %10.5d", getMostUsedValue());
    }
}
