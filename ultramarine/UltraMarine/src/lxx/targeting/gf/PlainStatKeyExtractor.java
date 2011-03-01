package lxx.targeting.gf;

import lxx.targeting.Target;
import lxx.utils.kd_tree.KeyExtractor;
import static lxx.StaticData.robot;import static java.lang.Math.round;

/**
 * User: jdev
 * Date: 05.12.2009
 */
public class PlainStatKeyExtractor implements KeyExtractor<Target> {
    public String extractKey(Target target, int level) {
        switch (level) {
            case 1:
                return target.getName();
            case 2:
                return robot.getOthers() == 1 ? "1" : "2";
            case 3:
                return String.valueOf(round(robot.aDistance(target) / 25));
            case 4:
                return String.valueOf(round(target.getVelocity() / 2));
        }
        throw new IllegalArgumentException("Unsupported level: " + level);
    }

    public boolean canExtract(int level) {
        return level < 5;
    }
}
