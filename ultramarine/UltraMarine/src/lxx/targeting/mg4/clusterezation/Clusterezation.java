package lxx.targeting.mg4.clusterezation;

import lxx.autosegmentation.model.Attribute;
import lxx.autosegmentation.model.FireSituation;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import static java.lang.Math.min;
import static java.lang.StrictMath.max;

/**
 * User: jdev
 * Date: 17.05.2010
 */
public class Clusterezation {

    private final Attribute attribute;

    private final Cluster[] clusters = new Cluster[3];
    private int realClusterCount = 0;

    public Clusterezation(Attribute attribute) {
        this.attribute = attribute;
        double step = 1D / (clusters.length + 1);
        int idx = 0;
        for (double center = step; center < 1; center += step, idx++) {
            clusters[idx] = new Cluster(attribute.getMinValue() + (attribute.getRange()) * center);
        }
    }

    public void addFireSituation(FireSituation fs) {
        double minDist = Integer.MAX_VALUE;
        Cluster closestCluster = null;
        final int value = fs.getAttributeValue(attribute);

        for (Cluster c : clusters) {
            int dist = c.distance(value);
            if (dist < minDist) {
                minDist = dist;
                closestCluster = c;
            }
        }

        if (closestCluster != null) {
            closestCluster.addValue(value);
            if (closestCluster.getValuesCount() == 1) {
                realClusterCount++;
            } else {
                for (Cluster c : clusters) {
                    if (c != closestCluster && closestCluster.intersects(c)) {
                        recluster(c, closestCluster);
                    }
                }
            }
        } else {
            throw new RuntimeException("Something wrong");
        }
    }

    private void recluster(Cluster c1, Cluster c2) {
        final List<Integer> values = new ArrayList<Integer>();
        values.addAll(c1.getValues());
        values.addAll(c2.getValues());
        Collections.sort(values);
        int minValue = min(c1.getMinValue(), c2.getMinValue());
        int maxValue = max(c1.getMaxValue(), c2.getMaxValue());
        double center1 = minValue + (maxValue - minValue) * 0.25;
        double center2 = minValue + (maxValue - minValue) * 0.75;
        c1.clear(center1);
        c2.clear(center2);
        for (Integer value : values) {
            if (c1.distance(value) < c2.distance(value)) {
                c1.addValue(value);
            } else {
                c2.addValue(value);
            }
        }
    }

    public int getRealClusterCount() {
        return realClusterCount;
    }

    public boolean hasIntersections() {
        for (int i = 0; i < clusters.length; i++) {
            for (int j = i + 1; j < clusters.length; j++) {
                if (clusters[i].intersects(clusters[j])) {
                    return true;
                }
            }
        }
        return false;
    }

    public String toString() {
        StringBuffer res = new StringBuffer(attribute.getName() + ": ");
        for (Cluster c : clusters) {
            if (c.getValuesCount() > 0) {
                res.append("[" + c.getMinValue() + " - " + c.getCenter() + " - " + c.getMaxValue() + "] ");
            }
        }
        return res.toString();
    }

    public double getIntersection(Clusterezation another) {
        if (attribute != another.attribute) {
            throw new RuntimeException("Something wrong");
        }
        int totalClustersInterSection = 0;
        for (int i = 0; i < clusters.length; i++) {
            for (int j = 0; j < another.clusters.length; j++) {
                totalClustersInterSection += clusters[i].getIntersection(another.clusters[j]);
            }
        }

        final double res = (double) totalClustersInterSection / attribute.getActualRange();
        if (res > 1) {
            throw new RuntimeException("Something wrong");
        }
        return res;
    }

}
