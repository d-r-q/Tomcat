package lxx.autosegmentation.segmentator;

import static lxx.StaticData.robot;
import lxx.autosegmentation.SegmentationsManager;
import lxx.autosegmentation.model.FireSituation;
import lxx.targeting.Target;
import lxx.targeting.TargetManagerListener;
import lxx.utils.LXXConstants;
import lxx.utils.Utils;
import lxx.wave.Wave;
import lxx.wave.WaveCallback;
import lxx.wave.WaveManager;
import robocode.Event;
import robocode.Rules;

import static java.lang.Math.*;
import java.util.HashMap;
import java.util.Map;

/**
 * User: jdev
 * Date: 10.03.2010
 */
public class SegmentatorsManager implements TargetManagerListener, WaveCallback {

    private static final Map<String, Segmentator> segmentators = new HashMap<String, Segmentator>();

    public static final String SEGMENTATOR_1 = "Segmentator 1";

    public static final String SEGMENTATOR_2 = "Segmentator 2";

    public static final String SEGMENTATOR_3 = "Segmentator 3";

    public static final String SEGMENTATOR_4 = "Segmentator 4";

    public static final String SEGMENTATOR_5 = "Segmentator 5";

    public static final String DYNAMIC_SEGMENTATOR = "Dynamic Segmentator";

    /*static {
        Segmentator segementator = new StaticSegmentator(
                new Attribute[]{
                        SegmentationsManager.lateralVelocityAttr,
                        SegmentationsManager.distBetweenAttr,
                        SegmentationsManager.distToHOWallAttr,
                        SegmentationsManager.lastVisitedGFAttr,
                        SegmentationsManager.enemyTravelTimeAttr
                });

        segmentators.put(SEGMENTATOR_1, segementator);

        segementator = new StaticSegmentator(
                new Attribute[]{
                        SegmentationsManager.lastVisitedGFAttr,
                        SegmentationsManager.enemyVelocityAttr,
                        SegmentationsManager.bulletFlightTime,
                        SegmentationsManager.enemyTravelTimeAttr,
                        SegmentationsManager.angleToTargetAttr
                });

        segmentators.put(SEGMENTATOR_2, segementator);

        segementator = new StaticSegmentator(
                new Attribute[]{
                        SegmentationsManager.gunBearing,
                        SegmentationsManager.bulletFlightTime,
                        SegmentationsManager.enemyHeadingAttr,
                        SegmentationsManager.enemyVelocityAttr,
                        SegmentationsManager.enemyXAttr,
                        SegmentationsManager.enemyYAttr
                });

        segmentators.put(SEGMENTATOR_3, segementator);

        segementator = new StaticSegmentator(
                new Attribute[]{
                        SegmentationsManager.gunBearing,
                        SegmentationsManager.timeSinceLastLatVelChange,
                        SegmentationsManager.timeSinceMyLastFire,
                        SegmentationsManager.enemyStopTime,
                        SegmentationsManager.lateralVelocityAttr
                });

        segmentators.put(SEGMENTATOR_4, segementator);

        segementator = new StaticSegmentator(
                new Attribute[]{
                        SegmentationsManager.lastVisitedGFAttr,
                });

        segmentators.put(SEGMENTATOR_5, segementator);

        segmentators.put(DYNAMIC_SEGMENTATOR, new DynamicSegmentator());
    }*/

    private final Map<Wave, FireSituation> fireSituations = new HashMap<Wave, FireSituation>();

    private final SegmentationsManager segmentationsManager;
    private final WaveManager waveManager;

    public SegmentatorsManager(SegmentationsManager segmentationsManager, WaveManager waveManager) {
        this.segmentationsManager = segmentationsManager;
        this.waveManager = waveManager;
        ((DynamicSegmentator)segmentators.get(DYNAMIC_SEGMENTATOR)).setSegmentationsManager(segmentationsManager);
        for (Segmentator s : segmentators.values()) {
            s.resegment();
        }
    }

    public void targetUpdated(Target oldState, Target newState, Event source) {
        FireSituation fs = null;//segmentationsManager.getFireSituation0(newState);

        Wave w = waveManager.launchWave(robot, newState, robot.getTime(), robot.angleTo(newState), Rules.getBulletSpeed(robot.firePower()), this);
        fireSituations.put(w, fs);
    }

    public void waveBroken(Wave w) {
        for (Segmentator s : segmentators.values()) {
            final FireSituation fs = fireSituations.get(w);

            if (robot.getOthers() == 0) {
                return;
            }
            Double angle = Utils.angle(w.sourcePos, w.target) - Utils.angle(w.sourcePos, w.targetPos);
            if (abs(angle) > Math.PI) {
                angle -= Math.PI * 2 * signum(angle);
            }
            if ((angle >= 0 && toDegrees(angle) > fs.getMaxEscapeAnglePos()) || (angle < 0 && toDegrees(angle) < fs.getMaxEscapeAngleNeg())) {
                return;
            }

            int gf = 0;
            if (angle > 0) {
                gf = (int) (toDegrees(angle) / fs.getMaxEscapeAnglePos() * LXXConstants.MAX_GUESS_FACTOR);
            } else {
                gf = (int) (toDegrees(abs(angle)) / fs.getMaxEscapeAngleNeg() * LXXConstants.MAX_GUESS_FACTOR);
            }
            fs.setGuessFactor(gf);

            s.addFireSituation(fs);
        }
    }

    public static Segmentator getSegmentator(String name) {
        return segmentators.get(name);
    }

}
