package lxx.wave;

import lxx.utils.LXXRobot;
import lxx.utils.APoint;
import lxx.utils.LXXPoint;import static java.lang.Math.sin;
import static java.lang.Math.cos;
import static java.lang.Math.abs;

/**
 * User: jdev
 * Date: 07.11.2009
 */
public class Wave {
    
    public final LXXRobot source;
    public final APoint sourcePos;

    public final LXXRobot target;
    public final APoint targetPos;
    public final double targetHeading;
    public final double targetVelocity;

    public final long launchTime;
    public final double speed;
    public final double heading;

    public final WaveCallback callback;
    public final double targetEnergy;

    public Wave(WaveCallback callback, LXXRobot source, LXXRobot target, long launchTime, double speed, double heading) {
        this.callback = callback;
        this.source = source;
        this.sourcePos = new LXXPoint(source.getPosition().getX() + sin(source.getHeading()) * (source.getVelocity()),
                source.getPosition().getY() + cos(source.getHeading()) * (source.getVelocity()));
        this.target = target;
        this.targetPos = new LXXPoint(target.getX(), target.getY());
        this.targetHeading = target.getAbsoluteHeading();
        this.targetVelocity = target.getVelocity();
        this.targetEnergy = target.getEnergy();

        this.launchTime = launchTime;
        this.speed = speed;
        this.heading = heading;
    }

    public double distance(APoint pos) {
        return sourcePos.aDistance(pos) - getTraveledDistance();
    }

    public double getTraveledDistance() {
        return (source.getTime() - launchTime + 1) * speed;
    }

    public boolean check() {
        return sourcePos.aDistance(target) < getTraveledDistance(); 
    }

    public APoint getSourcePos() {
        return sourcePos;
    }

    public APoint getTargetPos() {
        return targetPos;
    }

    public LXXRobot getTarget() {
        return target;
    }

    public WaveCallback getCallback() {
        return callback;
    }

    public LXXRobot getSource() {
        return source;
    }
}
