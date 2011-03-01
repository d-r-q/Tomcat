package lxx.utils;

import lxx.StaticData;
import static lxx.StaticData.robot;

import java.awt.geom.Point2D;
import java.awt.geom.AffineTransform;
import static java.lang.StrictMath.signum;
import static java.lang.Math.sin;
import static java.lang.Math.cos;
import static java.lang.Math.abs;

/**
 * User: jdev
 * Date: 27.02.2010
 */
public enum Wall {

    /*LEFT(new LXXPoint(0, 0), new LXXPoint(0, robot.getBattleFieldHeight()), Direction.VERTICAL, LXXConstants.RADIANS_90),
    TOP(LEFT.to, new LXXPoint(robot.getBattleFieldWidth(), robot.getBattleFieldHeight()), Direction.HORIZONTAL, LXXConstants.RADIANS_180),
    RIGHT(new LXXPoint(robot.getBattleFieldWidth(), 0), TOP.to, Direction.VERTICAL, LXXConstants.RADIANS_270),
    BOTTOM(LEFT.from, RIGHT.from , Direction.HORIZONTAL, LXXConstants.RADIANS_360);*/

    LEFT(new LXXPoint(0, 0), new LXXPoint(0, robot.getBattleFieldHeight()), Direction.VERTICAL, LXXConstants.RADIANS_270),
    TOP(LEFT.to, new LXXPoint(robot.getBattleFieldWidth(), robot.getBattleFieldHeight()), Direction.HORIZONTAL, LXXConstants.RADIANS_0),
    RIGHT(TOP.to, new LXXPoint(robot.getBattleFieldWidth(), 0), Direction.VERTICAL, LXXConstants.RADIANS_90),
    BOTTOM(RIGHT.to, LEFT.from, Direction.HORIZONTAL, LXXConstants.RADIANS_180);

    static final Wall[] walls = {LEFT, TOP, RIGHT, BOTTOM};

    private final LXXPoint from;
    private final LXXPoint to;
    private final Direction direction;
    private final Double perpendicular;
    private AffineTransform translateTransformation;
    private AffineTransform rotateTransformation;
    private static LXXPoint corner1 = new LXXPoint(0, 0);
    private static LXXPoint corner2 = new LXXPoint(0, robot.getBattleFieldHeight());
    private static LXXPoint corner3 = new LXXPoint(robot.getBattleFieldWidth(), robot.getBattleFieldHeight());
    private static LXXPoint corner4 = new LXXPoint(0, robot.getBattleFieldHeight());

    Wall(LXXPoint from, LXXPoint to, Direction direction, Double perpendicular) {
        this.from = from;
        this.to = to;
        this.direction = direction;
        this.perpendicular = perpendicular;

        translateTransformation = AffineTransform.getTranslateInstance(-this.from.getX(), -this.from.getY());
        rotateTransformation = AffineTransform.getRotateInstance(perpendicular);
    }

    public LXXPoint translate(LXXPoint pnt) {
        Point2D.Double res = new Point2D.Double();
        res = (Point2D.Double) translateTransformation.transform(pnt, res);
        res = (Point2D.Double) rotateTransformation.transform(res, res);
        return new LXXPoint(res);
    }

    public boolean intersects(LXXPoint from, LXXPoint to) {
        from = translate(from);
        to = translate(to);

        if (signum(from.getY()) == signum(to.getY())) {
            return false;
        }

        final double intersectionX = getIntersectionX(from, to);
        return intersectionX < translate(this.to).getX() && intersectionX > 0;
    }

    private double getIntersectionX(LXXPoint from, LXXPoint to) {
        double scale = abs(from.getY()) / abs(from.getY() - to.getY());
        return from.getX() + (to.getX() - from.getX()) * scale;
    }

    public double getDistanceTo(LXXPoint pos, double heading) {
        LXXPoint to = new LXXPoint(pos.getX() + sin(heading) * 2000,
                pos.getY() + cos(heading) * 2000);
        pos = translate(pos);
        to = translate(to);

        final double intersectionX = getIntersectionX(pos, to);

        return pos.distance(intersectionX, 0);
    }

    public static Wall getWall(LXXPoint pos, double heading) {
        if (robocode.util.Utils.normalRelativeAngle(Utils.angle(pos, corner1) - heading) <= 0) {
            if (robocode.util.Utils.normalRelativeAngle(Utils.angle(pos, corner3) - heading) <= 0) {
                return BOTTOM;
            } else {
                return RIGHT;
            }
        } else {
            if (robocode.util.Utils.normalRelativeAngle(Utils.angle(pos, corner2) - heading) <= 0) {
                return LEFT;
            } else {
                return TOP;
            }
        }
        /*LXXPoint to = new LXXPoint(pos.getX() + sin(heading) * 2000,
                pos.getY() + cos(heading) * 2000);

        Wall res = null;
        for (Wall w : Wall.walls) {
            if (w.intersects(pos, to)) {
                if (res != null) {
                    LXXPoint p1 = w.translate(pos);
                    LXXPoint t1 = w.translate(to);

                    LXXPoint p2 = res.translate(pos);
                    LXXPoint t2 = res.translate(to);
                    throw new RuntimeException("Somethong wrong: " + pos + ", " + to + ", " + res + ", " + w + ", " +
                            res.getIntersectionX(p2, t2) + ", " + w.getIntersectionX(p1, t1));
                }
                res = w;
            }
        }
        if (res == null) {
            throw new RuntimeException("Somethong wrong: " + pos + ", " + to);
        }*/
    }

    public static void main(String[] args) {
        LXXPoint pnt1 = new LXXPoint(10, 10);
        LXXPoint pnt2 = new LXXPoint(300, -20);
        assert BOTTOM.intersects(pnt1, pnt2);

        pnt1 = new LXXPoint(10, 10);
        pnt2 = new LXXPoint(-10, -20);
        assert !BOTTOM.intersects(pnt1, pnt2);

        pnt1 = new LXXPoint(10, 10);
        pnt2 = new LXXPoint(900, 0);
        assert !BOTTOM.intersects(pnt1, pnt2);

        pnt1 = new LXXPoint(10, 10);
        pnt2 = new LXXPoint(790, -1);
        assert BOTTOM.intersects(pnt1, pnt2);

        pnt1 = new LXXPoint(10, 10);
        pnt2 = new LXXPoint(300, -20);
        assert !LEFT.intersects(pnt1, pnt2);

        pnt1 = new LXXPoint(10, 10);
        pnt2 = new LXXPoint(-10, -20);
        assert LEFT.intersects(pnt1, pnt2);

        pnt1 = new LXXPoint(10, 10);
        pnt2 = new LXXPoint(900, 0);
        assert RIGHT.intersects(pnt1, pnt2);

        pnt1 = new LXXPoint(10, 10);
        pnt2 = new LXXPoint(790, -1);
        assert !TOP.intersects(pnt1, pnt2);
    }

    public LXXPoint getFrom() {
        return from;
    }

    public LXXPoint getTo() {
        return to;
    }
}
