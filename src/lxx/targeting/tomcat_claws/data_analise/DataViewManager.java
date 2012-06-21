/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.targeting.tomcat_claws.data_analise;

import lxx.RobotListener;
import lxx.events.TickEvent;
import lxx.targeting.Target;
import lxx.targeting.TargetManager;
import lxx.ts_log.TurnSnapshot;
import lxx.ts_log.TurnSnapshotsLog;
import lxx.ts_log.attributes.Attribute;
import lxx.ts_log.attributes.AttributesManager;
import robocode.Event;

/**
 * User: jdev
 * Date: 17.06.11
 */
public class DataViewManager implements RobotListener {

    private static final DataView mainDataView = new SingleSourceDataView(new Attribute[]{
            AttributesManager.enemyAcceleration,
            AttributesManager.enemySpeed,
            AttributesManager.enemyDistanceToForwardWall,
            AttributesManager.enemyDistanceToReverseWall,
            AttributesManager.enemyTimeSinceLastDirChange,
            AttributesManager.enemyTurnSign,
            AttributesManager.enemyBearingToMe,
            AttributesManager.enemyLast10TicksDist
    }, "Main", SingleSourceDataView.TimeDependencyType.NO, 50000);

    private static final DataView asDataView = new SingleSourceDataView(new Attribute[]{
            AttributesManager.distBetween,
            AttributesManager.enemySpeed,
            AttributesManager.enemyBearingToMe,
            AttributesManager.enemyDistanceToForwardWall,
            AttributesManager.enemyDistanceToReverseWall,
            AttributesManager.enemyBearingOffsetOnFirstBullet,
            AttributesManager.fireTimeDiff,
            AttributesManager.enemyLateralDirection
    }, "Anti-surfer", SingleSourceDataView.TimeDependencyType.DIRECT_HITS, 20000);

    private static final DataView afDataView = new SingleSourceDataView(new Attribute[]{
            AttributesManager.distBetween,
            AttributesManager.enemySpeed,
            AttributesManager.enemyBearingToMe,
            AttributesManager.enemyDistanceToForwardWall,
            AttributesManager.enemyDistanceToReverseWall,
            AttributesManager.enemyBearingOffsetOnFirstBullet,
            AttributesManager.fireTimeDiff,
            AttributesManager.enemyLateralDirection
    }, "Anti-flattener", SingleSourceDataView.TimeDependencyType.REVERCE_WAVES, 50000);

    private DataView[] duelViews = {mainDataView, asDataView, afDataView};

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
                for (DataView view : duelViews) {
                    view.addEntry(lastSnapshot);
                }
            }
        }
    }

    public DataView[] getDuelDataViews() {
        return duelViews;
    }

}
