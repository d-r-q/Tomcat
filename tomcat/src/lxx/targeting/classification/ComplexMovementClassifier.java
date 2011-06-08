/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.targeting.classification;

import lxx.model.TurnSnapshot;
import lxx.model.attributes.Attribute;
import lxx.office.AttributesManager;
import lxx.segmentation_tree.SegmentationTree;
import lxx.segmentation_tree.SegmentationTreeEntry;
import lxx.strategies.MovementDecision;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ComplexMovementClassifier implements MovementClassifier, ClassificationIterator {

    private static final Attribute[] accelAttrs = new Attribute[]{
            AttributesManager.enemyVelocity,
            AttributesManager.enemyAcceleration,
            AttributesManager.firstBulletFlightTime,
            AttributesManager.enemyTravelTime,
            AttributesManager.enemyDistanceToForwardWall,
            AttributesManager.bearingOffsetOnFirstBullet
    };

    private final SegmentationTree<MovementDecision> accelLog = new SegmentationTree<MovementDecision>(accelAttrs, 2, 0.01);

    private static final Attribute[] turnAttrs = new Attribute[]{
            AttributesManager.enemyVelocityModule,
            AttributesManager.enemyBearingToFirstBullet,
            AttributesManager.enemyDistanceToForwardWall,
            AttributesManager.enemyBearingToMe,
            AttributesManager.enemyTurnRate,
    };
    private final SegmentationTree<MovementDecision> turnLog = new SegmentationTree<MovementDecision>(turnAttrs, 2, 0.01);

    public void learn(TurnSnapshot turnSnapshot, MovementDecision decision) {
        final SegmentationTreeEntry<MovementDecision> entry = new SegmentationTreeEntry<MovementDecision>(turnSnapshot);
        entry.result = decision;
        accelLog.addEntry(entry);
        turnLog.addEntry(entry);
    }

    public MovementDecision classify(TurnSnapshot turnSnapshot) {
        final MovementDecision md = accelLog.getClosestEntry(turnSnapshot);
        if (md == null) {
            return null;
        }
        final double acceleration = md.getAcceleration();
        final MovementDecision.MovementDirection dir = md.getMovementDirection();
        final double turnRate = turnLog.getClosestEntry(turnSnapshot).getTurnRateRadians();

        return new MovementDecision(acceleration, turnRate, dir);
    }

    public ClassificationIterator classificationIterator() {
        return this;
    }

    public static Attribute[] getAttributes() {
        Set<Attribute> attrs = new HashSet<Attribute>();
        attrs.addAll(Arrays.asList(accelAttrs));
        attrs.addAll(Arrays.asList(turnAttrs));

        return attrs.toArray(new Attribute[attrs.size()]);
    }

    public MovementDecision next(TurnSnapshot turnSnapshot) {
        return classify(turnSnapshot);
    }
}
