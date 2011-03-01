package lxx.targeting.bullets;

import robocode.Bullet;
import lxx.targeting.Target;
import lxx.targeting.mg4.MegaGun5;
import lxx.targeting.predict.Predictor;
import lxx.utils.LXXPoint;
import lxx.utils.APoint;

import java.util.List;

/**
 * User: jdev
 * Date: 15.02.2010
 */
public class LXXBullet {

    private final Bullet bullet;
    private final Target target;
    private final LXXPoint firePosition;
    private final LXXPoint destination;
    private final List<Predictor> predictors;
    private final double maxExcapeNaglePos;
    private final double maxExcapeNagleNeg;
    private final LXXPoint targetPos;
    private final MegaGun5.PredictionData predictionData;


    public LXXBullet(Bullet bullet, Target target, LXXPoint destination, List<Predictor> predictors, LXXPoint firePosition, MegaGun5.PredictionData predictionData) {
        this.bullet = bullet;
        this.target = target;
        this.destination = destination;
        this.predictors = predictors;
        this.firePosition = firePosition;
        this.maxExcapeNagleNeg = target.maxEscapeAngle(-1);
        this.maxExcapeNaglePos = target.maxEscapeAngle(1);
        this.targetPos = new LXXPoint(target.getPosition());
        this.predictionData = predictionData;
    }

    public Bullet getBullet() {
        return bullet;
    }

    public Target getTarget() {
        return target;
    }

    public LXXPoint getDestination() {
        return destination;
    }

    public List<Predictor> getPredictors() {
        return predictors;
    }

    public LXXPoint getFirePosition() {
        return firePosition;
    }

    public double getTravelledDistance() {
        return firePosition.distance(bullet.getX(), bullet.getY());
    }

    public double getDistanceToTarget() {
        return firePosition.aDistance(target);
    }

    public double getFireDistance() {
        return firePosition.aDistance(destination);
    }

    public APoint getCurrentPos() {
        return new LXXPoint(bullet.getX(), bullet.getY());
    }

    public double getMaxExcapeNaglePos() {
        return maxExcapeNaglePos;
    }

    public double getMaxExcapeNagleNeg() {
        return maxExcapeNagleNeg;
    }

    public LXXPoint getTargetPos() {
        return targetPos;
    }

    public MegaGun5.PredictionData getPredictionData() {
        return predictionData;
    }
}
