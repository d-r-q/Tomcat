package lxx.targeting.mg3;

import lxx.targeting.predict.Predictor;
import lxx.targeting.Target;
import lxx.autosegmentation.segmentator.Segmentator;
import lxx.autosegmentation.SegmentationsManager;
import lxx.autosegmentation.model.FireSituation;
import lxx.utils.LXXConstants;
import static lxx.StaticData.robot;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import static java.lang.Math.toRadians;
import static java.lang.Math.ceil;
import static java.lang.Math.abs;
import static java.lang.Math.toDegrees;
import java.awt.*;

/**
 * User: jdev
 * Date: 14.03.2010
 */
public class MinProbablyGFGun implements Predictor {

    private final Segmentator segmentator;
    private final SegmentationsManager segmentationsManager;

    public MinProbablyGFGun(Segmentator segmentator, SegmentationsManager segmentationsManager) {
        this.segmentator = segmentator;
        this.segmentationsManager = segmentationsManager;
    }

    public Double predictAngle(Target t) {
        FireSituation fs = null;//segmentationsManager.getFireSituation0(t);
        List<FireSituation> fses = segmentator.getFireSutations(fs);
        if (fses == null || fses.size() == 0) {
            return null;
        }

        Map<Integer, Integer> gfCounts = new HashMap<Integer, Integer>();
        for (FireSituation f : fses) {
            Integer gfCount = gfCounts.get(f.getGuessFactor());
            if (gfCount == null) {
                gfCount = 0;
            }

            gfCount++;
            gfCounts.put(f.getGuessFactor(), gfCount);
        }

        int minVisitedGF = 0;
        int minVisits = Integer.MAX_VALUE;
        for (Map.Entry<Integer, Integer> e : gfCounts.entrySet()) {
            if (e.getValue() < minVisits && e.getValue() > 0) {
                minVisits = e.getValue();
                minVisitedGF = e.getKey();
            }
        }

        final double bearing = toRadians(ceil(abs(toDegrees(t.maxEscapeAngle(minVisitedGF >= 0 ? 1 : -1))) * minVisitedGF / LXXConstants.MAX_GUESS_FACTOR));
        return robot.angleTo(t) + bearing;
    }

    public void paint(Graphics2D g, Target t) {
    }

    public void onRoundStarted() {
    }

    public String getName() {
        return getClass().getSimpleName();
    }

    public void targetUpdated(Target oldState, Target newState, robocode.Event source) {
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MinProbablyGFGun that = (MinProbablyGFGun) o;

        return !(segmentator != null ? !segmentator.equals(that.segmentator) : that.segmentator != null);

    }

    public int hashCode() {
        return (segmentator != null ? segmentator.hashCode() : 0);
    }
}

