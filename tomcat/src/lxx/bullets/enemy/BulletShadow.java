/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.bullets.enemy;

import lxx.utils.IntervalDouble;

public class BulletShadow extends IntervalDouble {

    public boolean isPassed = false;

    public BulletShadow(double a, double b) {
        super(a, b);
    }
}
