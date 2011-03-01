/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.targeting.tomcat_eyes;

import lxx.targeting.bullets.BulletManager;
import lxx.targeting.bullets.LXXBullet;
import lxx.utils.AvgValue;
import lxx.utils.LXXRobot;
import lxx.utils.LXXUtils;
import robocode.util.Utils;

import static java.lang.Math.abs;
import static java.lang.Math.toDegrees;

/**
 * User: jdev
 * Date: 01.03.2011
 */
public class MovementMetaProfile {

    private final AvgValue avgVelocity = new AvgValue(10000);
    private final AvgValue avgTurnRate = new AvgValue(10000);
    private final AvgValue avgVelocityModule = new AvgValue(10000);
    private final AvgValue avgTurnRateModule = new AvgValue(10000);
    private final AvgValue avgAttackAngle = new AvgValue(10000);
    private final AvgValue avgBearing = new AvgValue(10000);
    private final AvgValue avgDistanceBetween = new AvgValue(10000);
    private final AvgValue avgFirstBulletAttackAngle = new AvgValue(10000);
    private final AvgValue avgDistanceToFirstBulletPos = new AvgValue(10000);
    private final AvgValue avgDistanceToCenter = new AvgValue(10000);

    private final LXXRobot owner;
    private final LXXRobot viewPoint;

    private final BulletManager bulletManager;

    public MovementMetaProfile(LXXRobot owner, LXXRobot viewPoint, BulletManager bulletManager) {
        this.owner = owner;
        this.viewPoint = viewPoint;
        this.bulletManager = bulletManager;
    }

    public void update() {
        avgVelocity.addValue(owner.getState().getVelocity());
        avgVelocityModule.addValue(owner.getState().getVelocityModule());

        avgTurnRate.addValue(toDegrees(owner.getState().getTurnRateRadians()));
        avgTurnRateModule.addValue(toDegrees(abs(owner.getState().getTurnRateRadians())));

        avgAttackAngle.addValue(toDegrees(LXXUtils.getAttackAngle(viewPoint, owner, owner.getState().getAbsoluteHeadingRadians())));
        avgBearing.addValue(toDegrees(Utils.normalRelativeAngle(owner.angleTo(viewPoint) - owner.getState().getAbsoluteHeadingRadians())));

        avgDistanceBetween.addValue(owner.aDistance(viewPoint));
        final LXXBullet firstBullet = bulletManager.getFirstBullet();
        if (firstBullet != null) {
            avgFirstBulletAttackAngle.addValue(toDegrees(LXXUtils.getAttackAngle(firstBullet.getFirePosition(), owner, owner.getState().getAbsoluteHeadingRadians())));
            avgDistanceToFirstBulletPos.addValue(firstBullet.getFirePosition().aDistance(owner));
        }

        avgDistanceToCenter.addValue(owner.getPosition().aDistance(owner.getState().getBattleField().center));
    }

    public String toString() {
        return String.format("%s's movement meta profile: " +
                "(average velocity = %1.3f) (average turn rate = %2.3f) " +
                "(average velocity module = %1.3f) (average turn rate module = %2.3f) " +
                "(average attack angle = %2.3f) (average bearing = %3.3f) " +
                "(average distance = %4.2f (average first bullet attack angle = %3.3f) " +
                "(average distance to first bullet fire pos = %4.2f) (average distance to center = %4.2f)",
                owner.getName(), avgVelocity.getCurrentValue(), avgTurnRate.getCurrentValue(),
                avgVelocityModule.getCurrentValue(), avgTurnRateModule.getCurrentValue(),
                avgAttackAngle.getCurrentValue(), avgBearing.getCurrentValue(),
                avgDistanceBetween.getCurrentValue(), avgFirstBulletAttackAngle.getCurrentValue(),
                avgDistanceToFirstBulletPos.getCurrentValue(), avgDistanceToCenter.getCurrentValue());
    }

    public String toShortString() {
        return String.format("%1.3f, %2.3f, %1.3f, %2.3f, %2.3f, %3.3f, %4.2f, %3.3f, %4.2f, %4.2f",
                avgVelocity.getCurrentValue(), avgTurnRate.getCurrentValue(),
                avgVelocityModule.getCurrentValue(), avgTurnRateModule.getCurrentValue(),
                avgAttackAngle.getCurrentValue(), avgBearing.getCurrentValue(),
                avgDistanceBetween.getCurrentValue(), avgFirstBulletAttackAngle.getCurrentValue(),
                avgDistanceToFirstBulletPos.getCurrentValue(), avgDistanceToCenter.getCurrentValue());
    }

    public double[] toArray() {
        return new double[]{
                avgVelocity.getCurrentValue(), avgTurnRate.getCurrentValue(),
                avgVelocityModule.getCurrentValue(), avgTurnRateModule.getCurrentValue(),
                avgAttackAngle.getCurrentValue(), avgBearing.getCurrentValue(),
                avgDistanceBetween.getCurrentValue(), avgFirstBulletAttackAngle.getCurrentValue(),
                avgDistanceToFirstBulletPos.getCurrentValue(), avgDistanceToCenter.getCurrentValue()};
    }

}
