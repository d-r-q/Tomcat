package lxx.stat;

import lxx.targeting.bullets.BulletManagerListener;
import lxx.targeting.bullets.LXXBullet;
import lxx.targeting.Target;
import lxx.targeting.TargetManager;
import lxx.targeting.predict.Predictor;
import lxx.targeting.predict.PredictorManager;
import lxx.utils.HitRate;
import lxx.StaticData;

import java.util.Map;
import java.util.HashMap;
import java.text.NumberFormat;
import java.text.DecimalFormat;

/**
 * User: jdev
 * Date: 15.02.2010
 */
public class HitRateStatistics implements BulletManagerListener {

    private static final int HIT_RATE_COUNT = (int)(StaticData.robot.getBattleFieldDiag() / 100);

    private static final Map<Target, HitRate[]> hitRatesByTarget = new HashMap<Target, HitRate[]>();
    private static final Map<Predictor, HitRate[]> hitRatesByPredictort = new HashMap<Predictor, HitRate[]>();

    private final TargetManager targetManager;
    private final PredictorManager predictorManager;

    public HitRateStatistics(TargetManager targetManager, PredictorManager predictorManager) {
        this.targetManager = targetManager;
        this.predictorManager = predictorManager;
    }

    private HitRate[] getHitRatesByTarget(Target t) {
        HitRate[] hitRates = HitRateStatistics.hitRatesByTarget.get(t);
        if (hitRates == null) {
            hitRates = new HitRate[HIT_RATE_COUNT + 1];
            for (int i = 0; i < hitRates.length; i++) {
                hitRates[i] = new HitRate();
            }
            HitRateStatistics.hitRatesByTarget.put(t, hitRates);
        }

        return hitRates;
    }

    private HitRate[] getHitRatesByPredictor(Predictor p) {
        HitRate[] hitRates = HitRateStatistics.hitRatesByPredictort.get(p);
        if (hitRates == null) {
            hitRates = new HitRate[HIT_RATE_COUNT + 1];
            for (int i = 0; i < hitRates.length; i++) {
                hitRates[i] = new HitRate();
            }
            HitRateStatistics.hitRatesByPredictort.put(p, hitRates);
        }

        return hitRates;
    }

    public void bulletHit(LXXBullet bullet) {
        HitRate[] hr = getHitRatesByTarget(bullet.getTarget());
        hr[((int)(bullet.getFireDistance() / 100))].hitCount++;
        hr[HIT_RATE_COUNT].hitCount++;

        for (Predictor p : bullet.getPredictors()) {
            hr = getHitRatesByPredictor(p);
            hr[((int)(bullet.getFireDistance() / 100))].hitCount++;
            hr[HIT_RATE_COUNT].hitCount++;
        }
    }

    public void bulletMiss(LXXBullet bullet) {
        HitRate[] hr = getHitRatesByTarget(bullet.getTarget());
        hr[((int)(bullet.getFireDistance() / 100))].missCount++;
        hr[HIT_RATE_COUNT].missCount++;

        //System.out.println(bullet.getPredictors());
        for (Predictor p : bullet.getPredictors()) {
            hr = getHitRatesByPredictor(p);
            hr[((int)(bullet.getFireDistance() / 100))].missCount++;
            hr[HIT_RATE_COUNT].missCount++;
        }
    }

    public void bulletNotHit(LXXBullet bullet) {
    }

    public String toString() {
        final NumberFormat format = new DecimalFormat();
        format.setMaximumFractionDigits(1);
        format.setMaximumIntegerDigits(3);

        final StringBuffer res = new StringBuffer();
        for (Target t : targetManager.getTargets().values()) {
            res.append("Hit rates for " + t.getName() + ": ");
            HitRate[] hrs = getHitRatesByTarget(t);
            /*for (int i = 0; i < hrs.length; i++) {
                final double hc = hrs[i].hitCount;
                final double tc = hc + hrs[i].missCount;
                res.append(i * 100 + "(" + (hc + "/" + tc) +  "): " + format.format(hc / tc * 100) + "%\n");
            }*/
            final double hc = hrs[hrs.length - 1].hitCount;
            final double tc = hc + hrs[hrs.length - 1].missCount;
            res.append("(" + hc + "/" + tc + ")" + format.format(hc / tc * 100) + "%\n");
        }
        res.append("\n");
        for (Predictor p : predictorManager.getPredictors()) {
            res.append("Hit rates for " + p.getName() + ": ");
            HitRate[] hrs = getHitRatesByPredictor(p);
            /*for (int i = 0; i < hrs.length; i++) {
                final double hc = hrs[i].hitCount;
                final double tc = hc + hrs[i].missCount;
                res.append((i * 100) + "(" + (hc + "/" + tc) + "): " + format.format(hc / tc * 100) + "%\n");
            }*/
            final double hc = hrs[hrs.length - 1].hitCount;
            final double tc = hc + hrs[hrs.length - 1].missCount;
            res.append("(" + hc + "/" + tc + ")" + format.format(hc / tc * 100) + "%\n");
        }
        return res.toString();
    }
}
