/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.strategies.duel;

import lxx.LXXRobotState;
import lxx.Tomcat;
import lxx.bullets.LXXBullet;
import lxx.office.Office;
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

    private static final double DISTANCE = 450;

    private final Office office;
    private final Tomcat robot;
    private final double gunCoolingRate;

    public DistanceController(Office office) {
        this.office = office;
        this.robot = office.getRobot();
        this.gunCoolingRate = office.getRobot().getGunCoolingRate();
    }

    public double getDesiredHeading(APoint surfPoint, LXXRobotState robot, WaveSurfingMovement.OrbitDirection orbitDirection) {
        final List<LXXBullet> bulletsOnAir = office.getEnemyBulletManager().getBulletsOnAir(0);

        final LXXBullet firstBullet = bulletsOnAir.size() > 0 ? bulletsOnAir.get(0) : null;
        if (firstBullet == null) {
            return getDesiredHeadingNoBullets(surfPoint, robot, orbitDirection);
        } else {
            return getDesiredHeadingWithBullets(surfPoint, robot, orbitDirection, firstBullet);
        }
    }

    private double getDesiredHeadingWithBullets(APoint surfPoint, LXXRobotState robot, WaveSurfingMovement.OrbitDirection orbitDirection, LXXBullet firstBullet) {
        final double distanceBetween = robot.aDistance(surfPoint);

        final double maxAttackAngle = LXXConstants.RADIANS_100 + MAX_ATTACK_DELTA_WITH_BULLETS * (firstBullet.getFlightTime(robot) / 15);
        final double minAttackAngle = LXXConstants.RADIANS_80 - MIN_ATTACK_DELTA_WITH_BULLETS * (firstBullet.getFlightTime(robot) / 15);
        final double attackAngle = LXXConstants.RADIANS_90 + (MAX_ATTACK_DELTA_WITH_BULLETS * (distanceBetween - DISTANCE) / DISTANCE);

        return Utils.normalAbsoluteAngle(surfPoint.angleTo(robot) +
                LXXUtils.limit(minAttackAngle, attackAngle, maxAttackAngle) * orbitDirection.sign);
    }

    private double getDesiredHeadingNoBullets(APoint surfPoint, LXXRobotState robot, WaveSurfingMovement.OrbitDirection orbitDirection) {
        final double maxCoolingTime = LXXConstants.INITIAL_GUN_HEAT / this.robot.getGunCoolingRate();
        final double currentCoolingTime = office.getTargetManager().getDuelOpponent().getGunHeat() / gunCoolingRate;
        final double distanceBetween = robot.aDistance(surfPoint);

        final double maxAttackAngle = LXXConstants.RADIANS_100 + MAX_ATTACK_DELTA_NO_BULLETS * (currentCoolingTime / maxCoolingTime);
        final double minAttackAngle = LXXConstants.RADIANS_80 - MIN_ATTACK_DELTA_NO_BULLETS * (currentCoolingTime / maxCoolingTime);
        final double attackAngle = LXXConstants.RADIANS_90 + (MAX_ATTACK_DELTA_NO_BULLETS * (distanceBetween - DISTANCE) / DISTANCE);

        return Utils.normalAbsoluteAngle(surfPoint.angleTo(robot) +
                LXXUtils.limit(minAttackAngle, attackAngle, maxAttackAngle) * orbitDirection.sign);
    }

}
