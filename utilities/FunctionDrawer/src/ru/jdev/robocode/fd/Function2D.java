package ru.jdev.robocode.fd;

/**
 * User: jdev
 * Date: 07.10.2010
 */
public interface Function2D {

    double f(double x);

    Interval getXInterval();
    Interval getYInterval();

    double step();

    public class Interval {
        public final double a;
        public final double b;

        public Interval(double a, double b) {
            this.a = a;
            this.b = b;
        }

        public double getCenter() {
            return (a + b) / 2;
        }
    }

}
