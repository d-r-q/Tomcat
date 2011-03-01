package lxx.strat.duel;

import lxx.utils.kd_tree.KeyExtractor;
import lxx.StaticData;
import static lxx.StaticData.robot;

import static java.lang.Math.round;

/**
 * User: jdev
 * Date: 09.01.2010
 */
public class FACKeyExtractor implements KeyExtractor<Object[]> {
    public String extractKey(Object[] data, int level) {
        switch (level) {
            case 1:
                // Source name
                return (String) data[0];
            case 2:
                return String.valueOf(robot.getOthers());
            /*case 3:
                return data[1].toString();
            case 4:
                final Double lateralVelocity = (Double) data[3];
                if (lateralVelocity < 0) {
                    return "0";
                } else if (lateralVelocity > 0) {
                    return "1";
                } else {
                    return "2";
                }*/
            /*case 2:
                // Distance
                return String.valueOf(round((Double)data[1]) / 100);
            case 3:
                // Target velocity
                return String.valueOf((Double)data[2] / 4);
            case 4:
                // lateral velocity
                return String.valueOf((Double)data[3] / 4);*/
        }

        throw new IllegalArgumentException("Unsupported level: " + level);
    }

    public boolean canExtract(int level) {
        return level < 3;
    }
}
