package voidious.move;

import robocode.Rules;
import voidious.utils.DiaWave;

/**
 * Copyright (c) 2009-2010 - Voidious
 * <p/>
 * This software is provided 'as-is', without any express or implied
 * warranty. In no event will the authors be held liable for any damages
 * arising from the use of this software.
 * <p/>
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 * <p/>
 * 1. The origin of this software must not be misrepresented; you must not
 * claim that you wrote the original software.
 * <p/>
 * 2. Altered source versions must be plainly marked as such, and must not be
 * misrepresented as being the original software.
 * <p/>
 * 3. This notice may not be removed or altered from any source
 * distribution.
 */

public class DistanceNormal extends voidious.utils.DistanceFormula {
    public DistanceNormal() {
        weights = new double[]{4, 3, 3, 3, 2, 4, 1, 3, 2};
    }

    public double[] dataPointFromWave(DiaWave w, boolean aiming) {
        double[] point = new double[]{
                (Math.min(91, w.targetDistance / w.bulletSpeed) / 91),
                (Math.abs(w.targetVelocity) / 8),
                (Math.sin(w.targetRelativeHeading)),
                ((Math.cos(w.targetRelativeHeading) + 1) / 2),
                (((w.targetAccel / (w.targetAccel < 0 ? Rules.DECELERATION : Rules.ACCELERATION)) + 1) / 2),
                (Math.min(1.0, w.targetWallDistance) / 1.0),
                (Math.min(1.0, w.targetRevWallDistance) / 1.0),
                (Math.min(1, w.targetVchangeTime / (w.targetDistance / w.bulletSpeed))),
                (w.targetDl8t / 64)
        };

        return point;
    }
}
