package lxx.targeting.mg;

import lxx.utils.kd_tree.KeyExtractor;
import lxx.targeting.Target;
import static lxx.StaticData.robot;
import lxx.UltraMarine;
import lxx.StaticData;

import static java.lang.Math.round;
import static java.lang.Math.toDegrees;
import static java.lang.Math.cos;

import robocode.util.Utils;
import robocode.Rules;

/**
 * User: jdev
 * Date: 07.11.2009
 */
public class DuelGFKeyExtractor implements KeyExtractor<Target> {

    public String extractKey(Target t, int level) {
        switch (level) {
            case 1:
                return t.getName();
            case 2:
                return String.valueOf(StaticData.robot.getCornerManager().isInCorner(t.getPosition()));
            case 3:
                return String.valueOf(round(robot.distance(t) / 200));
            case 4:
                return String.valueOf(round(t.getVelocity() * Math.sin(t.getHeading() - robot.getGunHeadingRadians())));
            case 5:
                return String.valueOf(round(toDegrees(robot.getGunHeadingRadians() - t.getHeading()) / 18));
        }
        throw new IllegalArgumentException("Unsupported level: " + level);
    }

    public boolean canExtract(int level) {
        return level < 6;
    }
}