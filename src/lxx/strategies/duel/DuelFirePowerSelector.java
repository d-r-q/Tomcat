/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.strategies.duel;

import lxx.Tomcat;
import lxx.office.StatisticsManager;
import lxx.strategies.FirePowerSelector;
import lxx.targeting.Target;
import lxx.utils.LXXUtils;
import robocode.Rules;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.StrictMath.ceil;

public class DuelFirePowerSelector implements FirePowerSelector {

    private final StatisticsManager statisticsManager;

    public DuelFirePowerSelector(StatisticsManager statisticsManager) {
        this.statisticsManager = statisticsManager;
    }

    public double selectFirePower(Tomcat robot, Target target) {
        if (target == null || robot.getEnergy() < 0.2) {
            return 0;
        }

        double bulletPower = 1.95;
        if (robot.aDistance(target) < 75) {
            return min(3, robot.getEnergy() - 0.1);
        }
        if (target.isRammingNow() && robot.aDistance(target) < 150) {
            bulletPower = 3;
        }

        final double firesPerHits = (statisticsManager.getMyRawHitRate() > 0
                ? ceil(1 / statisticsManager.getMyRawHitRate()) + 1
                : 20) * max(1, target.getEnergy() / robot.getEnergy()) * 3;
        bulletPower = LXXUtils.limit(0.1, min(bulletPower, robot.getEnergy() / firesPerHits), 3);

        if (Rules.getBulletDamage(bulletPower) > target.getEnergy()) {
            bulletPower = target.getEnergy() / 4D;
        }

        if (bulletPower < 0.1) {
            bulletPower = 0.1;
        }
        if (bulletPower >= 0.9 && bulletPower <= 1) {
            bulletPower = 1.1;
        }

        return bulletPower;


    }

}
