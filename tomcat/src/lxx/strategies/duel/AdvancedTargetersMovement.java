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

public class AdvancedTargetersMovement extends WaveSurfingMovement {

    private static final int DEFAULT_DISTANCE_AGAINST_ADVANCED = 500;

    public AdvancedTargetersMovement(Tomcat robot, TargetManager targetManager, EnemyBulletManager enemyBulletManager, TomcatEyes tomcatEyes) {
        super(robot, targetManager, enemyBulletManager, tomcatEyes);
    }

    protected double getEnemyPreferredDistance(Target opponent) {
        double enemyPreferredDistance = tomcatEyes.getEnemyPreferredDistance(opponent);
        if (enemyPreferredDistance == -1) {
            enemyPreferredDistance = DEFAULT_DISTANCE_AGAINST_ADVANCED;
        }
        enemyPreferredDistance = (int) min(enemyPreferredDistance - 75, opponent.getPosition().distanceToWall(robot.battleField, opponent.angleTo(robot)) - 75);

        return enemyPreferredDistance;
    }

    protected double getPointDanger(APoint pnt, Target opponent) {
        final double distanceToCenterDanger = pnt.aDistance(robot.battleField.center) / 600;
        return distanceToCenterDanger * distanceToCenterDanger;
    }

    protected double getTargetHeading(APoint surfPoint, LXXRobotState robot, WaveSurfingMovement.OrbitDirection orbitDirection, Target opponent, double distanceBetween) {
        if (distanceBetween > enemyPreferredDistance + 10) {
            return Utils.normalAbsoluteAngle(surfPoint.angleTo(robot) + LXXConstants.RADIANS_110 * orbitDirection.sign);
        } else if (distanceBetween < enemyPreferredDistance - 10) {
            return Utils.normalAbsoluteAngle(surfPoint.angleTo(robot) + LXXConstants.RADIANS_60 * orbitDirection.sign);
        } else {
            return Utils.normalAbsoluteAngle(surfPoint.angleTo(robot) + LXXConstants.RADIANS_90 * orbitDirection.sign);
        }
    }

}
