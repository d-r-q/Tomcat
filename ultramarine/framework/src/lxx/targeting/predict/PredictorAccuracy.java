package lxx.targeting.predict;

/**
 * User: jdev
 * Date: 31.10.2009
 */
public class PredictorAccuracy {

    public Predictor predictor;
    public double accuracy;
    public Boolean isHit;

    public String toString() {
        return predictor.getClass().getSimpleName() + ": " + accuracy;
    }
}
