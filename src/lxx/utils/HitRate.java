/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.utils;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * User: jdev
 * Date: 15.02.2010
 */
public class HitRate {

    private static final NumberFormat format;

    static {
        format = new DecimalFormat();
        format.setMaximumFractionDigits(1);
        format.setMaximumIntegerDigits(3);
    }

    private int hitCount = 0;
    private int missCount = 0;

    public void hit() {
        hitCount++;
    }

    public void miss() {
        missCount++;
    }

    public int getMissCount() {
        return missCount;
    }

    public String toString() {
        return "(" + hitCount + "/" + (hitCount + missCount) + ") = " + format.format(getHitRate() * 100) + "%";
    }

    public double getHitRate() {
        if (hitCount == 0 && missCount == 0) {
            return 0;
        }
        return (double) hitCount / (double) (hitCount + missCount);
    }

    public int getFireCount() {
        return hitCount + missCount;
    }

    public int getHitCount() {
        return hitCount;
    }
}
