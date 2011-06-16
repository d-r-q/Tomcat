/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.simulator;

import lxx.Tomcat;
import lxx.model.TurnSnapshot;
import lxx.model.attributes.Attribute;
import lxx.office.AttributesManager;
import lxx.strategies.MovementDecision;
import lxx.targeting.Target;
import lxx.targeting.bullets.LXXBullet;
import lxx.utils.*;
import lxx.wave.Wave;
import robocode.Bullet;
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
    private final Set<Attribute> attributesToSimulate = new HashSet<Attribute>();

    private final RobotProxy enemyProxy;
    private final RobotProxy meProxy;
    private final long time;
    private final int round;
    private final List<LXXBullet> myBullets;

    private long timeElapsed = 0;

    public RobocodeDuelSimulator(Target enemy, Tomcat robot, long time, int round, Attribute[] attributesToSimulate, List<LXXBullet> myBullets) {
        this.enemyProxy = new RobotProxy(enemy, time);
        this.meProxy = new RobotProxy(robot, time);
        this.time = time;
        this.round = round;

        this.attributesToSimulate.addAll(Arrays.asList(attributesToSimulate));
        this.attributesToSimulate.add(AttributesManager.enemyVelocity);
        this.attributesToSimulate.add(AttributesManager.enemyAcceleration);
        this.attributesToSimulate.add(AttributesManager.enemyTurnRate);

        this.myBullets = new ArrayList<LXXBullet>();
        for (LXXBullet bullet : myBullets) {
            this.myBullets.add(new LXXBullet(bullet.getBullet(), bullet.getWave(), bullet.getAimPredictionData()));
        }
        final LXXBullet nextFiredBullet = new LXXBullet(new Bullet(robot.angleTo(enemy), robot.getX(), robot.getY(), robot.firePower(), robot.getName(), enemy.getName(), true, -2),
                new Wave(robot.getState(), enemy.getState(), Rules.getBulletSpeed(robot.firePower()), time + 2), null);
        this.myBullets.add(nextFiredBullet);
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
        processBullets();
        timeElapsed++;
    }

    private void processBullets() {
        final List<LXXBullet> toRemove = new ArrayList<LXXBullet>();
        for (LXXBullet bullet : myBullets) {
            final Bullet oldBulletState = bullet.getBullet();
            final LXXPoint newBulletPos = bullet.getCurrentPosition().project(bullet.getHeadingRadians(), bullet.getSpeed());
            if (bullet.getFirePosition().aDistance(newBulletPos) > bullet.getFirePosition().aDistance(bullet.getTarget())) {
                toRemove.add(bullet);
            }
            final Bullet newBulletState = new Bullet(oldBulletState.getHeadingRadians(), newBulletPos.x, newBulletPos.y, oldBulletState.getPower(),
                    oldBulletState.getName(), oldBulletState.getVictim(), true, -2);
            bullet.setBullet(newBulletState);
        }

        myBullets.removeAll(toRemove);
    }

    private void apply(RobotProxy proxy, MovementDecision movementDecision) {
        final LXXRobotState state = proxy.getState();
        final double newHeading = Utils.normalAbsoluteAngle(state.getHeadingRadians() + movementDecision.getTurnRateRadians());
        final double accel = movementDecision.getAcceleration();
        final double maxVelocity = LXXUtils.limit(0, (signum(state.getVelocity()) == movementDecision.getMovementDirection().sign
                ? state.getVelocityModule() + accel
                : accel), Rules.MAX_VELOCITY);

        double newVelocity = maxVelocity * movementDecision.getMovementDirection().sign;

        double distanceToWall = max(new LXXPoint(state).distanceToWall(state.getBattleField(), state.getAbsoluteHeadingRadians()), 0);
        APoint newPosition;
        if (distanceToWall > newVelocity) {
            newPosition = proxy.getState().project(newVelocity >= 0 ? newHeading : Utils.normalAbsoluteAngle(newHeading + LXXConstants.RADIANS_180), abs(newVelocity));
        } else {
            do {
                newPosition = proxy.getState().project(newVelocity >= 0 ? newHeading : Utils.normalAbsoluteAngle(newHeading + LXXConstants.RADIANS_180), distanceToWall);
                distanceToWall -= 1;
            } while (!proxy.getState().getBattleField().contains(newPosition) && distanceToWall > 0);
            newVelocity = 0;
        }
        proxy.doTurn(new RobotImage(newPosition, newVelocity, newHeading, state.getBattleField(), movementDecision.getTurnRateRadians(), state.getEnergy()));
    }

    public TurnSnapshot getSimulatorSnapshot() {
        final double[] avs = new double[AttributesManager.attributesCount()];

        for (Attribute a : attributesToSimulate) {
            avs[a.getId()] = a.getExtractor().getAttributeValue(enemyProxy, meProxy, myBullets);
        }

        return new TurnSnapshot(avs, time + timeElapsed, round, enemyProxy.getName());
    }

    public RobotProxy getEnemyProxy() {
        return enemyProxy;
    }
}
