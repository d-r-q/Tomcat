/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.paint;

import lxx.RobotListener;
import lxx.events.LXXPaintEvent;
import robocode.Event;

import java.util.ArrayList;
import java.util.List;

public class PaintManager implements RobotListener {

    private final List<Painter> painters = new ArrayList<Painter>();

    public void addPainter(Painter painter) {
        painters.add(painter);
    }

    public void onEvent(Event event) {
        if (event instanceof LXXPaintEvent) {
            LXXGraphics g = ((LXXPaintEvent) event).getGraphics();
            for (Painter p : painters) {
                p.paint(g);
            }
        }
    }
}
