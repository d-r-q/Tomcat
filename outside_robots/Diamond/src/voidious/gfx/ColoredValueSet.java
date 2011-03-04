package voidious.gfx;

import voidious.utils.DiaUtils;

import java.awt.*;
import java.util.ArrayList;

/**
 * Copyright (c) 2009-2010 - Voidious
 * <p/>
 * This software is provided 'as-is', without any express or implied
 * warranty. In no event will the authors be held liable for any damages
 * arising from the use of this software.
 * <p/>
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 * <p/>
 * 1. The origin of this software must not be misrepresented; you must not
 * claim that you wrote the original software.
 * <p/>
 * 2. Altered source versions must be plainly marked as such, and must not be
 * misrepresented as being the original software.
 * <p/>
 * 3. This notice may not be removed or altered from any source
 * distribution.
 */

public class ColoredValueSet {
    protected ArrayList<ColoredValue> _values;
    protected double _avg;
    protected double _min;
    protected double _stDev;

    public ColoredValueSet() {
        _values = new ArrayList<ColoredValue>();
        _avg = 0;
        _min = Double.POSITIVE_INFINITY;
        _stDev = 0;
    }

    public void addValue(double value, double firingAngle) {
        ColoredValue cv = new ColoredValue(value, firingAngle);
        _avg = ((_avg * _values.size()) + cv.value) / (_values.size() + 1);
        _min = Math.min(_min, cv.value);
        _values.add(cv);

        double[] values = new double[_values.size()];
        for (int x = 0; x < _values.size(); x++) {
            values[x] = _values.get(x).value;
        }

        _stDev = DiaUtils.standardDeviation(values);
    }

    public ArrayList<ColoredValue> getColoredValues() {
        return _values;
    }

    public static int colorValue(double value, double avg,
                                 double stDev, double maxStDev) {

        return (int) DiaUtils.limit(0, 255 * (value - (avg - maxStDev * stDev))
                / (2 * maxStDev * stDev), 255);
    }

    public static int colorValueInverse(double value, double avg,
                                        double stDev, double maxStDev) {

        return 255 - colorValue(value, avg, stDev, maxStDev);
    }

    public class ColoredValue {
        public double value;
        public double firingAngle;

        public ColoredValue(double value, double firingAngle) {
            this.value = value;
            this.firingAngle = firingAngle;
        }

        public Color redColor() {
            return new Color(colorValue(value, _avg, _stDev, 1), 0, 0);
        }

        public Color greenColor() {
            return new Color(0, colorValue(value, _avg, _stDev, 1), 0);
        }

        public Color blueColor() {
            return new Color(0, 0, colorValue(value, _avg, _stDev, 1));
        }

        public Color blueToWhiteColor() {
            int cval = colorValue(value, _avg, _stDev, 1);
            return new Color(cval, cval, 255);
        }
    }
}
