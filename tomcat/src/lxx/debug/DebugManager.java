/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.debug;

import lxx.RobotListener;
import lxx.events.TickEvent;
import lxx.office.Office;
import robocode.BattleEndedEvent;
import robocode.Event;
import robocode.RoundEndedEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * User: jdev
 * Date: 30.09.2010
 */
public class DebugManager implements RobotListener {

    private final List<Debugger> debuggers = new ArrayList<Debugger>();

    public DebugManager(Office office) {
        if (office.isDebugMode()) {

            debuggers.add(new AvgBulletFlightTime());

            for (Debugger debugger : debuggers) {
                debugger.roundStarted(office);
            }
        }
    }

    public void onEvent(Event event) {
        if (event instanceof BattleEndedEvent) {
            for (Debugger debugger : debuggers) {
                debugger.battleEnded();
            }
        } else if (event instanceof TickEvent) {
            for (Debugger debugger : debuggers) {
                debugger.tick();
            }
        } else if (event instanceof RoundEndedEvent) {
            for (Debugger debugger : debuggers) {
                debugger.roundEnded();
            }
        }
    }
}
