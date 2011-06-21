/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.strategies.duel;

import lxx.Tomcat;
import lxx.office.Office;
import lxx.strategies.Movement;
import lxx.strategies.MovementDecision;
import lxx.targeting.TargetManager;
import lxx.utils.LXXConstants;

public class TakePositionMovement implements Movement {

    private final DistanceController distanceController;
    private final Tomcat robot;
    private final TargetManager targetManager;

    public TakePositionMovement(Office office) {
        this.robot = office.getRobot();
        this.targetManager = office.getTargetManager();

        this.distanceController = new DistanceController(robot, office.getEnemyBulletManager(), targetManager);
    }

    public MovementDecision getMovementDecision() {
        return MovementDecision.toMovementDecision(robot.getState(), 8,
                distanceController.getDesiredHeading(targetManager.getDuelOpponent(), robot.getState(), selectOrbitDirection()));
    }

    private WaveSurfingMovement.OrbitDirection selectOrbitDirection() {
        final double dangerCW = getDanger(WaveSurfingMovement.OrbitDirection.CLOCKWISE);
        final double dangerCCW = getDanger(WaveSurfingMovement.OrbitDirection.COUNTER_CLOCKWISE);

        return dangerCW < dangerCCW ? WaveSurfingMovement.OrbitDirection.CLOCKWISE : WaveSurfingMovement.OrbitDirection.COUNTER_CLOCKWISE;
    }

    private double getDanger(WaveSurfingMovement.OrbitDirection orbitDirection) {
        final double alpha = targetManager.getDuelOpponent().angleTo(robot) + LXXConstants.RADIANS_90 * orbitDirection.sign;

        return robot.project(alpha, 16).aDistance(robot.getState().getBattleField().center);
    }

}
