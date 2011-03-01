package lxx.targeting.predict;

import lxx.utils.HitRate;

import java.util.List;
import java.util.ArrayList;

/**
 * User: jdev
 * Date: 11.03.2010
 */
public class PredictorData {

    private final List<HitRate> hitRates = new ArrayList<HitRate>();
    private long lastHitTime = 0;

    private long totalHitTime;

    public long getLastHitTime() {
        return lastHitTime;
    }

    public void setLastHitTime(long lastHitTime) {
        this.lastHitTime = lastHitTime;
    }

    public void addHitRate(HitRate hr) {
        hitRates.add(hr);
        if (hitRates.size() > 11) {
            hitRates.remove(0);
        }
    }

    public void addHitTime(long hitTime) {
        totalHitTime += hitTime;
    }

    public long getAvgHitTime() {
        if (hitRates.size() == 0) {
            return 0;
        }
        return totalHitTime / hitRates.size();
    }

    public double getHitRate() {
        double hitCount = 0;
        double missCount = 0;
        for (HitRate hr : hitRates) {
            hitCount += hr.hitCount;
            missCount += hr.missCount;
        }
        if (missCount == 0 | (missCount == 0 && hitCount == 0)) {
            return 1;
        }
        return hitCount / (hitCount + missCount);
    }
}
