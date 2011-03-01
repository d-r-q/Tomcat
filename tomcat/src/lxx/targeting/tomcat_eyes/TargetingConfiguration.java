/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.targeting.tomcat_eyes;

import lxx.model.attributes.Attribute;

/**
 * User: jdev
 * Date: 01.03.2011
 */
public class TargetingConfiguration {

    private final String name;
    private final Attribute[] attributes;
    private final double[] weights;
    private final int[] indexes;

    public TargetingConfiguration(String name, Attribute[] attributes, double[] weights, int[] indexes) {
        this.name = name;
        this.attributes = attributes;
        this.weights = weights;
        this.indexes = indexes;
    }

    public String getName() {
        return name;
    }

    public Attribute[] getAttributes() {
        return attributes;
    }

    public double[] getWeights() {
        return weights;
    }

    public int[] getIndexes() {
        return indexes;
    }
}
