package lxx.targeting;

import lxx.utils.LXXPoint;

/**
 * Created by IntelliJ IDEA.
 * User: jdev
 * Date: 09.10.2009
 * Time: 20:56:18
 * To change this template use File | Settings | File Templates.
 */
public class TargetUpdatedEvent {

    private long time;
    private LXXPoint position;
    private double heading;
    private double velocity;
    private double energy;

    public TargetUpdatedEvent(long time, LXXPoint position, double heading, double velocity, double energy) {
        this.time = time;
        this.position = position;
        this.heading = heading;
        this.velocity = velocity;
        this.energy = energy;
    }

    public long getTime() {
        return time;
    }

    public LXXPoint getPosition() {
        return position;
    }

    public double getHeading() {
        return heading;
    }

    public double getVelocity() {
        return velocity;
    }

    public double getEnergy() {
        return energy;
    }
}
