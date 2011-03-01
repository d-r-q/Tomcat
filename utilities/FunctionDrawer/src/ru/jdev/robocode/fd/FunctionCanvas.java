package ru.jdev.robocode.fd;

import java.awt.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import static java.lang.Math.abs;

/**
 * User: jdev
 * Date: 07.10.2010
 */
public class FunctionCanvas extends Canvas {

    private final NumberFormat numberFormat = new DecimalFormat();

    private final Function2D function2D;

    private double kx;
    private double ky;

    public FunctionCanvas(Function2D function2D) {
        this.function2D = function2D;
        numberFormat.setMaximumFractionDigits(2);
        numberFormat.setGroupingUsed(false);
    }

    @Override
    public void paint(Graphics g) {
        System.out.println("Paint!");
        final Function2D.Interval ix = function2D.getXInterval();
        final Function2D.Interval iy = function2D.getYInterval();
        kx = ((double) getWidth() / abs(ix.a - ix.b));
        ky = ((double) (getHeight() - 200) / abs(iy.a - iy.b));
        double prevX = ix.a;
        double prevY = function2D.f(prevX);
        g.setColor(Color.BLACK);
        int stepCount = (int) ((ix.b - ix.a) / function2D.step()) / 24;
        int i = 0;
        for (double x = ix.a; x <= ix.b; x += function2D.step(), i++) {
            final double y = function2D.f(x);

            final int screenX = getScreenX(x);
            final int screenY = getScreenY(y);
            g.drawLine(getScreenX(prevX), getScreenY(prevY),
                    screenX, screenY);
            
            prevY = y;
            prevX = x;

            if (stepCount == 0 || i % stepCount == 0) {
                g.drawString("f(" + numberFormat.format(x) + ") = " + numberFormat.format(y), screenX + 5, screenY - 5);
                g.fillOval(screenX - 2, screenY - 2, 4, 4);
            }
        }
    }

    private int getScreenX(double x) {
        return (int) Math.round((x - function2D.getXInterval().a) * kx);
    }

    private int getScreenY(double y) {
        return 100 + ((getHeight() - 200) - (int) Math.round((y - function2D.getYInterval().a) * ky));
    }


}
