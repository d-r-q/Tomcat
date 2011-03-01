package ru.jdev.robocode.fd;

import static java.lang.Math.pow;

/**
 * User: jdev
 * Date: 07.10.2010
 */
public class Sigmoid implements Function2D {

    private static final int MIN_X = 0;
    private static final int MAX_X = 1;
    private Interval forceGradientInterval = new Interval(40, 150);

    @Override
    public double f(double x) {
        return x / (pow(x, 2) + 1);
    }

    @Override
    public Interval getXInterval() {
        return new Interval(MIN_X, MAX_X);
    }

    @Override
    public Interval getYInterval() {
        return new Interval(0, 1);
    }

    @Override
    public double step() {
        return 0.1;
    }
}
