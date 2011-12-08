/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.strategies;

import lxx.Tomcat;
import lxx.targeting.Target;

public abstract class AbstractStrategy implements Strategy {

    protected final Tomcat robot;

    protected AbstractStrategy(Tomcat robot) {
        this.robot = robot;
    }

    public TurnDecision makeDecision() {
        MovementDecision md = getMovementDecision();
        Target target = selectTarget();
        double firePower = selectFirePower(target);
        GunDecision gd = getGunDecision(target, firePower);
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
