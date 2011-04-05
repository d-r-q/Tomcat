/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.targeting.tomcat_eyes;

import lxx.kd_tree.LimitedPriorityKdTree;
import lxx.model.attributes.Attribute;
import lxx.strategies.MovementDecision;

/**
 * User: jdev
 * Date: 01.03.2011
 */
public class TargetingConfiguration {

    private final String name;
    private final Attribute[] attributes;
    private final LimitedPriorityKdTree<MovementDecision> log;

    public TargetingConfiguration(String name, Attribute[] attributes, double maxIntervalLength) {
        this.name = name;
        this.attributes = attributes;
        log = new LimitedPriorityKdTree<MovementDecision>(attributes, 2, maxIntervalLength);
    }

    public String getName() {
        return name;
    }

    public Attribute[] getAttributes() {
        return attributes;
    }

    public LimitedPriorityKdTree<MovementDecision> getLog() {
        return log;
    }

}
