/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.strategies.duel;

import lxx.LXXRobotState;
import lxx.targeting.Target;
import lxx.targeting.TargetManager;
import lxx.targeting.tomcat_eyes.TomcatEyes;
import lxx.utils.APoint;
import lxx.utils.LXXConstants;
import lxx.utils.LXXUtils;
import robocode.util.Utils;

/**
 * User: jdev
 * Date: 20.06.11
 */
public class DistanceController {

    private static final double MAX_ATTACK_DELTA_WITHOUT_BULLETS = LXXConstants.RADIANS_30;
    private static final double MIN_ATTACK_DELTA_WITHOUT_BULLETS = LXXConstants.RADIANS_30;

    private static final double SIMPLE_DISTANCE = 650;
    private static final int ANTI_RAM_DISTANCE = 150;

    private final TargetManager targetManager;
    private final TomcatEyes tomcatEyes;

    public DistanceController(TargetManager targetManager, TomcatEyes tomcatEyes) {
        this.targetManager = targetManager;
        this.tomcatEyes = tomcatEyes;
    }

    public double getDesiredHeading(APoint surfPoint, LXXRobotState robot, OrbitDirection orbitDirection) {
        return getDesiredHeadingWithBullets(surfPoint, robot, orbitDirection, SIMPLE_DISTANCE);
    }

    private double getDesiredHeadingWithBullets(APoint surfPoint, LXXRobotState robot, OrbitDirection orbitDirection,
                                                double desiredDistance) {
        final double distanceBetween = robot.aDistance(surfPoint);

        final double distanceDiff = distanceBetween - desiredDistance;
        final double attackAngleKoeff = distanceDiff / desiredDistance;
        final Target duelOpponent = targetManager.getDuelOpponent();
        final double antiRamAngle = (duelOpponent != null && distanceBetween < ANTI_RAM_DISTANCE && tomcatEyes.isRammingNow(duelOpponent))
                ? LXXConstants.RADIANS_50 * (ANTI_RAM_DISTANCE - distanceBetween) / ANTI_RAM_DISTANCE
                : 0;
        final double maxAttackAngle = LXXConstants.RADIANS_100 + MAX_ATTACK_DELTA_WITHOUT_BULLETS;
        final double minAttackAngle = LXXConstants.RADIANS_80 - MIN_ATTACK_DELTA_WITHOUT_BULLETS - antiRamAngle / 2;
        final double attackAngle = LXXConstants.RADIANS_90 + ((LXXConstants.RADIANS_30 + antiRamAngle) * attackAngleKoeff);

        return Utils.normalAbsoluteAngle(surfPoint.angleTo(robot) +
                LXXUtils.limit(minAttackAngle, attackAngle, maxAttackAngle) * orbitDirection.sign);
    }

}
