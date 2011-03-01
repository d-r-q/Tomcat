package lxx.movement.anti_grav;

import lxx.utils.LXXPoint;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import static java.lang.Math.signum;

/**
 * User: jdev
 * Date: 25.07.2009
 */
public class AntiGravModel {

    public static final double FORCE_MULTIPLECTOR = 50;

    private final Robot owener;

    private final List<OldingPoint> oldingPoints = new ArrayList<OldingPoint>();

    public AntiGravModel(Robot owener) {
        this.owener = owener;
    }

    public LXXPoint calculateTotalPower() {
        LXXPoint totalForce = new LXXPoint();
        /*for (GravitationPoint gravPoint : getGravitationPoints(null)) {
            if (gravPoint.power == 0) {
                continue;
            }

            LXXPoint force = calculateForce(gravPoint);
            totalForce.x += force.x;
            totalForce.y += force.y;
        }*/

        totalForce.x *= FORCE_MULTIPLECTOR;
        totalForce.y *= FORCE_MULTIPLECTOR;
        //tick();
        return totalForce;
    }

    private LXXPoint calculateForce(GravitationPoint gravPoint) {
        /*if (owener.distance(gravPoint) > gravPoint.effectiveDistance && gravPoint.inner ||
                owener.distance(gravPoint) < gravPoint.effectiveDistance && !gravPoint.inner) {
            return new LXXPoint(0, 0);
        }*/

        double alpha;
        LXXPoint force = new LXXPoint();
        //alpha = owener.angleTo(gravPoint.x, gravPoint.y);

        double distanceKoef = 1;
        //double distance = owener.distance(gravPoint);

        if (gravPoint.inner) {
            //distanceKoef = 1 / distance;
        } else {
            distanceKoef = 1;
        }

        /*force.x = Math.sin(alpha) * gravPoint.power * distanceKoef;
        force.y = Math.cos(alpha) * gravPoint.power * distanceKoef*/;

        return force;
    }

    /*private List<GravitationPoint> getGravitationPoints(Collection<Target> targets) {
        List<GravitationPoint> res = new ArrayList<GravitationPoint>();

        res.add(new GravitationPoint(0, owener.getY(), GravitationPoint.DEFAULT_POWER * -1, 200));
        res.add(new GravitationPoint(owener.getX(), owener.fHeight, GravitationPoint.DEFAULT_POWER * -1, 200));
        res.add(new GravitationPoint(owener.fWidth, owener.getY(), GravitationPoint.DEFAULT_POWER * -1, 200));
        res.add(new GravitationPoint(owener.getX(), 0, GravitationPoint.DEFAULT_POWER * -1, 200));

        for (Target t : targets) {
            LXXPoint targetPos = t.getPosition();
            res.add(new GravitationPoint(targetPos, GravitationPoint.DEFAULT_POWER * -1, 200));
        }

        res.addAll(oldingPoints);

        return res;
    }

    public void paint(Graphics2D g) {
        List<GravitationPoint> gravPoints = getGravitationPoints(targetManager.getAliveTargets());
        g.setColor(Color.RED);
        final int pointRadius = 8;
        for (GravitationPoint gp : gravPoints) {
            if (gp.power == 0) {
                continue;
            }

            LXXPoint force = calculateForce(gp);
            force.x *= FORCE_MULTIPLECTOR;
            force.y *= FORCE_MULTIPLECTOR;

            if (gp.power < 0) {
                g.setColor(Color.RED);
            } else {
                g.setColor(Color.BLUE);
            }
            g.fillOval((int) (gp.x - pointRadius / 2), ((int) gp.y - pointRadius / 2), pointRadius, pointRadius);
            g.drawLine((int) gp.x, (int) gp.y, (int) (gp.x + force.x), (int) (gp.y + force.y));
        }

        g.setColor(Color.GREEN);
        LXXPoint totalForce = calculateTotalPower();
        double x = owener.getX();
        double y = owener.getY();
        double alpha = owener.angleTo(x + totalForce.x, y + totalForce.y);
        int x2 = (int) Math.round(x + Math.sin(alpha) * 100D);
        int y2 = (int) Math.round(y + Math.cos(alpha) * 100D);
        g.drawLine((int) x, (int) y, x2, y2);

        g.fillOval((int) (x - pointRadius / 2), ((int) y - pointRadius / 2), pointRadius, pointRadius);
    }

    public void addOldingPoint(OldingPoint oldingPoint) {
        oldingPoints.add(oldingPoint);
    }

    public void endRaund() {
        oldingPoints.clear();
    }

    public void tick() {
        for (Iterator<OldingPoint> i = oldingPoints.iterator(); i.hasNext();) {
            OldingPoint op = i.next();
            if (op.power == 0) {
                i.remove();
                continue;
            }
            double s = -signum(op.power);
            op.power += s * op.oldingRate;
            if (s == signum(op.power)) {
                i.remove();
            }
        }
    }*/

}
