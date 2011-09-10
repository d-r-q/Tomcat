/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.utils;

public class IntervalDouble {

    public double a;
    public double b;

    public IntervalDouble(double a, double b) {
        this.a = a;
        this.b = b;
    }

    public double getLength() {
        return b - a;
    }

    public String toString() {
        return "[" + a + ", " + b + "]";
    }

    public double center() {
        return (a + b) / 2;
    }

    public boolean contains(double x) {
        return a <= x && b >= x;
    }

    public void extend(double x) {
        if (a > x) {
            a = x;
        }
        if (b < x) {
            b = x;
        }
    }
}
