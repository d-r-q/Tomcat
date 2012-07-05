/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.utils.time_profiling;

import lxx.utils.ValueInfo;

import static java.lang.StrictMath.max;

/**
 * User: jdev
 * Date: 10.12.11
 */
public enum TimeProfile {

    TURN_TIME,
    PROCESS_LISTENERS_TIME,
    EBM_WAVE_TIME,
    SELECT_ORBIT_DIRECTION_TIME,
    GUN_TIME,
    TR_RANGE_SEARCH_TIME,
    TR_SORT_TIME,
    MOVEMENT_TIME;

    private ValueInfo battleProfile;
    private ValueInfo roundProfile;
    private ValueInfo turnProfile;

    private long startTime = -1;

    public void start() {
        if (startTime != -1) {
            System.out.printf("[WARN] %s: Stop was not called\n", this.name());
            return;
        }
        startTime = System.nanoTime();
    }

    public void stop() {
        if (startTime == -1) {
            System.out.printf("[WARN] %s: Start was not called\n", this.name());
            return;
        }
        final long time = System.nanoTime() - startTime;
        battleProfile.addValue(time);
        roundProfile.addValue(time);
        turnProfile.addValue(time);
        startTime = -1;
    }

    public static void initBattle() {
        for (TimeProfile tp : values()) {
            tp.battleProfile = new ValueInfo(1500000);
        }
    }

    public static void initRound() {
        for (TimeProfile tp : values()) {
            tp.roundProfile = new ValueInfo(60000);
        }
    }

    public static void initTurn() {
        for (TimeProfile tp : values()) {
            tp.turnProfile = new ValueInfo(300);
        }
    }

    public static String getBattleProfilesString() {
        final StringBuilder res = new StringBuilder(" == Battle Time Profiles == \n");
        int maxPropertyNameLength = getMaxNameLen();
        for (TimeProfile tp : values()) {
            res.append(" ").append(String.format("%" + maxPropertyNameLength + "s", tp.name())).append(": ").append(tp.battleProfile.toString()).append("\n");
        }
        return res.toString();
    }

    public static String getRoundProfilesString() {
        final StringBuilder res = new StringBuilder(" == Round Time Profiles == \n");
        int maxPropertyNameLength = getMaxNameLen();
        for (TimeProfile tp : values()) {
            res.append(" ").append(String.format("%" + maxPropertyNameLength + "s", tp.name())).append(": ").append(tp.roundProfile.toString()).append("\n");
        }
        return res.toString();
    }

    public static String getTurnProfilesString() {
        final StringBuilder res = new StringBuilder(" == Turn Time Profiles == \n");
        int maxPropertyNameLength = getMaxNameLen();
        for (TimeProfile tp : values()) {
            res.append(" ").append(String.format("%" + maxPropertyNameLength + "s", tp.name())).append(": ").append(tp.turnProfile.toString()).append("\n");
        }
        return res.toString();
    }

    private static int getMaxNameLen() {
        int maxNameLen = 0;

        for (TimeProfile tp : values()) {
            maxNameLen = max(maxNameLen, tp.name().length());
        }

        return maxNameLen;
    }

}
