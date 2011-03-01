package lxx;

import lxx.movement.CornerManager;
import lxx.targeting.TargetManager;
import lxx.targeting.predict.PredictorManager;
import lxx.utils.APoint;
import lxx.utils.LXXConstants;
import lxx.utils.LXXPoint;
import lxx.utils.LXXRobot;
import lxx.wave.WaveManager;
import robocode.AdvancedRobot;
import robocode.BulletHitBulletEvent;
import robocode.BulletHitEvent;
import robocode.BulletMissedEvent;
import robocode.HitByBulletEvent;
import robocode.Rules;
import robocode.StatusEvent;
import robocode.util.Utils;

import java.awt.*;
import java.awt.geom.Point2D;
import static java.lang.Math.*;
import java.util.ArrayList;
import java.util.List;

/**
 * User: jdev
 * Date: 24.10.2009
 */
public class BasicRobot extends AdvancedRobot implements APoint, LXXRobot {

    public double width;
    public double height;

    public int others;
    public Rectangle battleField;

    protected final List<RobotListener> listeners = new ArrayList<RobotListener>();
    protected LXXPoint destination;
    protected TargetManager targetManager;
    protected CornerManager cornerManager;
    protected PredictorManager predictorManager;
    protected WaveManager waveManager = new WaveManager();
    protected double firePower;


    public void init() {
        battleField = new Rectangle(0, 0, (int)getBattleFieldWidth(), (int)getBattleFieldHeight());

        width = getWidth();
        height = getHeight();

        targetManager = new TargetManager();
        targetManager.endRound();
        cornerManager = new CornerManager(this, targetManager);

        predictorManager = new PredictorManager(targetManager, waveManager);
        predictorManager.onRoundStarted();

        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
    }

    public void goTo(double x, double y, double distance) {
        double alpha = angle(getX(), getY(), x, y);
        alpha = normalizeBearing(alpha - getHeadingRadians());

        if (Double.isNaN(alpha)) {
            alpha = 0;
        }
        if ((Math.abs(alpha) > LXXConstants.RADIANS_30) && (Math.abs(alpha) < LXXConstants.RADIANS_150)) {
            setMaxVelocity((10 / 0.75) - Math.min(Math.abs(toDegrees(alpha)), Rules.MAX_TURN_RATE) / 1.5);
        } else {
            setMaxVelocity(Rules.MAX_VELOCITY);
        }
        if (distance < 2) {
            return;
        }
        if (abs(alpha) > PI / 2) {
            setTurnLeftRadians(alpha - PI / 2 * signum(alpha));
            setBack(distance);
        } else {
            setTurnRightRadians(alpha);
            setAhead(distance);
        }

    }

    public void goTo(LXXPoint absolutePoint) {
        goTo(absolutePoint.x, absolutePoint.y, distance(absolutePoint));
    }

    public double angle(double x1, double y1, double x2, double y2) {
        double xo = x2 - x1;
        double yo = y2 - y1;
        double hyp = Point2D.distance(x1, y1, x2, y2);
        double arcSin = Math.asin(xo / hyp);
        double bearing = 0;

        if (xo >= 0 && yo > 0) {
            bearing = arcSin;
        } else if (xo < 0 && yo > 0) {
            bearing = PI * 2 + arcSin;
        } else if (xo >= 0 && yo < 0) {
            bearing = PI - arcSin;
        } else if (xo < 0 && yo < 0) {
            bearing = PI - arcSin;
        }

        return bearing;
    }

    public double angleTo(double x, double y) {
        return angle(getX(), getY(), x, y);
    }

    public double angleTo(APoint point) {
        return angleTo(point.getX(), point.getY());
    }

    public double angleDegrees(double x, double y) {
        return toDegrees(angle(getX(), getY(), x, y));
    }

    public double normalizeBearing(double angle) {
        while (angle > PI) {
            angle -= PI * 2;
        }
        while (angle < -PI) {
            angle += PI * 2;
        }

        return angle;
    }

    public double distance(double x1, double y1, double x2, double y2) {
        return new LXXPoint(x1, y1).distance(x2, y2);
    }

    public double aDistance(APoint p) {
        return p.distance(getX(), getY());
    }

    public double distance(double x, double y) {
        LXXPoint base = new LXXPoint(getX(), getY());
        return new LXXPoint(x, y).distance(base.getX(), base.getY());
    }

    public double distance(APoint p) {
        return new LXXPoint(p.getX(), p.getY()).distance(getX(), getY());
    }

    public LXXPoint getCoords(LXXPoint init, double alpha, double distance) {
        double x = init.x + Math.sin(alpha) * distance;
        double y = init.y + Math.cos(alpha) * distance;

        return new LXXPoint(x, y);
    }

    public LXXPoint getCoords(double alpha, double distance) {
        return getCoords(new LXXPoint(getX(), getY()), alpha, distance);
    }

    public LXXPoint getAbsolutePoint(LXXPoint p) {
        LXXPoint res = new LXXPoint(getX(), getY());

        res.x += p.x;
        res.y += p.y;

        return res;
    }

    public void onStatus(StatusEvent event) {
        others = event.getStatus().getOthers();
    }

    public int getOthers() {
        return others;
    }

    public Rectangle getBattleField() {
        return battleField;
    }

    public LXXPoint getPosition() {
        return new LXXPoint((int)getX(), (int)getY());
    }

    public void onBulletHitBullet(BulletHitBulletEvent event) {
        for (RobotListener rl : listeners) {
            rl.onBulletHitBullet(event);
        }
    }

    public void onHitByBullet(HitByBulletEvent event) {
        for (RobotListener rl : listeners) {
            rl.onHitByBullet(event);
        }
    }

    public void onBulletHit(BulletHitEvent event) {
        for (RobotListener rl : listeners) {
            rl.onBulletHit(event);
        }
    }

    public void onBulletMissed(BulletMissedEvent event) {
        for (RobotListener rl : listeners) {
            rl.onBulletMissed(event);
        }
    }

    

    public void addListener(RobotListener listener) {
        listeners.add(listener);
    }

    public void removeListener(RobotListener listener) {
        listeners.remove(listener);
    }

    public LXXPoint getDestination() {
        return destination;
    }

    /**
     * RETURN HEADING IN RADIANS!
     *
     * @return HEADING IN RADIANS!
     */
    public double getHeading() {
        return getHeadingRadians();
    }

    public double getAbsoluteHeading() {
        if (getVelocity() >= 0) {
            return getHeading();
        } else {
            return Utils.normalAbsoluteAngle(getHeading() - Math.PI);
        }
    }

    public TargetManager getTargetManager() {
        return targetManager;
    }

    public CornerManager getCornerManager() {
        return cornerManager;
    }

    public double getBattleFieldDiag() {
        return new Point().distance(battleField.width, battleField.height);
    }

    public WaveManager getWaveManager() {
        return waveManager;
    }

    public double firePower() {
        return firePower;
    }
}