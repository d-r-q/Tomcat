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

    public OrbitDirection getOpposite() {
        switch (this) {
            case CLOCKWISE:
                return COUNTER_CLOCKWISE;
            case COUNTER_CLOCKWISE:
                return CLOCKWISE;
            default:
                throw new IllegalArgumentException("Unsupported orbit direction: " + this);
        }
    }

}
