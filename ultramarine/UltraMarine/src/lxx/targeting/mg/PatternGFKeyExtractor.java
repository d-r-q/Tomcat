package lxx.targeting.mg;

import lxx.targeting.Target;
import lxx.utils.kd_tree.KeyExtractor;
import static lxx.StaticData.robot;
import lxx.UltraMarine;
import robocode.Rules;

import static java.lang.Math.cos;
import static java.lang.Math.round;
import static java.lang.Math.toDegrees;

/**
 * User: jdev
 * Date: 07.12.2009
 */
public class PatternGFKeyExtractor implements KeyExtractor<Target> {

    public String extractKey(Target t, int level) {
        switch (level) {
            case 1:
                return t.getName();
            case 2:
                return robot.getOthers() == 1 ? "1" : "2";
            case 3:
                return String.valueOf(round(robot.aDistance(t) / 200));
            case 4:
                return String.valueOf(round(toDegrees(robot.getGunHeadingRadians() - t.getHeading()) / 18));
            case 5:
                return String.valueOf(round(t.getX() / 50));
            case 6:
                return String.valueOf(round(t.getY() / 50));
        }
        throw new IllegalArgumentException("Unsupported level: " + level);
    }

    public boolean canExtract(int level) {
        return level < 7;
    }
}