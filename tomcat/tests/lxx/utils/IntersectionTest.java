/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.utils;

import junit.framework.TestCase;

import static java.lang.Math.abs;
import static java.lang.Math.random;

public class IntersectionTest extends TestCase {

    static {
        QuickMath.init();
    }

    public void testIntersection() throws Exception {
        for (int i = 0; i < 1000000; i++) {
            APoint center = new LXXPoint(100 * random(), 100 * random());
            double r = 50 * random();
            APoint pnt1 = new LXXPoint(LXXConstants.RADIANS_360 * random(), 75 * random());
            APoint pnt2 = new LXXPoint(LXXConstants.RADIANS_360 * random(), 75 * random());
            APoint[] res = LXXUtils.intersection(pnt1, pnt2, center, r);
            for (APoint pnt : res) {
                if (center.aDistance(pnt) != r) {
                    LXXUtils.intersection(pnt1, pnt2, center, r);
                }
                assertEquals(center.aDistance(pnt), r, 0.5);
            }
        }

        for (int i = 0; i < 1000000; i++) {
            APoint center = new LXXPoint(1000 * random(), 1000 * random());
            double r = 1000 * random();
            APoint pnt1 = new LXXPoint(LXXConstants.RADIANS_360 * random(), 1000 * random());
            APoint pnt2 = new LXXPoint(LXXConstants.RADIANS_360 * random(), 1000 * random());
            APoint[] res = LXXUtils.intersection(pnt1, pnt2, center, r);
            for (APoint pnt : res) {
                if (abs(center.aDistance(pnt) - r) > 0.5) {
                    LXXUtils.intersection(pnt1, pnt2, center, r);
                }
                assertEquals(center.aDistance(pnt), r, 0.5);
            }
        }

        APoint center = new LXXPoint(10, 0);
        double r = 5;
        APoint pnt1 = new LXXPoint(0, 0);
        APoint pnt2 = new LXXPoint(0, 10);
        APoint[] res = LXXUtils.intersection(pnt1, pnt2, center, r);
        for (APoint pnt : res) {
            assertEquals(center.aDistance(pnt), r);
        }

        center = new LXXPoint(10, 0);
        r = 5;
        pnt1 = new LXXPoint(0, 0);
        pnt2 = new LXXPoint(0, 30);
        res = LXXUtils.intersection(pnt1, pnt2, center, r);
        for (APoint pnt : res) {
            assertEquals(center.aDistance(pnt), r);
        }

        center = new LXXPoint(10, 0);
        r = 5;
        pnt1 = new LXXPoint(0, 0);
        pnt2 = new LXXPoint(10, 5);
        res = LXXUtils.intersection(pnt1, pnt2, center, r);
        for (APoint pnt : res) {
            assertEquals(center.aDistance(pnt), r, 0.01);
        }

        center = new LXXPoint(10, 0);
        r = 5;
        pnt1 = new LXXPoint(0, 0);
        pnt2 = new LXXPoint(10, -5);
        res = LXXUtils.intersection(pnt1, pnt2, center, r);
        for (APoint pnt : res) {
            assertEquals(center.aDistance(pnt), r, 0.1);
        }

        center = new LXXPoint(10, 0);
        r = 15;
        pnt1 = new LXXPoint(0, 0);
        pnt2 = new LXXPoint(10, -5);
        res = LXXUtils.intersection(pnt1, pnt2, center, r);
        for (APoint pnt : res) {
            assertEquals(center.aDistance(pnt), r);
        }

        center = new LXXPoint(10, 0);
        r = 5;
        pnt1 = new LXXPoint(0, 0);
        pnt2 = new LXXPoint(0, 1);
        res = LXXUtils.intersection(pnt1, pnt2, center, r);
        for (APoint pnt : res) {
            assertEquals(center.aDistance(pnt), r);
        }

        center = new LXXPoint(10, 0);
        r = 5;
        pnt1 = new LXXPoint(20, 20);
        pnt2 = new LXXPoint(0, 1);
        res = LXXUtils.intersection(pnt1, pnt2, center, r);
        for (APoint pnt : res) {
            assertEquals(center.aDistance(pnt), r);
        }

        center = new LXXPoint(10, 0);
        r = 5;
        pnt1 = new LXXPoint(0, 20);
        pnt2 = new LXXPoint(0, 1);
        res = LXXUtils.intersection(pnt1, pnt2, center, r);
        for (APoint pnt : res) {
            assertEquals(center.aDistance(pnt), r);
        }

        center = new LXXPoint(10, 0);
        r = 5;
        pnt1 = new LXXPoint(-20, -20);
        pnt2 = new LXXPoint(0, 1);
        res = LXXUtils.intersection(pnt1, pnt2, center, r);
        for (APoint pnt : res) {
            assertEquals(center.aDistance(pnt), r);
        }

        center = new LXXPoint(10, 0);
        r = 5;
        pnt1 = new LXXPoint(0, -20);
        pnt2 = new LXXPoint(0, 1);
        res = LXXUtils.intersection(pnt1, pnt2, center, r);
        for (APoint pnt : res) {
            assertEquals(center.aDistance(pnt), r);
        }

    }
}
