/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.ts_log.attributes;

import lxx.Tomcat;
import lxx.bullets.LXXBullet;
import lxx.bullets.my.BulletManager;
import lxx.office.Office;
import lxx.targeting.Target;
import lxx.ts_log.TurnSnapshot;
import lxx.ts_log.attributes.attribute_extractors.DistanceBetweenVE;
import lxx.ts_log.attributes.attribute_extractors.enemy.*;
import lxx.ts_log.attributes.attribute_extractors.my.*;
import lxx.utils.LXXUtils;
import robocode.Rules;

import java.util.List;

public class AttributesManager {


    public static final Attribute distBetween = new Attribute("Distance between", 0, 1700, new DistanceBetweenVE());

    public static final Attribute enemyX = new Attribute("Enemy x", 0, 1200, new EnemyXVE());
    public static final Attribute enemyY = new Attribute("Enemy y", 0, 1200, new EnemyYVE());
    public static final Attribute enemySpeed = new Attribute("Enemy speed", 0, 8, new EnemySpeedVE());
    public static final Attribute enemyAbsoluteHeading = new Attribute("Enemy heading", 0, 360, new EnemyHeadingVE());
    public static final Attribute enemyAcceleration = new Attribute("Enemy acceleration", -8, 1, new EnemyAccelerationVE());
    public static final Attribute enemyTurnRate = new Attribute("Enemy turn rate", -10.2, 10.2, new EnemyTurnRateVE());

    public static final Attribute enemyDistanceToForwardWall = new Attribute("Enemy forward wall distance", 0, 1700, new EnemyDistanceToForwardWallVE());
    public static final Attribute enemyBearingToForwardWall = new Attribute("Enemy bearing to head on wall", -90, 90, new EnemyBearingToHOWallVE());

    public static final Attribute firstBulletFlightTimeToEnemy = new Attribute("First bullet flight time", 0, 75, new FirstBulletFlightTimeToEnemyVE());
    public static final Attribute enemyBearingOffsetOnFirstBullet = new Attribute("Enemy bearing offset on first bullet", -50, 50, new EnemyBearingOffsetOnFirstBulletVE());
    public static final Attribute enemyBearingOffsetOnSecondBullet = new Attribute("Enemy bearing offset on second bullet", -50, 50, new EnemyBearingOffsetOnSecondBulletVE());
    public static final Attribute enemyTimeSinceLastDirChange = new Attribute("Enemy time since last direction change", 0, 2000, new EnemyTimeSinceDirChangeVE());
    public static final Attribute enemyBearingToMe = new Attribute("Enemy bearing to me", -180, 180, new EnemyBearingToMeVE());
    public static final Attribute lastVisitedGF1 = new Attribute("Enemy last visited gf", -1.1, 1.1, new LastVisitedGF(1));
    public static final Attribute lastVisitedGF2 = new Attribute("Enemy last visited gf", -1.1, 1.1, new LastVisitedGF(2));

    public static final Attribute myX = new Attribute("My x", 0, 1200, new MyXVE());
    public static final Attribute myY = new Attribute("My y", 0, 1200, new MyYVE());
    public static final Attribute mySpeed = new Attribute("My speed", 0, 8, new MySpeedVE());
    public static final Attribute myLateralSpeed = new Attribute("My lateral speed", 0, 8, new MyLateralSpeed());
    public static final Attribute myAbsoluteHeadingDegrees = new Attribute("My absolute heading", 0, 360, new MyHeadingVE());
    public static final Attribute myAcceleration = new Attribute("My acceleration", -2, 1, new MyAccelerationVE());
    public static final Attribute myDistToForwardWall = new Attribute("My distance to forward wall", 0, 1700, new MyDistanceToForwardWallVE());
    public static final Attribute myDistLast10Ticks = new Attribute("My dist last 10 ticks", 0, Rules.MAX_VELOCITY * 10 + 1, new MyDistanceLast10Ticks());

    public static final Attribute[] attributes = {
            distBetween,

            enemyX,
            enemyY,
            enemySpeed,
            enemyAbsoluteHeading,
            enemyAcceleration,
            enemyTurnRate,

            enemyDistanceToForwardWall,
            enemyBearingToForwardWall,

            firstBulletFlightTimeToEnemy,
            enemyBearingOffsetOnFirstBullet,
            enemyBearingOffsetOnSecondBullet,
            enemyTimeSinceLastDirChange,
            enemyBearingToMe,
            lastVisitedGF1,
            lastVisitedGF2,

            myX,
            myY,
            mySpeed,
            myLateralSpeed,
            myAbsoluteHeadingDegrees,
            myAcceleration,
            myDistToForwardWall,
            myDistLast10Ticks,
    };

    private final Office office;
    private final Tomcat robot;
    private BulletManager bulletManager;

    public AttributesManager(Office office, Tomcat robot) {
        this.office = office;
        this.robot = robot;
        this.bulletManager = office.getBulletManager();
    }

    public TurnSnapshot getTurnSnapshot(Target t) {
        final double[] attrValues = new double[attributes.length];
        final List<LXXBullet> myBullets = bulletManager.getBullets();
        for (final Attribute a : attributes) {
            double av = a.extractor.getAttributeValue(t, robot, myBullets, office);
            if (!a.maxRange.contains(av)) {
                System.out.println("[WARN]: " + a + " = " + av);
                av = LXXUtils.limit(a, av);
            }
            a.actualRange.extend(av);
            attrValues[a.id] = av;
        }

        return new TurnSnapshot(attrValues, robot.getTime(), robot.getRoundNum(), robot.getState(), t.getState());
    }

    public static int attributesCount() {
        return attributes.length;
    }
}
