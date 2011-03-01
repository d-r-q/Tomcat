/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.enemy_bullets.bo_matching;

import lxx.RobotListener;
import lxx.events.TickEvent;
import robocode.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * User: jdev
 * Date: 19.02.11
 */
public class BearingOffsetsSet implements RobotListener {

    private final DensityFormulaFactory densityFormulaFactory = new DensityFormulaFactory();
    private final BearingOffsetData[] bearingOffsetDatas = null;

    private int bearingOffsetIdx = 0;

    public void onEvent(Event event) {
        if (event instanceof TickEvent) {
            if (bearingOffsetIdx == bearingOffsetDatas.length) {
                bearingOffsetIdx = 0;
            }

            BearingOffsetData bearingOffsetData = bearingOffsetDatas[bearingOffsetIdx];
            DensityFormula formula = bearingOffsetData.getFormula();
            if (bearingOffsetData.bearingOffset.tryFormula(formula)) {
                for (BearingOffsetData data : bearingOffsetDatas) {
                    if (data != bearingOffsetData) {
                        data.addFormulaToTry(formula);
                    }
                }
            }
        }
    }

    private class BearingOffsetData {

        private final BearingOffset bearingOffset;

        private final List<DensityFormula> formulasToTryQueue = new ArrayList<DensityFormula>();

        public BearingOffsetData(BearingOffset bearingOffset) {
            this.bearingOffset = bearingOffset;
        }

        public DensityFormula getFormula() {
            if (formulasToTryQueue.size() == 0) {
                formulasToTryQueue.add(densityFormulaFactory.createRandomFormula());
            }
            return formulasToTryQueue.remove(0);
        }

        public void addFormulaToTry(DensityFormula formula) {
            formulasToTryQueue.add(formula);
        }
    }

}
