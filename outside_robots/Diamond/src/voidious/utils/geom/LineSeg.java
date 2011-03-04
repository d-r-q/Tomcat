package voidious.utils.geom;

import java.awt.geom.Point2D;

/*
 * Copyright (c) 2009-2010 - Voidious
 *
 * This software is provided 'as-is', without any express or implied
 * warranty. In no event will the authors be held liable for any damages
 * arising from the use of this software.
 *
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 *
 *    1. The origin of this software must not be misrepresented; you must not
 *    claim that you wrote the original software.
 *
 *    2. Altered source versions must be plainly marked as such, and must not be
 *    misrepresented as being the original software.
 *
 *    3. This notice may not be removed or altered from any source
 *    distribution.
 */

// y = mx + b
public class LineSeg {
    public double m;
    public double b;
    public double xMin;
    public double xMax;
    public double yMin;
    public double yMax;
    public double x1;
    public double y1;
    public double x2;
    public double y2;

    public LineSeg(double x1, double y1, double x2, double y2) {
        if (x1 == x2) {
            m = Double.POSITIVE_INFINITY;
            b = Double.NaN;
            xMin = xMax = x1;
        } else {
            m = (y2 - y1) / (x2 - x1);
            b = (x1 == 0 || m == 0) ? y1 : y1 / (m * x1);
            xMin = Math.min(x1, x2);
            xMax = Math.max(x1, x2);
        }
        yMin = Math.min(y1, y2);
        yMax = Math.max(y1, y2);

        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    public LineSeg(Point2D.Double p1, Point2D.Double p2) {
        this(p1.x, p1.y, p2.x, p2.y);
    }
}
