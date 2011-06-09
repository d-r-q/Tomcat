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
import lxx.utils.Interval;
import lxx.utils.LXXUtils;

import java.util.*;

import static java.lang.Math.round;

public class ComplexMovementClassifier implements MovementClassifier, ClassificationIterator {

    private static final Attribute[] accelAttrs = new Attribute[]{
            AttributesManager.enemyVelocity,
            AttributesManager.enemyAcceleration,
            AttributesManager.firstBulletFlightTime,
            AttributesManager.enemyDistanceToForwardWall,
            AttributesManager.enemyBearingOffsetOnFirstBullet,
            AttributesManager.enemyBearingOffsetOnSecondBullet,
    };

    private static final Map<Attribute, Integer> attrRanges = new HashMap<Attribute, Integer>();

    static {
        attrRanges.put(AttributesManager.enemyVelocity, 0);
        attrRanges.put(AttributesManager.enemyAcceleration, 0);
        attrRanges.put(AttributesManager.firstBulletFlightTime, 1);
        attrRanges.put(AttributesManager.enemyDistanceToForwardWall, 40);
        attrRanges.put(AttributesManager.enemyBearingOffsetOnFirstBullet, 5);
        attrRanges.put(AttributesManager.enemyBearingOffsetOnSecondBullet, 5);
    }

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
        final List<SegmentationTreeEntry<MovementDecision>> sortedSimilarEntries = accelLog.getSimilarEntries(getLimits(turnSnapshot));
        Collections.sort(sortedSimilarEntries, new Comparator<SegmentationTreeEntry<MovementDecision>>() {
            public int compare(SegmentationTreeEntry<MovementDecision> o1, SegmentationTreeEntry<MovementDecision> o2) {
                if (o1.predicate.getRound() == o2.predicate.getRound()) {
                    return (int) (o2.predicate.getTime() - o1.predicate.getTime());
                }
                return o2.predicate.getRound() - o1.predicate.getRound();
            }
        });

        final MovementDecision md;
        if (sortedSimilarEntries.size() == 0) {
            md = accelLog.getClosestEntry(turnSnapshot);
        } else {
            md = sortedSimilarEntries.get(0).result;
        }
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

    private Map<Attribute, Interval> getLimits(TurnSnapshot ts) {
        Map<Attribute, Interval> limits = new HashMap<Attribute, Interval>();

        for (Attribute a : accelAttrs) {
            Interval interval = new Interval(
                    (int) round(LXXUtils.limit(a, ts.getAttrValue(a) - attrRanges.get(a))),
                    (int) round(LXXUtils.limit(a, ts.getAttrValue(a) + attrRanges.get(a)))
            );
            limits.put(a, interval);
        }

        return limits;
    }

}
