package lxx.targeting.tomcat_claws;

import lxx.bullets.LXXBullet;
import lxx.targeting.tomcat_claws.clustering.Cluster2D;
import lxx.utils.APoint;
import lxx.utils.AimingPredictionData;
import lxx.utils.LXXConstants;
import lxx.paint.LXXGraphics;

import java.awt.*;
import java.util.Set;

/**
 * User: jdev
 * Date: 17.06.11
 */
class TCPredictionData implements AimingPredictionData {

    private final Set<Cluster2D> clusters;
    private final APoint initialPos;

    public TCPredictionData(Set<Cluster2D> clusters, APoint initialPos) {
        this.clusters = clusters;
        this.initialPos = initialPos;
    }

    public void paint(LXXGraphics g, LXXBullet bullet) {
        for (Cluster2D cluster : clusters) {
            final Color borderColor = cluster.getColor();
            final Color fillColor = new Color(borderColor.getRed(), borderColor.getGreen(), borderColor.getBlue(), borderColor.getAlpha() / 10);
            for (APoint predictedPos : cluster.getEntries()) {
                g.setColor(fillColor);
                g.fillSquare(predictedPos, LXXConstants.ROBOT_SIDE_HALF_SIZE);
                g.setColor(borderColor);
                g.drawRect(predictedPos, LXXConstants.ROBOT_SIDE_HALF_SIZE);
            }
        }
    }
}
