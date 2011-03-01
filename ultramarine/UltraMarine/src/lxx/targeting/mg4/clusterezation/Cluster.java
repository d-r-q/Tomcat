package lxx.targeting.mg4.clusterezation;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import static java.lang.Math.abs;

/**
 * User: jdev
 * Date: 17.05.2010
 */
public class Cluster {

    private final List<Integer> values = new ArrayList<Integer>();

    private double center;
    private int minValue = Integer.MAX_VALUE;
    private int maxValue = Integer.MIN_VALUE;

    public Cluster(double center) {
        this.center = center;
    }

    public void addValue(int value) {
        center = (center + value) / 2;
        values.add(value);
        if (value < minValue) {
            minValue = value;
        }
        if (value > maxValue) {
            maxValue = value;
        }
    }

    public int distance(int value) {
        return (int) abs(value - center);
    }

    public int getValuesCount() {
        return values.size();
    }

    public int getMinValue() {
        return minValue;
    }

    public int getMaxValue() {
        return maxValue;
    }

    public boolean intersects(Cluster another) {
        return another.getMinValue() >= minValue && another.getMinValue() <= maxValue ||
                another.getMaxValue() >= minValue && another.getMaxValue() <= maxValue;
    }

    public int getIntersection(Cluster another) {
        if (!intersects(another)) {
            return 0;
        }

        if (contains(another)) {
            return another.getRange();
        } else if (another.contains(this)) {
            return getRange();
        } else if (another.getMinValue() >= minValue && another.getMinValue() <= maxValue) {
            return maxValue - another.getMinValue() + 1;
        } else if (another.getMaxValue() >= minValue && another.getMaxValue() <= maxValue) {
            return another.getMaxValue() - minValue + 1;
        } else {
            throw new RuntimeException("Something wrong");
        }
    }

    public Collection<? extends Integer> getValues() {
        return values;
    }

    public void clear(double center) {
        this.center = center;
        minValue = Integer.MAX_VALUE;
        maxValue = Integer.MIN_VALUE;
        values.clear();
    }

    public double getCenter() {
        return center;
    }

    public boolean contains(Cluster another) {
        return another.minValue >= minValue && another.maxValue <= maxValue;
    }

    public int getRange() {
        return maxValue - minValue + 1;
    }
}