package lxx.stat;

import static lxx.StaticData.robot;
import lxx.autosegmentation.SegmentationsManager;
import lxx.autosegmentation.AttributeFactory;
import lxx.autosegmentation.model.FireSituation;
import lxx.autosegmentation.model.Attribute;
import lxx.targeting.Target;
import lxx.targeting.TargetImage;
import lxx.targeting.TargetManagerListener;
import lxx.utils.Utils;
import lxx.wave.Wave;
import lxx.wave.WaveCallback;
import lxx.wave.WaveManager;
import robocode.Event;
import robocode.RobocodeFileWriter;
import robocode.Rules;

import java.awt.*;
import java.io.IOException;
import static java.lang.Math.round;
import static java.lang.Math.toDegrees;
import java.util.HashMap;
import java.util.Map;

/**
 * User: jdev
 * Date: 12.05.2010
 */
public class DVStat implements TargetManagerListener, WaveCallback {

    private static RobocodeFileWriter writer;

    static {
        try {
            writer = new RobocodeFileWriter(robot.getDataDirectory().getAbsolutePath() + "/druss.dat", true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private WaveManager waveManager;
    private SegmentationsManager segmentationsManager;
    private Map<Wave, TargetImage> targets = new HashMap<Wave, TargetImage>();
    private Map<Wave, FireSituation> fireSituations = new HashMap<Wave, FireSituation>();
    private AttributeFactory attributeFactory;

    public DVStat(WaveManager waveManager, SegmentationsManager segmentationsManager, AttributeFactory attributeFactory) {
        this.waveManager = waveManager;
        this.segmentationsManager = segmentationsManager;
        this.attributeFactory = attributeFactory;
    }

    public void targetUpdated(Target oldState, Target newState, Event source) {
        Wave w = waveManager.launchWave(robot, newState, robot.getTime(), robot.angleTo(newState), Rules.getBulletSpeed(2), this);
        targets.put(w, new TargetImage(newState));
        fireSituations.put(w, attributeFactory.getFireSituation(newState));
    }

    public void waveBroken(Wave w) {
        TargetImage ti = targets.remove(w);
        FireSituation fs = fireSituations.remove(w);
        final double angle = robocode.util.Utils.normalRelativeAngle(Utils.angle(w.targetPos, w.target) - w.targetHeading);
        double alpha = round(toDegrees(angle) / 5);
        int dist = (int) (round(w.targetPos.distance(w.target.getX(), w.target.getY()) / 20));
        if (dist > 100) {
            // todo: fix me
            return;
            //ti.getMaxEscapeDistance(angle, w.getSourcePos());
        }

        StringBuffer str = new StringBuffer();
        int[] attrValues = new int[attributeFactory.getAttributes().length];
        for (Attribute a : attributeFactory.getAttributes()) {
            attrValues[a.getId()] = fs.getAttributeValue(a);
        }
        for (Integer i : attrValues) {
            str.append(i).append(";");
        }
        str.append(alpha).append(";");
        str.append(dist).append("\n");

        try {
            writer.write(str.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
