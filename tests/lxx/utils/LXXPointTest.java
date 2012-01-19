/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.utils;

import junit.framework.TestCase;

import static java.lang.Math.toRadians;

public class LXXPointTest extends TestCase {

    static {
        QuickMath.init();
    }

    public void testDistanceToWall() {
        final BattleField battleField = new BattleField(0, 0, 800, 600);

        LXXPoint pnt = new LXXPoint(10, 10);
        assertEquals(590D, pnt.distanceToWall(battleField, toRadians(0)), 0.01);
        assertEquals(790D, pnt.distanceToWall(battleField, toRadians(90)), 0.01);
        assertEquals(10D, pnt.distanceToWall(battleField, toRadians(180)), 0.01);
        assertEquals(14.14D, pnt.distanceToWall(battleField, toRadians(225)), 0.01);
        assertEquals(10D, pnt.distanceToWall(battleField, toRadians(270)), 0.01);
        assertEquals(590D, pnt.distanceToWall(battleField, toRadians(360)), 0.01);

        pnt = new LXXPoint(10D, battleField.height - 10);
        assertEquals(10D, pnt.distanceToWall(battleField, toRadians(0)), 0.01);
        assertEquals(790D, pnt.distanceToWall(battleField, toRadians(90)), 0.01);
        assertEquals(590D, pnt.distanceToWall(battleField, toRadians(180)), 0.01);
        assertEquals(10D, pnt.distanceToWall(battleField, toRadians(270)), 0.01);
        assertEquals(14.14D, pnt.distanceToWall(battleField, toRadians(315)), 0.01);
        assertEquals(10D, pnt.distanceToWall(battleField, toRadians(360)), 0.01);

        pnt = new LXXPoint(battleField.width - 10, battleField.height - 10);
        assertEquals(10D, pnt.distanceToWall(battleField, toRadians(0)), 0.01);
        assertEquals(14.14D, pnt.distanceToWall(battleField, toRadians(45)), 0.01);
        assertEquals(10D, pnt.distanceToWall(battleField, toRadians(90)), 0.01);
        assertEquals(590D, pnt.distanceToWall(battleField, toRadians(180)), 0.01);
        assertEquals(790D, pnt.distanceToWall(battleField, toRadians(270)), 0.01);
        assertEquals(10D, pnt.distanceToWall(battleField, toRadians(360)), 0.01);

        pnt = new LXXPoint(battleField.width - 10, 10);
        assertEquals(590D, pnt.distanceToWall(battleField, toRadians(0)), 0.01);
        assertEquals(10D, pnt.distanceToWall(battleField, toRadians(90)), 0.01);
        assertEquals(14.14D, pnt.distanceToWall(battleField, toRadians(135)), 0.01);
        assertEquals(10D, pnt.distanceToWall(battleField, toRadians(180)), 0.01);
        assertEquals(790D, pnt.distanceToWall(battleField, toRadians(270)), 0.01);
        assertEquals(590D, pnt.distanceToWall(battleField, toRadians(360)), 0.01);
    }

}
