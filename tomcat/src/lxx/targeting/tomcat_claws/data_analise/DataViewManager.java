package lxx.targeting.tomcat_claws.data_analise;

import lxx.RobotListener;
import lxx.events.TickEvent;
import lxx.model.attributes.Attribute;
import lxx.office.AttributesManager;
import lxx.office.TargetManager;
import lxx.office.TurnSnapshotsLog;
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

    private static final DataView mainDataView = new SingleSourceDataView(mainDVAttrs, mainDVRanges);

    private DataView[] views = {mainDataView};

    private final TargetManager targetManager;
    private final TurnSnapshotsLog turnSnapshotLog;

    public DataViewManager(TargetManager targetManager, TurnSnapshotsLog turnSnapshotLog) {
        this.targetManager = targetManager;
        this.turnSnapshotLog = turnSnapshotLog;
    }

    public void onEvent(Event event) {
        if (event instanceof TickEvent) {
            for (Target t : targetManager.getAliveTargets()) {
                for (DataView view : views) {
                    view.addEntry(turnSnapshotLog.getLastSnapshot(t));
                }
            }
        }
    }

    public DataView getDuelDataView() {
        return mainDataView;
    }

}
