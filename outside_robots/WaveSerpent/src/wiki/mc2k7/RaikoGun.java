package wiki.mc2k7;

import robocode.AdvancedRobot;
import robocode.Condition;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
/*
	Raiko 0.41 by Jamougha.  
						
	Credit for ideas or parts of this code go to PEZ, Kawigi and probably others who I've forgotten to mention.
	
	Released under the RWPCL.
	
	Modified to be plugged into MC2K7 bots by Simonton.
	http://robowiki.net/?MovementChallenge2K7
	
	TO USE IN THE MOVEMENT CHALLENGE 2K7 - put your bot's movement code into
	this template.
	
	public class MyMC2K7Bot extends AdvancedRobot {
		static RaikoGun raikoGun;
		
		public static void run() {
			
			// place your code here - MUST BE NON-BLOCKING CALLS ONLY
			
			if (raikoGun == null) {
				raikoGun = new RaikoGun(this);
			}
			raikoGun.run();
		}
		
		public static void onScannedRobot(ScannedRobotEvent e) {
			
			raikoGun.onScannedRobot(e);
			
			// place your code here - DON'T AFFECT FIREPOWER, GUN TURN, OR RADAR
		}
		
		// you can certainly place code here, too.
	}
*/

public class RaikoGun {

    //	private static final double BEST_DISTANCE = 525;
//	private static boolean flat = true;
    private static double bearingDirection = 1, lastLatVel, lastVelocity, /*lastReverseTime, circleDir = 1, enemyFirePower,*/
            enemyEnergy, enemyDistance, lastVChangeTime, enemyLatVel, enemyVelocity/*, enemyFireTime, numBadHits*/;
    private static Point2D.Double enemyLocation;
    private static final int GF_ZERO = 15;
    private static final int GF_ONE = 30;
    private static String enemyName;
    private static int[][][][][][] guessFactors = new int[3][5][3][3][8][GF_ONE + 1];
//	private static double numWins;

    private AdvancedRobot bot;

    public RaikoGun(AdvancedRobot bot) {
        this.bot = bot;
    }

    public void run() {
//        setColors(Color.red, Color.white, Color.white);
        bot.setAdjustGunForRobotTurn(true);
        bot.setAdjustRadarForGunTurn(true);
        do {
            bot.turnRadarRightRadians(Double.POSITIVE_INFINITY);
        } while (true);
    }

    public void onScannedRobot(ScannedRobotEvent e) {


        /*-------- setup data -----*/
        if (enemyName == null) {

            enemyName = e.getName();
//			restoreData();		
        }
        Point2D.Double robotLocation = new Point2D.Double(bot.getX(), bot.getY());
        double theta;
        double enemyAbsoluteBearing = bot.getHeadingRadians() + e.getBearingRadians();
        enemyDistance = e.getDistance();
        enemyLocation = projectMotion(robotLocation, enemyAbsoluteBearing, enemyDistance);

//        if ((enemyEnergy -= e.getEnergy()) >= 0.1 && enemyEnergy <= 3.0) {
//            enemyFirePower = enemyEnergy;
//			enemyFireTime = bot.getTime();
//		}

        enemyEnergy = e.getEnergy();

        Rectangle2D.Double BF = new Rectangle2D.Double(18, 18, 764, 564);

//		/* ---- Movement ---- */
//		
//		Point2D.Double newDestination;
//		
//		double distDelta = 0.02 + Math.PI/2 + (enemyDistance > BEST_DISTANCE  ? -.1 : .5);
//		
//		while (!BF.contains(newDestination = projectMotion(robotLocation, enemyAbsoluteBearing + circleDir*(distDelta-=0.02), 170)));
//
//		theta = 0.5952*(20D - 3D*enemyFirePower)/enemyDistance;
//		if ( (flat && Math.random() > Math.pow(theta, theta)) || distDelta < Math.PI/5 || (distDelta < Math.PI/3.5 && enemyDistance < 400) ){
//			circleDir = -circleDir;
//			lastReverseTime = getTime();
//		}
//		
//		theta = absoluteBearing(robotLocation, newDestination) - getHeadingRadians();
//		setAhead(Math.cos(theta)*100);
//		setTurnRightRadians(Math.tan(theta));
//		

        /* ------------- Fire control ------- */

        /*
              To explain the below; if the enemy's absolute acceleration is
              zero then we segment on time since last velocity change, lateral
              acceleration and lateral velocity.
              If their absolute acceleration is non zero then we segment on absolute
              acceleration and absolute velocity.
              Regardless we segment on walls (near/far approach to walls) and distance.
              I'm trying to have my cake and eat it, basically. :-)
          */
        MicroWave w = new MicroWave();

        lastLatVel = enemyLatVel;
        lastVelocity = enemyVelocity;
        enemyLatVel = (enemyVelocity = e.getVelocity()) * Math.sin(e.getHeadingRadians() - enemyAbsoluteBearing);

        int distanceIndex = (int) enemyDistance / 140;

        double bulletPower = distanceIndex == 0 ? 3 : 2;
        theta = Math.min(bot.getEnergy() / 4, Math.min(enemyEnergy / 4, bulletPower));
        if (theta == bulletPower)
            bot.addCustomEvent(w);
        bulletPower = theta;
        w.bulletVelocity = 20D - 3D * bulletPower;

        int accelIndex = (int) Math.round(Math.abs(enemyLatVel) - Math.abs(lastLatVel));

        if (enemyLatVel != 0)
            bearingDirection = enemyLatVel > 0 ? 1 : -1;
        w.bearingDirection = bearingDirection * Math.asin(8D / w.bulletVelocity) / GF_ZERO;

        double moveTime = w.bulletVelocity * lastVChangeTime++ / enemyDistance;
        int bestGF = moveTime < .1 ? 1 : moveTime < .3 ? 2 : moveTime < 1 ? 3 : 4;

        int vIndex = (int) Math.abs(enemyLatVel / 3);

        if (Math.abs(Math.abs(enemyVelocity) - Math.abs(lastVelocity)) > .6) {
            lastVChangeTime = 0;
            bestGF = 0;

            accelIndex = (int) Math.round(Math.abs(enemyVelocity) - Math.abs(lastVelocity));
            vIndex = (int) Math.abs(enemyVelocity / 3);
        }

        if (accelIndex != 0)
            accelIndex = accelIndex > 0 ? 1 : 2;

        w.firePosition = robotLocation;
        w.enemyAbsBearing = enemyAbsoluteBearing;
        //now using PEZ' near-wall segment
        w.waveGuessFactors = guessFactors[accelIndex][bestGF][vIndex][BF.contains(projectMotion(robotLocation, enemyAbsoluteBearing + w.bearingDirection * GF_ZERO, enemyDistance)) ? 0 : BF.contains(projectMotion(robotLocation, enemyAbsoluteBearing + .5 * w.bearingDirection * GF_ZERO, enemyDistance)) ? 1 : 2][distanceIndex];


        bestGF = GF_ZERO;

        for (int gf = GF_ONE; gf >= 0 && enemyEnergy > 0; gf--)
            if (w.waveGuessFactors[gf] > w.waveGuessFactors[bestGF])
                bestGF = gf;

        bot.setTurnGunRightRadians(Utils.normalRelativeAngle(enemyAbsoluteBearing - bot.getGunHeadingRadians() + w.bearingDirection * (bestGF - GF_ZERO)));


        if (bot.getEnergy() > 1 || distanceIndex == 0)
            bot.setFire(bulletPower);

        bot.setTurnRadarRightRadians(Utils.normalRelativeAngle(enemyAbsoluteBearing - bot.getRadarHeadingRadians()) * 2);

    }
//    public void onHitByBullet(HitByBulletEvent e) {
//		/* 
//		The infamous Axe-hack
//	 	see: http://robowiki.net/?Musashi 
//		*/
//		if ((double)(bot.getTime() - lastReverseTime) > enemyDistance/e.getVelocity() && enemyDistance > 200 && !flat) 
//	    	flat = (++numBadHits/(bot.getRoundNum()+1) > 1.1);
//    }


    private static Point2D.Double projectMotion(Point2D.Double loc, double heading, double distance) {

        return new Point2D.Double(loc.x + distance * Math.sin(heading), loc.y + distance * Math.cos(heading));
    }

    private static double absoluteBearing(Point2D.Double source, Point2D.Double target) {
        return Math.atan2(target.x - source.x, target.y - source.y);
    }


//	public void onWin(WinEvent e){
//		numWins++;
//		saveData();
//	}
//	public void onDeath(DeathEvent e){
//		saveData();	
//	}

//	//Stole Kawigi's smaller save/load methods
//	private void restoreData(){
//		try
//		{
//			ObjectInputStream in = new ObjectInputStream(new GZIPInputStream(new FileInputStream(bot.getDataFile(enemyName))));
//			guessFactors = (int[][][][][][])in.readObject();
//			in.close();
//		} catch (Exception ex){flat = false;}
//	}

//	private void saveData()
//	{
//		if (flat && numWins/(getRoundNum()+1) < .7 && getNumRounds() == getRoundNum()+1)
//		try{
//			ObjectOutputStream out = new ObjectOutputStream(new GZIPOutputStream(new RobocodeFileOutputStream(getDataFile(enemyName))));
//			out.writeObject(guessFactors);
//			out.close();
//		}
//		catch (IOException ex){}
//	}


    class MicroWave extends Condition {

        Point2D.Double firePosition;
        int[] waveGuessFactors;
        double enemyAbsBearing, distance, bearingDirection, bulletVelocity;

        public boolean test() {

            if ((RaikoGun.enemyLocation).distance(firePosition) <= (distance += bulletVelocity) + bulletVelocity) {
                try {
                    waveGuessFactors[(int) Math.round((Utils.normalRelativeAngle(absoluteBearing(firePosition, RaikoGun.enemyLocation) - enemyAbsBearing)) / bearingDirection + GF_ZERO)]++;
                } catch (ArrayIndexOutOfBoundsException e) {
                }
                bot.removeCustomEvent(this);
            }
            return false;
        }
    }


}
