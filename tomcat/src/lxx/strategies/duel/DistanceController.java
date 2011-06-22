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
import robocode.util.Utils;

import java.util.List;

/**
 * User: jdev
 * Date: 20.06.11
 */
public class DistanceController {

    private static final double MAX_ATTACK_DELTA_NO_BULLETS = LXXConstants.RADIANS_75;
    private static final double MIN_ATTACK_DELTA_NO_BULLETS = LXXConstants.RADIANS_20;
    private static final double MAX_ATTACK_DELTA_WITH_BULLETS = LXXConstants.RADIANS_135;
    private static final double MIN_ATTACK_DELTA_WITH_BULLETS = LXXConstants.RADIANS_10;

    private static final double SIMPLE_NOT_AGRESSIVE_DISTANCE = 450;
    private static final double SIMPLE_AGRESSIVE_DISTANCE = 1000;

    private final Tomcat robot;
    private final double gunCoolingRate;
    private final EnemyBulletManager enemyBulletManager;
    private final TargetManager targetManager;
    private final TomcatEyes tomcatEyes;

    public DistanceController(Tomcat robot, EnemyBulletManager enemyBulletManager, TargetManager targetManager, TomcatEyes tomcatEyes) {
        this.robot = robot;
        this.enemyBulletManager = enemyBulletManager;
        this.targetManager = targetManager;
        this.tomcatEyes = tomcatEyes;

        this.gunCoolingRate = this.robot.getGunCoolingRate();
    }

    public double getDesiredHeading(APoint surfPoint, LXXRobotState robot, WaveSurfingMovement.OrbitDirection orbitDirection) {
        final List<LXXBullet> bulletsOnAir = enemyBulletManager.getBulletsOnAir(0);

        final LXXBullet firstBullet = bulletsOnAir.size() > 0 ? bulletsOnAir.get(0) : null;
        if (firstBullet == null) {
            final Target t = targetManager.getDuelOpponent();
            if (t != null && tomcatEyes.getEnemyGunType(t) != GunType.ADVANCED) {
                if (tomcatEyes.getEnemyPreferredDistance(t) > 350) {
                    return getDesiredHeadingNoBullets(surfPoint, robot, orbitDirection, SIMPLE_NOT_AGRESSIVE_DISTANCE);
                } else {
                    return getDesiredHeadingNoBullets(surfPoint, robot, orbitDirection, SIMPLE_AGRESSIVE_DISTANCE);
                }
            } else {
                return getDesiredHeadingNoBullets(surfPoint, robot, orbitDirection, SIMPLE_NOT_AGRESSIVE_DISTANCE);
            }
        } else {
            Target t = targetManager.getDuelOpponent();
            if (tomcatEyes.getEnemyGunType(t) != GunType.ADVANCED) {
                if (tomcatEyes.getEnemyPreferredDistance(t) > 350) {
                    return getDesiredHeadingWithBullets(surfPoint, robot, orbitDirection, firstBullet, SIMPLE_NOT_AGRESSIVE_DISTANCE);
                } else {
                    return getDesiredHeadingWithBullets(surfPoint, robot, orbitDirection, firstBullet, SIMPLE_AGRESSIVE_DISTANCE);
                }
            } else {
                return getDesiredHeadingWithBullets(surfPoint, robot, orbitDirection, firstBullet, SIMPLE_NOT_AGRESSIVE_DISTANCE);
            }
        }
    }

    private double getDesiredHeadingWithBullets(APoint surfPoint, LXXRobotState robot, WaveSurfingMovement.OrbitDirection orbitDirection, LXXBullet firstBullet, double desiredDistance) {
        final double distanceBetween = robot.aDistance(surfPoint);

        final double maxAttackAngle = LXXConstants.RADIANS_100 + MAX_ATTACK_DELTA_WITH_BULLETS * (firstBullet.getFlightTime(robot) / 15);
        final double minAttackAngle = LXXConstants.RADIANS_80 - MIN_ATTACK_DELTA_WITH_BULLETS * (firstBullet.getFlightTime(robot) / 15);
        final double attackAngle = LXXConstants.RADIANS_90 + (MAX_ATTACK_DELTA_WITH_BULLETS * (distanceBetween - desiredDistance) / desiredDistance);

        return Utils.normalAbsoluteAngle(surfPoint.angleTo(robot) +
                LXXUtils.limit(minAttackAngle, attackAngle, maxAttackAngle) * orbitDirection.sign);
    }

    private double getDesiredHeadingNoBullets(APoint surfPoint, LXXRobotState robot, WaveSurfingMovement.OrbitDirection orbitDirection, double desiredDistance) {
        final double maxCoolingTime = LXXConstants.INITIAL_GUN_HEAT / this.robot.getGunCoolingRate();
        final double currentCoolingTime = targetManager.getDuelOpponent().getGunHeat() / gunCoolingRate;
        final double distanceBetween = robot.aDistance(surfPoint);

        final double maxAttackAngle = LXXConstants.RADIANS_100 + MAX_ATTACK_DELTA_NO_BULLETS * (currentCoolingTime / maxCoolingTime);
        final double minAttackAngle = LXXConstants.RADIANS_80 - MIN_ATTACK_DELTA_NO_BULLETS * (currentCoolingTime / maxCoolingTime);
        final double attackAngle = LXXConstants.RADIANS_90 + (MAX_ATTACK_DELTA_NO_BULLETS * (distanceBetween - desiredDistance) / desiredDistance);

        return Utils.normalAbsoluteAngle(surfPoint.angleTo(robot) +
                LXXUtils.limit(minAttackAngle, attackAngle, maxAttackAngle) * orbitDirection.sign);
    }

}
