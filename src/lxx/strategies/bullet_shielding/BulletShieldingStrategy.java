package lxx.strategies.bullet_shielding;

import lxx.MySnapshot;
import lxx.Tomcat;
import lxx.bullets.LXXBullet;
import lxx.bullets.enemy.EnemyBulletManager;
import lxx.office.Office;
import lxx.strategies.FirePowerSelector;
import lxx.strategies.Gun;
import lxx.strategies.GunDecision;
import lxx.strategies.Movement;
import lxx.strategies.duel.DuelStrategy;
import lxx.targeting.Target;
import lxx.targeting.TargetManager;
import lxx.utils.APoint;
import lxx.utils.LXXUtils;
import robocode.Rules;
import robocode.util.Utils;

/**
 * User: Aleksey Zhidkov
 * Date: 21.06.12
 */
public class BulletShieldingStrategy extends DuelStrategy {

    public BulletShieldingStrategy(Tomcat robot, Movement withBulletsMovement, Gun gun, FirePowerSelector firePowerSelector, TargetManager targetManager, EnemyBulletManager enemyBulletManager, Office office) {
        super(robot, withBulletsMovement, gun, firePowerSelector, targetManager, enemyBulletManager, office);
    }

    @Override
    public boolean match() {
        return super.match()/* && (target == null || target.getEnergy() > robot.getEnergy() * 1.1)*/ && enemyBulletManager.getBulletsOnAir(0).size() > 0;
    }

    @Override
    protected GunDecision getGunDecision(Target target, double firePower) {
        final LXXBullet closestBullet = enemyBulletManager.getBulletsOnAir(0).get(0);
        double ebTravelledDist = closestBullet.getTravelledDistance();
        final double ebSpeed = closestBullet.getSpeed();

        final MySnapshot currentSnapshot = robot.getCurrentSnapshot();
        ebTravelledDist += ebSpeed * currentSnapshot.getTurnsToGunCool();
        APoint firePos = currentSnapshot.project(currentSnapshot.getAbsoluteHeadingRadians(), currentSnapshot.getSpeed());
        double mbTravelledDist = 0;
        final double mbSpeed = Rules.getBulletSpeed(0.1);
        APoint[] intersection;

        while ((intersection = LXXUtils.intersection(closestBullet.getFirePosition(), ebTravelledDist, firePos, mbTravelledDist)).length == 0) {
            ebTravelledDist += ebSpeed;
            mbTravelledDist += mbSpeed;
        }

        final double alpha;
        if (intersection.length == 1) {
            alpha = firePos.aDistance(intersection[0]);
        } else {
            final double alpha1 = firePos.aDistance(intersection[0]);
            final double alpha2 = firePos.aDistance(intersection[1]);
            alpha = Utils.normalAbsoluteAngle(alpha1 + alpha2) / 2;
        }

        return new GunDecision(Utils.normalRelativeAngle(alpha - robot.getGunHeadingRadians()), new BSAimingPredictionData(firePos, intersection));
    }

    @Override
    protected double selectFirePower(Target target) {
        return 0.1;
    }
}
