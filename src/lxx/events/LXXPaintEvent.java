/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.events;

import lxx.paint.LXXGraphics;
import robocode.Event;

public class LXXPaintEvent extends Event {

    private final LXXGraphics graphics;

    public LXXPaintEvent(LXXGraphics graphics) {
        this.graphics = graphics;
    }

    public LXXGraphics getGraphics() {
        return graphics;
    }
}
