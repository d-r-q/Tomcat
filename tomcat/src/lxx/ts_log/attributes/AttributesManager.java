/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.ts_log.attributes;

import lxx.Tomcat;
import lxx.bullets.LXXBullet;
import lxx.office.Office;
import lxx.targeting.Target;
import lxx.ts_log.TurnSnapshot;
import lxx.ts_log.attributes.attribute_extractors.DistanceBetweenVE;
import lxx.ts_log.attributes.attribute_extractors.enemy.*;
import lxx.ts_log.attributes.attribute_extractors.my.*;

import java.util.List;

public class AttributesManager {


    public static final Attribute distBetween = new Attribute("Distance between", 0, 1700, new DistanceBetweenVE());

    public static final Attribute enemyX = new Attribute("Enemy x", 0, 1200, new EnemyXVE());
    public static final Attribute enemyY = new Attribute("Enemy y", 0, 1200, new EnemyYVE());
    public static final Attribute enemyVelocity = new Attribute("Enemy velocity", -8, 8, new EnemyVelocityVE());
    public static final Attribute enemySpeed = new Attribute("Enemy speed", 0, 8, new EnemySpeedVE());
    public static final Attribute enemyAbsoluteHeading = new Attribute("Enemy heading", 0, 360, new EnemyHeadingVE());
    public static final Attribute enemyAcceleration = new Attribute("Enemy acceleration", -8, 1, new EnemyAccelerationVE());
    public static final Attribute enemyTurnRate = new Attribute("Enemy turn rate", -10.2, 10.2, new EnemyTurnRateVE());

    public static final Attribute enemyDistanceToForwardWall = new Attribute("Enemy forward WaveSurfingMovement distance", 0, 1700, new EnemyDistanceToForwardWallVE());
    public static final Attribute enemyDistanceToReverseWall = new Attribute("Enemy reverse WaveSurfingMovement distance", 0, 1700, new EnemyDistanceToReverceWallVE());
    public static final Attribute enemyBearingToForwardWall = new Attribute("Enemy bearing to head on WaveSurfingMovement", -90, 90, new EnemyBearingToHOWallVE());

    public static final Attribute firstBulletFlightTimeToEnemy = new Attribute("First bullet flight time", 0, 75, new FirstBulletFlightTimeToEnemyVE());
    public static final Attribute enemyBearingOffsetOnFirstBullet = new Attribute("Enemy bearing offset on first bullet", -50, 50, new EnemyBearingOffsetOnFirstBulletVE());
    public static final Attribute enemyBearingOffsetOnSecondBullet = new Attribute("Enemy bearing offset on second bullet", -50, 50, new EnemyBearingOffsetOnSecondBulletVE());
    public static final Attribute enemyTimeSinceLastDirChange = new Attribute("Enemy time since last direction change", 0, 2000, new EnemyTimeSinceDirChangeVE());

    public static final Attribute myX = new Attribute("My x", 0, 1200, new MyXVE());
    public static final Attribute myY = new Attribute("My y", 0, 1200, new MyYVE());
    public static final Attribute myVelocity = new Attribute("My velocity", -8, 8, new MyVelocityVE());
    public static final Attribute mySpeed = new Attribute("My speed", 0, 8, new MySpeedVE());
    public static final Attribute myLateralSpeed = new Attribute("My lateral velocity speed", 0, 8, new MyLateralSpeed());
    public static final Attribute myAbsoluteHeadingDegrees = new Attribute("My absolute heading", 0, 360, new MyHeadingVE());
    public static final Attribute myRelativeHeading = new Attribute("My relative heading", -180, 180, new MyRelativeHeadingVE());
    public static final Attribute myAcceleration = new Attribute("My acceleration", -2, 1, new MyAccelerationVE());
    public static final Attribute myDistToForwardWall = new Attribute("My distance to forward WaveSurfingMovement", 0, 1700, new MyDistanceToForwardWallVE());
    public static final Attribute myDistToReverseWall = new Attribute("My distance to reverse WaveSurfingMovement", 0, 1700, new MyDistanceToReverseWallVE());
    public static final Attribute myTravelTime = new Attribute("My travel time", 0, 255, new MyTravelTimeVE());

    public static final Attribute[] attributes = {
            distBetween,

            enemyX,
            enemyY,
            enemyVelocity,
            enemySpeed,
            enemyAbsoluteHeading,
            enemyAcceleration,
            enemyTurnRate,

            enemyDistanceToForwardWall,
            enemyDistanceToReverseWall,
            enemyBearingToForwardWall,

            firstBulletFlightTimeToEnemy,
            enemyBearingOffsetOnFirstBullet,
            enemyBearingOffsetOnSecondBullet,
            enemyTimeSinceLastDirChange,

            myX,
            myY,
            myVelocity,
            mySpeed,
            myLateralSpeed,
            myAbsoluteHeadingDegrees,
            myRelativeHeading,
            myAcceleration,
            myDistToForwardWall,
            myDistToReverseWall,
            myTravelTime,
    };

    private final Office office;
    private final Tomcat robot;

    public AttributesManager(Office office, Tomcat robot) {
        this.office = office;
        this.robot = robot;
    }

    public TurnSnapshot getBattleSnapshot(Target t) {
        final double[] attrValues = new double[attributes.length];
        final List<LXXBullet> myBullets = office.getBulletManager().getBullets();
        for (final Attribute a : attributes) {
            if (a.getId() >= attributes.length) {
                throw new RuntimeException("Something wrong!");
            }
            final double av = a.getExtractor().getAttributeValue(t, robot, myBullets);
            if (av < a.getMinValue() || av > a.getMaxValue()) {
                a.getExtractor().getAttributeValue(t, robot, myBullets);
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

        return new TurnSnapshot(attrValues, robot.getTime(), robot.getRoundNum(), t.getName());
    }

    public static int attributesCount() {
        return attributes.length;
    }
}
