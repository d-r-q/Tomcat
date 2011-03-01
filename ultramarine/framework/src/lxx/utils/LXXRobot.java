package lxx.utils;

/**
 * User: jdev
 * Date: 07.11.2009
 */
public interface LXXRobot extends APoint {
    
    double getHeading();
    double getAbsoluteHeading();
    String getName();
    long getTime();

    double getVelocity();

    double getEnergy();
}
