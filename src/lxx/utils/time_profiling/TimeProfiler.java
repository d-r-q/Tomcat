/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.utils.time_profiling;

import lxx.RobotListener;
import lxx.events.TickEvent;
import lxx.utils.ValueInfo;
import robocode.DeathEvent;
import robocode.Event;
import robocode.SkippedTurnEvent;
import robocode.WinEvent;

import java.util.LinkedList;

import static java.lang.Math.max;

/**
 * User: jdev
 * Date: 10.12.11
 */
public class TimeProfiler implements RobotListener {

    private static final TimeProfileProperties[] registeredProperties = TimeProfileProperties.values();

    private static int maxPropertyNameLength = 0;

    private static final ValueInfo[] battleProfile = new ValueInfo[TimeProfileProperties.values().length];

    static {
        final TimeProfileProperties[] values = TimeProfileProperties.values();
        for (int i = 0; i < values.length; i++) {
            battleProfile[i] = new ValueInfo(50000);
            maxPropertyNameLength = max(maxPropertyNameLength, values[i].name.length());
        }
    }

    private static final LinkedList<ValueInfo[]> roundProfiles = new LinkedList<ValueInfo[]>();

    private static final ValueInfo[] turnProfile = new ValueInfo[TimeProfileProperties.values().length];
    private static final String ROUND_TIME_PROFILE_NAME = "== Round Time Profile ==";
    private static final String BATTLE_TIME_PROFILE_NAME = "== Battle Time Profile ==";

    public TimeProfiler() {
        final ValueInfo[] roundProfile = new ValueInfo[TimeProfileProperties.values().length];
        for (int i = 0; i < TimeProfileProperties.values().length; i++) {
            roundProfile[i] = new ValueInfo(3000);
        }
        roundProfiles.add(roundProfile);
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof TickEvent) {
            for (int i = 0; i < TimeProfileProperties.values().length; i++) {
                turnProfile[i] = new ValueInfo(300);
            }
        } else if (event instanceof SkippedTurnEvent) {
            printProfiles();
        } else if (event instanceof DeathEvent || event instanceof WinEvent) {
            printProfile(ROUND_TIME_PROFILE_NAME, roundProfiles.getLast());
            printProfile(BATTLE_TIME_PROFILE_NAME, battleProfile);
        }
    }

    public void stopAndSaveProperty(TimeProfileProperties property) {
        if (property.getStartTime() == -1) {
            throw new IllegalStateException("Start was not called");
        }
        property.stop();
        final long lastTime = property.getLastTime();
        battleProfile[property.idx].addValue(lastTime);
        roundProfiles.getLast()[property.idx].addValue(lastTime);
        turnProfile[property.idx].addValue(lastTime);
    }

    private void printProfiles() {
        // printProfile(BATTLE_TIME_PROFILE_NAME, battleProfile);
        // printProfile(ROUND_TIME_PROFILE_NAME, roundProfiles.getLast());
        printProfile("== Turn Time Profile ==", turnProfile);
    }

    private void printProfile(String profileName, ValueInfo[] profile) {
        System.out.println(profileName);
        for (TimeProfileProperties property : TimeProfileProperties.values()) {
            System.out.printf("  - %" + maxPropertyNameLength + "s: %s\n", property.name, profile[property.idx].toString());
        }
    }

}
