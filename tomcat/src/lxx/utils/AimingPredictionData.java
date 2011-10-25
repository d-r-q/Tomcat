/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.utils;

import lxx.bullets.LXXBullet;
import lxx.paint.LXXGraphics;

/**
 * User: jdev
 * Date: 29.05.2010
 */
public interface AimingPredictionData {

    long getPredictionRoundTime();

    void paint(LXXGraphics g, LXXBullet bullet);

}
