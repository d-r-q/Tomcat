package lxx.targeting.tomcat_claws.data_analise;

import lxx.RobotListener;
import lxx.events.TickEvent;
import lxx.targeting.TargetManager;
import lxx.ts_log.TurnSnapshot;
import lxx.ts_log.TurnSnapshotsLog;
import lxx.ts_log.attributes.Attribute;
import lxx.ts_log.attributes.AttributesManager;
import lxx.targeting.Target;
import lxx.utils.LXXUtils;
import robocode.Event;

import java.util.Map;

/**
 * User: jdev
 * Date: 17.06.11
 */
public class DataViewManager implements RobotListener {

    private static final Attribute[] mainDVAttrs = {
            AttributesManager.enemyAcceleration,
            AttributesManager.enemyVelocityModule,
            AttributesManager.enemyDistanceToForwardWall,
            AttributesManager.enemyBearingToForwardWall,
    };

    private static final Map<Attribute, Integer> mainDVRanges = LXXUtils.toMap(
            AttributesManager.enemyAcceleration, 0,
            AttributesManager.enemyVelocityModule, 0,
            AttributesManager.enemyDistanceToForwardWall, 9,
            AttributesManager.enemyBearingToForwardWall, 11
    );
    private static final DataView mainDataView = new SingleSourceDataView(mainDVAttrs, mainDVRanges, 35);

    private static final Attribute[] randomDVAttrs = {
            AttributesManager.enemyAcceleration,
            AttributesManager.enemyVelocityModule,
            AttributesManager.enemyDistanceToForwardWall,
            AttributesManager.enemyBearingToForwardWall,
            AttributesManager.firstBulletFlightTimeToEnemy,
    };

    private static final Map<Attribute, Integer> randomDVRanges = LXXUtils.toMap(
            AttributesManager.enemyAcceleration, 0,
            AttributesManager.enemyVelocityModule, 1,
            AttributesManager.enemyDistanceToForwardWall, 20,
            AttributesManager.enemyBearingToForwardWall, 20,
            AttributesManager.firstBulletFlightTimeToEnemy, 2
    );
    private static final DataView randomDataView = new SingleSourceDataView(randomDVAttrs, randomDVRanges, 10);

    private static final CompositeDataView duelCompositeDataView = new CompositeDataView(randomDataView);

    private DataView[] views = {mainDataView, randomDataView, duelCompositeDataView};

    private final TargetManager targetManager;
    private final TurnSnapshotsLog turnSnapshotLog;

    public DataViewManager(TargetManager targetManager, TurnSnapshotsLog turnSnapshotLog) {
        this.targetManager = targetManager;
        this.turnSnapshotLog = turnSnapshotLog;
    }

    public void onEvent(Event event) {
        if (event instanceof TickEvent) {
            for (Target t : targetManager.getAliveTargets()) {
                final TurnSnapshot lastSnapshot = turnSnapshotLog.getLastSnapshot(t);
                for (DataView view : views) {
                    view.addEntry(lastSnapshot);
                }
            }
        }
    }

    public DataView getDuelDataView() {
        return duelCompositeDataView;
    }

}
