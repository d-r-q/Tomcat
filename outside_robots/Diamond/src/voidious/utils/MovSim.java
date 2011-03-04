package voidious.utils;

// Coded by Albert
// http://robowiki.net?FuturePosition

import robocode.AdvancedRobot;
import robocode.util.Utils;

public class MovSim {

    private double systemMaxTurnRate = Math.toRadians(10.0);
    private double systemMaxVelocity = 8.0;
    private double maxBraking = 2.0;
    private double maxAcceleration = 1.0;


    public double defaultMaxTurnRate = 10.0;
    public double defaultMaxVelocity = 8.0;


    public MovSim() {
    }

    ;


    public MovSimStat[] futurePos(int steps, AdvancedRobot b) {
        return futurePos(steps, b, defaultMaxVelocity, defaultMaxTurnRate);
    }

    public MovSimStat[] futurePos(int steps, AdvancedRobot b, double maxVel, double maxTurnRate) {
        return futurePos(steps, b.getX(), b.getY(), b.getVelocity(), maxVel, b.getHeadingRadians(), b.getDistanceRemaining(), b.getTurnRemainingRadians(), maxTurnRate, b.getBattleFieldWidth(), b.getBattleFieldHeight());
    }


    public MovSimStat[] futurePos(int steps, double x, double y, double velocity, double maxVelocity, double heading, double distanceRemaining, double angleToTurn, double maxTurnRate, double battleFieldW, double battleFieldH) {
        //maxTurnRate in degrees
        MovSimStat[] pos = new MovSimStat[steps];
        double acceleration = 0;
        boolean slowingDown = false;
        double moveDirection;

        maxTurnRate = Math.toRadians(maxTurnRate);
        if (distanceRemaining == 0) moveDirection = 0;
        else if (distanceRemaining < 0.0) moveDirection = -1;
        else moveDirection = 1;

        //heading, accel, velocity, distance
        for (int i = 0; i < steps; i++) {
            //heading
            double lastHeading = heading;
            double turnRate = Math.min(maxTurnRate, ((0.4 + 0.6 * (1.0 - (Math.abs(velocity) / systemMaxVelocity))) * systemMaxTurnRate));
            if (angleToTurn > 0.0) {
                if (angleToTurn < turnRate) {
                    heading += angleToTurn;
                    angleToTurn = 0.0;
                } else {
                    heading += turnRate;
                    angleToTurn -= turnRate;
                }
            } else if (angleToTurn < 0.0) {
                if (angleToTurn > -turnRate) {
                    heading += angleToTurn;
                    angleToTurn = 0.0;
                } else {
                    heading -= turnRate;
                    angleToTurn += turnRate;
                }
            }
            heading = Utils.normalAbsoluteAngle(heading);
            //movement
            if (distanceRemaining != 0.0 || velocity != 0.0) {
                //lastX = x; lastY = y;
                if (!slowingDown && moveDirection == 0) {
                    slowingDown = true;
                    if (velocity > 0.0) moveDirection = 1;
                    else if (velocity < 0.0) moveDirection = -1;
                    else moveDirection = 0;
                }
                double desiredDistanceRemaining = distanceRemaining;
                if (slowingDown) {
                    if (moveDirection == 1 && distanceRemaining < 0.0) desiredDistanceRemaining = 0.0;
                    else if (moveDirection == -1 && distanceRemaining > 1.0) desiredDistanceRemaining = 0.0;
                }
                double slowDownVelocity = (double) (int) (maxBraking / 2.0 * ((Math.sqrt(4.0 * Math.abs(desiredDistanceRemaining) + 1.0)) - 1.0));
                if (moveDirection == -1) slowDownVelocity = -slowDownVelocity;
                if (!slowingDown) {
                    if (moveDirection == 1) {
                        if (velocity < 0.0) acceleration = maxBraking;
                        else acceleration = maxAcceleration;
                        if (velocity + acceleration > slowDownVelocity) slowingDown = true;
                    } else if (moveDirection == -1) {
                        if (velocity > 0.0) acceleration = -maxBraking;
                        else acceleration = -maxAcceleration;
                        if (velocity + acceleration < slowDownVelocity) slowingDown = true;
                    }
                }
                if (slowingDown) {
                    if (distanceRemaining != 0.0 && Math.abs(velocity) <= maxBraking && Math.abs(distanceRemaining) <= maxBraking)
                        slowDownVelocity = distanceRemaining;
                    double perfectAccel = slowDownVelocity - velocity;
                    if (perfectAccel > maxBraking) perfectAccel = maxBraking;
                    else if (perfectAccel < -maxBraking) perfectAccel = -maxBraking;
                    acceleration = perfectAccel;
                }
                if (velocity > maxVelocity || velocity < -maxVelocity) acceleration = 0.0;
                velocity += acceleration;
                if (velocity > maxVelocity) velocity -= Math.min(maxBraking, velocity - maxVelocity);
                if (velocity < -maxVelocity) velocity += Math.min(maxBraking, -velocity - maxVelocity);
                double dx = velocity * Math.sin(heading);
                double dy = velocity * Math.cos(heading);
                x += dx;
                y += dy;
                //boolean updateBounds = false;
                //if (dx != 0.0 || dy != 0.0) updateBounds = true;
                if (slowingDown && velocity == 0.0) {
                    distanceRemaining = 0.0;
                    moveDirection = 0;
                    slowingDown = false;
                    acceleration = 0.0;
                }
                //if (updateBounds) updateBoundingBox();
                distanceRemaining -= velocity;
                if (x < 17.9998 || y < 17.9998 || x > battleFieldW - 17.9998 || y > battleFieldH - 17.9998) {
                    // TODO: Is this really how Robocode handles wall collisions?
                    distanceRemaining = 0;
                    angleToTurn = 0;
                    velocity = 0;
                    moveDirection = 0;
                    x = Math.max(18, Math.min(battleFieldW - 18, x));
                    y = Math.max(18, Math.min(battleFieldH - 18, y));
                }
            }
            //add position
            pos[i] = new MovSimStat(x, y, velocity, heading, Utils.normalRelativeAngle(heading - lastHeading));
        }
        return pos;
    }

}
