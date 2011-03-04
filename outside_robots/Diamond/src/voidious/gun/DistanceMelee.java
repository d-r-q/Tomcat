package voidious.gun;

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

public class DistanceMelee extends voidious.utils.DistanceFormula {
    protected int _enemiesTotal;

    public DistanceMelee(int enemiesTotal) {
        _enemiesTotal = enemiesTotal;
        weights = new double[]{4, 6, 2, 4, 3, 1, 1, 1};
    }

    public double[] dataPointFromWave(DiaWave w, boolean aiming) {
//        double agForce = w.targetAgForce / DiamondFist.ANTIGRAV_MAX_FORCE;
        double[] point;
        point = new double[]{
                (Math.min(91, w.targetDistance / w.bulletSpeed) / 91),
//            (w.lateralVelocity() / 8),
                (Math.abs(w.targetVelocity) / 8),
//            (Math.sin(w.targetRelativeHeading)),
                ((Math.cos(w.targetRelativeHeading) + 1) / 2),
//            (((Math.sin(w.targetAgHeading) * agForce) + 1) / 2),
//            (((Math.cos(w.targetAgHeading) * agForce) + 1) / 2),
//            (Math.min(w.targetCornerDistance, 300) / 300),
//            ((Math.sin(w.targetCornerBearing) + 1) / 2),
//            ((Math.cos(w.targetCornerBearing) + 1) / 2),
                (Math.min(1.0, w.targetWallDistance)),
                (Math.min(1.0, w.targetRevWallDistance)),
                (w.targetDl8t / 64),
                (w.targetDl20t / 160),
//            (w.targetDl40t / 320),
                (Math.sqrt(((double) (w.enemiesAlive - 1)) / (Math.max(_enemiesTotal - 1, 1))))
        };
        return point;
    }
}
