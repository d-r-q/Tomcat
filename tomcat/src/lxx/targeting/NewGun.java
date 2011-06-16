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
import lxx.segmentation_tree.SegmentationTree;
import lxx.segmentation_tree.SegmentationTreeEntry;
import lxx.strategies.Gun;
import lxx.strategies.GunDecision;
import lxx.targeting.bullets.LXXBullet;
import lxx.utils.*;
import robocode.Event;
import robocode.Rules;
import robocode.util.Utils;

import java.awt.geom.Rectangle2D;
import java.util.*;

import static java.lang.Math.*;

public class NewGun implements Gun, RobotListener {

    private static final int AIMING_TIME = 2;

    private static final Attribute[] attrs = {
            AttributesManager.enemyAcceleration,
            AttributesManager.enemyVelocity,
            AttributesManager.enemyDistanceToForwardWall,
            AttributesManager.enemyBearingToForwardWall,
    };

    private static final Map<Attribute, Integer> ranges = new HashMap<Attribute, Integer>();

    static {
        ranges.put(AttributesManager.enemyAcceleration, 0);
        ranges.put(AttributesManager.enemyVelocity, 0);
        ranges.put(AttributesManager.enemyDistanceToForwardWall, 25);
        ranges.put(AttributesManager.enemyBearingToForwardWall, 20);
    }

    private static final SegmentationTree tree = new SegmentationTree(attrs, 2, 0.0001);

    private final Tomcat robot;
    private final TurnSnapshotsLog log;
    private final TargetManager targetManager;

    private AimingPredictionData predictionData;
    private APoint robotPosAtFireTime;

    public NewGun(Tomcat robot, TurnSnapshotsLog log, TargetManager targetManager) {
        this.robot = robot;
        this.log = log;
        this.targetManager = targetManager;
    }

    public GunDecision getGunDecision(Target t, double firePower) {
        final double angleToTarget = robot.angleTo(t);
        if (robot.getTurnsToGunCool() > AIMING_TIME || t.getEnergy() == 0) {
            predictionData = null;
            return new GunDecision(getGunTurnAngle(angleToTarget), new NGPresictionData());
        }

        if (predictionData == null) {

            final double bulletSpeed = Rules.getBulletSpeed(firePower);
            robotPosAtFireTime = robot.project(robot.getAbsoluteHeadingRadians(), robot.getVelocityModule() * AIMING_TIME);

            // todo(zhidkov): if no data
            if (true) {
                return new GunDecision(getGunTurnAngle(angleToTarget), new NGPresictionData());
            }
        }


        return new GunDecision(getGunTurnAngle(0), new NGPresictionData());
    }

    private double getGunTurnAngle(double angleToPredictedPos) {
        return Utils.normalRelativeAngle(angleToPredictedPos - robot.getGunHeadingRadians());
    }

    private APoint getPredictedPos(Target t, List<TurnSnapshot> starts, double bulletSpeed) {
        final List<APoint> futurePoses = new ArrayList<APoint>();
        for (TurnSnapshot start : starts) {
            futurePoses.add(getFuturePos(t, start, bulletSpeed));
        }

        if (futurePoses.size() == 0) {
            return null;
        } else if (futurePoses.size() == 1) {
            return futurePoses.get(0);
        } else if (futurePoses.size() < 4) {
            return futurePoses.get((int) (futurePoses.size() * random()));
        }

        final List<APoint> clustersCenters = findFarestPoints(futurePoses);
        clustersCenters.add(findFarestPoint(clustersCenters, futurePoses));

        return null;
    }

    private APoint findFarestPoint(List<APoint> toPoints, List<APoint> candidates) {
        APoint farestPoint = null;
        double maxMinDist = Integer.MIN_VALUE;
        for (APoint candidate : candidates) {
            double minDist = Integer.MAX_VALUE;
            for (APoint pnt : toPoints) {
                minDist = min(minDist, candidate.aDistance(pnt));
            }

            if (minDist > maxMinDist) {
                maxMinDist = minDist;
                farestPoint = candidate;
            }
        }

        return farestPoint;
    }

    private List<APoint> findFarestPoints(List<APoint> poses) {
        APoint pnt1;
        APoint pnt2;
        APoint farestPnt1 = null;
        APoint farestPnt2 = null;
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

        return Arrays.asList(farestPnt1, farestPnt2);
    }

    private APoint getFuturePos(Target t, TurnSnapshot start, double bulletSpeed) {
        APoint futurePos = new LXXPoint(t.getPosition());

        int timeDelta = -AIMING_TIME;
        while (!isBulletHitEnemy(futurePos, timeDelta, bulletSpeed)) {
            final TurnSnapshot nextTS = start.getNext();
            if (nextTS == null) {
                return null;
            }
            final DeltaVector dv = getDeltaVector(start, nextTS);
            futurePos = futurePos.project(dv);
            if (!robot.getState().getBattleField().contains(futurePos)) {
                return null;
            }
            start = nextTS;
        }

        return futurePos;
    }

    private DeltaVector getDeltaVector(TurnSnapshot ts1, TurnSnapshot ts2) {
        final double targetHeading = ts1.getAttrValue(AttributesManager.enemyAbsoluteHeading);
        final APoint enemyPos1 = LXXUtils.getEnemyPos(ts1);
        final APoint enemyPos2 = LXXUtils.getEnemyPos(ts2);
        final double alpha = enemyPos1.angleTo(enemyPos2);
        return new DeltaVector(Utils.normalRelativeAngle(alpha - targetHeading), enemyPos1.aDistance(enemyPos2));
    }

    private List<TurnSnapshot> getSimilarSnapshots(TurnSnapshot ts) {
        final List<TurnSnapshot> similarSnapshots = new ArrayList<TurnSnapshot>();
        final List<SegmentationTreeEntry> similarEntries = tree.getSimilarEntries(getLimits(ts));
        if (similarEntries.size() == 0) {
            similarSnapshots.add(tree.getClosestEntry(ts).predicate);
        }

        for (SegmentationTreeEntry e : similarEntries) {
            similarSnapshots.add(e.predicate);
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

        for (Attribute a : attrs) {
            Interval interval = new Interval(
                    (int) round(LXXUtils.limit(a, ts.getAttrValue(a) - ranges.get(a))),
                    (int) round(LXXUtils.limit(a, ts.getAttrValue(a) + ranges.get(a)))
            );
            limits.put(a, interval);
        }

        return limits;
    }

    private class NGPresictionData implements AimingPredictionData {

        public void paint(LXXGraphics g, LXXBullet bullet) {

        }
    }

}
