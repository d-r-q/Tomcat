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
            AttributesManager.enemySpeed,
            AttributesManager.enemyDistanceToForwardWall,
            AttributesManager.enemyBearingToForwardWall,
    };

    private static final Map<Attribute, Integer> mainDVRanges = LXXUtils.toMap(
            AttributesManager.enemyAcceleration, 0,
            AttributesManager.enemySpeed, 0,
            AttributesManager.enemyDistanceToForwardWall, 9,
            AttributesManager.enemyBearingToForwardWall, 11
    );
    private static final DataView mainDataView = new SingleSourceDataView(mainDVAttrs, mainDVRanges, 15);

    private static final Attribute[] asDVAttrs = {
            AttributesManager.enemyAcceleration,
            AttributesManager.enemySpeed,
            AttributesManager.enemyDistanceToForwardWall,
            AttributesManager.enemyBearingToMe,
            AttributesManager.firstBulletFlightTimeToEnemy,
            AttributesManager.enemyBearingOffsetOnFirstBullet,
            AttributesManager.enemyBearingOffsetOnSecondBullet,
    };
    private static final Map<Attribute, Integer> asDVRanges = LXXUtils.toMap(
            AttributesManager.enemyAcceleration, 0,
            AttributesManager.enemySpeed, 2,
            AttributesManager.enemyDistanceToForwardWall, 20,
            AttributesManager.enemyBearingToMe, 20,
            AttributesManager.firstBulletFlightTimeToEnemy, 1,
            AttributesManager.enemyBearingOffsetOnFirstBullet, 10,
            AttributesManager.enemyBearingOffsetOnSecondBullet, 15
    );
    private static final DataView asDataView = new SingleSourceDataView(asDVAttrs, asDVRanges, 4);

    private static final Attribute[] distanceDVAttrs = {
            AttributesManager.enemyAcceleration,
            AttributesManager.enemySpeed,
            AttributesManager.enemyDistanceToForwardWall,
            AttributesManager.enemyBearingToMe,
            AttributesManager.firstBulletFlightTimeToEnemy,
            AttributesManager.distBetween,
    };
    private static final Map<Attribute, Integer> distanceDVRanges = LXXUtils.toMap(
            AttributesManager.enemyAcceleration, 0,
            AttributesManager.enemySpeed, 2,
            AttributesManager.enemyDistanceToForwardWall, 20,
            AttributesManager.enemyBearingToMe, 20,
            AttributesManager.firstBulletFlightTimeToEnemy, 1,
            AttributesManager.distBetween, 50
    );
    private static final DataView distanceDataView = new SingleSourceDataView(distanceDVAttrs, distanceDVRanges, 10);

    private static final Attribute[] timeSinceDirChangeDVAttrs = {
            AttributesManager.enemyAcceleration,
            AttributesManager.enemySpeed,
            AttributesManager.enemyDistanceToForwardWall,
            AttributesManager.enemyBearingToMe,
            AttributesManager.firstBulletFlightTimeToEnemy,
            AttributesManager.enemyTimeSinceLastDirChange,
    };
    private static final Map<Attribute, Integer> timeSinceDirChangeDVRanges = LXXUtils.toMap(
            AttributesManager.enemyAcceleration, 0,
            AttributesManager.enemySpeed, 2,
            AttributesManager.enemyDistanceToForwardWall, 20,
            AttributesManager.enemyBearingToMe, 20,
            AttributesManager.firstBulletFlightTimeToEnemy, 1,
            AttributesManager.enemyTimeSinceLastDirChange, 2
    );
    private static final DataView timeSinceDirChangeDataView = new SingleSourceDataView(timeSinceDirChangeDVAttrs, timeSinceDirChangeDVRanges, 5);

    private static final CompositeDataView duelCompositeDataView = new CompositeDataView(mainDataView, asDataView, distanceDataView, timeSinceDirChangeDataView);

    private DataView[] views = {mainDataView, asDataView, distanceDataView, duelCompositeDataView, timeSinceDirChangeDataView};

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
