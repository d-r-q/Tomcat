/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.targeting.classification;

import lxx.model.TurnSnapshot;
import lxx.model.attributes.Attribute;
import lxx.segmentation_tree.SegmentationTree;
import lxx.segmentation_tree.SegmentationTreeEntry;
import lxx.strategies.MovementDecision;

import static java.lang.Math.abs;

public class SegmentationTreeMovementClassifier implements MovementClassifier, ClassificationIterator {

    private final SegmentationTree<MovementDecision> log;

    public SegmentationTreeMovementClassifier(Attribute[] attributes, double maxIntervalLength) {
        log = new SegmentationTree<MovementDecision>(attributes, 2, maxIntervalLength);
    }

    public MovementDecision classify(TurnSnapshot turnSnapshot) {
        return log.getClosestEntryResult(turnSnapshot);
    }

    public ClassificationIterator classificationIterator() {
        return this;
    }

    public void learn(TurnSnapshot turnSnapshot, MovementDecision decision) {
        final SegmentationTreeEntry<MovementDecision> entry = new SegmentationTreeEntry<MovementDecision>(turnSnapshot);
        entry.result = decision;
        if (abs(entry.result.getAcceleration()) > 2) {
            return;
        }

        log.addEntry(entry);
    }

    public MovementDecision next(TurnSnapshot turnSnapshot) {
        return classify(turnSnapshot);
    }
}
