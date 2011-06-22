/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.strategies.duel;

import lxx.Tomcat;
import lxx.office.Office;
import lxx.strategies.Movement;
import lxx.strategies.MovementDecision;
import lxx.targeting.Target;
import lxx.targeting.TargetManager;
import lxx.targeting.tomcat_eyes.TomcatEyes;

public class TakePositionMovement implements Movement {

    private final DistanceController distanceController;
    private final Tomcat robot;
    private final TargetManager targetManager;

    public TakePositionMovement(Office office, TomcatEyes tomcatEyes) {
        this.robot = office.getRobot();
        this.targetManager = office.getTargetManager();

        this.distanceController = new DistanceController(robot, office.getEnemyBulletManager(), targetManager, tomcatEyes);
    }

    public MovementDecision getMovementDecision() {
        final WaveSurfingMovement.OrbitDirection orbitDirection = selectOrbitDirection();
        final double desiredHeading = distanceController.getDesiredHeading(targetManager.getDuelOpponent(), robot.getState(), orbitDirection);
        return MovementDecision.toMovementDecision(robot.getState(), 8,
                robot.getState().getBattleField().smoothWalls(robot.getPosition(), desiredHeading, orbitDirection == WaveSurfingMovement.OrbitDirection.CLOCKWISE));
    }

    private WaveSurfingMovement.OrbitDirection selectOrbitDirection() {
        final double dangerCW = getDanger(WaveSurfingMovement.OrbitDirection.CLOCKWISE);
        final double dangerCCW = getDanger(WaveSurfingMovement.OrbitDirection.COUNTER_CLOCKWISE);

        return dangerCW < dangerCCW ? WaveSurfingMovement.OrbitDirection.CLOCKWISE : WaveSurfingMovement.OrbitDirection.COUNTER_CLOCKWISE;
    }

    private double getDanger(WaveSurfingMovement.OrbitDirection orbitDirection) {
        final Target duelOpponent = targetManager.getDuelOpponent();
        final double desiredHeading = distanceController.getDesiredHeading(targetManager.getDuelOpponent(), robot.getState(), orbitDirection);
        final double alpha = robot.getState().getBattleField().smoothWalls(robot, desiredHeading, orbitDirection == WaveSurfingMovement.OrbitDirection.CLOCKWISE);

        return 100D / robot.project(alpha, 28).aDistance(duelOpponent);
    }

}
