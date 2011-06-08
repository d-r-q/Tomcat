/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.targeting.classification;

import lxx.model.TurnSnapshot;
import lxx.strategies.MovementDecision;

public interface ClassificationIterator {

    MovementDecision next(TurnSnapshot turnSnapshot);

}
