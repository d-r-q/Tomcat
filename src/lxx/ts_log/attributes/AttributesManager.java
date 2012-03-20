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
import lxx.ts_log.attributes.attribute_extractors.DistanceBetween;
import lxx.ts_log.attributes.attribute_extractors.FireTimeDiff;
import lxx.ts_log.attributes.attribute_extractors.enemy.*;
import lxx.ts_log.attributes.attribute_extractors.my.*;
import lxx.utils.LXXUtils;
import robocode.Rules;

import java.util.List;

public class AttributesManager {


    public static final Attribute distBetween = new Attribute("Distance between", 0, 1700, new DistanceBetween());

    public static final Attribute enemyX = new Attribute("Enemy x", 0, 1200, new EnemyX());
    public static final Attribute enemyY = new Attribute("Enemy y", 0, 1200, new EnemyY());
    public static final Attribute enemySpeed = new Attribute("Enemy speed", 0, 8, new EnemySpeed());
    public static final Attribute enemyAbsoluteHeading = new Attribute("Enemy heading", 0, 360, new EnemyHeading());
    public static final Attribute enemyAcceleration = new Attribute("Enemy acceleration", -8, 1, new EnemyAcceleration());
    public static final Attribute enemyTurnRate = new Attribute("Enemy turn rate", -10.2, 10.2, new EnemyTurnRate());

    public static final Attribute enemyDistanceToForwardWall = new Attribute("Enemy forward wall distance", 0, 1700, new EnemyDistanceToForwardWall());
    public static final Attribute enemyBearingToForwardWall = new Attribute("Enemy bearing to head on wall", -90, 90, new EnemyBearingToHOWall());

    public static final Attribute firstBulletFlightTimeToEnemy = new Attribute("First bullet flight time", 0, 75, new FirstBulletFlightTimeToEnemy());
    public static final Attribute enemyBearingOffsetOnFirstBullet = new Attribute("Enemy bearing offset on first bullet", -50, 50, new EnemyBearingOffsetOnFirstBullet());
    public static final Attribute enemyBearingOffsetOnSecondBullet = new Attribute("Enemy bearing offset on second bullet", -50, 50, new EnemyBearingOffsetOnSecondBullet());
    public static final Attribute enemyTimeSinceLastDirChange = new Attribute("Enemy time since last direction change", 0, 2000, new EnemyTimeSinceDirChange());
    public static final Attribute enemyBearingToMe = new Attribute("Enemy bearing to me", -180, 180, new EnemyBearingToMe());
    public static final Attribute lastVisitedGF1 = new Attribute("Enemy last visited gf", -1.1, 1.1, new LastVisitedGF(1));
    public static final Attribute lastVisitedGF2 = new Attribute("Enemy last visited gf", -1.1, 1.1, new LastVisitedGF(2));

    public static final Attribute myLateralSpeed = new Attribute("My lateral speed", 0, 8, new MyLateralSpeed());
    public static final Attribute myAcceleration = new Attribute("My acceleration", -2, 1, new MyAcceleration());
    public static final Attribute myDistToForwardWall = new Attribute("My distance to forward wall", 0, 1700, new MyDistanceToForwardWall());
    public static final Attribute myDistLast10Ticks = new Attribute("My dist last 10 ticks", 0, Rules.MAX_VELOCITY * 10 + 1, new MyDistanceLast10Ticks());
    public static final Attribute fireTimeDiff = new Attribute("Fire time diff", 0, 40, new FireTimeDiff());

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

            myLateralSpeed,
            myAcceleration,
            myDistToForwardWall,
            myDistLast10Ticks,
            fireTimeDiff
    };

    private final Tomcat robot;

    public AttributesManager(Tomcat robot) {
        this.robot = robot;
    }

    public TurnSnapshot getTurnSnapshot(Target t) {
        robot.getCurrentSnapshot().setBullets(robot.getBulletsInAir());
        return new TurnSnapshot(robot.getTime(), robot.getRoundNum(), robot.getCurrentSnapshot(), t.getCurrentSnapshot());
    }

    public static int attributesCount() {
        return attributes.length;
    }
}
