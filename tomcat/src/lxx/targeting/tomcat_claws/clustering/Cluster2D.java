package lxx.targeting.tomcat_claws.clustering;

import lxx.utils.APoint;
import lxx.utils.AvgValue;
import lxx.utils.LXXPoint;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
* User: jdev
* Date: 17.06.11
*/
public class Cluster2D {

    private final AvgValue avgX = new AvgValue(1000);
    private final AvgValue avgY = new AvgValue(1000);
    private final List<APoint> entries = new ArrayList<APoint>();
    private final Color color;

    public Cluster2D(Color color) {
        this.color = color;
    }

    public void addEntry(APoint entry) {
        avgX.addValue(entry.getX());
        avgY.addValue(entry.getY());
        entries.add(entry);
    }

    public double distance(APoint pnt) {
        return pnt.aDistance(new LXXPoint(avgX.getCurrentValue(), avgY.getCurrentValue()));
    }

    public APoint getCenterPoint() {
        return new LXXPoint(avgX.getCurrentValue(), avgY.getCurrentValue());
    }

    public List<APoint> getEntries() {
        return entries;
    }

    public Color getColor() {
        return color;
    }
}
