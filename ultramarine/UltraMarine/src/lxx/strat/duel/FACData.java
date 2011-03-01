package lxx.strat.duel;

import lxx.utils.kd_tree.KDData;
import lxx.utils.Utils;
import lxx.wave.Wave;
import lxx.StaticData;

import java.util.Map;
import java.util.HashMap;
import static java.lang.StrictMath.round;
import static java.lang.Math.toDegrees;
import static java.lang.Math.abs;

/**
 * User: jdev
 * Date: 09.01.2010
 */
public class FACData implements KDData {

    private final Map<Double, Double> angleRisks = new HashMap<Double, Double>();

    public FACData() {
        angleRisks.put(0D, 0D);
        for (double i = 2; i <= 30; i += 2) {
            angleRisks.put(i, 0D);
            angleRisks.put(-i, 0D);
        }
    }

    public void addStat(Object... data) {
        Wave w = (Wave) data[0];
        boolean hitInThisTick = (Boolean) data[1];

        double alpha = round(toDegrees(Utils.angle(w.sourcePos, w.targetPos) - Utils.angle(w.sourcePos, StaticData.robot))) / 2;
        Double risk = angleRisks.get(alpha);
        if (risk == null) {
            risk = 0D;
        }
        if (hitInThisTick) {
            angleRisks.put(alpha, risk + 1);
        }

        for (Double angle : angleRisks.keySet()) {
            if (angle == alpha) {
                continue;
            }
            risk = angleRisks.get(angle);
            angleRisks.put(angle, risk - 0.01);
        }
    }

    public Map<Double, Double> getAngleRisks() {
        return angleRisks;
    }

    public KDData createInstance() {
        return new FACData();
    }
}
