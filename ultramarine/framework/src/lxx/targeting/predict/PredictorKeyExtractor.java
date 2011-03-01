package lxx.targeting.predict;

import lxx.utils.kd_tree.KeyExtractor;
import lxx.StaticData;


import static java.lang.Math.round;

/**
 * User: jdev
 * Date: 31.10.2009
 */
public class PredictorKeyExtractor implements KeyExtractor<Object[]> {

    public String extractKey(Object[] data, int level) {
        switch (level) {
            case 1:
                // target name
                return (String) data[0];
            case 2:
                // enemy energy
                return String.valueOf(((Double) data[5]) > 1);
            /*case 3:
                return String.valueOf(StaticData.robot.getOthers() > 1);
            case 4:
                // is in corner
                return String.valueOf(data[3]);
            case 5:
                // distance
                return String.valueOf(round((Double) data[1]) / 100);
            case 6:
                // lateral velocity
                return String.valueOf(round(((Double) data[2])));
            case 7:
                // distance to center
                return String.valueOf(round(((Double) data[4]) / 100));
            case 8:
                // stop time
                return String.valueOf(round(((Long) data[6]) / 2));*/

            default:
                throw new IllegalArgumentException("Unsupported level: " + level);
        }
    }

    public boolean canExtract(int level) {
        return level < 3;
    }
}
