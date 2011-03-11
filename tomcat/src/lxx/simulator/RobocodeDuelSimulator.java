/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.simulator;

import lxx.Tomcat;
import lxx.model.BattleSnapshot;
import lxx.model.attributes.Attribute;
import lxx.office.AttributesManager;
import lxx.strategies.MovementDecision;
import lxx.targeting.Target;
import lxx.utils.*;
import robocode.Rules;
import robocode.util.Utils;

import java.util.*;

import static java.lang.Math.*;

/**
 * User: jdev
 * Date: 24.02.2011
 */
public class RobocodeDuelSimulator {

    private final Map<RobotProxy, MovementDecision> movementDecisions = new HashMap<RobotProxy, MovementDecision>();
    private final RobotProxy enemyProxy;
    private final RobotProxy meProxy;
    private final long time;
    private final long battleTime;
    private final Set<Attribute> attributesToSimulate = new HashSet<Attribute>();

    private long timeElapsed = 0;

    public RobocodeDuelSimulator(Target enemy, Tomcat robot, long time, long battleTime, Attribute[] attributesToSimulate) {
        this.enemyProxy = new RobotProxy(enemy, time);
        this.meProxy = new RobotProxy(robot, time);
        this.time = time;
        this.battleTime = battleTime;

        this.attributesToSimulate.addAll(Arrays.asList(attributesToSimulate));
        this.attributesToSimulate.add(AttributesManager.enemyAcceleration);
        this.attributesToSimulate.add(AttributesManager.enemyTurnRate);
    }

    public void setEnemyMovementDecision(MovementDecision movementDecision) {
        movementDecisions.put(enemyProxy, movementDecision);
    }

    public void setMyMovementDecision(MovementDecision myMovementDecision) {
        movementDecisions.put(meProxy, myMovementDecision);
    }

    public void doTurn() {
        for (RobotProxy proxy : movementDecisions.keySet()) {
            final MovementDecision md = movementDecisions.get(proxy);
            apply(proxy, md);
        }
        timeElapsed++;
    }

    private void apply(RobotProxy proxy, MovementDecision movementDecision) {
        final LXXRobotState state = proxy.getState();
        final double newHeading = Utils.normalAbsoluteAngle(state.getHeadingRadians() + movementDecision.getTurnRateRadians());
        final double maxVelocity = LXXUtils.limit(0, abs(state.getVelocity()) + movementDecision.getAcceleration(), Rules.MAX_VELOCITY);

        double newVelocity;
        if (signum(state.getVelocity()) == signum(movementDecision.getMovementDirection().sign) ||
                state.getVelocity() == 0) {
            newVelocity = maxVelocity * movementDecision.getMovementDirection().sign;
        } else {
            newVelocity = max(0, abs(state.getVelocity()) - Rules.DECELERATION);
        }

        final double distanceToWall = new LXXPoint(state).distanceToWall(state.getBattleField(), state.getAbsoluteHeadingRadians());
        final APoint newPosition;
        if (distanceToWall > newVelocity) {
            newPosition = proxy.getState().project(newVelocity >= 0 ? newHeading : Utils.normalAbsoluteAngle(newHeading + LXXConstants.RADIANS_180), abs(newVelocity));
        } else {
            newPosition = proxy.getState().project(newVelocity >= 0 ? newHeading : Utils.normalAbsoluteAngle(newHeading + LXXConstants.RADIANS_180), distanceToWall);
            newVelocity = 0;
        }
        proxy.doTurn(new RobotImage(newPosition, newVelocity, newHeading, state.getBattleField(), movementDecision.getTurnRateRadians(), state.getEnergy()));
    }

    public BattleSnapshot getSimulatorSnapshot() {
        final int[] avs = new int[AttributesManager.attributesCount()];

        for (Attribute a : attributesToSimulate) {
            avs[a.getId()] = a.getExtractor().getAttributeValue(enemyProxy, meProxy);
        }

        return new BattleSnapshot(avs, time + timeElapsed, battleTime + timeElapsed, null);
    }

    public RobotProxy getEnemyProxy() {
        return enemyProxy;
    }
}
