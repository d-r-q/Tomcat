/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.strategies;

public interface Strategy {

    boolean match();

    TurnDecision makeDecision();

}
