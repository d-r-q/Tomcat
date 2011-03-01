/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.targeting.pattern_tree;

import lxx.Primarch;
import lxx.RobotListener;
import lxx.events.TickEvent;
import lxx.model.BattleSnapshot;
import lxx.model.attributes.Attribute;
import lxx.office.*;
import lxx.simulator.RobocodeDuelSimulator;
import lxx.strategies.Gun;
import lxx.strategies.GunDecision;
import lxx.strategies.MovementDecision;
import lxx.targeting.GunAimingPredictionDataImpl;
import lxx.targeting.Target;
import lxx.utils.*;
import robocode.Event;
import robocode.Rules;
import robocode.util.Utils;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.lang.Math.toDegrees;

/**
 * User: jdev
 * Date: 21.02.11
 */
public class PatternTreeGun implements RobotListener, Gun {

    private static PatternTreeNode root = new PatternTreeNode(null, null, 0);

    private static final List<TurnPrediction> NO_PREDICTED_POSES = Collections.unmodifiableList(new ArrayList<TurnPrediction>());
    private static final int AIMING_TIME = 2;
    private static final int PATTERN_LENGTH = 4;
    private static final int LOOKUP_TIME = 15;

    private final Primarch robot;
    private final BattleSnapshotManager battleSnapshotManager;
    private final TargetManager targetManager;
    private final Timer timer;

    private final Attribute[] attributes;
    private final int[] indexes;
    private final double[] weights;

    private List<TurnPrediction> predictedPoses = null;
    private RobocodeDuelSimulator duelSimulator;
    private APoint robotPosAtFireTime;

    public PatternTreeGun(Office office) {
        this.battleSnapshotManager = office.getBattleSnapshotManager();
        this.targetManager = office.getTargetManager();
        this.timer = office.getBattleTimeManager();

        robot = office.getPrimarch();

        attributes = new Attribute[]{
                AttributesManager.enemyBearingToMe,
                AttributesManager.enemyDistanceToCenter,
                AttributesManager.enemyStopTime,
                AttributesManager.enemyBearingToForwardWall,
                AttributesManager.enemyVelocityModule,
        };

        indexes = new int[attributes.length];
        weights = new double[AttributesManager.attributesCount()];
        double weight = 1;
        int idx = 0;
        for (Attribute a : attributes) {
            indexes[idx++] = a.getId();
            weights[a.getId()] = weight / a.getActualRange();
            weight = weight * 4 + 1;
        }
    }

    public void onEvent(Event event) {
        if (event instanceof TickEvent && event.getTime() > PATTERN_LENGTH + LOOKUP_TIME && targetManager.hasDuelOpponent()) {
            if (targetManager.getDuelOpponent().getEnergy() == 0) {
                return;
            }
            long time = event.getTime() - PATTERN_LENGTH;
            PatternTreeNode node = root;
            while (time <= event.getTime()) {
                final BattleSnapshot predicate = battleSnapshotManager.getSnapshotByRoundTime(targetManager.getDuelOpponentName(), time - 1);
                final BattleSnapshot currentState = battleSnapshotManager.getSnapshotByRoundTime(targetManager.getDuelOpponentName(), time);
                node = node.addChild(predicate, currentState);
                time++;
            }

            robot.setDebugProperty("Nodes count", String.valueOf(PatternTreeNode.nodesCount));
        }
    }

    public GunDecision getGunDecision(Target t, double firePower) {
        final double angleToTarget = robot.angleTo(t);
        if (robot.getTurnsToGunCool() > AIMING_TIME || t.getEnergy() == 0) {
            predictedPoses = null;
            return new GunDecision(getGunTurnAngle(angleToTarget), new GunAimingPredictionDataImpl(NO_PREDICTED_POSES, robotPosAtFireTime));
        }

        if (predictedPoses == null || predictedPoses.size() == 0) {
            predictedPoses = new ArrayList<TurnPrediction>();
            duelSimulator = new RobocodeDuelSimulator(t, robot, t.getTime(), timer.getBattleTime(), attributes);
            robotPosAtFireTime = robot.project(robot.getAbsoluteHeadingRadians(), robot.getVelocityModule() * AIMING_TIME);

            final double bulletSpeed = Rules.getBulletSpeed(firePower);
            buildPattern(bulletSpeed);

            if (timer.getBattleTime() > 70 && (predictedPoses == null || predictedPoses.size() == 0 || predictedPoses.get(0).enemyPos.aDistance(predictedPoses.get(predictedPoses.size() - 1).enemyPos) < 10)) {

                duelSimulator = new RobocodeDuelSimulator(t, robot, t.getTime(), timer.getBattleTime(), attributes);
                buildPattern(bulletSpeed);
                return new GunDecision(getGunTurnAngle(angleToTarget), new GunAimingPredictionDataImpl(NO_PREDICTED_POSES, robotPosAtFireTime));
            }
        }

        final double angleToPredictedPos = getAngleToPredictedPos(predictedPoses.get(predictedPoses.size() - 1).enemyPos, this.robotPosAtFireTime);

        return new GunDecision(getGunTurnAngle(angleToPredictedPos), new GunAimingPredictionDataImpl(predictedPoses, robotPosAtFireTime));
    }

    private double getGunTurnAngle(double angleToPredictedPos) {
        return Utils.normalRelativeAngle(angleToPredictedPos - robot.getGunHeadingRadians());
    }

    public void buildPattern(double bulletSpeed) {
        final List<EnemyMovementDecision> enemyMovementDecisions = getLastEnemyMovementDecisions();
        long timeDelta = 0;
        while (!isBulletHitEnemy(duelSimulator.getEnemyProxy(), timeDelta, bulletSpeed)) {
            final PatternTreeNode node = getNode(enemyMovementDecisions);
            final PatternTreeNode.PatternTreeNodeSelectionData n = node.getChildBySnapshot(duelSimulator.getSimulatorSnapshot(), indexes, weights);
            final EnemyMovementDecision emd = n.getDecision();
            if (emd.acceleration < -Rules.DECELERATION || emd.acceleration > Rules.ACCELERATION) {
                throw new RuntimeException("Something wrong!");
            }
            LXXRobotState enemyState = duelSimulator.getEnemyProxy().getState();
            if ((emd.acceleration == 0 && enemyState.getVelocity() > 0 && enemyState.getVelocity() < Rules.MAX_VELOCITY) ||
                    (emd.acceleration > 0 && duelSimulator.getSimulatorSnapshot().getAttrValue(AttributesManager.enemyBearingToForwardWall) != 0) ||
                    (emd.turnRateRadians == 0 && enemyState.getVelocity() == 0 && emd.acceleration == 0)) {
                node.getChildBySnapshot(duelSimulator.getSimulatorSnapshot(), indexes, weights);
            }
            final MovementDecision movementDecision = new MovementDecision(emd.acceleration, emd.turnRateRadians, getMovementDirection(enemyState));
            duelSimulator.setEnemyMovementDecision(movementDecision);
            duelSimulator.doTurn();
            enemyMovementDecisions.add(PatternTreeNode.getEnemyMovementDecision(duelSimulator.getSimulatorSnapshot()));
            if (timeDelta >= AIMING_TIME) {
                enemyState = duelSimulator.getEnemyProxy().getState();
                final LXXPoint predictedPos = new LXXPoint(enemyState);
                predictedPoses.add(new TurnPrediction(predictedPos, enemyState.getVelocity(), toDegrees(enemyState.getAbsoluteHeadingRadians()), node, emd,
                        PatternTreeNode.getEnemyMovementDecision(duelSimulator.getSimulatorSnapshot())));
            }
            timeDelta++;
        }
    }

    private MovementDecision.MovementDirection getMovementDirection(LXXRobotState duelOpponent) {
        return LXXUtils.anglesDiff(duelOpponent.getAbsoluteHeadingRadians(), duelOpponent.getHeadingRadians()) < LXXConstants.RADIANS_90
                ? MovementDecision.MovementDirection.FORWARD
                : MovementDecision.MovementDirection.BACKWARD;
    }

    public PatternTreeNode getNode(List<EnemyMovementDecision> enemyMovementDecisions) {
        PatternTreeNode node;
        for (int predicateLength = PATTERN_LENGTH; predicateLength >= 0; predicateLength--) {
            node = root;

            for (int idx = enemyMovementDecisions.size() - 1 - predicateLength; node != null && idx < enemyMovementDecisions.size(); idx++) {
                node = node.getChild(enemyMovementDecisions.get(idx));
            }

            if (node != null && node.getChildrenCount() > 2 && node.getVisitCount() > 15) {
                return node;
            }
        }

        return root;
    }

    private Double getAngleToPredictedPos(APoint predictedPos, APoint robotFuturePos) {
        return Utils.normalAbsoluteAngle(robotFuturePos.angleTo(predictedPos));
    }

    private boolean isBulletHitEnemy(APoint predictedPos, long timeDelta, double bulletSpeed) {
        final double angleToPredictedPos = robotPosAtFireTime.angleTo(predictedPos);
        final int bulletTravelledDistance = (int) ((timeDelta - AIMING_TIME) * bulletSpeed);
        final LXXPoint bulletPos = (LXXPoint) robotPosAtFireTime.project(angleToPredictedPos, bulletTravelledDistance);
        final Rectangle2D enemyRectAtPredictedPos = LXXUtils.getBoundingRectangleAt(predictedPos);
        return enemyRectAtPredictedPos.contains(bulletPos);
    }

    public List<EnemyMovementDecision> getLastEnemyMovementDecisions() {
        final List<EnemyMovementDecision> lastEnemyMovementDecisions = new ArrayList<EnemyMovementDecision>();
        final Target opponent = targetManager.getDuelOpponent();
        for (int i = PATTERN_LENGTH; i >= 0; i--) {
            lastEnemyMovementDecisions.add(PatternTreeNode.getEnemyMovementDecision(battleSnapshotManager.getLastSnapshot(opponent, i)));
        }

        return lastEnemyMovementDecisions;
    }

    public final class TurnPrediction {

        public final LXXPoint enemyPos;
        public final double enemyVelocity;
        public final double enemyHeading;
        public final PatternTreeNode node;
        public final EnemyMovementDecision emd;
        public final EnemyMovementDecision remd;

        public TurnPrediction(LXXPoint enemyPos, double enemyVelocity, double enemyHeading, PatternTreeNode node, EnemyMovementDecision emd, EnemyMovementDecision remd) {
            this.enemyPos = enemyPos;
            this.enemyVelocity = enemyVelocity;
            this.enemyHeading = enemyHeading;
            this.node = node;
            this.emd = emd;
            this.remd = remd;
        }

        public String toString() {
            return String.format("(enemy = %s, %f, %f); node = %s, (emd = %s - %s)", enemyPos.toString(), enemyVelocity, enemyHeading, node.getPath(), emd.key, remd.key);
        }
    }

}
