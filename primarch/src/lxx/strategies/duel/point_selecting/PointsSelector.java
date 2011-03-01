/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.strategies.duel.point_selecting;

import lxx.utils.APoint;
import lxx.utils.LXXConstants;
import lxx.utils.LXXRobotState;
import lxx.utils.LXXUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Math.max;

public class PointsSelector {

    private static final int ATTRIBUTES_COUNT = 2;

    private static final int DIST_TO_ENEMY_ATTR_IDX = 0;
    private static final int ENEMY_ATTACK_ANGLE_ATTR_IDX = 1;

    private static final double distToEnemyWeight = 100;
    private static final DecreasingActivator distToEnemyActivator = new DecreasingActivator(500, 200, 100);
    private static final double enemyAttackAngleWeight = 100;
    private static final DecreasingActivator enemyAttackAngleActivator = new DecreasingActivator(LXXConstants.RADIANS_60, LXXConstants.RADIANS_3, LXXConstants.RADIANS_15);

    public APoint selectPoint(Collection<APoint> points, LXXRobotState robot, LXXRobotState opponent) {
        final Map<APoint, double[]> pointAttributesMap = new HashMap<APoint, double[]>(points.size());

        final double[] idealPoint = getIdealPoint();
        for (APoint pnt : points) {
            final double[] pointAttributes = getPointAttributes(pnt, opponent, robot);
            idealPoint[DIST_TO_ENEMY_ATTR_IDX] = max(idealPoint[DIST_TO_ENEMY_ATTR_IDX], pointAttributes[DIST_TO_ENEMY_ATTR_IDX]);
            idealPoint[ENEMY_ATTACK_ANGLE_ATTR_IDX] = max(idealPoint[ENEMY_ATTACK_ANGLE_ATTR_IDX], pointAttributes[ENEMY_ATTACK_ANGLE_ATTR_IDX]);
            pointAttributesMap.put(pnt, pointAttributes);
        }

        double minDist = Integer.MAX_VALUE;
        APoint minDistPoint = null;

        final double[] weights = getWeights(robot, opponent);
        for (APoint pnt : points) {
            double dist = LXXUtils.manhattanDistance(pointAttributesMap.get(pnt), idealPoint, weights);
            if (dist < minDist) {
                minDist = dist;
                minDistPoint = pnt;
            }
        }

        return minDistPoint;
    }

    private double[] getWeights(LXXRobotState robot, LXXRobotState opponent) {
        final double[] weights = new double[ATTRIBUTES_COUNT];

        weights[DIST_TO_ENEMY_ATTR_IDX] = opponent == null
                ? 0
                : distToEnemyWeight * distToEnemyActivator.activate(robot.aDistance(opponent));
        weights[ENEMY_ATTACK_ANGLE_ATTR_IDX] = opponent == null
                ? 0
                : enemyAttackAngleWeight * enemyAttackAngleActivator.activate(LXXUtils.getAttackAngle(opponent, robot, robot.getAbsoluteHeadingRadians()));

        return weights;
    }

    private double[] getIdealPoint() {
        final double[] idealPointAttributes = new double[ATTRIBUTES_COUNT];

        idealPointAttributes[DIST_TO_ENEMY_ATTR_IDX] = Integer.MIN_VALUE;
        idealPointAttributes[ENEMY_ATTACK_ANGLE_ATTR_IDX] = Integer.MIN_VALUE;

        return idealPointAttributes;
    }

    private double[] getPointAttributes(APoint pnt, LXXRobotState t, LXXRobotState robot) {
        final double[] pointAttributes = new double[ATTRIBUTES_COUNT];

        pointAttributes[DIST_TO_ENEMY_ATTR_IDX] = t == null ? 0 : pnt.aDistance(t);
        pointAttributes[ENEMY_ATTACK_ANGLE_ATTR_IDX] = t == null ? 0 : LXXUtils.getAttackAngle(t, pnt, robot.angleTo(pnt));

        return pointAttributes;
    }

}
