package lxx;

import lxx.strategies.MovementDecision;
import lxx.utils.*;
import robocode.Rules;
import robocode.util.Utils;

import static java.lang.Math.abs;
import static java.lang.Math.signum;

public class RobotImage2 implements LXXRobotState2 {

    private LXXPoint position;
    private double velocity;
    private double heading;
    private BattleField battleField;
    private double energy;
    private double speed;
    private double absoluteHeadingRadians;
    private String name;

    public RobotImage2(LXXRobotSnapshot2 original) {
        this.position = new LXXPoint(original.getX(), original.getY());
        this.velocity = original.getVelocity();
        this.speed = abs(velocity);
        this.heading = original.getHeadingRadians();
        absoluteHeadingRadians = velocity >= 0 ? heading : Utils.normalAbsoluteAngle(heading + LXXConstants.RADIANS_180);
        this.battleField = original.getBattleField();
        this.energy = original.getEnergy();
        name = original.getName();
    }

    public void apply(MovementDecision movementDecision) {
        heading = Utils.normalAbsoluteAngle(heading + movementDecision.getTurnRateRadians());
        final double acceleration;
        final double desiredVelocity = movementDecision.getDesiredVelocity();
        if (abs(signum(velocity) - signum(desiredVelocity)) <= 1) {
            acceleration = LXXUtils.limit(-Rules.DECELERATION, abs(desiredVelocity) - speed, Rules.ACCELERATION);
            speed += acceleration;
            velocity = speed * signum(velocity != 0 ? velocity : desiredVelocity);
        } else {
            // robocode has difficult 2-step rules in this case,
            // but we will keep it simple
            if (speed > Rules.DECELERATION) {
                velocity -= Rules.DECELERATION * signum(velocity);
                speed -= Rules.DECELERATION;
            } else {
                velocity = 0;
                speed = 0;
            }
        }

        absoluteHeadingRadians = velocity >= 0 ? heading : Utils.normalAbsoluteAngle(heading + LXXConstants.RADIANS_180);
        position = position.project(absoluteHeadingRadians, speed);
    }

    public double getX() {
        return position.x;
    }

    public double getY() {
        return position.y;
    }

    public double aDistance(APoint p) {
        return position.aDistance(p);
    }

    public double angleTo(APoint pnt) {
        return position.angleTo(pnt);
    }

    public APoint project(double alpha, double distance) {
        return position.project(alpha, distance);
    }

    public APoint project(DeltaVector dv) {
        return position.project(dv);
    }

    public double getSpeed() {
        return speed;
    }

    public String getName() {
        return name;
    }

    public double getHeadingRadians() {
        return heading;
    }

    public BattleField getBattleField() {
        return battleField;
    }

    public double getVelocity() {
        return velocity;
    }

    public double getEnergy() {
        return energy;
    }

    public LXXPoint getPosition() {
        return position;
    }

    public double getGunHeat() {
        throw new UnsupportedOperationException();
    }

    public double getWidth() {
        throw new UnsupportedOperationException();
    }

    public double getHeight() {
        throw new UnsupportedOperationException();
    }

}
