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

import static java.lang.Math.round;

public class ComplexMovementClassifier implements MovementClassifier, ClassificationIterator {

    private final Attribute[] accelAttrs;
    private final Attribute[] turnAttrs;

    protected final SegmentationTree<MovementDecision> accelLog;
    protected final SegmentationTree<MovementDecision> turnLog;
    protected final Map<Attribute, Integer> attrRanges;

    public ComplexMovementClassifier(Attribute[] accelAttrs, Attribute[] turnAttrs, Map<Attribute, Integer> attrRanges) {
        this.accelAttrs = accelAttrs;
        this.turnAttrs = turnAttrs;
        this.attrRanges = attrRanges;

        this.accelLog = new SegmentationTree<MovementDecision>(this.accelAttrs, 2, 0.001);
        this.turnLog = new SegmentationTree<MovementDecision>(this.turnAttrs, 2, 0.001);
    }

    public void learn(TurnSnapshot turnSnapshot, MovementDecision decision) {
        final SegmentationTreeEntry<MovementDecision> entry = new SegmentationTreeEntry<MovementDecision>(turnSnapshot);
        entry.result = decision;
        accelLog.addEntry(entry);
        turnLog.addEntry(entry);
    }

    public MovementDecision classify(TurnSnapshot turnSnapshot) {
        final List<EntryMatch<MovementDecision>> sortedSimilarEntries = accelLog.getSortedSimilarEntries(turnSnapshot, getLimits(turnSnapshot));

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

    public Attribute[] getAttributes() {
        final Set<Attribute> attrs = new HashSet<Attribute>();
        attrs.addAll(Arrays.asList(accelAttrs));
        attrs.addAll(Arrays.asList(turnAttrs));

        return attrs.toArray(new Attribute[attrs.size()]);
    }

    public MovementDecision next(TurnSnapshot turnSnapshot) {
        return classify(turnSnapshot);
    }

    protected Map<Attribute, Interval> getLimits(TurnSnapshot ts) {
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
