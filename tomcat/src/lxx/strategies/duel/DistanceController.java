/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.strategies.duel;

import lxx.LXXRobotState;
import lxx.Tomcat;
import lxx.bullets.LXXBullet;
import lxx.bullets.enemy.EnemyBulletManager;
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

    private static final double SIMPLE_DISTANCE = 450;
    private static final int ANTI_RAM_DISTANCE = 150;

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

    public double getDesiredHeading(APoint surfPoint, LXXRobotState robot, WaveSurfingMovement.OrbitDirection orbitDirection, List<LXXBullet> bulletsOnAir) {
        final double timeToTravel = getFirstBulletFlightTime(robot, bulletsOnAir);
        return getDesiredHeadingWithBullets(surfPoint, robot, orbitDirection, getPreferredDistance(), timeToTravel, bulletsOnAir);
    }

    public double getPreferredDistance() {
        return SIMPLE_DISTANCE;
    }

    private double getDesiredHeadingWithBullets(APoint surfPoint, LXXRobotState robot, WaveSurfingMovement.OrbitDirection orbitDirection,
                                                double desiredDistance, double timeToTravel, List<LXXBullet> bulletsOnAir) {
        final double distanceBetween = robot.aDistance(surfPoint);

        final double k = 1;//min(1, timeToTravel / (distanceBetween / getBulletSpeed(bulletsOnAir)));
        final double maxAttackAngle = LXXConstants.RADIANS_100 + (MAX_ATTACK_DELTA_WITHOUT_BULLETS * k);
        final double minAttackAngle = LXXConstants.RADIANS_80 - (MIN_ATTACK_DELTA_WITHOUT_BULLETS * k);
        final double distanceDiff = distanceBetween - desiredDistance;
        final double attackAngleKoeff = distanceDiff / desiredDistance;
        final Target duelOpponent = targetManager.getDuelOpponent();
        final double antiRamAngle = (duelOpponent != null && distanceBetween < ANTI_RAM_DISTANCE && tomcatEyes.isRammingNow(duelOpponent))
                ? LXXConstants.RADIANS_50 * (ANTI_RAM_DISTANCE - distanceBetween) / ANTI_RAM_DISTANCE
                : 0;
        final double attackAngle = LXXConstants.RADIANS_90 + ((LXXConstants.RADIANS_40 + antiRamAngle) * attackAngleKoeff);

        return Utils.normalAbsoluteAngle(surfPoint.angleTo(robot) +
                LXXUtils.limit(minAttackAngle, attackAngle, maxAttackAngle) * orbitDirection.sign);
    }

    private double getFirstBulletFlightTime(APoint pos, List<LXXBullet> bulletsOnAir) {
        final Target duelOpponent = targetManager.getDuelOpponent();
        if (bulletsOnAir.size() > 0) {
            return bulletsOnAir.get(0).getFlightTime(pos);
        } else {
            return duelOpponent.getGunHeat() / robot.getGunCoolingRate() + duelOpponent.aDistance(pos) / Rules.getBulletSpeed(max(0.1, duelOpponent.getFirePower()));
        }
    }

    private double getBulletSpeed(List<LXXBullet> bulletsOnAir) {
        final Target duelOpponent = targetManager.getDuelOpponent();
        if (bulletsOnAir.size() > 0) {
            return bulletsOnAir.get(0).getSpeed();
        } else {
            return Rules.getBulletSpeed(duelOpponent.getFirePower());
        }
    }

}
