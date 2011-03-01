package lxx.targeting.mg;

import lxx.targeting.Target;
import lxx.utils.kd_tree.KeyExtractor;
import lxx.utils.LXXPoint;
import static lxx.StaticData.robot;

import static java.lang.Math.round;
import static java.lang.Math.toDegrees;

/**
 * User: jdev
 * Date: 17.02.2010
 */
public class WallsKeyExtractor implements KeyExtractor<Target> {

    public String extractKey(Target target, int level) {
        switch (level) {
            case 1:
                return target.getName();
            case 2:
                return String.valueOf(round(target.getLateralVelocity() / 2));
            case 3:
                return String.valueOf(round(robot.aDistance(target) / 100));
            case 4:
                return String.valueOf(round(target.distanceToHeadOnWall()) / 100);
            case 5:
                return String.valueOf(round((robot.getTime() - target.getLastStopTime()) / 10));
        }

        throw new IllegalArgumentException("Not supported level: " + level);
    }

    public boolean canExtract(int level) {
        return level < 6;
    }
}
