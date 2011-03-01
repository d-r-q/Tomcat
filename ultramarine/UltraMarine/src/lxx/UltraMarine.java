package lxx;

import lxx.autosegmentation.SegmentationsManager;
import lxx.autosegmentation.AttributeFactory;
import lxx.movement.Strategy;
import lxx.movement.minimal_risk.MinimalRiskModel;
import lxx.stat.HitRateStatistics;
import lxx.stat.DVStat;
import lxx.strat.RandomStrategy;
import lxx.strat.duel.DuelStrategy;
import lxx.strat.duel.FireAngleMonitor;
import lxx.strat.meele.HoldCornerStrategy;
import lxx.strat.meele.KeepDistanceStrategy;
import lxx.strat.meele.MinimalRiskStrategy;
import lxx.strat.meele.TakeCornerStrategy;
import lxx.strat.reactive.EDMStrategy;
import lxx.strat.reactive.HitReactStrategy;
import lxx.targeting.Target;
import lxx.targeting.TargetChooser;
import lxx.targeting.TargetChooserImpl;
import lxx.targeting.mg6.MG6;
import lxx.targeting.mg4.MegaGun4;
import lxx.targeting.mg4.PredictionData;
import lxx.targeting.mg4.MegaGun5;
import lxx.targeting.bullets.BulletManager;
import lxx.targeting.bullets.LXXBullet;
import lxx.targeting.predict.Prediction;
import lxx.utils.LXXConstants;
import lxx.utils.LXXPoint;
import robocode.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import static java.lang.Math.abs;
import static java.lang.StrictMath.round;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class UltraMarine extends BasicRobot {

    private static int[] placeDethCount = new int[11];
    private static double[] placeEnergyCount = new double[11];
    private static int[] placePassed = new int[11];

    private static int staticFireCount;
    private static int staticHitCount;

    private MinimalRiskModel minimalRiskModel;
    private TargetChooser defaultTargetChooser;
    private HitRateStatistics hitRateStatistics;
    private BulletManager bulletManager;

    private int lastRadarTurnDirection = 1;
    private double lastHitBulletHeading;

    private List<Strategy> strategies = new ArrayList<Strategy>();
    private Strategy currentStrategy = null;

    private FireAngleMonitor fireAngleMonitor;

    private int fireCount = 0;
    private int hitCount = 0;

    private long lastFireTime = 0;
    private SegmentationsManager segmentationsManager;
    private Prediction lastPrediction;
    //private MegaGun5 megaGun5;
    private AttributeFactory attributeFactory;

    //private List<PredictionData> imgs = new ArrayList<PredictionData>();
    //private MegaGun4 megaGun4;

    public UltraMarine() {
        StaticData.robot = this;
    }

    public void run() {
        init();
        while (true) {
            firePower = getTargetChooser() != null ? getTargetChooser().firePower() : 0.1;
            if (firePower > 3) {
                firePower = 3;
            }
            firePower = 2;
            turnRadar();
            fire();
            move();
            execute();
            for (RobotListener rl : listeners) {
                rl.tick();
            }
        }
    }

    public void init() {
        super.init();

        minimalRiskModel = new MinimalRiskModel(getBattleFieldWidth(), getBattleFieldHeight(), targetManager);
        defaultTargetChooser = new TargetChooserImpl(this, targetManager, minimalRiskModel);
        hitRateStatistics = new HitRateStatistics(targetManager, predictorManager);
        bulletManager = new BulletManager(targetManager);
        addListener(bulletManager);
        bulletManager.addListener(hitRateStatistics);
        attributeFactory = new AttributeFactory(bulletManager);

        //segmentationsManager = new SegmentationsManager(waveManager, bulletManager, targetManager);
        //targetManager.addListener(segmentationsManager);

        /*SegmentatorsManager segmentatorsManager = new SegmentatorsManager(segmentationsManager, waveManager);
        targetManager.addListener(segmentatorsManager);*/
        targetManager.addListener(new DVStat(waveManager, segmentationsManager, attributeFactory));

//        predictorManager.addPredictor(new ClassicGFPredictor(waveManager));
//        predictorManager.addPredictor(new CircularPredictor());
//        predictorManager.addPredictor(new HeadOnPredictor());
//
//        predictorManager.addPredictor(new DuelGFPredictor(waveManager));
//        predictorManager.addPredictor(new PlainStatGFPredictor(waveManager));
//        predictorManager.addPredictor(new PatternGFPredictor(waveManager));
//        predictorManager.addPredictor(new MeleeDVPredictor(waveManager));
//        predictorManager.addPredictor(new DuelDVGun_2(waveManager));
//
//        predictorManager.addPredictor(new DuelGFGun(waveManager, 0.75D));
//        predictorManager.addPredictor(new PatternGFGun(waveManager, 0.75D));
//        predictorManager.addPredictor(new DuelGFGun_2(waveManager, 0.75D));
//
//        predictorManager.addPredictor(new DuelGFPredictor(waveManager));
//
//        predictorManager.addPredictor(new DuelDVGun_2(waveManager));
//        predictorManager.addPredictor(new DuelGFGun_2(waveManager, 0.75D));
//
//        predictorManager.addPredictor(new DuelistGFGun(waveManager, 0.75D));
//
        //predictorManager.addPredictor(new MegaGun2(waveManager, segmentationsManager));
        /*megaGun4 = new MegaGun4(waveManager, new AttributeFactory(bulletManager));
        predictorManager.addPredictor(megaGun4);
        targetManager.addListener(megaGun4);*/
        attributeFactory = new AttributeFactory(bulletManager);
        MG6 mg6 = new MG6(attributeFactory, waveManager);
        predictorManager.addPredictor(mg6);
//        predictorManager.addPredictor(new WallsGFGun(waveManager, 0.99D));
//
//        predictorManager.addPredictor(new MostProbablyGFGun(SegmentatorsManager.getSegmentator(SegmentatorsManager.DYNAMIC_SEGMENTATOR),
//                segmentationsManager));
//        predictorManager.addPredictor(new MathExpectionGFGun(SegmentatorsManager.getSegmentator(SegmentatorsManager.DYNAMIC_SEGMENTATOR),
//                segmentationsManager));
//        predictorManager.addPredictor(new MinProbablyGFGun(SegmentatorsManager.getSegmentator(SegmentatorsManager.DYNAMIC_SEGMENTATOR),
//                segmentationsManager));
//        predictorManager.addPredictor(new MostProbablyGFGun(SegmentatorsManager.getSegmentator(SegmentatorsManager.SEGMENTATOR_2),
//                segmentationsManager));
//        predictorManager.addPredictor(new MinProbablyGFGun(SegmentatorsManager.getSegmentator(SegmentatorsManager.SEGMENTATOR_2),
//                segmentationsManager));
//        predictorManager.addPredictor(new MostProbablyGFGun(SegmentatorsManager.getSegmentator(SegmentatorsManager.SEGMENTATOR_3),
//                segmentationsManager));
//        predictorManager.addPredictor(new MathExpectionGFGun(SegmentatorsManager.getSegmentator(SegmentatorsManager.SEGMENTATOR_3),
//                segmentationsManager));
//        predictorManager.addPredictor(new MostProbablyGFGun(SegmentatorsManager.getSegmentator(SegmentatorsManager.SEGMENTATOR_4),
//                segmentationsManager));
//        predictorManager.addPredictor(new MathExpectionGFGun(SegmentatorsManager.getSegmentator(SegmentatorsManager.SEGMENTATOR_4),
//                segmentationsManager));
//        predictorManager.addPredictor(new MinProbablyGFGun(SegmentatorsManager.getSegmentator(SegmentatorsManager.SEGMENTATOR_5),
//                segmentationsManager));
//        predictorManager.addPredictor(new MinProbablyGFGun(SegmentatorsManager.getSegmentator(SegmentatorsManager.SEGMENTATOR_1),
//                segmentationsManager));
//        predictorManager.addPredictor(new MinProbablyGFGun(SegmentatorsManager.getSegmentator(SegmentatorsManager.SEGMENTATOR_3),
//                segmentationsManager));
        //strategies.add(new AvoidWallStrategy(this));
        strategies.add(new EDMStrategy(this, targetManager));
        //strategies.add(new GoingToEnemyStrategy(this, targetManager));
        strategies.add(new HitReactStrategy(this));
        fireAngleMonitor = new FireAngleMonitor(waveManager, this, targetManager);
        strategies.add(new DuelStrategy(targetManager, this, fireAngleMonitor));
        targetManager.addListener(fireAngleMonitor);

        strategies.add(new KeepDistanceStrategy(targetManager));
        strategies.add(new TakeCornerStrategy(this, targetManager, cornerManager));
        strategies.add(new HoldCornerStrategy(this, targetManager, cornerManager));
        strategies.add(new MinimalRiskStrategy(this, minimalRiskModel));
        strategies.add(new RandomStrategy(this));

        addListener(waveManager);
        addListener(fireAngleMonitor);

        setColors(new Color(141, 0, 207), new Color(38, 26, 160), new Color(38, 26, 160),
                new Color(141, 0, 207), new Color(141, 0, 207));
    }

    private void turnRadar() {
        TargetChooser tc = currentStrategy != null ? currentStrategy.getTargetChooser() : defaultTargetChooser;
        if (tc == null) {
            tc = defaultTargetChooser;
        }
        Target bestTarget = tc.getBestTarget();
        Target lastUpdated = targetManager.getLastUpdatedTarget();
        if (bestTarget == null || lastUpdated == null || lastUpdated.getLatency() > 25 || targetManager.getAliveTargetCount() < getOthers()) {
            setTurnRadarRightRadians(Rules.RADAR_TURN_RATE_RADIANS * lastRadarTurnDirection);
        } else {
            final LXXPoint p = bestTarget.getPosition();
            double angle = normalizeBearing(angleTo(p) - getRadarHeadingRadians());
            if (Math.abs(angle) < Rules.RADAR_TURN_RATE_RADIANS) {
                angle += (Rules.RADAR_TURN_RATE_RADIANS - Math.abs(angle)) / 2 * Math.signum(angle);
            }
            setTurnRadarRightRadians(angle);
            lastRadarTurnDirection = (int) Math.signum(angle);
            if (lastRadarTurnDirection == 0) {
                lastRadarTurnDirection = 1;
            }
        }
    }

    public void fire() {
        TargetChooser tc = currentStrategy != null ? currentStrategy.getTargetChooser() : defaultTargetChooser;
        if (tc == null) {
            tc = defaultTargetChooser;
        }
        Target bestTarget = tc.getBestTarget();
        if (bestTarget == null || getOthers() == 0) {
            return;
        }

        if (Math.abs(getGunTurnRemainingRadians()) > LXXConstants.RADIANS_1_26) {
            return;
        }

        if (getTurnsToGunCool() > 5) {
            setTurnGunRightRadians(normalizeBearing(angleTo(bestTarget) - getGunHeadingRadians()));
            lastPrediction = null;
            return;
        } else if (lastPrediction == null) {
            Double gunTurnAngle;
            gunTurnAngle = getGunTurnAngle(bestTarget);
            setTurnGunRightRadians(gunTurnAngle);
            return;
        }

        //imgs.add(megaGun4.getPredictionData(bestTarget));
        final Double gunTurnAngle = getGunTurnAngle(bestTarget);
        if (abs(gunTurnAngle) > LXXConstants.RADIANS_1_26) {
            setTurnGunRightRadians(gunTurnAngle);
            //imgs.remove(imgs.size() - 1);
            return;
        }

        double bulletPower = firePower;
        if (bulletPower == 0) {
            bulletPower = 0.1;
        }

        if ((bulletPower + 4 < getEnergy() || bestTarget.getEnergy() < 0.2)) {
            final Bullet bullet = fireBullet(bulletPower);
            if (bullet != null) {
                fireCount++;
                staticFireCount++;
                bulletManager.addBullet(new LXXBullet(bullet, bestTarget, bestTarget.getPosition(), lastPrediction.predictors, getPosition(), null));
                lastFireTime = getTime();
                lastPrediction = null;
            } else {
                //imgs.remove(imgs.size() - 1);
            }
        } else {
            //imgs.remove(imgs.size() - 1);
        }
    }

    private Double getGunTurnAngle(Target bestTarget) {
        Double gunTurnAngle;
        final Prediction prediction = predictorManager.getAngle(bestTarget);
        if (prediction == null || prediction.angle == null) {
            return normalizeBearing(angleTo(bestTarget) - getGunHeadingRadians());
        } else {
            gunTurnAngle = normalizeBearing(prediction.angle - getGunHeadingRadians());
        }

        lastPrediction = prediction;
        return gunTurnAngle;
    }

    public int getTurnsToGunCool() {
        return (int) round(getGunHeat() / getGunCoolingRate());
    }

    private void move() {
        goToDestination();
    }

    public void onCustomEvent(CustomEvent event) {
    }

    public void onScannedRobot(ScannedRobotEvent event) {
        targetManager.updateTarget(event);
    }

    public void onRobotDeath(RobotDeathEvent event) {
        System.out.println(event.getName() + " has die");
        targetManager.onTargetKilled(event.getName());
        placeEnergyCount[getOthers() + 2] = (placeEnergyCount[getOthers() + 2] * placePassed[getOthers() + 2] + getEnergy()) / (placePassed[getOthers() + 2] + 1);
        placePassed[getOthers() + 2]++;
    }

    public void onHitRobot(HitRobotEvent event) {
        targetManager.updateTarget(event);
        minimalRiskModel.recalculate();
    }

    public void onBulletHit(BulletHitEvent event) {
        super.onBulletHit(event);
        targetManager.updateTarget(event);
        hitCount++;
        staticHitCount++;
        //imgs.remove(0);
    }

    public void onBulletHitBullet(BulletHitBulletEvent event) {
        staticFireCount--;
        fireCount--;
        //imgs.remove(0);
    }

    public void onHitByBullet(HitByBulletEvent event) {
        super.onHitByBullet(event);
        lastHitBulletHeading = event.getHeadingRadians();
        targetManager.updateTarget(event);
        minimalRiskModel.recalculate();
    }

    private void goToDestination() {
        for (Strategy ms : strategies) {
            if (ms.match()) {
                currentStrategy = ms;
                LXXPoint dst = ms.getDestination(!ms.equals(currentStrategy));
                if (dst != null) {
                    destination = dst;
                }
                break;
            }
        }
        if (destination != null) {
            goTo(destination);
        }
    }

    public void onWin(WinEvent event) {
        placeDethCount[1]++;
        placePassed[1]++;
        printStat();
        predictorManager.onRoundStarted();
        System.out.println(hitRateStatistics.toString());
    }

    public void onDeath(DeathEvent event) {
        placeDethCount[getOthers() + 1]++;
        printStat();
        predictorManager.onRoundStarted();
        System.out.println(hitRateStatistics.toString());
    }

    public void onPaint(Graphics2D graphics2D) {
        targetManager.paint(graphics2D);
        if (currentStrategy != null) {
            //currentStrategy.paint(graphics2D);
        }
        //predictorManager.paint(graphics2D);
        //waveManager.paint(graphics2D);
        //fireAngleMonitor.paint(graphics2D);

        if (destination != null) {
            graphics2D.setColor(Color.WHITE);
            graphics2D.fillOval((int) destination.x - 4, (int) destination.y - 4, 8, 8);
            graphics2D.drawOval((int) destination.x - 5, (int) destination.y - 5, 10, 10);
            float[] dashPattern = {6, 3, 3, 3, 3};
            graphics2D.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_MITER, 10,
                    dashPattern, 0));
            graphics2D.drawLine((int) getX(), (int) getY(), (int) destination.getX(), (int) destination.getY());
        }

        NumberFormat format = new DecimalFormat();
        format.setMaximumFractionDigits(1);
        format.setMaximumIntegerDigits(3);
/*graphics2D.setFont(new Font("Arial", Font.PLAIN, 14));
graphics2D.drawString("Static hit rate (" + staticHitCount + "/" + staticFireCount + ") = " + format.format((double) staticHitCount / (double) staticFireCount * 100) + "%", 5, 5);*/

        graphics2D.setStroke(new BasicStroke());
//segmentationsManager.paint(graphics2D);
        bulletManager.paint(graphics2D);
        /*if (imgs.size() > 0) {
            imgs.get(0).paint(graphics2D);
        }*/
    }

    public void onHitWall(HitWallEvent event) {
        System.out.println("HIT WALL!!!!!!!!!!!!!!!!!!!!!!!!!");
    }

    public double getLastHitBulletHeading() {
        return lastHitBulletHeading;
    }

    public void onBulletMissed(BulletMissedEvent event) {
        super.onBulletMissed(event);
        //imgs.remove(0);
    }

    public void onBattleEnded(BattleEndedEvent event) {
        printStat();
    }


    private void printStat() {
        NumberFormat format = new DecimalFormat();
        format.setMaximumFractionDigits(1);
        format.setMaximumIntegerDigits(3);
        for (int i = 1; i < placeDethCount.length; i++) {
            System.out.println(i + ": " + placePassed[i] + " / " + placeDethCount[i] +
                    " (" + format.format((double) placePassed[i] / (double) placeDethCount[i]) + "), " + format.format(placeEnergyCount[i]));
        }
        System.out.println("Hit rate (" + hitCount + "/" + fireCount + ") = " + format.format((double) hitCount / (double) fireCount * 100) + "%");
        System.out.println("Static hit rate (" + staticHitCount + "/" + staticFireCount + ") = " + format.format((double) staticHitCount / (double) staticFireCount * 100) + "%");
    }

    public LXXPoint getMostSafePoint() {
        return minimalRiskModel.getSafestPoint();
    }

    public TargetChooser getTargetChooser() {
        if (currentStrategy == null) {
            return defaultTargetChooser;
        }
        return currentStrategy.getTargetChooser() != null ? currentStrategy.getTargetChooser() : defaultTargetChooser;
    }

    public long getLastFireTime() {
        return lastFireTime;
    }
}
