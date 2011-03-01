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
import java.util.List;

/**
 * User: jdev
 * Date: 10.03.2010
 */
public class MostProbablyGFGun implements Predictor {

    private final Segmentator segmentator;
    private final SegmentationsManager segmentationsManager;

    public MostProbablyGFGun(Segmentator segmentator, SegmentationsManager segmentationsManager) {
        this.segmentator = segmentator;
        this.segmentationsManager = segmentationsManager;
    }

    public Double predictAngle(Target t) {
        FireSituation fs = null;//segmentationsManager.getFireSituation0(t);
        List<FireSituation> fses = segmentator.getFireSutations(fs);
        if (fses == null || fses.size() == 0) {
            return null;
        }
        
        int[] gfCounts = new int[LXXConstants.MAX_GUESS_FACTOR * 2 + 1];
        for (FireSituation f : fses) {
            gfCounts[f.getGuessFactor() + LXXConstants.MAX_GUESS_FACTOR]++;
        }

        int maxVisitedGF = 0;
        int maxVisits = 0;
        for (int i = 1; i < gfCounts.length - 1; i++) {
            final int visits = gfCounts[i - 1] + gfCounts[i] + gfCounts[i + 1];
            if (visits > maxVisits) {
                maxVisits = visits;
                maxVisitedGF = i - LXXConstants.MAX_GUESS_FACTOR;
            }
        }

        final double bearing = toRadians(ceil(abs(toDegrees(t.maxEscapeAngle(maxVisitedGF >= 0 ? 1 : -1))) * maxVisitedGF / LXXConstants.MAX_GUESS_FACTOR));
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

        MostProbablyGFGun that = (MostProbablyGFGun) o;

        return !(segmentator != null ? !segmentator.equals(that.segmentator) : that.segmentator != null);

    }

    public int hashCode() {
        return (segmentator != null ? segmentator.hashCode() : 0);
    }
}
