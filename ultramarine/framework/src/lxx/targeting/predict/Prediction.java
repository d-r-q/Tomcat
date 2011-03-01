package lxx.targeting.predict;

import lxx.utils.LXXPoint;
import lxx.utils.APoint;

import java.util.List;

/**
 * User: jdev
 * Date: 30.10.2009
 */

public class Prediction {

    public final Double angle;
    public final List<Predictor> predictors;

    public Prediction(Double angle, List<Predictor> predictors) {
        this.angle = angle;
        this.predictors = predictors;
    }
    
}
