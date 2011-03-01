/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.strategies;

import lxx.Primarch;
import lxx.targeting.Target;

public abstract class AbstractStrategy implements Strategy {

    private static final double DEFAULT_FIRE_POWER = 1.5;
    protected final Primarch robot;

    protected AbstractStrategy(Primarch robot) {
        this.robot = robot;
    }

    public TurnDecision makeDecision() {
        MovementDecision md;
        try {
            md = getMovementDecision();
        } catch (Throwable t) {
            t.printStackTrace();
            md = new MovementDecision(0, 0, MovementDecision.MovementDirection.FORWARD);
        }
        Target target = null;
        try {
            target = selectTarget();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        double firePower = DEFAULT_FIRE_POWER;
        try {
            firePower = selectFirePower(target);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        GunDecision gd;
        try {
            gd = getGunDecision(target, firePower);
        } catch (Throwable t) {
            t.printStackTrace();
            gd = new GunDecision(0, null);
        }
        return new TurnDecision(md,
                gd.getGunTurnAngleRadians(), firePower,
                getRadarTurnAngleRadians(), target, gd.getAimPredictionData());
    }

    protected abstract MovementDecision getMovementDecision();

    protected abstract double selectFirePower(Target t);

    protected abstract Target selectTarget();

    protected abstract GunDecision getGunDecision(Target target, double firePower);

    protected abstract double getRadarTurnAngleRadians();

}
