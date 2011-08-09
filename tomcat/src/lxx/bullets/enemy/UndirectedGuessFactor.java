/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.bullets.enemy;

import java.io.Serializable;

class UndirectedGuessFactor implements Serializable {

    public final double guessFactor;
    public final double lateralDirection;

    UndirectedGuessFactor(double guessFactor, double lateralDirection) {
        this.guessFactor = guessFactor;
        this.lateralDirection = lateralDirection;
    }
}
