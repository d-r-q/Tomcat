package lxx.targeting.gf;

import lxx.utils.kd_tree.KeyExtractor;
import lxx.utils.Utils;
import lxx.targeting.Target;
import static lxx.StaticData.robot;
import lxx.UltraMarine;
import lxx.StaticData;

import static java.lang.Math.round;
import static java.lang.Math.toDegrees;

import robocode.Rules;

/**
 * User: jdev
 * Date: 05.01.2010
 */
public class MeleeDVKeyExtractor implements KeyExtractor<Target> {
    public String extractKey(Target t, int level) {
        final double clearBulletSpeed = Rules.getBulletSpeed(((UltraMarine) robot).firePower());
        final Target closest = robot.getTargetManager().getClosestTergetToT(t);
        switch (level) {
            case 1:
                return t.getName();
            case 2:
                return String.valueOf(round(clearBulletSpeed / 2));
            case 3:
                return String.valueOf(StaticData.robot.getCornerManager().isInCorner(t.getPosition()));
            case 4:
                return String.valueOf(round(t.distance(robot.battleField.width / 2, robot.battleField.height / 2) / 100));
            case 5:
                return String.valueOf(round(t.getVelocity() / 2));
            case 6:
                if (closest == null) {
                    return "0";
                }
                return String.valueOf(round(t.aDistance(closest) / 100));
            case 7:
                if (closest == null) {
                    return "0";
                }
                return String.valueOf(round(toDegrees(Utils.angle(t, closest))) / 80);
        }
        throw new IllegalArgumentException("Unsupported level: " + level);
    }

    public boolean canExtract(int level) {
        return level < 8;
    }
}
