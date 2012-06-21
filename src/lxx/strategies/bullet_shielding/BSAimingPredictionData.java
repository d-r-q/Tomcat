package lxx.strategies.bullet_shielding;

import lxx.bullets.LXXBullet;
import lxx.paint.LXXGraphics;
import lxx.ts_log.TurnSnapshot;
import lxx.utils.APoint;
import lxx.utils.AimingPredictionData;

import java.awt.*;

/**
 * User: jdev
 * Date: 21.06.12
 */
public class BSAimingPredictionData implements AimingPredictionData {

    private final APoint firePos;
    private final APoint[] intersection;

    public BSAimingPredictionData(APoint firePos, APoint[] intersection) {
        this.firePos = firePos;
        this.intersection = intersection;
    }

    public void paint(LXXGraphics g, LXXBullet bullet) {
        g.setColor(new Color(255, 255, 255, 150));
        g.fillCircle(firePos, 7);

        for (APoint iPnt : intersection) {
            g.fillCircle(iPnt, 3);
        }
    }

    public TurnSnapshot getTs() {
        return null;
    }

    public long getPredictionRoundTime() {
        return 0;
    }

}
