package lxx.targeting;

import lxx.utils.LXXPoint;
import lxx.StaticData;
import static lxx.StaticData.robot;

import static java.lang.Math.sin;
import static java.lang.Math.cos;

import robocode.util.Utils;

/**
 * User: jdev
 * Date: 30.10.2009
 */

public class TargetImage extends Target {
    public TargetImage(Target realTarget) {
        super(realTarget);
    }

    public void setHeading(double heading) {
        this.heading = heading;
    }

    public void setVelocity(double velocity) {
        this.velocity = velocity;
    }

    public void setVelocityDelta(double velocityDelta) {
        this.velocityDelta = velocityDelta;
    }

    public void setHeadingDelta(double headingDelta) {
        this.headingDelta = headingDelta;
    }

    public void tick() {
        heading = Utils.normalAbsoluteAngle(heading + headingDelta);
        velocity += velocityDelta;

        final double newX = position.x + sin(heading) * velocity;
        if (newX < 0 || newX > robot.getBattleField().width) {
            velocity = 0;
            return;
        }
        position.x = newX;
        final double newY = position.y + cos(heading) * velocity;
        if (newY < 0 || newY > robot.getBattleField().height) {
            velocity = 0;
            return;
        }
        position.y = newY;
    }

    public void setPosition(LXXPoint position) {
        this.position = position;
    }

    public void setLateralVelocity(double lateralVelocity) {
        this.lateralVelocity = lateralVelocity;
    }
}
