/**
 * $Id$
 *
 * Copyright (c) 2009 Zodiac Interactive. All Rights Reserved.
 */
package lxx.autosegmentation;

import static lxx.StaticData.robot;
import lxx.autosegmentation.attribute_extractors.*;
import lxx.autosegmentation.model.Attribute;
import lxx.autosegmentation.model.FireSituation;
import lxx.targeting.Target;
import lxx.targeting.bullets.BulletManager;
import lxx.utils.LXXConstants;

import static java.lang.Math.ceil;
import static java.lang.Math.toDegrees;

public class AttributeFactory {

    private static final Attribute lateralVelocityAttr = new Attribute("Lateral velocity", -4, 4, new LateralVelocityVE());
    private static final Attribute enemyTravelTimeAttr = new Attribute("Enemy travel time", 0, 255, new EnemyTravelTimeVE());
    private static final Attribute distBetweenAttr = new Attribute("Distance between", 0, 1500, new DistanceBetweenVE());
    // todo: fix me
    public static final Attribute distToHOWallAttr = new Attribute("Heading wall distance", 0, 800, new HOWallDistVE());
    private static final Attribute distToCenterAttr = new Attribute("Distance to center", 0, 72, new DistToCenterVE());
    private static final Attribute lastVisitedGFAttr = new Attribute("Enemy last visited gf", -LXXConstants.MAX_GUESS_FACTOR, LXXConstants.MAX_GUESS_FACTOR, new EnemyLastGFVE());

    private static final Attribute enemyVelocityAttr = new Attribute("Enemy velocity", -8, 8, new EnemyVelocityVE());
    private static final Attribute bulletFlightTime = new Attribute("Bullet flight time", 0, 1000, new BulletFlightTimeVE());
    private static final Attribute angleToTargetAttr = new Attribute("Angle to target", 0, 18, new AngleToTargetVE());
    private static final Attribute enemyHeadingAttr = new Attribute("Enemy heading", 0, 18, new EnemyHeadingVE());
    private static final Attribute enemyXAttr = new Attribute("Enemy x", 0, 80, new EnemyXVE());
    private static final Attribute enemyYAttr = new Attribute("Enemy y", 0, 60, new EnemyYVE());

    private static final Attribute gunBearing = new Attribute("Gun bearing", -18, 18, new GunBearingVE());
    private static final Attribute enemyStopTime = new Attribute("Enemy stop time", 0, 255, new EnemyStopTimeVE());

    public static final Attribute timeSinceMyLastFire = new Attribute("Time since my last fire", 0, 21, new TimeSinceMyLastFireVE());
    private static final Attribute timeSinceLastLatVelChange = new Attribute("Time since lateral velocity dir change", 0, 255, new TimeSinceLateralVelocityDirChangeVE());
    private static final Attribute enemyTurnRate = new Attribute("Enemy turn rate", -10, 10, new EnemyTurnRateVE());

    private static final Attribute avgBulletBearing1 = new Attribute("Avg bullet bearing1", -45, 46, new AvgBearing1VE());
    private static final Attribute avgBulletBearing2 = new Attribute("Avg bullet bearing2", -45, 46, new AvgBearing2VE());
    private static final Attribute bearingToClosestWall = new Attribute("Bearing to closest wall", -180, 180, new BearingToClosestWallVE());

    private static final Attribute enemyAcceleration = new Attribute("Enemy acceleration", -8, 8, new EnemyAccelerationVE());
    private static final Attribute firstBulletFlightTime = new Attribute("First bullet flight time", 0, 21, new LateralAccelerationVE.FirstBulletFlightTimeVE());
    private static final Attribute lastBulletFlightTime = new Attribute("Last bullet flight time", 0, 21, new LateralAccelerationVE.LastBulletFlightTimeVE());
    private static final Attribute firePower = new Attribute("Fire power", 0, 3, new FirePowerVE());
    private static final Attribute firstBulletBearing = new Attribute("First bullet bearing", -18, 19, new FistBulletBearingVE());
    private static final Attribute lastBulletBearing = new Attribute("Last bullet bearing", -18, 19, new LastBulletBearingVE());
    private static final Attribute distTraveledOnLastWave = new Attribute("Dist traveled on last wave", 0, 100, new DistTravelledLastWaveVE());
    public static final Attribute timeSinceLastHit = new Attribute("Time since last hit", 0, 255, new LastHitTimeVE());
    private static final Attribute distToClosestWall = new Attribute("Dist to closest wall", 0, 800, new DistToClosestWallVE());

    private final Attribute[] attributes = {
            angleToTargetAttr,
            avgBulletBearing1,
            avgBulletBearing2,
            bearingToClosestWall,
            enemyStopTime,
            distBetweenAttr,
            distToCenterAttr,
            enemyHeadingAttr,
            enemyTravelTimeAttr,
            gunBearing,
            distToHOWallAttr,
            timeSinceMyLastFire,
            lateralVelocityAttr,
            timeSinceLastLatVelChange,
            bulletFlightTime,
            // todo fix me
            enemyXAttr,
            enemyYAttr,
            enemyVelocityAttr,
            enemyAcceleration,
            enemyTurnRate,
            lastVisitedGFAttr,
            firstBulletFlightTime,
            lastBulletFlightTime,
            firePower,
            firstBulletBearing,
            lastBulletBearing,
            distTraveledOnLastWave,
            timeSinceLastHit,
            distToClosestWall,
    };

    private final BulletManager bulletManager;

    public AttributeFactory(BulletManager bulletManager) {
        this.bulletManager = bulletManager;
    }

    public FireSituation getFireSituation(Target t) {
        int[] attrValues = new int[attributes.length];
        for (int i = 0; i < attributes.length; i++) {
            final Attribute a = attributes[i];
            if (a.getId() >= attributes.length) {
                throw new RuntimeException("Something wrong!");
            }
            final int av = a.getExtractor().getAttributeValue(t, robot, bulletManager);
            if (av < a.getMinValue() || av > a.getMaxValue()) {
                throw new RuntimeException(a + " = " + av);
            }
            if (a.getActualMin() > av) {
                a.setActualMin(av);
            }
            if (a.getActualMax() < av) {
                a.setActualMax(av);
            }
            attrValues[a.getId()] = av;
        }

        return new FireSituation((int)-ceil(toDegrees(t.maxEscapeAngle(-1))),
                (int)ceil(toDegrees(t.maxEscapeAngle(-1))), 0, attrValues, this);
    }

    public Attribute[] getAttributes() {
        return attributes;
    }

    public Attribute getAttribute(int i) {
        return attributes[i];
    }
}
