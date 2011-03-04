package voidious.utils;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

/**
 * Copyright (c) 2009-2010 - Voidious
 * <p/>
 * This software is provided 'as-is', without any express or implied
 * warranty. In no event will the authors be held liable for any damages
 * arising from the use of this software.
 * <p/>
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 * <p/>
 * 1. The origin of this software must not be misrepresented; you must not
 * claim that you wrote the original software.
 * <p/>
 * 2. Altered source versions must be plainly marked as such, and must not be
 * misrepresented as being the original software.
 * <p/>
 * 3. This notice may not be removed or altered from any source
 * distribution.
 */

public class RobotState {
    public Point2D.Double location;
    public double heading;
    public double velocity;
    public long time;
    public boolean smoothing;
    protected Rectangle2D.Double _botRect = null;
    protected ArrayList<Line2D.Double> _botSides = null;

    public RobotState(Point2D.Double botLocation, double botHeadingRadians,
                      double botVelocity) {

        location = botLocation;
        heading = botHeadingRadians;
        velocity = botVelocity;
        smoothing = false;
    }

    public RobotState(Point2D.Double botLocation, double botHeadingRadians,
                      double botVelocity, long currentTime) {

        this(botLocation, botHeadingRadians, botVelocity);

        time = currentTime;
    }

    public RobotState(Point2D.Double botLocation, double botHeadingRadians,
                      double botVelocity, long currentTime, boolean smooth) {

        this(botLocation, botHeadingRadians, botVelocity, currentTime);

        smoothing = smooth;
    }

    public Rectangle2D.Double botRect() {
        if (_botRect == null) {
            _botRect = DiaUtils.botRect(this.location);
        }

        return _botRect;
    }

    public ArrayList<Line2D.Double> botSides() {
        if (_botSides == null) {
            _botSides = DiaUtils.botSides(this.location);
        }

        return _botSides;
    }

    public Object clone() {
        return new RobotState((Point2D.Double) location.clone(), heading,
                velocity, time, smoothing);
    }
}
