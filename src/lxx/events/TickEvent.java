/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.events;

import robocode.Event;

public class TickEvent extends Event {

    private final long time;

    public TickEvent(long time) {
        this.time = time;
    }

    public long getTime() {
        return time;
    }
}
