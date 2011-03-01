package lxx.targeting.mg3;

import static lxx.StaticData.robot;
import lxx.autosegmentation.SegmentationsManager;
import lxx.autosegmentation.model.FireSituation;
import lxx.autosegmentation.segmentator.Segmentator;
import lxx.targeting.Target;
import lxx.targeting.predict.Predictor;
import lxx.utils.LXXConstants;

import java.awt.*;
import static java.lang.Math.*;
import java.util.HashMap;
import java.util.Map;

/**
 * User: jdev
 * Date: 11.03.2010
 */
public class MathExpectionGFGun implements Predictor {

    private final Segmentator segmentator;
    private final SegmentationsManager segmentationsManager;

    public MathExpectionGFGun(Segmentator segmentator, SegmentationsManager segmentationsManager) {
        this.segmentator = segmentator;
        this.segmentationsManager = segmentationsManager;
    }

    public Double predictAngle(Target t) {
        FireSituation fs = null; //segmentationsManager.getFireSituation0(t);
        java.util.List<FireSituation> fses = segmentator.getFireSutations(fs);
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

        double mathExpection = 0;
        for (Map.Entry<Integer, Integer> e : gfCounts.entrySet()) {
            mathExpection += e.getKey() * ((double)e.getValue() / fses.size());
        }

        final double bearing = toRadians(ceil(abs(toDegrees(t.maxEscapeAngle(mathExpection >= 0 ? 1 : -1))) * mathExpection / LXXConstants.MAX_GUESS_FACTOR));
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

        MathExpectionGFGun that = (MathExpectionGFGun) o;

        if (segmentator != null ? !segmentator.equals(that.segmentator) : that.segmentator != null) return false;

        return true;
    }

    public int hashCode() {
        return (segmentator != null ? segmentator.hashCode() : 0);
    }
}
