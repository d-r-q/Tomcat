/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.targeting.tomcat_claws;

import static java.lang.Math.*;

public class EnemyMovementDecision {

    public final double acceleration;
    public final double turnRateRadians;

    public final String key;

    public EnemyMovementDecision(double acceleration, double turnRateRadians) {
        this.acceleration = acceleration;
        this.turnRateRadians = turnRateRadians;

        StringBuffer key = new StringBuffer();
        if (acceleration < -2) {
            key.append("-2.0");
        } else {
            key.append(signum(acceleration));
        }

        key.append(".");
        key.append(floor(toDegrees(turnRateRadians) / 2));

        this.key = key.toString();
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;

        EnemyMovementDecision that = (EnemyMovementDecision) o;

        if (key != null ? !key.equals(that.key) : that.key != null) return false;

        return true;
    }

    public int hashCode() {
        return key != null ? key.hashCode() : 0;
    }

    public String toString() {
        return key;
    }
}
