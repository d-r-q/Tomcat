/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.strategies;

import lxx.Tomcat;
import lxx.office.Office;
import lxx.targeting.Target;
import lxx.utils.time_profiling.TimeProfileProperties;

public abstract class AbstractStrategy implements Strategy {

    protected final Tomcat robot;
    protected final Office office;

    protected AbstractStrategy(Tomcat robot, Office office) {
        this.robot = robot;
        this.office = office;
    }

    public TurnDecision makeDecision() {
        TimeProfileProperties.MOVEMENT_TIME.start();
        MovementDecision md = getMovementDecision();
        office.getTimeProfiler().stopAndSaveProperty(TimeProfileProperties.MOVEMENT_TIME);

        Target target = selectTarget();
        double firePower = selectFirePower(target);

        TimeProfileProperties.GUN_TIME.start();
        GunDecision gd = getGunDecision(target, firePower);
        office.getTimeProfiler().stopAndSaveProperty(TimeProfileProperties.GUN_TIME);

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
