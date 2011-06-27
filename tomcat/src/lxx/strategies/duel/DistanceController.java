/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.strategies.duel;

import lxx.LXXRobot;
import lxx.bullets.LXXBullet;
import lxx.utils.APoint;
import lxx.utils.LXXConstants;
import lxx.utils.LXXUtils;
import robocode.Rules;
import robocode.util.Utils;

import java.util.List;

import static java.lang.Math.*;

/**
 * User: jdev
 * Date: 20.06.11
 */
public class DistanceController {

    private static final double MAX_ATTACK_DELTA_WITHOUT_BULLETS = LXXConstants.RADIANS_40;
    private static final double MIN_ATTACK_DELTA_WITHOUT_BULLETS = LXXConstants.RADIANS_30;

    private static final double SIMPLE_DISTANCE = 450;

    private final double gunCoolingRate;

    public DistanceController(double gunCoolingRate) {
        this.gunCoolingRate = gunCoolingRate;
    }

    public double getDesiredHeading(APoint surfPoint, LXXRobot me, LXXRobot enemy,
                                    WaveSurfingMovement.OrbitDirection orbitDirection, List<LXXBullet> bulletsOnAir) {
        final double timeToTravel = getFirstBulletFlightTime(me, enemy, bulletsOnAir);
        return getDesiredHeadingWithBullets(surfPoint, me, enemy, orbitDirection, bulletsOnAir,
                getPreferredDistance(), timeToTravel);
    }

    public double getPreferredDistance() {
        return SIMPLE_DISTANCE;
    }

    private double getDesiredHeadingWithBullets(APoint surfPoint, LXXRobot me, LXXRobot enemy,
                                                WaveSurfingMovement.OrbitDirection orbitDirection,
                                                List<LXXBullet> bulletsOnAir,
                                                double desiredDistance, double timeToTravel) {
        final double distanceBetween = me.aDistance(surfPoint);

        final double k = min(1, timeToTravel / (distanceBetween / getBulletSpeed(bulletsOnAir, enemy)));
        final double maxAttackAngle = LXXConstants.RADIANS_110 + (MAX_ATTACK_DELTA_WITHOUT_BULLETS * k);
        final double minAttackAngle = LXXConstants.RADIANS_80 - (MIN_ATTACK_DELTA_WITHOUT_BULLETS * k);
        final double distanceDiff = distanceBetween - desiredDistance;
        final double attackAngleKoeff = distanceDiff < 0
                ? -sqrt(abs(distanceDiff)) / sqrt(desiredDistance)
                : distanceDiff / desiredDistance;
        final double attackAngle = LXXConstants.RADIANS_90 + (LXXConstants.RADIANS_90 * attackAngleKoeff);

        return Utils.normalAbsoluteAngle(surfPoint.angleTo(me) +
                LXXUtils.limit(minAttackAngle, attackAngle, maxAttackAngle) * orbitDirection.sign);
    }

    private double getFirstBulletFlightTime(APoint pos, LXXRobot enemy, List<LXXBullet> bulletsOnAir) {
        if (bulletsOnAir.size() > 0) {
            return bulletsOnAir.get(0).getFlightTime(pos);
        } else {
            return enemy.getGunHeat() / gunCoolingRate + enemy.aDistance(pos) / Rules.getBulletSpeed(max(0.1, enemy.getFirePower()));
        }
    }

    private double getBulletSpeed(List<LXXBullet> bulletsOnAir, LXXRobot duelOpponent) {
        if (bulletsOnAir.size() > 0) {
            return bulletsOnAir.get(0).getSpeed();
        } else {
            return Rules.getBulletSpeed(duelOpponent.getFirePower());
        }
    }

}
