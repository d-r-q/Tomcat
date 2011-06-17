/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.targeting;

import lxx.RobotListener;
import lxx.Tomcat;
import lxx.events.TickEvent;
import lxx.model.TurnSnapshot;
import lxx.model.attributes.Attribute;
import lxx.office.AttributesManager;
import lxx.office.TargetManager;
import lxx.office.TurnSnapshotsLog;
import lxx.segmentation_tree.EntryMatch;
import lxx.segmentation_tree.SegmentationTree;
import lxx.segmentation_tree.SegmentationTreeEntry;
import lxx.strategies.Gun;
import lxx.strategies.GunDecision;
import lxx.targeting.bullets.LXXBullet;
import lxx.utils.*;
import robocode.Event;
import robocode.Rules;
import robocode.util.Utils;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;

import static java.lang.Math.*;

public class NewGun implements Gun, RobotListener {

    private static final List<NGPoint> NO_PREDICTED_POSES = Collections.unmodifiableList(new ArrayList<NGPoint>());

    private static final int AIMING_TIME = 2;

    private static final Attribute[] attributes = {
            AttributesManager.enemyAcceleration,
            AttributesManager.enemyVelocity,
            AttributesManager.enemyDistanceToForwardWall,
            AttributesManager.enemyBearingToForwardWall,
    };

    private static final Map<Attribute, Integer> ranges = new HashMap<Attribute, Integer>();

    static {
        ranges.put(AttributesManager.enemyAcceleration, 0);
        ranges.put(AttributesManager.enemyVelocity, 0);
        ranges.put(AttributesManager.enemyDistanceToForwardWall, 9);
        ranges.put(AttributesManager.enemyBearingToForwardWall, 11);
    }

    private static final SegmentationTree tree = new SegmentationTree(attributes, 2, 0.0001);

    private final Tomcat robot;
    private final TurnSnapshotsLog log;
    private final TargetManager targetManager;

    private APoint robotPosAtFireTime;
    private APoint futurePos;
    private List<NGPoint> futurePoses;

    public NewGun(Tomcat robot, TurnSnapshotsLog log, TargetManager targetManager) {
        this.robot = robot;
        this.log = log;
        this.targetManager = targetManager;
        robot.addListener(this);
    }

    public GunDecision getGunDecision(Target t, double firePower) {
        final double angleToTarget = robot.angleTo(t);
        APoint initialPos = t.getPosition();
        if (robot.getTurnsToGunCool() > AIMING_TIME || t.getEnergy() == 0) {
            futurePos = null;
            futurePoses = null;
            return new GunDecision(getGunTurnAngle(angleToTarget), new NGPresictionData(NO_PREDICTED_POSES, initialPos));
        }

        if (futurePos == null) {

            final double bulletSpeed = Rules.getBulletSpeed(firePower);
            robotPosAtFireTime = robot.project(robot.getAbsoluteHeadingRadians(), robot.getVelocityModule() * AIMING_TIME);

            final List<EntryMatch> similarSnapshots = getSimilarSnapshots(log.getLastSnapshot(t));
            filterOutCloseByTime(similarSnapshots);
            futurePos = getPredictedPos(t, similarSnapshots, bulletSpeed);
            if (futurePos == null) {
                return new GunDecision(getGunTurnAngle(angleToTarget), new NGPresictionData(NO_PREDICTED_POSES, initialPos));
            }
        }

        final double angleToPredictedPos = Utils.normalAbsoluteAngle(robotPosAtFireTime.angleTo(futurePos));

        return new GunDecision(getGunTurnAngle(angleToPredictedPos), new NGPresictionData(futurePoses, initialPos));
    }

    private void filterOutCloseByTime(List<EntryMatch> similarSnapshots) {
        Collections.sort(similarSnapshots, new Comparator<EntryMatch>() {
            public int compare(EntryMatch o1, EntryMatch o2) {
                if (o1.predicate.getRound() == o2.predicate.getRound()) {
                    return (int) (o1.predicate.getTime() - o2.predicate.getTime());
                }
                return (o1.predicate.getRound() - o2.predicate.getRound());
            }
        });

        for (int i = 0; i < similarSnapshots.size() - 1; i++) {
            EntryMatch em1 = similarSnapshots.get(i);
            EntryMatch em2 = similarSnapshots.get(i + 1);
            if (em1.predicate.getRound() == em2.predicate.getRound() &&
                    em1.predicate.getTime() + 5 > em2.predicate.getTime()) {
                if (em1.match < em2.match) {
                    similarSnapshots.remove(i + 1);
                } else {
                    similarSnapshots.remove(i);
                }
                i--;
            }
        }
    }

    private double getGunTurnAngle(double angleToPredictedPos) {
        return Utils.normalRelativeAngle(angleToPredictedPos - robot.getGunHeadingRadians());
    }

    private APoint getPredictedPos(Target t, List<EntryMatch> starts, double bulletSpeed) {
        futurePoses = new ArrayList<NGPoint>();
        for (EntryMatch start : starts) {
            final NGPoint futurePos = getFuturePos(t, start.predicate, bulletSpeed);
            if (futurePos != null) {
                futurePoses.add(futurePos);
            }
        }

        if (futurePoses.size() == 0) {
            return null;
        } else if (futurePoses.size() == 1) {
            return futurePoses.get(0);
        } else if (futurePoses.size() < 4) {
            return futurePoses.get((int) (futurePoses.size() * random()));
        }

        final Set<NGPoint> clustersCenters = findFarestPoints(futurePoses);
        clustersCenters.add(findFarestPoint(clustersCenters, futurePoses));

        final List<Cluster> clusters = new ArrayList<Cluster>();
        final Iterator<Color> colorsIterator = Arrays.asList(new Color(255, 0, 0, 155), new Color(0, 255, 0, 155),
                new Color(0, 0, 255, 155)).iterator();
        for (NGPoint clusterCenter : clustersCenters) {
            final Cluster c1 = new Cluster(colorsIterator.next());
            c1.addEntry(clusterCenter);
            clusters.add(c1);
        }

        for (NGPoint futurePoint : futurePoses) {
            double minDist = Integer.MAX_VALUE;
            Cluster minDistCluster = null;
            for (Cluster c : clusters) {
                final double dist = c.distance(futurePoint);
                if (dist < minDist) {
                    minDist = dist;
                    minDistCluster = c;
                }
            }

            minDistCluster.addEntry(futurePoint);
        }

        int biggestClusterSize = Integer.MIN_VALUE;
        Cluster biggestCluster = null;
        for (Cluster c : clusters) {
            if (c.entries.size() > biggestClusterSize) {
                biggestClusterSize = c.entries.size();
                biggestCluster = c;
            }
        }

        return biggestCluster.getCenterPoint();
    }

    private NGPoint findFarestPoint(Set<NGPoint> toPoints, List<NGPoint> candidates) {
        NGPoint farestPoint = null;
        double maxMinDist = Integer.MIN_VALUE;
        for (NGPoint candidate : candidates) {
            double minDist = Integer.MAX_VALUE;
            for (NGPoint pnt : toPoints) {
                minDist = min(minDist, candidate.aDistance(pnt));
            }

            if (minDist > maxMinDist) {
                maxMinDist = minDist;
                farestPoint = candidate;
            }
        }

        return farestPoint;
    }

    private Set<NGPoint> findFarestPoints(List<NGPoint> poses) {
        NGPoint pnt1;
        NGPoint pnt2;
        NGPoint farestPnt1 = null;
        NGPoint farestPnt2 = null;
        double maxDist = Integer.MIN_VALUE;

        for (int i = 0; i < poses.size(); i++) {
            pnt1 = poses.get(i);
            for (int j = i + 1; j < poses.size(); j++) {
                pnt2 = poses.get(j);

                final double dist = pnt1.aDistance(pnt2);
                if (dist > maxDist) {
                    maxDist = dist;
                    farestPnt1 = pnt1;
                    farestPnt2 = pnt2;
                }
            }
        }

        return new HashSet<NGPoint>(Arrays.asList(farestPnt1, farestPnt2));
    }

    private NGPoint getFuturePos(Target t, TurnSnapshot start, double bulletSpeed) {
        List<PredictionStatus> pses = new ArrayList<PredictionStatus>();
        NGPoint futurePos = new NGPoint(t.getPosition());

        int timeDelta = -AIMING_TIME;
        TurnSnapshot currentSnapshot = start.getNext();
        while (!isBulletHitEnemy(futurePos, timeDelta, bulletSpeed)) {
            PredictionStatus ps = new PredictionStatus();
            ps.timeDelta = timeDelta;
            pses.add(ps);
            ps.currentSnapshot = currentSnapshot;
            if (currentSnapshot == null) {
                return null;
            }
            final DeltaVector dv = LXXUtils.getEnemyDeltaVector(start, currentSnapshot);
            ps.dv = dv;
            final double alpha = t.getAbsoluteHeadingRadians() + dv.getAlphaRadians();
            futurePos = new NGPoint(t.getPosition().project(alpha, dv.getLength()));
            ps.futurePos = futurePos;
            if (!robot.getState().getBattleField().contains(futurePos)) {
                return null;
            }
            currentSnapshot = currentSnapshot.getNext();
            timeDelta++;
        }

        return futurePos;
    }

    private List<EntryMatch> getSimilarSnapshots(TurnSnapshot ts) {
        final List<EntryMatch> similarSnapshots = new ArrayList<EntryMatch>();
        final List<EntryMatch> similarEntries = tree.getSortedSimilarEntries(ts, getLimits(ts));
        if (similarEntries.size() == 0) {
            final EntryMatch closestEntry = tree.getClosestEntry(ts);
            if (closestEntry != null) {
                similarSnapshots.add(closestEntry);
            }
        }

        for (EntryMatch e : similarEntries) {
            similarSnapshots.add(e);
        }

        return similarSnapshots;
    }

    public void onEvent(Event event) {
        if (event instanceof TickEvent) {
            final Target duelOpponent = targetManager.getDuelOpponent();
            if (duelOpponent != null) {
                tree.addEntry(new SegmentationTreeEntry(log.getLastSnapshot(duelOpponent)));
            }
        }
    }

    private boolean isBulletHitEnemy(APoint predictedPos, long timeDelta, double bulletSpeed) {
        final double angleToPredictedPos = robotPosAtFireTime.angleTo(predictedPos);
        final int bulletTravelledDistance = (int) (timeDelta * bulletSpeed);
        final LXXPoint bulletPos = (LXXPoint) robotPosAtFireTime.project(angleToPredictedPos, bulletTravelledDistance);
        final Rectangle2D enemyRectAtPredictedPos = LXXUtils.getBoundingRectangleAt(predictedPos);
        return enemyRectAtPredictedPos.contains(bulletPos) || bulletTravelledDistance > robotPosAtFireTime.aDistance(predictedPos) + LXXConstants.ROBOT_SIDE_HALF_SIZE;
    }

    private Map<Attribute, Interval> getLimits(TurnSnapshot ts) {
        Map<Attribute, Interval> limits = new HashMap<Attribute, Interval>();

        for (Attribute a : attributes) {
            Interval interval = new Interval(
                    (int) round(LXXUtils.limit(a, ts.getAttrValue(a) - ranges.get(a))),
                    (int) round(LXXUtils.limit(a, ts.getAttrValue(a) + ranges.get(a)))
            );
            limits.put(a, interval);
        }

        return limits;
    }

    private class NGPresictionData implements AimingPredictionData {

        private final List<NGPoint> predictedPoses;
        private final APoint initialPos;

        public NGPresictionData(List<NGPoint> predictedPoses, APoint initialPos) {
            this.predictedPoses = predictedPoses;
            this.initialPos = initialPos;
        }

        public void paint(LXXGraphics g, LXXBullet bullet) {
            for (NGPoint predictedPos : predictedPoses) {
                Color c = predictedPos.color;
                g.setColor(predictedPos.color);
                g.drawRect(predictedPos, LXXConstants.ROBOT_SIDE_HALF_SIZE, LXXConstants.ROBOT_SIDE_HALF_SIZE);
            }
        }
    }

    private class Cluster {

        private final AvgValue avgX = new AvgValue(1000);
        private final AvgValue avgY = new AvgValue(1000);
        private final List<APoint> entries = new ArrayList<APoint>();
        private final Color color;

        public Cluster(Color color) {
            this.color = color;
        }

        public void addEntry(NGPoint entry) {
            avgX.addValue(entry.getX());
            avgY.addValue(entry.getY());
            entries.add(entry);
            entry.color = this.color;
        }

        public double distance(APoint pnt) {
            return pnt.aDistance(new LXXPoint(avgX.getCurrentValue(), avgY.getCurrentValue()));
        }

        public APoint getCenterPoint() {
            return new LXXPoint(avgX.getCurrentValue(), avgY.getCurrentValue());
        }

    }

    public final class NGPoint extends LXXPoint implements APoint {

        private Color color = new Color(255, 255, 255, 55);

        public NGPoint(APoint position) {
            super(position);
        }
    }

    private class PredictionStatus {

        public TurnSnapshot currentSnapshot;
        public TurnSnapshot nextTS;
        public DeltaVector dv;
        public double targetHeading;
        public APoint prevPos;
        public double alpha;
        public NGPoint futurePos;
        public double newTargetHeading;
        public int timeDelta;
    }

}
