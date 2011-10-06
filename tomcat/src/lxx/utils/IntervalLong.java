/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.utils;

public class IntervalLong {

    public long a;
    public long b;

    public IntervalLong(long a, long b) {
        this.a = a;
        this.b = b;
    }

    public long getLength() {
        return b - a;
    }

    public String toString() {
        return "[" + a + ", " + b + "]";
    }

    public long center() {
        return (a + b) / 2;
    }

    public boolean contains(long x) {
        return a <= x && b >= x;
    }

    public void extend(long x) {
        if (a > x) {
            a = x;
        }
        if (b < x) {
            b = x;
        }
    }

}
