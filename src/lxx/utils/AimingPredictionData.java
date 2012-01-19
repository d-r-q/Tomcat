/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.utils;

import lxx.bullets.LXXBullet;
import lxx.paint.LXXGraphics;
import lxx.ts_log.TurnSnapshot;

/**
 * User: jdev
 * Date: 29.05.2010
 */
public interface AimingPredictionData {

    TurnSnapshot getTs();

    long getPredictionRoundTime();

    void paint(LXXGraphics g, LXXBullet bullet);

}
