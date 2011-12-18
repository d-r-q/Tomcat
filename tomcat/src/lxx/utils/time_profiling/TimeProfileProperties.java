/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.utils.time_profiling;

/**
 * User: jdev
 * Date: 10.12.11
 */
public enum TimeProfileProperties {

    TURN_TIME(0, "Turn time"),
    PROCESS_LISTENERS_TIME(1, "Listeners time"),
    MOVEMENT_TIME(2, "Movement time"),
    GUN_TIME(3, "Gun time"),
    EBM_WAVE_TIME(4, "EBM wave time"),
    TR_RANGE_SEARCH_TIME(5, "TR Range search time"),
    TR_SORT_TIME(6, "TR sort time"),
    SELECT_ORBIT_DIRECTION_TIME(7, "Select orb. dir. time");

    public final int idx;
    public final String name;

    private long startTime = -1;
    private long lastTime;

    TimeProfileProperties(int idx, String name) {
        this.idx = idx;
        this.name = name;
    }

    public void start() {
        if (startTime != -1) {
            System.out.printf("[WARN] %s: Stop was not called\n", name);
        }
        startTime = System.nanoTime();
    }

    public void stop() {
        lastTime = System.nanoTime() - startTime;
        startTime = -1;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getLastTime() {
        return lastTime;
    }

    @Override
    public String toString() {
        return name;
    }
}
