package lxx.targeting.gf;

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
public class DuelDVKeyExtractor implements KeyExtractor<Target> {
    
    public String extractKey(Target t, int level) {
        final double clearBulletSpeed = Rules.getBulletSpeed(((UltraMarine) robot).firePower());
        switch (level) {
            case 1:
                return t.getName();
            case 2:
                return String.valueOf(round(clearBulletSpeed / 2));
            case 3:
                final double bulletSpeed = clearBulletSpeed - cos(t.getHeading() - robot.angleTo(t)) * t.getVelocity();
                return String.valueOf(round(Math.ceil(robot.distance(t) / bulletSpeed) - 1) / 5);
            case 4:
                return String.valueOf(round(t.getVelocity() / 2));
            case 5:
                return String.valueOf(round(t.getVelocity() * Math.sin(t.getHeading() - robot.angleTo(t))) / 3);
            case 6:
                return String.valueOf(StaticData.robot.getCornerManager().isInCorner(t.getPosition()));
            case 7:
                return String.valueOf(t.getLastStopTime() - robot.getTime());
        }
        throw new IllegalArgumentException("Unsupported level: " + level);
    }

    public boolean canExtract(int level) {
        return level < 8;
    }
}
