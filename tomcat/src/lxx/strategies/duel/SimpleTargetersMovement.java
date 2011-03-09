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

import static java.lang.Math.min;

public class SimpleTargetersMovement extends WaveSurfingMovement {

    protected static final int DEFAULT_DISTANCE_AGAINST_SIMPLE = 400;

    public SimpleTargetersMovement(Tomcat robot, TargetManager targetManager, EnemyBulletManager enemyBulletManager, TomcatEyes tomcatEyes) {
        super(robot, targetManager, enemyBulletManager, tomcatEyes);
    }

    protected double getEnemyPreferredDistance(Target opponent) {
        return DEFAULT_DISTANCE_AGAINST_SIMPLE;
    }

    protected double getPointDanger(APoint pnt, Target opponent) {
        double danger = 0;

        final double distanceEnemyDanger = DEFAULT_DISTANCE_AGAINST_SIMPLE / pnt.aDistance(opponent);
        danger += distanceEnemyDanger / 20;

        return danger;
    }

    protected double getTargetHeading(APoint surfPoint, LXXRobotState robot, OrbitDirection orbitDirection, Target opponent, double distanceBetween) {
        if (distanceBetween > DEFAULT_DISTANCE_AGAINST_SIMPLE + 10) {
            return Utils.normalAbsoluteAngle(surfPoint.angleTo(robot) + LXXConstants.RADIANS_100 * orbitDirection.sign);
        } else if (distanceBetween < DEFAULT_DISTANCE_AGAINST_SIMPLE - 10) {
            return Utils.normalAbsoluteAngle(surfPoint.angleTo(robot) + LXXConstants.RADIANS_60 * orbitDirection.sign);
        } else {
            return Utils.normalAbsoluteAngle(surfPoint.angleTo(robot) + LXXConstants.RADIANS_90 * orbitDirection.sign);
        }
    }

}
