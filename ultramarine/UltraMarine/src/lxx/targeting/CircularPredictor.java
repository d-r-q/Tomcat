package lxx.targeting;

import lxx.targeting.predict.Predictor;
import lxx.utils.LXXPoint;
import static lxx.StaticData.robot;
import lxx.StaticData;
import lxx.UltraMarine;

import java.awt.*;
import static java.lang.Math.signum;
import static java.lang.Math.abs;
import static java.lang.Math.sin;
import static java.lang.Math.cos;
import static java.lang.Math.PI;
import java.util.*;
import java.util.List;

import robocode.*;

/**
 * User: jdev
 * Date: 01.11.2009
 */
public class CircularPredictor implements Predictor {

    private final Map<Target, List<Target0>> targets = new HashMap<Target, List<Target0>>();

    public Double predictAngle(Target t) {
        LXXPoint pos = new LXXPoint(t.getPosition());
        double heading = t.getHeading();
        double velocity = t.getVelocity();
        double headingDelta = 0;
        double velocityDelta = 0;
        List<Target0> targets = null;//this.targets.get(t);
        if (targets != null) {
            for (Target0 t0 : targets) {
                headingDelta += t0.headingDelta;
                velocityDelta += t0.velocityDelta;
            }
            headingDelta /= targets.size();
            velocityDelta /= targets.size();
        } else {
            headingDelta = t.getHeadingDelta();
            velocityDelta = t.getVelocityDelta();
        }
        if (t.getEnergy() == 0) {
            velocity = 0;
            velocityDelta = 0;
        }
        int time = 0;
        do {
            pos = predictMovement(pos, velocity, heading);
            if (!robot.getBattleField().contains(pos)) {
                pos = predictMovement(pos, -velocity, -heading);
                heading = heading + PI / 2;
            }
            velocity += velocityDelta;
            if (abs(velocity) > Rules.MAX_VELOCITY) {
                velocity = Rules.MAX_VELOCITY * signum(velocityDelta);
            }
            heading += headingDelta;
        } while (robot.distance(pos) > Rules.getBulletSpeed(((UltraMarine)robot).firePower()) * time++);


        return robot.angleTo(pos);
    }

    private LXXPoint predictMovement(LXXPoint initPos, double velocity, double heading) {
        LXXPoint res = new LXXPoint(initPos);
        res.x += sin(heading) * velocity;
        res.y += cos(heading) * velocity;
        return res;
    }

    public void paint(Graphics2D g, Target t) {
    }

    public void onRoundStarted() {
    }

    public String getName() {
        return getClass().getSimpleName();
    }

    public void targetUpdated(Target oldState, Target newState, robocode.Event source) {
        Target0 t = new Target0();
        t.headingDelta = newState.getHeadingDelta();
        t.velocityDelta = newState.getVelocityDelta();
        List<Target0> targets = this.targets.get(newState);
        if (targets == null) {
            targets = new ArrayList<Target0>();
            this.targets.put(newState, targets);
        }
        targets.add(t);
        if (targets.size() > 10) {
            targets.remove(0);
        }
    }

    private class Target0 {
        private double headingDelta;
        private double velocityDelta;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CircularPredictor that = (CircularPredictor) o;

        return !(getName() != null ? !getName().equals(that.getName()) : that.getName() != null);

    }

    public int hashCode() {
        return (getName() != null ? getName().hashCode() : 0);
    }

}
