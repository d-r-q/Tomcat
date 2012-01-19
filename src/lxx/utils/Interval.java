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

    public int getLength() {
        return b - a + 1;
    }

    public boolean contains(int c) {
        return c >= a && c <= b;
    }

    public boolean contains(double c) {
        return c >= a && c <= b;
    }

    public String toString() {
        return "[" + a + ", " + b + "]";
    }
}