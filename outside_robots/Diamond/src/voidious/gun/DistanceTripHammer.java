package voidious.gun;

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

public class DistanceTripHammer extends voidious.utils.DistanceFormula {
    public DistanceTripHammer() {
        weights = new double[]{7, 4, 2, 2, 2, 7, 4, 1.5, 0.4};
    }

    public double[] dataPointFromWave(DiaWave w, boolean aiming) {
        double[] point;

        point = new double[]{
                (Math.min(91, w.targetDistance / w.bulletSpeed) / 91),
//            (w.lateralVelocity() / 8),
                (Math.abs(w.targetVelocity) / 8),
                (Math.sin(w.targetRelativeHeading)),
                ((Math.cos(w.targetRelativeHeading) + 1) / 2),
                (((w.targetAccel / (w.targetAccel < 0 ? Rules.DECELERATION : Rules.ACCELERATION)) + 1) / 2),
                (Math.min(1.0, w.targetWallDistance)),
                (Math.min(1.0, w.targetRevWallDistance)),
                (Math.min(1.0, w.targetVchangeTime / (w.targetDistance / w.bulletSpeed)) / 1.0),
                (aiming ? 0 : w.virtuality())
        };
        return point;
    }
}
