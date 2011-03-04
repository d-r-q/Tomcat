package kc.serpent.movement;

public interface MovementSystem {
    public void init(MovementBase base);

    public void reset();

    public void logHit(double GF, MovementWave w);

    public void logVisit(double GF, MovementWave w);

    public void setWaveFeatures(MovementWave w);

    public void setWaveData(MovementWave w);

    public double getRisk(double GF, int movementMode, MovementWave w);

    public double getRisk(double[] window, int movementMode, MovementWave w);

    public void paint(MovementWave w, int movementMode, java.awt.Graphics2D g);

    public void printStats();
}