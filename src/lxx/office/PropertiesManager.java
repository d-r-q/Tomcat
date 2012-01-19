/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.office;

import lxx.RobotListener;
import robocode.DeathEvent;
import robocode.Event;
import robocode.WinEvent;

import java.util.Map;
import java.util.TreeMap;

/**
 * User: jdev
 * Date: 19.06.11
 */
public class PropertiesManager implements RobotListener {

    private static final Map<String, String> properties = new TreeMap<String, String>();

    public static void setDebugProperty(String name, String value) {
        properties.put(name, value);
    }

    public static String getDebugProperty(String name) {
        return properties.get(name);
    }

    public void onEvent(Event event) {
        if (event instanceof DeathEvent || event instanceof WinEvent) {
            System.out.println(" === Debug Properties ===");
            for (Map.Entry<String, String> e : properties.entrySet()) {
                System.out.println(e.getKey() + "=" + e.getValue());
            }
        }
    }
}
