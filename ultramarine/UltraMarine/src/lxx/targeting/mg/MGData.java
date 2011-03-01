package lxx.targeting.mg;

import lxx.utils.kd_tree.KDData;

import java.util.Map;
import java.util.HashMap;

/**
 * User: jdev
 * Date: 07.11.2009
 */
public class MGData implements KDData {

    private Map<Double, Double> stat = new HashMap<Double, Double>();
    private int statCount = 0;

    private double factor;

    public MGData(double factor) {
        this.factor = factor;
    }

    public void addStat(Object... data) {
        final Double d = (Double) data[0];
        Double count = stat.get(d);
        if (count == null) {
            count = 0D;
        }
        count += 1D;
        stat.put(d, count);

        for(Double key : stat.keySet()) {
            stat.put(key, stat.get(key) * factor);
        }

        statCount++;
    }

    public KDData createInstance() {
        return new MGData(factor);
    }

    public Map<Double, Double> getStat() {
        return stat;
    }

    public String toString() {
        return statCount + ", " + stat;
    }
}