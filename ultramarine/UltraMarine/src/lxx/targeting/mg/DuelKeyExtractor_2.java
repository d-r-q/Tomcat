package lxx.targeting.mg;

import lxx.utils.kd_tree.KeyExtractor;
import lxx.utils.Utils;
import lxx.utils.LXXPoint;
import lxx.targeting.Target;
import static lxx.StaticData.robot;
import lxx.UltraMarine;

import static java.lang.Math.toDegrees;
import static java.lang.StrictMath.round;

/**
 * User: jdev
 * Date: 14.02.2010
 */
public class DuelKeyExtractor_2 implements KeyExtractor<Target> {

    public String extractKey(Target target, int level) {
        LXXPoint center = new LXXPoint(robot.getBattleFieldWidth() / 2, robot.getBattleFieldHeight() / 2);
        final long time = robot.getTime();
        switch (level) {
            case 1:
                return target.getName();
            case 2:
                return String.valueOf(round((robot.getLastFireTime() - time) / 5));
            case 3:
                return String.valueOf(round(((target.getLastTrevelTime() - time) / 5)));
            case 4:
                return String.valueOf(round(((target.getLastStopTime() - time) / 5)));
            case 5:
                return String.valueOf(round(robot.aDistance(target) / 100));
            case 6:
                return String.valueOf(round(target.aDistance(center) / 100));
            /*case 7:
                return String.valueOf(round(toDegrees(Utils.angle(target, robot) -
                        target.getHeading()) / 10));*/

        }

        throw new IllegalArgumentException("Unsupported level: " + level);
    }

    public boolean canExtract(int level) {
        return level < 7;
    }
}
