package lxx.targeting.cgf;

import lxx.targeting.predict.Predictor;
import lxx.targeting.Target;
import lxx.utils.LXXPoint;
import lxx.wave.WaveManager;
import lxx.wave.WaveCallback;
import lxx.wave.Wave;
import static lxx.StaticData.robot;

import java.awt.*;
import static java.lang.Math.sin;
import static java.lang.Math.cos;

import robocode.util.Utils;

/**
 * User: jdev
 * Date: 12.11.2009
 */
public class ClassicGFPredictor implements Predictor, WaveCallback {

    private final WaveManager waveManager;
    private long[][][] segments = new long[13][17][31];

    public ClassicGFPredictor(WaveManager waveManager) {
        this.waveManager = waveManager;
    }

    public Double predictAngle(Target t) {
        int bestindex = 15;	// initialize it to be in the middle, guessfactor 0.
        double dist = robot.aDistance(t);
        long[] currentStats = segments[((int) dist / 100)][((int) (t.getVelocity() + 8))];
        for (int i=0; i<31; i++)
			if (currentStats[bestindex] < currentStats[i])
				bestindex = i;

		//this should do the opposite of the math in the WaveBullet:
		double guessfactor = (double)(bestindex - (currentStats.length - 1) / 2) / ((currentStats.length - 1) / 2);
        int direction = 1;
        double absBearing = robot.angleTo(t);
        if (t.getVelocity() != 0) {
            if (Math.sin(t.getHeading() - absBearing) * t.getVelocity() < 0)
                direction = -1;
            else
                direction = 1;
        }
		double angleOffset = direction * guessfactor * maxEscapeAngle();
        return Utils.normalRelativeAngle(absBearing + angleOffset);
    }

    public void paint(Graphics2D g, Target t) {
    }

    public void onRoundStarted() {
    }

    public void targetUpdated(Target oldState, Target newState, robocode.Event source) {
        waveManager.launchWave(robot, newState, robot.getTime(), robot.angleTo(newState), 3, this);
    }

    private double getBulletSpeed(int power) {
        return 20 - power * 3;
    }

    private double maxEscapeAngle() {
        return Math.asin(8 / getBulletSpeed(1));
    }


    public void waveBroken(Wave w) {
        double desiredDirection = Math.atan2(w.getTarget().getX() - w.getTargetPos().getX(),
                w.getTarget().getY() - w.getTargetPos().getY());
        double absBearing = robot.angleTo(w.getTargetPos());
        double angleOffset = Utils.normalRelativeAngle(desiredDirection -
                absBearing);
        int direction = 1;
        if (w.targetVelocity != 0) {
            if (Math.sin(w.targetHeading - absBearing) * w.targetVelocity < 0)
                direction = -1;
            else
                direction = 1;
        }

        double guessFactor =
                Math.max(-1, Math.min(1, angleOffset / maxEscapeAngle())) * direction;
        long[] segment = segments[((int) (w.sourcePos.aDistance(w.targetPos) / 100))][((int) (w.getTarget().getVelocity() + 8))];
        int index = (int) Math.round((segment.length - 1) / 2 * (guessFactor + 1));
        segment[index]++;
    }

    public String getName() {
        return getClass().getSimpleName();
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClassicGFPredictor that = (ClassicGFPredictor) o;

        return !(getName() != null ? !getName().equals(that.getName()) : that.getName() != null);

    }

    public int hashCode() {
        return (getName() != null ? getName().hashCode() : 0);
    }
}
