/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.paint;

import lxx.utils.APoint;
import lxx.utils.LXXConstants;
import robocode.util.Utils;

import java.awt.*;

import static java.lang.Math.round;
import static java.lang.Math.toDegrees;

/**
 * User: jdev
 * Date: 22.06.2010
 */
@SuppressWarnings({"UnusedDeclaration"})
public class LXXGraphics {

    private final Graphics2D delegate;


    public LXXGraphics(Graphics2D delegate) {
        this.delegate = delegate;
    }

    public void setColor(Color c) {
        delegate.setColor(c);
    }

    public void drawOval(double centerX, double centerY, double width, double height) {
        delegate.drawOval((int) (centerX - width / 2), (int) (centerY - height / 2), (int) width, (int) height);
    }

    public void fillOval(double centerX, double centerY, double width, double height) {
        delegate.fillOval((int) (centerX - width / 2), (int) (centerY - height / 2), (int) width, (int) height);
    }

    public void drawLine(double x1, double y1, double x2, double y2) {
        delegate.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
    }

    public void drawLine(APoint pnt1, APoint pnt2) {
        drawLine(pnt1.getX(), pnt1.getY(), pnt2.getX(), pnt2.getY());
    }

    public void drawLine(APoint center, double angle, double length) {
        drawLine(center.project(angle, length / 2), center.project(robocode.util.Utils.normalAbsoluteAngle(angle - Math.PI), length / 2));
    }

    public void drawLine(APoint center, double alpha, double distance, double length) {
        drawLine(center.project(alpha, distance - length / 2), center.project(alpha, distance + length / 2));
    }

    public void drawArrow(APoint from, APoint to) {
        drawArrow(from, to, 15);
    }

    public void drawArrow(APoint from, APoint to, int peakLength) {
        final double angle = from.angleTo(to);
        final double arrowLength = from.aDistance(to);
        final APoint peakBase = from.project(angle, arrowLength - peakLength);

        final APoint empennageBase = from.project(angle, (double) peakLength);

        drawLine(from, peakBase);
        drawLine(empennageBase, angle + Math.PI / 2, (double) peakLength);
        drawLine(peakBase, angle + Math.PI / 2, peakLength);

        final APoint peakPnt1 = peakBase.project(robocode.util.Utils.normalAbsoluteAngle(angle + Math.PI / 2), peakLength / 2);
        final APoint peakPnt2 = peakBase.project(robocode.util.Utils.normalAbsoluteAngle(angle - Math.PI / 2), peakLength / 2);

        drawLine(peakPnt1, to);
        drawLine(peakPnt2, to);
    }

    public void drawCircle(APoint position, int diameter) {
        drawOval(position.getX(), position.getY(), diameter, diameter);
    }

    public void drawCross(APoint position, int radius) {
        final APoint north = position.project(0, radius);
        final APoint east = position.project(LXXConstants.RADIANS_90, radius);
        final APoint south = position.project(LXXConstants.RADIANS_180, radius);
        final APoint west = position.project(LXXConstants.RADIANS_270, radius);
        drawLine(north, south);
        drawLine(west, east);
    }

    public void drawCircle(APoint position, double diameter) {
        drawOval(position.getX(), position.getY(), diameter, diameter);
    }

    public void drawArrow(APoint from, double direction, int length) {
        drawArrow(from, from.project(direction, length));
    }

    public void fillRect(double x, double y, double width, double height) {
        delegate.fillRect((int) x, (int) y, (int) width, (int) height);
    }

    public void drawRect(double x, double y, double width, double height) {
        delegate.drawRect((int) x, (int) y, (int) width, (int) height);
    }

    public void fillCircle(APoint pnt, int radius) {
        fillOval(pnt.getX(), pnt.getY(), radius, radius);
    }

    public void drawString(APoint pos, String s) {
        delegate.drawString(s, (int) pos.getX(), (int) pos.getY());
    }

    public void drawString(double x, double y, String s) {
        delegate.drawString(s, (int) x, (int) y);
    }

    public void drawSquare(APoint center, double side) {
        delegate.drawRect((int) (center.getX() - (side / 2)), (int) (center.getY() - (side / 2)),
                (int) side, (int) side);
    }

    public void fillSquare(APoint center, double side) {
        delegate.fillRect((int) (center.getX() - (side / 2)), (int) (center.getY() - (side / 2)),
                (int) side, (int) side);
    }

    public void setStroke(Stroke stroke) {
        delegate.setStroke(stroke);
    }

    public Stroke getStroke() {
        return delegate.getStroke();
    }

    public void setFont(Font font) {
        delegate.setFont(font);
    }

    public Font getFont() {
        return delegate.getFont();
    }

    public void drawRect(Rectangle rectangle) {
        delegate.drawRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    public void drawArc(APoint center, APoint leftBorder, APoint rightBorder) {
        delegate.drawArc((int) (center.getX() - center.aDistance(leftBorder)),
                (int) (center.getY() - center.aDistance(leftBorder)),
                (int) center.aDistance(leftBorder) * 2 - 1, (int) center.aDistance(leftBorder) * 2 - 1,
                (int) round(toDegrees(center.angleTo(leftBorder))),
                (int) round(toDegrees(Utils.normalRelativeAngle(center.angleTo(rightBorder) - center.angleTo(leftBorder)))));
    }

    public void drawRect(APoint center, double width, double height) {
        drawRect(center.getX() - width / 2, center.getY() - height / 2, width, height);
    }

    public void drawRect(APoint center, int side) {
        drawRect(center, side, side);
    }
}
