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

    private final AvgValue avgVelocity = new AvgValue(3000);
    private final AvgValue avgTurnRate = new AvgValue(3000);
    private final AvgValue avgStopTime = new AvgValue(3000);
    private final AvgValue avgTurnRateModule = new AvgValue(3000);
    private final AvgValue avgAttackAngle = new AvgValue(3000);
    private final Median bearingMedian = new Median(10000);
    private final AvgValue avgDistanceBetween = new AvgValue(3000);
    private final AvgValue avgTravelTime = new AvgValue(3000);
    private final AvgValue avgDistanceToFirstBulletPos = new AvgValue(3000);
    private final AvgValue avgDistanceToCenter = new AvgValue(3000);
    private final Median firstBulletBearingMedian = new Median(10000);
    private final AvgValue avgVelocityModuleOnFirstBullet = new AvgValue(10000);
    private final AvgValue avgAccelModuleOnFirstBullet = new AvgValue(10000);
    private final AvgValue avgVelocityModuleNoBullet = new AvgValue(10000);
    private final AvgValue avgAccelModuleNoBullet = new AvgValue(10000);
    private final AvgValue avgVelocityModuleOnFire = new AvgValue(10000);
    private final AvgValue avgAccelModuleOnFire = new AvgValue(10000);
    private final AvgValue avgDistToForwardWall = new AvgValue(3000);
    private final Median bearingNBMedian = new Median(10000);
    private final Median dirChangeRateNBMedian = new Median(10000);
    private final Median dirChangeRateOBMedian = new Median(10000);

    private int enemyPreferredDistance = -1;
    private boolean rammer = false;

    private int updateCountNB;
    private int dirChangeCountNB;
    private int updateCountOB;
    private int dirChangeCountOB;

    public void update(LXXRobot owner, LXXRobot viewPoint, BulletManager bulletManager) {
        avgVelocity.addValue(owner.getState().getVelocity());
        avgStopTime.addValue(owner.getTime() - owner.getLastTravelTime());

        avgTurnRate.addValue(toDegrees(owner.getState().getTurnRateRadians()));
        avgTurnRateModule.addValue(toDegrees(abs(owner.getState().getTurnRateRadians())));

        avgAttackAngle.addValue(toDegrees(LXXUtils.getAttackAngle(viewPoint, owner, owner.getState().getAbsoluteHeadingRadians())));
        bearingMedian.addValue(abs(toDegrees(Utils.normalRelativeAngle(owner.angleTo(viewPoint) - owner.getState().getAbsoluteHeadingRadians()))));

        final double distanceBetween = owner.aDistance(viewPoint);
        avgDistanceBetween.addValue(distanceBetween);
        final LXXBullet firstBullet = bulletManager.getFirstBullet();
        if (firstBullet != null) {
            avgTravelTime.addValue(owner.getTime() - owner.getLastStopTime());
            avgDistanceToFirstBulletPos.addValue(firstBullet.getFirePosition().aDistance(owner));
            firstBulletBearingMedian.addValue(toDegrees(abs(Utils.normalRelativeAngle(owner.angleTo(firstBullet.getFirePosition()) - owner.getState().getAbsoluteHeadingRadians()))));
            if ((firstBullet.getFirePosition().aDistance(owner) - firstBullet.getTravelledDistance()) / firstBullet.getSpeed() < 1) {
                avgVelocityModuleOnFirstBullet.addValue(owner.getState().getVelocityModule());
                avgAccelModuleOnFirstBullet.addValue(abs(owner.getAcceleration()));
            }

            if (owner.getPrevState() != null) {
                if (signum(owner.getPrevState().getVelocity()) != signum(owner.getState().getVelocity())) {
                    dirChangeCountOB++;
                }
            }
            updateCountOB++;
        } else {
            bearingNBMedian.addValue(abs(toDegrees(Utils.normalRelativeAngle(owner.angleTo(viewPoint) - owner.getState().getAbsoluteHeadingRadians()))));

            if (owner.getPrevState() != null) {
                if (signum(owner.getPrevState().getVelocity()) != signum(owner.getState().getVelocity())) {
                    dirChangeCountNB++;
                }
            }
            updateCountNB++;
            avgVelocityModuleNoBullet.addValue(owner.getState().getVelocityModule());
            avgAccelModuleNoBullet.addValue(abs(owner.getAcceleration()));
        }
        final LXXBullet lastBullet = bulletManager.getLastBullet();
        if (lastBullet != null && owner.getTime() - lastBullet.getWave().getLaunchTime() == 2) {
            avgVelocityModuleOnFire.addValue(owner.getState().getVelocityModule());
            avgAccelModuleOnFire.addValue(abs(owner.getAcceleration()));
        }
        double dirChangeRateNB = ((double) dirChangeCountNB) / updateCountNB;
        if (!Double.isInfinite(dirChangeRateNB) && !Double.isNaN(dirChangeRateNB)) {
            this.dirChangeRateNBMedian.addValue(dirChangeRateNB);
        }
        double dirChangeRateOB = ((double) dirChangeCountOB) / updateCountOB;
        if (!Double.isInfinite(dirChangeRateOB) && !Double.isNaN(dirChangeRateOB)) {
            this.dirChangeRateOBMedian.addValue(dirChangeRateOB);
        }

        avgDistanceToCenter.addValue(owner.getPosition().aDistance(owner.getState().getBattleField().center));
        if (owner.getState().getVelocityModule() > 0) {
            int idx = (int) round(distanceBetween / DISTANCE_SEGMENTS);
            if (distancesMedianAngles[idx] == null) {
                distancesMedianAngles[idx] = new Median(2000);
            }
            final double angle = toDegrees(LXXUtils.anglesDiff(viewPoint.angleTo(owner), owner.getState().getAbsoluteHeadingRadians()));
            distancesMedianAngles[idx].addValue((int) angle);
            if (owner.getTime() % 10 == 0) {
                checkRammer();
            }
        }
        avgDistToForwardWall.addValue(owner.getPosition().distanceToWall(owner.getState().getBattleField(), owner.getState().getAbsoluteHeadingRadians()));
    }

    private void checkRammer() {
        rammer = true;
        for (Median distancesMedianAngle : distancesMedianAngles) {
            if (distancesMedianAngle == null) {
                continue;
            }
            rammer &= distancesMedianAngle.getMedian() > 88;
        }
    }
    // 95,131, 94,126, 4,299, 6,028, 5,121, 1,042, 0,661, 0,811, 88,456, 0,040, 0,112 - Druss
    // 94,423, 88,641, 6,000, 1,000 - Samekh
    // 91,703, 90,294, 4,954, 7,272, 5,160, 0,942, 0,173, 0,885, 91,991, 0,013, 0,096 - Shadow
    // 88,497, 88,577, 6,000, 1,000 - Midboss
    // 92,109, 93,238, 5,831, 1,645, 0,218, 0,858, 90,041, 0,041, 0,073 - Shiva
    // 95,318, 86,116, 8,000, 0,750, 118,523, 0,046, 0,073 - RaikoMX

    // 90,695, 91,654, 5,264, 5,165, 5,110, 0,701, 0,680, 0,767, 90,218, 0,098, 0,108 - Ocnirp
    // 92,353, 92,555, 5,665, 5,098, 0,817, 0,707, 112,235, 0,096, 0,080 - Raiko
    // 76,838, 75,526, 6,494, 5,859, 0,581, 0,435, 85,667, 0,044, 0,037 - DoctorBob
    // 89,769, 89,401, 3,496, 4,972, 0,487, 0,654, 91,361, 0,046, 0,048 - SandboxDT

    // 100,445, 91,416, 6,672, 5,170, 0,591, 0,493, 105,837, 0,074, 0,060 - Star2

    // 92,375, 93,531, 5,874, 0,969, 5,733, 0,639, 0,062, 0,678, 128,838, 0,000, 0,035 - GrubbmGrb
    public String toShortString() {
        /* return String.format("%1.3f, %2.3f, %1.3f, %2.3f, %2.3f, %3.3f, %4.2f, %3.3f, %4.2f, %4.2f, %3.2f, %1.1f, %1.1f, %3.2f",
       avgVelocity.getCurrentValue(), avgTurnRate.getCurrentValue(),
       avgStopTime.getCurrentValue(), avgTurnRateModule.getCurrentValue(),
       avgAttackAngle.getCurrentValue(), bearingMedian.getMedian(),
       avgDistanceBetween.getCurrentValue(), avgTravelTime.getCurrentValue(),
       avgDistanceToFirstBulletPos.getCurrentValue(), avgDistanceToCenter.getCurrentValue(),
       firstBulletBearingMedian.getMedian(), avgVelocityModuleOnFirstBullet.getMedian(),
       avgAccelModuleOnFirstBullet.getMedian(), avgDistToForwardWall.getCurrentValue());*/
        return String.format("%3.3f, %3.3f, %1.3f, %1.3f, %1.3f, %1.3f, %1.3f, %1.3f, %3.3f, %1.3f, %1.3f",
                bearingMedian.getMedian(), firstBulletBearingMedian.getMedian(),
                avgVelocityModuleOnFirstBullet.getCurrentValue(), avgVelocityModuleNoBullet.getCurrentValue(), avgVelocityModuleOnFire.getCurrentValue(),
                avgAccelModuleOnFirstBullet.getCurrentValue(), avgAccelModuleNoBullet.getCurrentValue(), avgAccelModuleOnFire.getCurrentValue(),
                bearingNBMedian.getMedian(), dirChangeRateNBMedian.getMedian(), dirChangeRateOBMedian.getMedian()
        );
    }

    public double[] toArray() {
        return new double[]{
                avgVelocity.getCurrentValue(), avgTurnRate.getCurrentValue(),
                avgStopTime.getCurrentValue(), avgTurnRateModule.getCurrentValue(),
                avgAttackAngle.getCurrentValue(), bearingMedian.getMedian(),
                avgDistanceBetween.getCurrentValue(), avgTravelTime.getCurrentValue(),
                avgDistanceToFirstBulletPos.getCurrentValue(), avgDistanceToCenter.getCurrentValue(),
                firstBulletBearingMedian.getMedian(), avgVelocityModuleOnFirstBullet.getCurrentValue(),
                avgAccelModuleOnFirstBullet.getCurrentValue(), avgDistToForwardWall.getCurrentValue()};
    }

    public int getPreferredDistance() {
        for (int i = 0; i < distancesMedianAngles.length - 1; i++) {
            if (distancesMedianAngles[i] == null ||
                    distancesMedianAngles[i + 1] == null) {
                continue;
            }
            double m1 = distancesMedianAngles[i].getMedian();
            double m2 = distancesMedianAngles[i + 1].getMedian();
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
