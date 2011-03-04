package kc.serpent.gun;

import kc.serpent.utils.*;

public interface GunSystem {
    public void init(GunBase g);

    public void reset();

    public void wavePassed(double GF, GunWave w);

    public void printStats();

    public double getFiringAngle(GunWave w);

    public String getName();
}
