/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.targeting.tomcat_eyes;

import lxx.fire_log.FireLog;
import lxx.model.attributes.Attribute;
import lxx.strategies.MovementDecision;

/**
 * User: jdev
 * Date: 01.03.2011
 */
public class TargetingConfiguration {

    private final String name;
    private final Attribute[] attributes;
    private final FireLog<MovementDecision> log;

    public TargetingConfiguration(String name, Attribute[] attributes) {
        this.name = name;
        this.attributes = attributes;
        log = new FireLog<MovementDecision>(attributes, 2, 0.001);
    }

    public String getName() {
        return name;
    }

    public Attribute[] getAttributes() {
        return attributes;
    }

    public FireLog<MovementDecision> getLog() {
        return log;
    }

}
