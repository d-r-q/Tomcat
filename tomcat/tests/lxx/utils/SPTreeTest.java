/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.utils;

import junit.framework.TestCase;
import lxx.ts_log.TurnSnapshot;
import lxx.ts_log.attributes.Attribute;
import lxx.ts_log.attributes.AttributesManager;
import lxx.utils.sp_tree.SPTree;
import lxx.utils.sp_tree.SPTreeEntry;

import java.util.*;

import static java.lang.Math.*;

/**
 * User: jdev
 * Date: 24.09.11
 */
public class SPTreeTest extends TestCase {

    static {
        QuickMath.init();
    }

    public void testSPTree() {

        double min = Integer.MAX_VALUE;
        double max = Integer.MIN_VALUE;
        AvgValue avg = new AvgValue(100 * 100 * 1000);

        for (int i = 0; i < 100; i++) {
            System.out.println("i = " + i);
            final List<TurnSnapshot> data = generateRandomData(10000);

            for (int j = 0; j < 100; j++) {
                final Map<Attribute, Interval> hypercube = generateRandomHypercube(16);
                final SPTree tree = new SPTree(hypercube.keySet().toArray(new Attribute[hypercube.keySet().size()]));
                for (TurnSnapshot ts : data) {
                    tree.add(new SPTreeEntry(ts));
                }
                final TurnSnapshot loc = generateRandomTS(i * j);
                final Collection<SPTreeEntry> treeRes = tree.rangeSearch(loc, hypercube);
                final Set<SPTreeEntry> linRes = rangeSearch(data, loc, hypercube);

                Set<Integer> tt = new TreeSet<Integer>();
                for (SPTreeEntry e : treeRes) {
                    tt.add(e.location.roundTime);
                    double epsilon = 0;
                    for (Interval interval : hypercube.values()) {
                        epsilon += interval.getLength() + 1;
                    }
                    final double dst = getDist(loc, e.location, hypercube.keySet());
                    final double m = abs(e.distance - dst) / epsilon;
                    /*if (m > 10) {
                        System.out.println("AAAAAAAAAAAAAA");
                    }*/
                    min = min(min, m);
                    max = max(max, m);
                    avg.addValue(m);
                    /*if (m > epsilon) {
                        System.out.println(e.distance + " : " + getDist(loc, e.location, hypercube.keySet()));
                    }*/
                }
                for (SPTreeEntry e : linRes) {
                    if (!tt.contains(e.location.roundTime)) {
                        System.out.println(treeRes);
                        System.out.println(linRes);
                        System.out.println(tt);
                        tree.rangeSearch(loc, hypercube);
                        break;
                    }
                }
            }
        }

        System.out.println(min + " : " + max + " : " + avg);

    }

    public static double getDist(TurnSnapshot ts1, TurnSnapshot ts2, Collection<Attribute> attributes) {
        double totalDist = 0;

        for (Attribute a : attributes) {
            double diff = ts1.getAttrValue(a) - ts2.getAttrValue(a);
            totalDist += diff * diff * 100D / a.getRange().getLength();
        }

        return totalDist;
    }

    private Set<SPTreeEntry> rangeSearch(List<TurnSnapshot> data, TurnSnapshot loc, Map<Attribute, Interval> hypercube) {
        final Set<SPTreeEntry> res = new HashSet<SPTreeEntry>();

        for (TurnSnapshot ts : data) {
            boolean matches = true;

            for (Attribute a : hypercube.keySet()) {
                Interval i = hypercube.get(a);
                if (!i.contains((int) round(ts.getAttrValue(a)))) {
                    matches = false;
                    break;
                }
            }

            if (matches) {
                res.add(new SPTreeEntry(ts));
            }
        }

        return res;
    }

    private Map<Attribute, Interval> generateRandomHypercube(int maxK) {
        final Map<Attribute, Interval> res = new HashMap<Attribute, Interval>();
        final List<Attribute> attrs = new ArrayList<Attribute>(Arrays.asList(AttributesManager.attributes));

        int k = (int) max(1, maxK * random());
        for (int i = 0; i < k; i++) {
            final Attribute a = attrs.remove((int) (attrs.size() * random()));
            final int halfWidth = (int) (a.getRange().getLength() * 0.4 * random());
            final int center = (int) (a.getRange().a + round(a.getRange().getLength() * random()));
            res.put(a, new Interval((int) LXXUtils.limit(a.getMinValue(), center - halfWidth, a.getMaxValue()),
                    (int) LXXUtils.limit(a.getMinValue(), center + halfWidth, a.getMaxValue())));
        }

        return res;
    }

    private List<TurnSnapshot> generateRandomData(int count) {
        final List<TurnSnapshot> res = new ArrayList<TurnSnapshot>();
        for (int i = 0; i < count; i++) {
            res.add(generateRandomTS(i));
        }

        return res;
    }

    private TurnSnapshot generateRandomTS(int i) {
        double[] attrValues = new double[AttributesManager.attributesCount()];
        for (Attribute a : AttributesManager.attributes) {
            attrValues[a.getId()] = a.getRange().a + a.getRange().getLength() * random();
        }
        return new TurnSnapshot(attrValues, i, 0, "t");
    }

}
