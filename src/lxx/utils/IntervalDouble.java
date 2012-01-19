/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.utils;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class IntervalDouble implements Comparable<IntervalDouble> {

    public double a;
    public double b;

    public IntervalDouble() {
        this.a = Long.MAX_VALUE;
        this.b = Long.MIN_VALUE;
    }

    public IntervalDouble(double a, double b) {
        this.a = a;
        this.b = b;
    }

    public IntervalDouble(IntervalDouble ival) {
        this.a = ival.a;
        this.b = ival.b;
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

    public boolean intersects(IntervalDouble another) {
        return (a <= another.a && b >= another.a) ||
                (another.a <= a && another.b >= a);
    }

    public double intersection(IntervalDouble another) {
        return min(b, another.b) - max(a, another.a);
    }

    public boolean contains(IntervalDouble another) {
        return a <= another.a && b >= another.b;
    }

    public void merge(IntervalDouble another) {
        a = min(a, another.a);
        b = max(b, another.b);
    }

    public int compareTo(IntervalDouble another) {
        return a < another.a ? -1 : a == another.a ? 0 : 1;
    }

}
