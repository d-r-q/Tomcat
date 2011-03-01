package lxx.autosegmentation.model;

import lxx.autosegmentation.AttributeFactory;

import static java.lang.Math.abs;

/**
 * User: jdev
 * Date: 10.03.2010
 */
public class FireSituation {

    private final int maxEscapeAnglePos;
    private final int maxEscapeAngleNeg;
    private final int[] fsAttributes;
    private final AttributeFactory attributeFactory;

    private int guessFactor;

    public FireSituation(int maxEscapeAnglePos, int maxEscapeAngleNeg, int guessFactor, int[] fsAttributes, AttributeFactory attributeFactory) {
        this.maxEscapeAnglePos = maxEscapeAnglePos;
        this.maxEscapeAngleNeg = maxEscapeAngleNeg;
        this.guessFactor = guessFactor;
        this.fsAttributes = fsAttributes;
        this.attributeFactory = attributeFactory;
    }

    public int getMaxEscapeAnglePos() {
        return maxEscapeAnglePos;
    }

    public int getMaxEscapeAngleNeg() {
        return maxEscapeAngleNeg;
    }

    public int getGuessFactor() {
        return guessFactor;
    }

    public int getAttributeValue(Attribute a) {
        return fsAttributes[a.getId()];
    }

    public void setGuessFactor(int guessFactor) {
        this.guessFactor = guessFactor;
    }

    public double match(FireSituation fs) {
        double res = 0;

        for (int i = 0; i < fsAttributes.length; i++) {
            final Attribute attribute = attributeFactory.getAttribute(i);
            int idx = attribute.getId();
            final double v = (double) abs(fsAttributes[idx] - fs.fsAttributes[idx]) / attribute.getActualRange();
            if (v > 1) {
                throw new RuntimeException("Something wrong");
            }
            res += v;
        }
        if (res / fsAttributes.length > 1) {
            throw new RuntimeException("Something wrong");
        }

        return res / fsAttributes.length;
    }

    public int[] getFsAttributes() {
        return fsAttributes;
    }

}
