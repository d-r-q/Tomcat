package lxx.utils;

import java.text.NumberFormat;
import java.text.DecimalFormat;

/**
 * User: jdev
 * Date: 15.02.2010
 */
public class HitRate {
    
    public double hitCount = 0;
    public double missCount = 0;

    public HitRate() {
    }

    public HitRate(boolean isHit) {
        if (isHit) {
            hitCount++;
        } else {
            missCount++;
        }
    }

    public String toString() {
        NumberFormat format = new DecimalFormat();
        format.setMaximumFractionDigits(1);
        format.setMaximumIntegerDigits(3);
        return "Predictor hit rate (" + hitCount + "/" + (hitCount + missCount) + ") = " + format.format((double) hitCount / (double) (hitCount + missCount) * 100) + "%";
    }
}
