/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.model;

import lxx.model.attributes.Attribute;
import lxx.office.AttributesManager;
import lxx.utils.LXXUtils;

import java.io.Serializable;
import java.util.Arrays;

import static java.lang.Math.signum;
import static java.lang.Math.toRadians;

/**
 * User: jdev
 * Date: 10.03.2010
 */
public class BattleSnapshot implements Serializable {

    private final int[] bsAttributes;
    private final long time;
    private final long battleTime;
    private final String targetName;

    public BattleSnapshot(int[] bsAttributes, long time, long battleTime, String targetName) {
        this.bsAttributes = bsAttributes;
        this.time = time;
        this.battleTime = battleTime;
        this.targetName = targetName;
    }

    public int getAttrValue(Attribute a) {
        return bsAttributes[a.getId()];
    }

    public String getTargetName() {
        return targetName;
    }

    public final double quickDistance(int[] indexes, BattleSnapshot bs, double[] weights) {
        return LXXUtils.weightedManhattanDistance(indexes, bsAttributes, bs.bsAttributes, weights);
    }

    public long getTime() {
        return time;
    }

    public long getBattleTime() {
        return battleTime;
    }

    public String toString() {
        return Arrays.toString(bsAttributes);
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BattleSnapshot that = (BattleSnapshot) o;

        if (battleTime != that.battleTime) return false;
        if (targetName != null ? !targetName.equals(that.targetName) : that.targetName != null) return false;

        return true;
    }

    public int hashCode() {
        int result = (int) (battleTime ^ (battleTime >>> 32));
        result = 31 * result + (targetName != null ? targetName.hashCode() : 0);
        return result;
    }

    public double getEnemyAbsoluteHeadingRadians() {
        return toRadians(bsAttributes[AttributesManager.enemyAbsoluteHeading.getId()]);
    }

    public double getEnemyVelocityModule() {
        return bsAttributes[AttributesManager.enemyVelocityModule.getId()];
    }

    public double getMyVelocityModule() {
        return bsAttributes[AttributesManager.myVelocityModule.getId()];
    }

    public double getMyAbsoluteHeadingRadians() {
        return bsAttributes[AttributesManager.myAbsoluteHeadingDegrees.getId()];
    }

    public double getMyLateralVelocity() {
        return LXXUtils.lateralVelocity2(LXXUtils.getEnemyPos(this), LXXUtils.getMyPos(this),
                getMyVelocityModule(), getMyAbsoluteHeadingRadians());
    }

    public double getMyLateralDirection() {
        return signum(getMyLateralVelocity());
    }

}
