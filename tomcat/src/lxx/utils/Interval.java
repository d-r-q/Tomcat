/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.utils;

/**
 * User: jdev
 * Date: 07.03.2010
 */
public class Interval {

    public int a;
    public int b;

    public Interval(int a, int b) {
        this.a = a;
        this.b = b;
    }

    public Interval(Interval original) {
        this.a = original.a;
        this.b = original.b;
    }

    public int getLength() {
        return b - a + 1;
    }

    public boolean contains(int c) {
        return c >= a && c <= b;
    }

    public boolean intersects(Interval another) {
        return contains(another.a) || contains(another.b) || another.contains(a) || another.contains(b);
    }

    public String toString() {
        return "[" + a + ", " + b + "]";
    }

    public int center() {
        return (a + b) / 2;
    }
}