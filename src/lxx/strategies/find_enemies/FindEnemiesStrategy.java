/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.strategies.find_enemies;

import lxx.Tomcat;
import lxx.targeting.TargetManager;
import lxx.strategies.MovementDecision;
import lxx.strategies.Strategy;
import lxx.strategies.TurnDecision;
import robocode.Rules;
import robocode.util.Utils;

import static java.lang.Math.signum;

public class FindEnemiesStrategy implements Strategy {

    private final Tomcat robot;
    private final TargetManager targetManager;
    private final int enemiesCount;
    private final int turnDirection;

    public FindEnemiesStrategy(Tomcat robot, TargetManager targetManager, int enemiesCount) {
        this.robot = robot;
        this.targetManager = targetManager;
        this.enemiesCount = enemiesCount;

        turnDirection = (int) signum(Utils.normalRelativeAngle(robot.angleTo(robot.battleField.center) - robot.getRadarHeadingRadians()));
    }

    public boolean match() {
        return robot.getTime() < 20 && targetManager.getAliveTargetCount() < enemiesCount;
    }

    public TurnDecision makeDecision() {
        return new TurnDecision(
                new MovementDecision(0, Rules.MAX_TURN_RATE_RADIANS * turnDirection),
                Rules.GUN_TURN_RATE_RADIANS * turnDirection, 0,
                Rules.RADAR_TURN_RATE_RADIANS * turnDirection,
                null, null);
    }

}
