/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.office;

import lxx.Tomcat;
import lxx.model.TurnSnapshot;
import lxx.model.attributes.Attribute;
import lxx.model.attributes.attribute_extractors.DistanceBetweenVE;
import lxx.model.attributes.attribute_extractors.DistanceBetween_100VE;
import lxx.model.attributes.attribute_extractors.RoundTimeVE;
import lxx.model.attributes.attribute_extractors.my.*;
import lxx.model.attributes.attribute_extractors.target.*;
import lxx.targeting.Target;
import lxx.targeting.bullets.LXXBullet;

import java.util.List;

public class AttributesManager {


    public static final Attribute distBetween = new Attribute("Distance between", 0, 1700, new DistanceBetweenVE());
    public static final Attribute distBetween_100 = new Attribute("Distance between / 100", 0, 17, new DistanceBetween_100VE());
    public static final Attribute roundTime = new Attribute("Round time", 0, 5000, new RoundTimeVE());

    public static final Attribute enemyX = new Attribute("Enemy x", 0, 1200, new EnemyXVE());
    public static final Attribute enemyY = new Attribute("Enemy y", 0, 1200, new EnemyYVE());
    public static final Attribute enemyVelocity = new Attribute("Enemy velocity", -8, 8, new EnemyVelocityVE());
    public static final Attribute enemyVelocityModule = new Attribute("Enemy velocity module", 0, 8, new EnemyVelocityModuleVE());
    public static final Attribute enemyAcceleration = new Attribute("Enemy acceleration", -8, 1, new EnemyAccelerationVE());
    public static final Attribute enemyAbsoluteHeading = new Attribute("Enemy heading", 0, 360, new EnemyHeadingVE());
    public static final Attribute enemyTurnRate = new Attribute("Enemy turn rate", -10, 10, new EnemyTurnRateVE());

    public static final Attribute enemyDistanceToForwardWall = new Attribute("Enemy forward wall distance", 0, 1700, new EnemyDistanceToForwardWallVE());
    public static final Attribute enemyDistanceToReverceWall = new Attribute("Enemy reverce wall distance", 0, 1700, new EnemyDistanceToReverceWallVE());
    public static final Attribute enemyBearingToForwardWall = new Attribute("Enemy bearing to head on wall", -90, 90, new EnemyBearingToHOWallVE());
    public static final Attribute enemyBearingToMe = new Attribute("Enemy bearing to me", -180, 180, new EnemyBearingToMeVE());
    public static final Attribute enemyStopTime = new Attribute("Enemy stop time", 0, 2000, new EnemyStopTimeVE());
    public static final Attribute enemyTravelTime = new Attribute("Enemy travel time", 0, 2000, new EnemyTravelTimeVE());
    public static final Attribute enemyTurnTime = new Attribute("Enemy turn time", 0, 2000, new EnemyTurnTimeVE());
    public static final Attribute enemyDistanceToCenter = new Attribute("Enemy distance to center", 0, 850, new EnemyDistanceToCenterVE());

    public static final Attribute firstBulletBearingOffset = new Attribute("First bullet bearing offset", -2, 2, new FirstBulletBearingOffsetVE());
    public static final Attribute firstBulletFlightTime = new Attribute("First bullet flight time", 0, 75, new FirstBulletFlightTimeVE());

    public static final Attribute myX = new Attribute("My x", 0, 1200, new MyXVE());
    public static final Attribute myY = new Attribute("My y", 0, 1200, new MyYVE());
    public static final Attribute myVelocity = new Attribute("My velocity", -8, 8, new MyVelocityVE());
    public static final Attribute myVelocityModule = new Attribute("My velocity module", 0, 8, new MyVelocityModuleVE());
    public static final Attribute myLateralVelocity_2 = new Attribute("My lateral velocity module / 2", 0, 4, new MyLateralVelocityModule_2VE());
    public static final Attribute myAbsoluteHeadingDegrees = new Attribute("My absolute heading", 0, 360, new MyHeadingVE());
    public static final Attribute myRelativeHeading = new Attribute("My relative heading", -180, 180, new MyRelativeHeadingVE());
    public static final Attribute myAcceleration = new Attribute("My acceleration", -2, 1, new MyAccelerationVE());
    public static final Attribute myDistToForwardWall = new Attribute("My distance to forward wall", 0, 1700, new MyDistanceToForwardWallVE());
    public static final Attribute myDistToReverseWall = new Attribute("My distance to reverse wall", 0, 1700, new MyDistanceToReverseWallVE());
    public static final Attribute myTravelTime = new Attribute("My travel time", 0, 255, new MyTravelTimeVE());

    public static final Attribute[] attributes = {
            distBetween,
            distBetween_100,
            roundTime,

            enemyX,
            enemyY,
            enemyVelocity,
            enemyVelocityModule,
            enemyAbsoluteHeading,
            enemyAcceleration,
            enemyTurnRate,

            enemyDistanceToForwardWall,
            enemyDistanceToReverceWall,
            enemyBearingToForwardWall,
            enemyBearingToMe,
            enemyStopTime,
            enemyTravelTime,
            enemyTurnTime,
            enemyDistanceToCenter,

            firstBulletBearingOffset,
            firstBulletFlightTime,

            myX,
            myY,
            myVelocity,
            myVelocityModule,
            myLateralVelocity_2,
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
        int[] attrValues = new int[attributes.length];
        List<LXXBullet> myBullets = office.getBulletManager().getBullets();
        for (final Attribute a : attributes) {
            if (a.getId() >= attributes.length) {
                throw new RuntimeException("Something wrong!");
            }
            final int av = a.getExtractor().getAttributeValue(t, robot, myBullets);
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

        return new TurnSnapshot(attrValues, robot.getTime(), office.getBattleTimeManager().getBattleTime(), t.getName());
    }

    public static int attributesCount() {
        return attributes.length;
    }
}
