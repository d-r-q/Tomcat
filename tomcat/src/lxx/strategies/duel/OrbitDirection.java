/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.strategies.duel;

public enum OrbitDirection {

    CLOCKWISE(1),
    COUNTER_CLOCKWISE(-1);

    public final int sign;

    OrbitDirection(int sign) {
        this.sign = sign;
    }
}
