/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.targeting.tomcat_claws;

import lxx.RobotListener;
import lxx.Tomcat;
import lxx.events.TickEvent;
import lxx.model.BattleSnapshot;
import lxx.office.BattleSnapshotManager;
import lxx.office.Office;
import lxx.office.TargetManager;
import lxx.office.Timer;
import lxx.simulator.RobocodeDuelSimulator;
import lxx.strategies.Gun;
import lxx.strategies.GunDecision;
import lxx.strategies.MovementDecision;
import lxx.targeting.Target;
import lxx.targeting.tomcat_eyes.TargetingConfiguration;
import lxx.targeting.tomcat_eyes.TomcatEyes;
import lxx.utils.APoint;
import lxx.utils.LXXPoint;
import lxx.utils.LXXRobotState;
import lxx.utils.LXXUtils;
import robocode.Event;
import robocode.Rules;
import robocode.util.Utils;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static java.lang.Math.toDegrees;

/**
 * User: jdev
 * Date: 21.02.11
 */
public class TomcatClaws implements RobotListener, Gun {

    private static PatternTreeNode root = new PatternTreeNode(null, null, 0);

    private static final List<TurnPrediction> NO_PREDICTED_POSES = Collections.unmodifiableList(new ArrayList<TurnPrediction>());
    private static final int AIMING_TIME = 2;
    private static final int PATTERN_LENGTH = 4;
    private static final int LOOKUP_TIME = 15;

    private final Tomcat robot;
    private final BattleSnapshotManager battleSnapshotManager;
    private final TargetManager targetManager;
    private final Timer timer;
    private final TomcatEyes tomcatEyes;

    private LinkedList<TurnPrediction> predictedPoses = null;
    private RobocodeDuelSimulator duelSimulator;
    private APoint robotPosAtFireTime;

    public TomcatClaws(Office office, TomcatEyes tomcatEyes) {
        this.battleSnapshotManager = office.getBattleSnapshotManager();
        this.targetManager = office.getTargetManager();
        this.timer = office.getBattleTimeManager();
        this.tomcatEyes = tomcatEyes;

        robot = office.getRobot();
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
        final TargetingConfiguration targetingConfig = tomcatEyes.getConfiguration(t);
        if (robot.getTurnsToGunCool() > AIMING_TIME || t.getEnergy() == 0 || targetingConfig == null) {
            predictedPoses = null;
            return new GunDecision(getGunTurnAngle(angleToTarget), new TCPredictionData(NO_PREDICTED_POSES, robotPosAtFireTime));
        }

        if (predictedPoses == null || predictedPoses.size() == 0) {
            predictedPoses = new LinkedList<TurnPrediction>();
            robot.setDebugProperty("Use targeting config", targetingConfig.getName());
            robot.setDebugProperty("Enemy gun type", tomcatEyes.getEnemyGunType(t).toString());
            duelSimulator = new RobocodeDuelSimulator(t, robot, t.getTime(), timer.getBattleTime(), targetingConfig.getAttributes());
            robotPosAtFireTime = robot.project(robot.getAbsoluteHeadingRadians(), robot.getVelocityModule() * AIMING_TIME);

            final double bulletSpeed = Rules.getBulletSpeed(firePower);
            buildPattern(bulletSpeed);

            if (predictedPoses == null || predictedPoses.size() == 0) {
                return new GunDecision(getGunTurnAngle(angleToTarget), new TCPredictionData(NO_PREDICTED_POSES, robotPosAtFireTime));
            }
        }

        final double angleToPredictedPos = getAngleToPredictedPos(predictedPoses.getLast().enemyPos, this.robotPosAtFireTime);

        return new GunDecision(getGunTurnAngle(angleToPredictedPos), new TCPredictionData(predictedPoses, robotPosAtFireTime));
    }

    private double getGunTurnAngle(double angleToPredictedPos) {
        return Utils.normalRelativeAngle(angleToPredictedPos - robot.getGunHeadingRadians());
    }

    public void buildPattern(double bulletSpeed) {
        final List<MovementDecision> enemyMovementDecisions = getLastEnemyMovementDecisions();
        final TargetingConfiguration targetingConfiguration = tomcatEyes.getConfiguration(targetManager.getDuelOpponent());

        long timeDelta = 0;
        while (!isBulletHitEnemy(duelSimulator.getEnemyProxy(), timeDelta, bulletSpeed)) {
            final PatternTreeNode node = getNode(enemyMovementDecisions);
            final PatternTreeNode.PatternTreeNodeSelectionData n = node.getChildBySnapshot(duelSimulator.getSimulatorSnapshot(), targetingConfiguration.getIndexes(), targetingConfiguration.getWeights());
            final MovementDecision movementDecision = n.getDecision();
            duelSimulator.setEnemyMovementDecision(movementDecision);
            duelSimulator.setMyMovementDecision(new MovementDecision(1, 0, robot.getVelocity() >= 0 ? MovementDecision.MovementDirection.FORWARD : MovementDecision.MovementDirection.BACKWARD));
            duelSimulator.doTurn();
            enemyMovementDecisions.add(PatternTreeNode.getMovementDecision(duelSimulator.getSimulatorSnapshot()));
            if (timeDelta >= AIMING_TIME) {
                final LXXRobotState enemyState = duelSimulator.getEnemyProxy().getState();
                final LXXPoint predictedPos = new LXXPoint(enemyState);
                predictedPoses.add(new TurnPrediction(predictedPos, enemyState.getVelocity(), toDegrees(enemyState.getAbsoluteHeadingRadians()), node, movementDecision,
                        PatternTreeNode.getMovementDecision(duelSimulator.getSimulatorSnapshot())));
            }
            timeDelta++;
        }
    }

    public PatternTreeNode getNode(List<MovementDecision> enemyMovementDecisions) {
        PatternTreeNode node;
        for (int predicateLength = PATTERN_LENGTH; predicateLength >= 0; predicateLength--) {
            node = root;

            for (int idx = enemyMovementDecisions.size() - 1 - predicateLength; node != null && idx < enemyMovementDecisions.size(); idx++) {
                node = node.getChild(enemyMovementDecisions.get(idx));
            }

            if (node != null && node.getChildrenCount() > 4 && node.getVisitCount() > 30) {
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

    public List<MovementDecision> getLastEnemyMovementDecisions() {
        final List<MovementDecision> lastEnemyMovementDecisions = new ArrayList<MovementDecision>();
        final Target opponent = targetManager.getDuelOpponent();
        for (int i = PATTERN_LENGTH; i >= 0; i--) {
            lastEnemyMovementDecisions.add(PatternTreeNode.getMovementDecision(battleSnapshotManager.getLastSnapshot(opponent, i)));
        }

        return lastEnemyMovementDecisions;
    }

    public final class TurnPrediction {

        public final LXXPoint enemyPos;
        public final double enemyVelocity;
        public final double enemyHeading;
        public final PatternTreeNode node;
        public final MovementDecision emd;
        public final MovementDecision remd;

        public TurnPrediction(LXXPoint enemyPos, double enemyVelocity, double enemyHeading, PatternTreeNode node, MovementDecision emd, MovementDecision remd) {
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
