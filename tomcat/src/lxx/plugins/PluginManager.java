/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.plugins;

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
public class PluginManager implements RobotListener {

    @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection"})
    private final List<Plugin> plugins = new ArrayList<Plugin>();

    public PluginManager(Office office) {
        if (office.isDebugMode()) {

            for (Plugin plugin : plugins) {
                plugin.roundStarted(office);
            }
        }
    }

    public void onEvent(Event event) {
        if (event instanceof BattleEndedEvent) {
            for (Plugin plugin : plugins) {
                plugin.battleEnded();
            }
        } else if (event instanceof TickEvent) {
            for (Plugin plugin : plugins) {
                plugin.tick();
            }
        } else if (event instanceof RoundEndedEvent) {
            for (Plugin plugin : plugins) {
                plugin.roundEnded();
            }
        }
    }
}
