/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.office;

import lxx.RobotListener;
import lxx.utils.IntWrapper;
import robocode.Event;
import robocode.SkippedTurnEvent;
import robocode.StatusEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * User: jdev
 * Date: 15.09.2010
 */
public class Timer implements RobotListener {

    private static final String GLOBAL_TIME_DATA_KEY = "GLOBAL_TIME";
    private static final String ROUND_START_TIMES_DATA_KEY = "ROUND_START_TIMES";

    private final IntWrapper battleTime;
    private final Map<Integer, Integer> roundStartTimes;

    public Timer(StaticDataManager staticDataManager, int currentRound) {
        if (staticDataManager.isDefined(GLOBAL_TIME_DATA_KEY)) {
            battleTime = (IntWrapper) staticDataManager.getData(GLOBAL_TIME_DATA_KEY);
            //noinspection unchecked
            roundStartTimes = (Map<Integer, Integer>) staticDataManager.getData(ROUND_START_TIMES_DATA_KEY);
        } else {
            battleTime = new IntWrapper();
            staticDataManager.add(GLOBAL_TIME_DATA_KEY, battleTime);
            roundStartTimes = new HashMap<Integer, Integer>();
            staticDataManager.add(ROUND_START_TIMES_DATA_KEY, roundStartTimes);
        }

        roundStartTimes.put(currentRound, battleTime.value);
    }

    public void onEvent(Event event) {
        if (event instanceof StatusEvent) {
            battleTime.value++;
        } else if (event instanceof SkippedTurnEvent) {
            battleTime.value++;
        }
    }

    public int getBattleTime() {
        return battleTime.value;
    }

    public int getRoundGlobalStartTime(int round) {
        return roundStartTimes.get(round);
    }

}
