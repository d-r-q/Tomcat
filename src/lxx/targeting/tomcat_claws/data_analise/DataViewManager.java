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
            AttributesManager.enemyBearingToForwardWall,
    }, new double[]{0.25, 0.75});

    private static final DataView asDataView = new SingleSourceDataView(new Attribute[]{
            AttributesManager.enemyAcceleration,
            AttributesManager.enemySpeed,
            AttributesManager.enemyDistanceToForwardWall,
            AttributesManager.enemyBearingToMe,
            AttributesManager.firstBulletFlightTimeToEnemy,
            AttributesManager.enemyBearingOffsetOnFirstBullet,
            AttributesManager.enemyBearingOffsetOnSecondBullet,
    }, new double[]{0.75, 0.25});

    private static final DataView asDataView2 = new SingleSourceDataView(new Attribute[]{
            AttributesManager.enemyAcceleration,
            AttributesManager.enemySpeed,
            AttributesManager.enemyDistanceToForwardWall,
            AttributesManager.enemyBearingToMe,
            AttributesManager.firstBulletFlightTimeToEnemy,
            AttributesManager.lastVisitedGF1,
            AttributesManager.lastVisitedGF2,
    }, new double[]{0.75, 0.25});

    private static final DataView distanceDataView = new SingleSourceDataView(new Attribute[]{
            AttributesManager.enemyAcceleration,
            AttributesManager.enemySpeed,
            AttributesManager.enemyDistanceToForwardWall,
            AttributesManager.enemyBearingToMe,
            AttributesManager.firstBulletFlightTimeToEnemy,
            AttributesManager.distBetween,
            AttributesManager.enemyTurnRate
    }, new double[]{0.5, 0.5});

    private static final DataView timeSinceDirChangeDataView = new SingleSourceDataView(new Attribute[]{
            AttributesManager.enemyAcceleration,
            AttributesManager.enemySpeed,
            AttributesManager.enemyDistanceToForwardWall,
            AttributesManager.enemyBearingToMe,
            AttributesManager.firstBulletFlightTimeToEnemy,
            AttributesManager.enemyTimeSinceLastDirChange,
            AttributesManager.enemyTurnRate
    }, new double[]{0.5, 0.5});

    private static final CompositeDataView duelCompositeDataView = new CompositeDataView(mainDataView, asDataView, asDataView2, distanceDataView, timeSinceDirChangeDataView);

    private DataView[] views = {mainDataView, asDataView, asDataView2, distanceDataView, duelCompositeDataView, timeSinceDirChangeDataView};

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
