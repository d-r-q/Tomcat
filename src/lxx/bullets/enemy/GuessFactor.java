/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.bullets.enemy;

import java.io.Serializable;

public class GuessFactor implements Serializable {

    public final double guessFactor;

    public GuessFactor(double guessFactor) {
        this.guessFactor = guessFactor;
    }

}
