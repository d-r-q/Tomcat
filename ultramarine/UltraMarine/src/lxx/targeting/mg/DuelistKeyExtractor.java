package lxx.targeting.mg;

import lxx.targeting.Target;
import lxx.utils.kd_tree.KeyExtractor;
import lxx.utils.LXXPoint;
import lxx.UltraMarine;
import static lxx.StaticData.robot;

import static java.lang.Math.round;import static java.lang.Math.toDegrees;

/**
 * User: jdev
 * Date: 17.02.2010
 */
public class DuelistKeyExtractor implements KeyExtractor<Target> {
    public String extractKey(Target target, int level) {
        LXXPoint center = new LXXPoint(robot.getBattleFieldWidth() / 2, robot.getBattleFieldHeight() / 2);
        switch (level) {
            case 1:
                return target.getName();
            case 2:
                return String.valueOf(round(target.distanceToHeadOnWall()) / 25);
            case 3:
                return String.valueOf(round(toDegrees(target.getHeading())));
            case 4:
                return String.valueOf(Math.round((robot.getTime() - ((UltraMarine)robot).getLastFireTime()) / 10));
            case 5:
                return String.valueOf(round(toDegrees(target.bearingToClosestWall()) / 18));
            case 6:
                return String.valueOf(round(target.getVelocityDelta()));
            case 7:
                return String.valueOf(round((robot.getTime() - target.getLastTrevelTime()) / 30));
/*            case 5:
                return String.valueOf(round(target.getHeading() / 50));
            case 6:
                return String.valueOf(round(target.aDistance(center) / 50));
            case 7:
                return String.valueOf(StrictMath.round((((UltraMarine)robot).getLastFireTime() - robot.getTime()) / 2));*/
        }

        throw new IllegalArgumentException("Unsupported level: " + level);
    }

    public boolean canExtract(int level) {
        return level < 8;
    }
}
