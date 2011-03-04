package kc.serpent.gun;

import kc.serpent.utils.*;
import robocode.util.Utils;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.TreeSet;

public class PatternMatcher implements GunSystem {
    static final int MAX_LOG_SIZE = 40000;
    static final int MAX_CLUSTER_SIZE = 20;
    static final int LAT_VELOCITIES = 9;
    static final int ADV_VELOCITIES = 9;
    static final int ROUND_BREAK_VALUE = 2 * LAT_VELOCITIES * ADV_VELOCITIES;

    static Rectangle2D battleField;
    static double battleFieldHeight;
    static double battleFieldWidth;

    TreeSet cluster = new TreeSet();
    public int ticksUntilShot;

    Node head;
    Node tail;
    int totalNodes;

    public void init(GunBase g) {
        battleFieldHeight = g.robot.getBattleFieldHeight();
        battleFieldWidth = g.robot.getBattleFieldWidth();
        battleField = KUtils.makeField(battleFieldWidth, battleFieldHeight, 17.999);
    }

    public void reset() {
        scan(ROUND_BREAK_VALUE, 0, 1, 1, 0, 0, 1);
    }

    public void scan(double enemyVelocity, double enemyDeltaH, int velocitySignChange, int deltaHSignChange, double latVelocity, double advancingVelocity, int latVelSignChange) {
        Node current = new Node(enemyVelocity, enemyDeltaH, velocitySignChange, deltaHSignChange, latVelocity, advancingVelocity, latVelSignChange);

        int worstEntry = 1000;
        cluster = new TreeSet();

        Node n = head;
        while (n != null) {
            Node p = n.previous;

            if (n.value == current.value) {
                if (p != null && n.value != ROUND_BREAK_VALUE) {
                    n.matchLength = p.matchLength;
                } else {
                    n.matchLength = 0;
                }
                n.matchLength += n.value != ROUND_BREAK_VALUE ? 1 : 5;
            } else {
                n.matchLength = 0;
            }

            if (n.matchLength != 0 || cluster.size() == 0) {
                if (cluster.size() < MAX_CLUSTER_SIZE) {
                    cluster.add(n);
                    if (n.matchLength < worstEntry) {
                        worstEntry = n.matchLength;
                    }
                } else {
                    if (n.matchLength > worstEntry) {
                        worstEntry = n.matchLength;
                        cluster.remove(cluster.first());
                        cluster.add(n);
                    }
                }

            }
            n = p;
        }

        if (tail != null) {
            head.next = current;
            current.previous = head;
        } else {
            tail = current;
        }
        head = current;
        if (totalNodes++ > MAX_LOG_SIZE) {
            tail = tail.next;
            tail.previous = null;
        }
    }

    public double getFiringAngle(GunWave w) {
        Point2D.Double myLocation = w.source;

        int clusterSize = cluster.size();
        double[] GFList = new double[clusterSize];
        double[] weightList = new double[clusterSize];
        int index = 0;

        Iterator it = cluster.iterator();
        while (it.hasNext()) {
            Node n = (Node) (it.next());
            double weight = n.matchLength + 1;

            Point2D.Double predictLocation = w.enemyLocation;
            double predictHeading = w.enemyHeading;
            double predictVelocity = w.enemyVelocity;
            double radius = -ticksUntilShot * w.speed;

            int velocitySign = w.velocitySign;
            int deltaHSign = w.deltaHSign;

            while (predictLocation.distance(myLocation) > (radius += w.speed)) {
                if (n.next == null) {
                    weight *= 0.000001;
                    break;
                } else {
                    n = n.next;
                }

                if (n.value == ROUND_BREAK_VALUE) {
                    weight *= 0.00001;
                    break;
                }

                deltaHSign *= n.deltaHSignChange;
                velocitySign *= n.velocitySignChange;
                predictHeading += n.deltaH * deltaHSign;
                predictVelocity = n.velocity * velocitySign;
                predictLocation = KUtils.projectMotion(predictLocation, predictHeading, predictVelocity);

                if (!battleField.contains(predictLocation)) {
                    predictLocation.x = KUtils.minMax(predictLocation.x, 18.0, battleFieldWidth - 18.0);
                    predictLocation.y = KUtils.minMax(predictLocation.y, 18.0, battleFieldHeight - 18.0);
                    weight *= 0.0001;

                    break;
                }
            }

            weightList[index] = weight;
            GFList[index] = Utils.normalRelativeAngle(KUtils.absoluteBearing(myLocation, predictLocation) - w.absoluteBearing) / w.maxEscapeAngle;
            index++;
        }

        double bestGF = GFList[0];
        double bestValue = Double.NEGATIVE_INFINITY;
        double windowSize = KUtils.windowFactor(15.0, w.distance, w.maxEscapeAngle);

        double[][] comparisonValue = new double[index][index];
        for (int i = 0; i < index; i++) {
            for (int ii = i; ii < index; ii++) {
                double diff = Math.abs(GFList[i] - GFList[ii]);
                comparisonValue[i][ii] = comparisonValue[ii][i] = KUtils.thirtysecond(2.0 - diff) + (diff < windowSize ? 2000000000.0 : 0.0);
            }
        }

        for (int i = 0; i < index; i++) {
            double value = 0;

            for (int ii = 0; ii < index; ii++) {
                value += weightList[ii] * comparisonValue[i][ii];
            }

            if (value > bestValue) {
                bestValue = value;
                bestGF = GFList[i];
            }
        }
        return w.absoluteBearing + (bestGF * w.maxEscapeAngle);
    }

    public String getName() {
        return "pattern matching gun";
    }

    public void wavePassed(double GF, GunWave w) {
    }

    public void printStats() {
    }

    public class Node implements Comparable {
        double velocity;
        double deltaH;
        int velocitySignChange;
        int deltaHSignChange;

        int value;
        int matchLength;

        Node previous;
        Node next;

        public Node(double velocity, double deltaH, int velocitySignChange, int deltaHSignChange, double latVelocity, double advVelocity, int latVelSignChange) {
            this.velocity = Math.abs(velocity);
            this.deltaH = Math.abs(deltaH);
            this.velocitySignChange = velocitySignChange;
            this.deltaHSignChange = deltaHSignChange;
            this.previous = previous;

            int latVelValue = (int) Math.round((LAT_VELOCITIES - 1) * Math.abs(latVelocity) / 8);
            int advVelValue = (int) Math.round((ADV_VELOCITIES - 1) * (advVelocity + 8) / 16);
            int LVSCValue = latVelSignChange == 1 ? 0 : LAT_VELOCITIES * ADV_VELOCITIES;
            value = latVelValue + (LAT_VELOCITIES * advVelValue) + LVSCValue;

            if (velocity > ROUND_BREAK_VALUE - 0.001) {
                value = ROUND_BREAK_VALUE;
            }
        }

        public int compareTo(Object o) {
            Node n = (Node) (o);
            return (int) Math.signum(matchLength - n.matchLength);
        }
    }
}