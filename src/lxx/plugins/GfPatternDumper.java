package lxx.plugins;

import lxx.Tomcat;
import lxx.office.Office;
import lxx.targeting.Target;
import lxx.targeting.TargetManagerListener;
import robocode.RobocodeFileOutputStream;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;

/**
 * User: Aleksey Zhidkov
 * Date: 14.06.12
 */
public class GfPatternDumper implements Plugin, TargetManagerListener {

    private List<Double> gfPattern;
    private Tomcat robot;

    public void roundStarted(Office office) {
        office.getTargetManager().addListener(this);
        robot = office.getRobot();
    }

    public void targetUpdated(Target target) {
        gfPattern = target.getCurrentSnapshot().getVisitedGuessFactors();
    }

    public void battleEnded() {
        try {
            ObjectOutputStream gf_pattern = new ObjectOutputStream(new RobocodeFileOutputStream(robot.getDataFile("gf_pattern")));
            gf_pattern.writeObject(gfPattern);
            gf_pattern.flush();
            gf_pattern.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tick() {
    }

}
