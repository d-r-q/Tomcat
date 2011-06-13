/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.targeting.classification;

import lxx.model.TurnSnapshot;
import lxx.model.attributes.Attribute;
import lxx.office.AttributesManager;
import lxx.segmentation_tree.EntryMatch;
import lxx.segmentation_tree.SegmentationTree;
import lxx.segmentation_tree.SegmentationTreeEntry;
import lxx.strategies.MovementDecision;
import lxx.utils.Interval;
import lxx.utils.LXXUtils;

import java.util.*;

import static java.lang.Math.abs;
import static java.lang.Math.round;

public class AdjustingClassifier implements MovementClassifier {

    private static final Attribute[] accelAttrs = new Attribute[]{
            AttributesManager.enemyVelocity,
            AttributesManager.enemyAcceleration,
            AttributesManager.enemyDistanceToForwardWall,
            AttributesManager.enemyTravelTime,
            AttributesManager.enemyBearingToMe,
    };

    private static final Map<Attribute, Integer> attrRanges = new HashMap<Attribute, Integer>();

    static {
        attrRanges.put(AttributesManager.enemyVelocity, 0);
        attrRanges.put(AttributesManager.enemyAcceleration, 0);
        attrRanges.put(AttributesManager.enemyDistanceToForwardWall, 10);
        attrRanges.put(AttributesManager.enemyTravelTime, 3);
        attrRanges.put(AttributesManager.enemyBearingToMe, 10);
    }

    private final SegmentationTree<MovementDecision> log;

    public AdjustingClassifier() {
        log = new SegmentationTree<MovementDecision>(accelAttrs, 2, 0.0001);
    }

    public MovementDecision classify(TurnSnapshot turnSnapshot) {
        return log.getClosestEntry(turnSnapshot);
    }

    public ClassificationIterator classificationIterator() {
        return new AdjustingClassificationIterator();
    }

    public void learn(TurnSnapshot turnSnapshot, MovementDecision decision) {
        final SegmentationTreeEntry<MovementDecision> entry = new SegmentationTreeEntry<MovementDecision>(turnSnapshot);
        entry.result = decision;
        log.addEntry(entry);
    }

    public class AdjustingClassificationIterator implements ClassificationIterator {

        public TurnSnapshot firstSnapshot;
        public TurnSnapshot currentSnapshot;

        public MovementDecision next(TurnSnapshot turnSnapshot) {
            if (currentSnapshot == null || currentSnapshot.getNext() == null ||
                    currentSnapshot.getAttrValue(AttributesManager.firstBulletFlightTime) <
                            currentSnapshot.getNext().getAttrValue(AttributesManager.firstBulletFlightTime) ||
                    currentSnapshot.getNext().getRoundedAttrValue(AttributesManager.firstBulletFlightTime) == 4 ||
                    abs(turnSnapshot.getAttrValue(AttributesManager.firstBulletFlightTime) - currentSnapshot.getAttrValue(AttributesManager.firstBulletFlightTime)) > 2) {
                List<EntryMatch<MovementDecision>> similarEntries = log.getSortedSimilarEntries(turnSnapshot, getLimits(turnSnapshot));
                if (similarEntries == null || similarEntries.size() == 0) {
                    similarEntries = log.getSimilarEntries(turnSnapshot, 1);
                }
                if (similarEntries == null || similarEntries.size() == 0) {
                    return null;
                }
                currentSnapshot = similarEntries.get(0).predicate;
                if (firstSnapshot == null) {
                    firstSnapshot = currentSnapshot;
                }
            }

            currentSnapshot = currentSnapshot.getNext();

            return MovementDecision.getMovementDecision(currentSnapshot);
        }
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

    public static Attribute[] getAttributes
            () {
        Set<Attribute> attrs = new HashSet<Attribute>();
        attrs.addAll(Arrays.asList(accelAttrs));

        return attrs.toArray(new Attribute[attrs.size()]);
    }

}
