/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.strategies.duel;

import lxx.targeting.Target;
import lxx.targeting.TargetManager;
import lxx.utils.LXXConstants;
import lxx.utils.LXXPoint;
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

    public DistanceController(TargetManager targetManager) {
        this.targetManager = targetManager;
    }

    public double getDesiredHeading(LXXPoint surfPoint, LXXPoint robotPos, OrbitDirection orbitDirection) {
        final double distanceBetween = robotPos.aDistance(surfPoint);

        final double distanceDiff = distanceBetween - SIMPLE_DISTANCE;
        final double attackAngleKoeff = distanceDiff / SIMPLE_DISTANCE;
        final Target duelOpponent = targetManager.getDuelOpponent();
        final double antiRamAngle = (duelOpponent != null && distanceBetween < ANTI_RAM_DISTANCE && duelOpponent.isRammingNow())
                ? LXXConstants.RADIANS_50 * (ANTI_RAM_DISTANCE - distanceBetween) / ANTI_RAM_DISTANCE
                : 0;
        final double maxAttackAngle = LXXConstants.RADIANS_100 + MAX_ATTACK_DELTA_WITHOUT_BULLETS;
        final double minAttackAngle = LXXConstants.RADIANS_80 - MIN_ATTACK_DELTA_WITHOUT_BULLETS - antiRamAngle / 2;
        final double attackAngle = LXXConstants.RADIANS_90 + ((LXXConstants.RADIANS_30 + antiRamAngle) * attackAngleKoeff);

        return Utils.normalAbsoluteAngle(LXXUtils.angle(surfPoint.x, surfPoint.y, robotPos.x, robotPos.y) +
                LXXUtils.limit(minAttackAngle, attackAngle, maxAttackAngle) * orbitDirection.sign);
    }

}
