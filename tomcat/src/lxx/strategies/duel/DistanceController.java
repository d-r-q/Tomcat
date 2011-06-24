/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.strategies.duel;

import lxx.LXXRobotState;
import lxx.Tomcat;
import lxx.bullets.LXXBullet;
import lxx.bullets.enemy.EnemyBulletManager;
import lxx.targeting.GunType;
import lxx.targeting.Target;
import lxx.targeting.TargetManager;
import lxx.targeting.tomcat_eyes.TomcatEyes;
import lxx.utils.APoint;
import lxx.utils.LXXConstants;
import lxx.utils.LXXUtils;
import robocode.Rules;
import robocode.util.Utils;

import java.util.List;

import static java.lang.Math.max;

/**
 * User: jdev
 * Date: 20.06.11
 */
public class DistanceController {

    private static final double MAX_ATTACK_DELTA_WITHOUT_BULLETS = LXXConstants.RADIANS_30;
    private static final double MIN_ATTACK_DELTA_WITHOUT_BULLETS = LXXConstants.RADIANS_30;

    private static final double SIMPLE_NOT_AGGRESSIVE_DISTANCE = 450;
    private static final double SIMPLE_AGGRESSIVE_DISTANCE = 1000;

    private final Tomcat robot;
    private final EnemyBulletManager enemyBulletManager;
    private final TargetManager targetManager;
    private final TomcatEyes tomcatEyes;

    public DistanceController(Tomcat robot, EnemyBulletManager enemyBulletManager, TargetManager targetManager, TomcatEyes tomcatEyes) {
        this.robot = robot;
        this.enemyBulletManager = enemyBulletManager;
        this.targetManager = targetManager;
        this.tomcatEyes = tomcatEyes;
    }

    public double getDesiredHeading(APoint surfPoint, LXXRobotState robot, WaveSurfingMovement.OrbitDirection orbitDirection) {
        final double timeToTravel = getFirstBulletFlightTime(robot);
        Target t = targetManager.getDuelOpponent();
        if (tomcatEyes.getEnemyGunType(t) != GunType.ADVANCED) {
            if (tomcatEyes.getEnemyPreferredDistance(t) > 350) {
                return getDesiredHeadingWithBullets(surfPoint, robot, orbitDirection, SIMPLE_NOT_AGGRESSIVE_DISTANCE, timeToTravel);
            } else {
                return getDesiredHeadingWithBullets(surfPoint, robot, orbitDirection, SIMPLE_AGGRESSIVE_DISTANCE, timeToTravel);
            }
        } else {
            return getDesiredHeadingWithBullets(surfPoint, robot, orbitDirection, SIMPLE_NOT_AGGRESSIVE_DISTANCE, timeToTravel);
        }
    }

    private double getDesiredHeadingWithBullets(APoint surfPoint, LXXRobotState robot, WaveSurfingMovement.OrbitDirection orbitDirection,
                                                double desiredDistance, double timeToTravel) {
        final double distanceBetween = robot.aDistance(surfPoint);

        final double maxAttackAngle = LXXConstants.RADIANS_110 + (MAX_ATTACK_DELTA_WITHOUT_BULLETS * (timeToTravel / 30));
        final double minAttackAngle = LXXConstants.RADIANS_80 - (MIN_ATTACK_DELTA_WITHOUT_BULLETS * (timeToTravel / 30));
        final double attackAngle = LXXConstants.RADIANS_90 + (LXXConstants.RADIANS_90 * (distanceBetween - desiredDistance) / desiredDistance);

        return Utils.normalAbsoluteAngle(surfPoint.angleTo(robot) +
                LXXUtils.limit(minAttackAngle, attackAngle, maxAttackAngle) * orbitDirection.sign);
    }

    private double getFirstBulletFlightTime(APoint pos) {
        final List<LXXBullet> bulletsOnAir = enemyBulletManager.getBulletsOnAir(0);
        final Target duelOpponent = targetManager.getDuelOpponent();
        if (bulletsOnAir.size() > 0) {
            return bulletsOnAir.get(0).getFlightTime(pos);
        } else {
            return duelOpponent.getGunHeat() / robot.getGunCoolingRate() + duelOpponent.aDistance(pos) / Rules.getBulletSpeed(max(0.1, duelOpponent.getFirePower()));
        }
    }

}
