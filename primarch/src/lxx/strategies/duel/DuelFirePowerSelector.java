/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.strategies.duel;

import lxx.Primarch;
import lxx.strategies.FirePowerSelector;
import lxx.targeting.Target;
import robocode.Rules;

public class DuelFirePowerSelector implements FirePowerSelector {

    public double selectFirePower(Primarch robot, Target target) {
        if (target == null || robot.getEnergy() < 0.2) {
            return 0;
        }
        if (robot.aDistance(target) < 100) {
            return 3;
        }
        double bulletPower = 1.9;

        if (Rules.getBulletDamage(bulletPower) > target.getEnergy()) {
            bulletPower = target.getEnergy() / 4D;
        }


        if (bulletPower < 0.1) {
            bulletPower = 0.1;
        }
        if (robot.getEnergy() < 10) {
            bulletPower = robot.getEnergy() / 20;
        } else if (bulletPower >= 0.9 && bulletPower <= 1) {
            bulletPower = 1.1;
        }

        return bulletPower;


    }

}
