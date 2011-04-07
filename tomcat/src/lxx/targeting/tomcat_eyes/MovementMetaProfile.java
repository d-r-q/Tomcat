/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.targeting.tomcat_eyes;

import lxx.targeting.bullets.BulletManager;
import lxx.targeting.bullets.LXXBullet;
import lxx.utils.AvgValue;
import lxx.utils.LXXRobot;
import lxx.utils.LXXUtils;
import lxx.utils.Median;
import robocode.util.Utils;

import static java.lang.Math.*;

/**
 * User: jdev
 * Date: 01.03.2011
 */
public class MovementMetaProfile {

    private static final int DISTANCE_SEGMENTS = 25;
    private final Median[] distancesMedianAngles = new Median[1700 / DISTANCE_SEGMENTS];

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

    private int enemyPreferredDistance = -1;
    private boolean rammer = false;

    public void update(LXXRobot owner, LXXRobot viewPoint, BulletManager bulletManager) {
        avgVelocity.addValue(owner.getState().getVelocity());
        avgVelocityModule.addValue(owner.getState().getVelocityModule());

        avgTurnRate.addValue(toDegrees(owner.getState().getTurnRateRadians()));
        avgTurnRateModule.addValue(toDegrees(abs(owner.getState().getTurnRateRadians())));

        avgAttackAngle.addValue(toDegrees(LXXUtils.getAttackAngle(viewPoint, owner, owner.getState().getAbsoluteHeadingRadians())));
        avgBearing.addValue(toDegrees(Utils.normalRelativeAngle(owner.angleTo(viewPoint) - owner.getState().getAbsoluteHeadingRadians())));

        final double distanceBetween = owner.aDistance(viewPoint);
        avgDistanceBetween.addValue(distanceBetween);
        final LXXBullet firstBullet = bulletManager.getFirstBullet();
        if (firstBullet != null) {
            avgFirstBulletAttackAngle.addValue(toDegrees(LXXUtils.getAttackAngle(firstBullet.getFirePosition(), owner, owner.getState().getAbsoluteHeadingRadians())));
            avgDistanceToFirstBulletPos.addValue(firstBullet.getFirePosition().aDistance(owner));
        }

        avgDistanceToCenter.addValue(owner.getPosition().aDistance(owner.getState().getBattleField().center));
        if (owner.getState().getVelocityModule() > 0) {
            int idx = (int) round(distanceBetween / DISTANCE_SEGMENTS);
            if (distancesMedianAngles[idx] == null) {
                distancesMedianAngles[idx] = new Median();
            }
            final double angle = toDegrees(LXXUtils.anglesDiff(viewPoint.angleTo(owner), owner.getState().getAbsoluteHeadingRadians()));
            distancesMedianAngles[idx].addValue((int) angle);
            if (owner.getTime() % 10 == 0) {
                checkRammer();
            }
        }
    }

    private void checkRammer() {
        rammer = true;
        for (Median distancesMedianAngle : distancesMedianAngles) {
            if (distancesMedianAngle == null) {
                continue;
            }
            rammer &= distancesMedianAngle.getMediana() > 88;
        }
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

    public int getPreferredDistance() {
        for (int i = 0; i < distancesMedianAngles.length - 1; i++) {
            if (distancesMedianAngles[i] == null ||
                    distancesMedianAngles[i + 1] == null) {
                continue;
            }
            double m1 = distancesMedianAngles[i].getMediana();
            double m2 = distancesMedianAngles[i + 1].getMediana();
            if (m1 > 75 && m1 < 90 &&
                    m2 > 90 && m2 < 100) {
                enemyPreferredDistance = (i + 1) * DISTANCE_SEGMENTS;
                break;
            }
        }

        return enemyPreferredDistance;
    }

    public boolean isRammer() {
        return rammer;
    }
}
