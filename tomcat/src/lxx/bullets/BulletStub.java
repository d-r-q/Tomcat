package lxx.bullets;

import robocode.Bullet;
import robocode.Rules;
import robocode.peer.BulletPeer;

import static java.lang.Math.toDegrees;

/**
 * User: jdev
 * Date: 19.06.11
 */
public class BulletStub extends Bullet {

    private double heading;
    private double x;
    private double y;
    private double bulletPower;
    private String name;
    private String victim;
    private boolean isActive;
    private int id;

    public BulletStub(double heading, double x, double y, double bulletPower, String name, String victim, boolean isActive, int id) {
        super((BulletPeer)null);

        this.heading = heading;
        this.x = x;
        this.y = y;
        this.bulletPower = bulletPower;
        this.name = name;
        this.victim = victim;
        this.isActive = isActive;
        this.id = id;
    }

    @Override
    public double getHeading() {
        return toDegrees(heading);
    }

    @Override
    public double getHeadingRadians() {
        return heading;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public double getPower() {
        return bulletPower;
    }

    @Override
    public double getVelocity() {
        return Rules.getBulletSpeed(bulletPower);
    }

    @Override
    public String getVictim() {
        return victim;
    }

    @Override
    public double getX() {
        return x;
    }

    @Override
    public double getY() {
        return y;
    }

    @Override
    public boolean isActive() {
        return isActive;
    }
}
