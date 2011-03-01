package lxx.targeting.gf;

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
       final double clearBulletSpeed = Rules.getBulletSpeed(((UltraMarine) robot).firePower());
        switch (level) {
            case 1:
                return t.getName();
            case 2:
                return robot.getOthers() == 1 ? "1" : "2";
            case 3:
                return String.valueOf(round(clearBulletSpeed / 4));
            case 4:
                final double bulletSpeed = clearBulletSpeed - cos(t.getHeading() - robot.angleTo(t)) * t.getVelocity();
                return String.valueOf(round(Math.ceil(robot.distance(t) / bulletSpeed) - 1) / 10);
            case 5:
                return String.valueOf(round(t.getVelocity() / 2));
            case 6:
                return String.valueOf(round(toDegrees(t.getHeading() / 36)));
            case 7:
                return String.valueOf(round(t.getX() / 10) + "x" + round(t.getY() / 10));
            /*case 4:
                return String.valueOf(round(target.getVelocityDelta()) / 2);
            case 5:
                return String.valueOf(round(target.getHeadingDelta() / 4));
            case 6:
                return String.valueOf(target.getVelocity() / 8);
            case 7:
                return String.valueOf(round(toDegrees(target.getHeading() / 20)));*/
        }
        throw new IllegalArgumentException("Unsupported level: " + level);
    }

    public boolean canExtract(int level) {
        return level <8;
    }
}
