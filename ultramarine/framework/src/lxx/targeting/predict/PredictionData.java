package lxx.targeting.predict;

import lxx.utils.kd_tree.KDData;
import lxx.utils.HitRate;

import java.util.Map;
import java.util.HashMap;

/**
 * User: jdev
 * Date: 31.10.2009
 */
public class PredictionData implements KDData {

    private final Map<Predictor, HitRate> hitRates = new HashMap<Predictor, HitRate>();
    private int visitCount;

    public void addStat(Object... data) {
        for (Object o : data) {
            PredictorAccuracy pa = (PredictorAccuracy) o;
            if (pa == null) {
                continue;
            }
            HitRate hr = hitRates.get(pa.predictor);
            if (hr == null) {
                hr = new HitRate();
                hitRates.put(pa.predictor, hr);
            }

            if (pa.isHit == null) {
                hr.hitCount += 0.5;
            } else if (pa.isHit) {
                hr.hitCount++;
            } else {
                hr.missCount++;
            }

            for (HitRate hr1 : hitRates.values()) {
                hr.hitCount *= 0.61;
                hr.missCount *= 0.61;
            }
        }

        visitCount++;
    }

    public KDData createInstance() {
        return new PredictionData();
    }

    public Map<Predictor, HitRate> getHitRates() {
        return hitRates;
    }

    public int getVisitCount() {
        return visitCount;
    }

    public String toString() {
        return hitRates.toString();
    }

}
