/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.strategies;

import lxx.targeting.Target;
import lxx.utils.AimingPredictionData;

/**
 * User: jdev
 * Date: 12.02.2011
 */
public class TurnDecision {

    private final MovementDecision movementDecision;

    private final Double gunTurnRate;
    private final double firePower;
    private final Target target;
    private final AimingPredictionData aimAimPredictionData;

    private final Double radarTurnRate;

    public TurnDecision(MovementDecision movementDecision, Double gunTurnRate,
                        double firePower, Double radarTurnRate, Target target,
                        AimingPredictionData aimAimPredictionData) {
        this.movementDecision = movementDecision;
        this.gunTurnRate = gunTurnRate;
        this.firePower = firePower;
        this.radarTurnRate = radarTurnRate;
        this.target = target;
        this.aimAimPredictionData = aimAimPredictionData;
    }

    public MovementDecision getMovementDecision() {
        return movementDecision;
    }

    public Double getGunTurnRate() {
        return gunTurnRate;
    }

    public double getFirePower() {
        return firePower;
    }

    public Double getRadarTurnRate() {
        return radarTurnRate;
    }

    public Target getTarget() {
        return target;
    }

    public AimingPredictionData getAimAimPredictionData() {
        return aimAimPredictionData;
    }

}
