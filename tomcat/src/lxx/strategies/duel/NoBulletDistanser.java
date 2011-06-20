package lxx.strategies.duel;

import lxx.LXXRobotState;
import lxx.Tomcat;
import lxx.office.Office;
import lxx.utils.APoint;
import lxx.utils.LXXConstants;
import lxx.utils.LXXUtils;
import robocode.util.Utils;

/**
 * User: jdev
 * Date: 20.06.11
 */
public class NoBulletDistanser implements Distancer {

    private static final double MAX_ATTACK_DELTA = LXXConstants.RADIANS_40;
    private static final double MIN_ATTACK_DELTA = LXXConstants.RADIANS_15;

    private static final double DISTANCE = 450;

    private final Office office;
    private final Tomcat robot;
    private final double gunCoolingRate;

    public NoBulletDistanser(Office office) {
        this.office = office;
        this.robot = office.getRobot();
        this.gunCoolingRate = office.getRobot().getGunCoolingRate();
    }

    public double getDesiredHeading(APoint surfPoint, LXXRobotState robot, WaveSurfingMovement.OrbitDirection orbitDirection) {
        final double maxCoolingTime = LXXConstants.INITIAL_GUN_HEAT / this.robot.getGunCoolingRate();
        final double currentCoolingTime = office.getTargetManager().getDuelOpponent().getGunHeat() / gunCoolingRate;
        final double distanceBetween = robot.aDistance(surfPoint);

        final double maxAttackAngle = LXXConstants.RADIANS_95 + MAX_ATTACK_DELTA * (currentCoolingTime / maxCoolingTime);
        final double minAttackAngle = LXXConstants.RADIANS_85 - MIN_ATTACK_DELTA * (currentCoolingTime / maxCoolingTime);
        final double attackAngle = LXXConstants.RADIANS_90 + (MAX_ATTACK_DELTA * (distanceBetween - DISTANCE) / DISTANCE);

        return Utils.normalAbsoluteAngle(surfPoint.angleTo(robot) +
                LXXUtils.limit(minAttackAngle, attackAngle, maxAttackAngle) * orbitDirection.sign);
    }
}
