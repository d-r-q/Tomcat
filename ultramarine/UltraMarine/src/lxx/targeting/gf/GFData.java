package lxx.targeting.gf;

import lxx.utils.kd_tree.KDData;

import java.util.Map;
import java.util.HashMap;

/**
 * User: jdev
 * Date: 07.11.2009
 */
public class GFData implements KDData {

    private Map<String, Double> stat = new HashMap<String, Double>();
    private int statCount = 0;

    public void addStat(Object... data) {
        final String d = data[0] + ":" + data[1];
        Double count = stat.get(d);
        if (count == null) {
            count = 0D;
        }
        count += 1D;
        stat.put(d, count);

        for(String key : stat.keySet()) {
            stat.put(key, stat.get(key) * 0.91);
        }

        statCount++;
    }

    public KDData createInstance() {
        return new GFData();
    }

    public Map<String, Double> getStat() {
        return stat;
    }

    public String toString() {
        return statCount + ", " + stat;
    }
}
