/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.strategies;

import lxx.utils.AimingPredictionData;

public class GunDecision {

    private final double gunTurnAngleRadians;
    private final AimingPredictionData aimPredictionData;

    public GunDecision(double gunTurnAngleRadians, AimingPredictionData aimPredictionData) {
        this.gunTurnAngleRadians = gunTurnAngleRadians;
        this.aimPredictionData = aimPredictionData;
    }

    public double getGunTurnAngleRadians() {
        return gunTurnAngleRadians;
    }

    public AimingPredictionData getAimPredictionData() {
        return aimPredictionData;
    }
}
