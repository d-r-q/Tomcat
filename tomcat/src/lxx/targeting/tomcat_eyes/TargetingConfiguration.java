/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.targeting.tomcat_eyes;

import lxx.model.attributes.Attribute;
import lxx.targeting.classification.MovementClassifier;

/**
 * User: jdev
 * Date: 01.03.2011
 */
public class TargetingConfiguration {

    private final String name;
    private final MovementClassifier movementClassifier;
    private final Attribute[] attributes;

    public TargetingConfiguration(String name, MovementClassifier movementClassifier, Attribute[] attributes) {
        this.name = name;
        this.movementClassifier = movementClassifier;
        this.attributes = attributes;
    }

    public String getName() {
        return name;
    }

    public MovementClassifier getMovementClassifier() {
        return movementClassifier;
    }

    public Attribute[] getAttributes() {
        return attributes;
    }
}
