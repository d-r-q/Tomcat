/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.enemy_bullets.bo_matching;

import lxx.model.attributes.Attribute;
import lxx.office.AttributesManager;

import java.util.Arrays;
import java.util.List;

import static java.lang.Math.random;

/**
 * User: jdev
 * Date: 19.02.11
 */
public class DensityFormulaFactory {

    private final Attribute[] myMovementAttributes;

    public DensityFormulaFactory() {
        myMovementAttributes = new Attribute[]{AttributesManager.myAcceleration,
                AttributesManager.myVelocityModule, AttributesManager.distBetween,
                AttributesManager.myDistToForwardWall, AttributesManager.myDistToReverseWall,
                AttributesManager.myTravelTime};
    }

    public DensityFormula createRandomFormula() {
        final int attributesCount = (int) (1 + (myMovementAttributes.length - 1) * random());
        final List<Attribute> possibleAttributes = Arrays.asList(myMovementAttributes);

        final Attribute[] attributes = new Attribute[attributesCount];
        for (int i = 0; i < attributesCount; i++) {
            attributes[i] = possibleAttributes.remove((int) (possibleAttributes.size() * random()));
        }

        final double weights[] = new double[attributesCount];
        for (int i = 0; i < attributesCount; i++) {
            weights[i] = random();
        }

        return new DensityFormula(attributes, (long) (5000 * random()), weights);
    }

}
