/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.targeting.classification;

import lxx.model.TurnSnapshot;
import lxx.strategies.MovementDecision;

public interface MovementClassifier {

    void learn(TurnSnapshot turnSnapshot, MovementDecision decision);

    MovementDecision classify(TurnSnapshot turnSnapshot);

    ClassificationIterator classificationIterator();

}
