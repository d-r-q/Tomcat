/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.plugins;

import lxx.RobotListener;
import lxx.events.TickEvent;
import lxx.office.Office;
import lxx.office.PropertiesManager;
import robocode.BattleEndedEvent;
import robocode.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * User: jdev
 * Date: 30.09.2010
 */
public class PluginManager implements RobotListener {

    private final List<Plugin> plugins = new ArrayList<Plugin>();

    public PluginManager(Office office) {
        if ("true".equals(PropertiesManager.getDebugProperty("aegm.debug"))) {
            plugins.add(new AEGMDebugger());
        }
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
        }
    }
}
