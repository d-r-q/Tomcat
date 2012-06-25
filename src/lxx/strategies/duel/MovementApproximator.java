package lxx.strategies.duel;

import ags.utils.KdTree;
import lxx.LXXRobotSnapshot;
import lxx.RobotListener;
import lxx.data_analysis.kd_tree.KdTreeAdapter;
import lxx.events.TickEvent;
import lxx.strategies.MovementDecision;
import lxx.targeting.Target;
import lxx.targeting.TargetManager;
import lxx.targeting.TargetManagerListener;
import lxx.ts_log.TurnSnapshot;
import lxx.ts_log.TurnSnapshotsLog;
import lxx.ts_log.attributes.AttributesManager;
import robocode.Event;
import robocode.util.Utils;

import java.util.List;

import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;

/**
 * User: Aleksey Zhidkov
 * Date: 25.06.12
 */
public class MovementApproximator implements TargetManagerListener {

    private static final KdTree<TurnSnapshot> tree = new KdTree.SqrEuclid<TurnSnapshot>(6, 50000);

    private final TurnSnapshotsLog log;

    public MovementApproximator(TurnSnapshotsLog log) {
        this.log = log;
    }

    public MovementDecision getMovementDecision(LXXRobotSnapshot me, LXXRobotSnapshot enemy) {
        final double[] loc = {
                me.aDistance(enemy),
                enemy.getSpeed(),
                enemy.getAcceleration(),
                enemy.getPosition().distanceToWall(enemy.getBattleField(), enemy.getAbsoluteHeadingRadians()),
                toDegrees(Utils.normalRelativeAngle(enemy.angleTo(me) - enemy.getAbsoluteHeadingRadians())),
                toDegrees(Utils.normalRelativeAngle(me.angleTo(enemy) - me.getAbsoluteHeadingRadians()))
        };
        final List<KdTree.Entry<TurnSnapshot>> entries = tree.nearestNeighbor(loc, 1, false);
        if (entries.size() == 0) {
            return new MovementDecision(enemy.getVelocity(), 0);
        }
        final TurnSnapshot ts = entries.get(0).value;
        return new MovementDecision(ts.next.enemySnapshot.getVelocity(),
                Utils.normalRelativeAngle(ts.next.enemySnapshot.getAbsoluteHeadingRadians() -
                        ts.enemySnapshot.getAbsoluteHeadingRadians()));
    }

    public void targetUpdated(Target target) {
        final TurnSnapshot ts = log.getLastSnapshot(target, 1);
        if (ts != null) {
            tree.addPoint(getLocation(ts), ts);
        }
    }

    private double[] getLocation(TurnSnapshot ts) {
        return new double[]{
                ts.getAttrValue(AttributesManager.distBetween),
                ts.getAttrValue(AttributesManager.enemySpeed),
                ts.getAttrValue(AttributesManager.enemyAcceleration),
                ts.getAttrValue(AttributesManager.enemyDistanceToForwardWall),
                ts.getAttrValue(AttributesManager.enemyBearingToMe),
                toDegrees(Utils.normalRelativeAngle(ts.mySnapshot.angleTo(ts.enemySnapshot) - ts.mySnapshot.getAbsoluteHeadingRadians()))
        };
    }

}
