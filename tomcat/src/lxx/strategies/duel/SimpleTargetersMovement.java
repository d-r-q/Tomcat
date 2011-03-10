/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.strategies.duel;

import lxx.Tomcat;
import lxx.office.EnemyBulletManager;
import lxx.office.TargetManager;
import lxx.targeting.Target;
import lxx.targeting.tomcat_eyes.TomcatEyes;
import lxx.utils.APoint;
import lxx.utils.LXXConstants;
import lxx.utils.LXXRobotState;
import robocode.util.Utils;

import static java.lang.Math.round;

public class SimpleTargetersMovement extends WaveSurfingMovement {

    private static final int DEFAULT_DISTANCE_AGAINST_SIMPLE = 300;
    private static final double FEAR_DISTANCE = 150;

    public SimpleTargetersMovement(Tomcat robot, TargetManager targetManager, EnemyBulletManager enemyBulletManager, TomcatEyes tomcatEyes) {
        super(robot, targetManager, enemyBulletManager, tomcatEyes);
    }

    protected double getEnemyPreferredDistance(Target opponent) {
        return DEFAULT_DISTANCE_AGAINST_SIMPLE;
    }

    protected double getPointDanger(APoint pnt, Target opponent) {
        double danger = 0;

        final double distanceToCenterDanger = round(pnt.aDistance(robot.battleField.center) / 10);
        danger += distanceToCenterDanger;
        final double distanceEnemyDanger = round(DEFAULT_DISTANCE_AGAINST_SIMPLE / pnt.aDistance(opponent) * 10);
        danger += distanceEnemyDanger;

        return danger;
    }

    protected double getTargetHeading(APoint surfPoint, LXXRobotState robot, OrbitDirection orbitDirection, Target opponent, double distanceBetween) {
        if (distanceBetween > DEFAULT_DISTANCE_AGAINST_SIMPLE + 10) {
            return Utils.normalAbsoluteAngle(surfPoint.angleTo(robot) + LXXConstants.RADIANS_95 * orbitDirection.sign);
        } else if (distanceBetween < FEAR_DISTANCE) {
            return Utils.normalAbsoluteAngle(surfPoint.angleTo(robot) + LXXConstants.RADIANS_60 * orbitDirection.sign);
        } else if (distanceBetween < DEFAULT_DISTANCE_AGAINST_SIMPLE - 10) {
            return Utils.normalAbsoluteAngle(surfPoint.angleTo(robot) + LXXConstants.RADIANS_80 * orbitDirection.sign);
        } else {
            return Utils.normalAbsoluteAngle(surfPoint.angleTo(robot) + LXXConstants.RADIANS_90 * orbitDirection.sign);
        }
    }

}
