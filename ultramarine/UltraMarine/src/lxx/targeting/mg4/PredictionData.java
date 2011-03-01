package lxx.targeting.mg4;

import lxx.targeting.TargetImage;
import lxx.targeting.Target;
import lxx.autosegmentation.model.FireSituation;
import static lxx.StaticData.robot;
import lxx.utils.LXXPoint;

import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.awt.*;
import static java.lang.Math.toRadians;
import static java.lang.Math.sin;
import static java.lang.Math.cos;

/**
 * User: jdev
 * Date: 15.05.2010
 */
public class PredictionData {

    private final Map<DeltaVector, Double> matches;
    private final Pattern bestPattern;
    private final TargetImage targetImage;
    private final Target target;
    private final FireSituation fireSituation;
    private final LXXPoint sourcePos;
    private final MegaGun4 megaGun4;

    public PredictionData(Map<DeltaVector, Double> matches, Pattern bestPattern, TargetImage targetImage, Target target, FireSituation fireSituation, LXXPoint sourcePos, MegaGun4 megaGun4) {
        this.matches = matches;
        this.bestPattern = bestPattern;
        this.targetImage = targetImage;
        this.fireSituation = fireSituation;
        this.sourcePos = sourcePos;
        this.megaGun4 = megaGun4;
        this.target = target;
    }

    public void paint(Graphics2D g) {
        double maxMatch = -1;
        double minMatch = Integer.MAX_VALUE;
        DeltaVector dv = null;
        for (Map.Entry<DeltaVector, Double> e : matches.entrySet()) {
            if (e.getValue() > maxMatch) {
                maxMatch = e.getValue();
                dv = e.getKey();
            }
            if (e.getValue() < minMatch) {
                minMatch = e.getValue();
            }
        }

        if (bestPattern == null) {
            return;
        }

        drawPattern(g, bestPattern, 0);
        Pattern curPattern = megaGun4.getPattern(megaGun4.getDeltaVector(targetImage, target, sourcePos));
        if (curPattern != null) {
            drawPattern(g, curPattern, (int) (robot.getBattleFieldWidth() / 2));
        }

        Set<Integer> alphas = new HashSet<Integer>();
        for (Map.Entry<DeltaVector, Double> e : matches.entrySet()) {
            double maxEscapeDist = targetImage.getMaxEscapeDistance(toRadians(e.getKey().alpha * MegaGun4.ANGLE_SCALE), sourcePos);
            if (!alphas.contains(e.getKey().alpha)) {
                LXXPoint newPos = new LXXPoint(targetImage.getX() + sin(targetImage.getAbsoluteHeading() + toRadians(e.getKey().alpha * MegaGun4.ANGLE_SCALE)) * maxEscapeDist,
                        targetImage.getY() + cos(targetImage.getAbsoluteHeading() + toRadians(e.getKey().alpha * MegaGun4.ANGLE_SCALE)) * maxEscapeDist);
                g.setColor(new Color(0, 255, 0, 50));
                g.fillOval((int) newPos.getX() - 4, (int) newPos.getY() - 4, 8, 8);
                g.drawLine((int) targetImage.getX(), (int) targetImage.getY(), (int) newPos.getX(), (int) newPos.getY());
                alphas.add(e.getKey().alpha);
            }
            LXXPoint newPos = new LXXPoint(targetImage.getX() + sin(targetImage.getAbsoluteHeading() + toRadians(e.getKey().alpha * MegaGun4.ANGLE_SCALE)) * e.getKey().dist * maxEscapeDist,
                    targetImage.getY() + cos(targetImage.getAbsoluteHeading() + toRadians(e.getKey().alpha * MegaGun4.ANGLE_SCALE)) * e.getKey().dist * maxEscapeDist);
            g.setColor(new Color((int) ((e.getValue() - minMatch) / (maxMatch - minMatch) * 255), 0, 0));
            g.fillOval((int) newPos.getX() - 3, (int) newPos.getY() - 3, 6, 6);
        }

        double maxEscapeDist = targetImage.getMaxEscapeDistance(toRadians(dv.alpha * MegaGun4.ANGLE_SCALE), sourcePos);
        LXXPoint newPos = new LXXPoint(targetImage.getX() + sin(targetImage.getAbsoluteHeading() + toRadians(dv.alpha * MegaGun4.ANGLE_SCALE)) * dv.dist * maxEscapeDist,
                targetImage.getY() + cos(targetImage.getAbsoluteHeading() + toRadians(dv.alpha * MegaGun4.ANGLE_SCALE)) * dv.dist * maxEscapeDist);
        g.setColor(Color.WHITE);
        g.drawRect((int) newPos.getX() - 16, (int) newPos.getY() - 16, 32, 32);
    }

    private void drawPattern(Graphics2D g, Pattern pattern, int dX) {
        bestPattern.match(fireSituation);
        java.util.List<AttributeValuesRange> ranges = pattern.getRanges();
        int height = (int) robot.getBattleFieldHeight();
        int width = (int) robot.getBattleFieldWidth() / 2;
        int rangeHeight = height / ranges.size();
        int idx = 0;
        for (AttributeValuesRange range : ranges) {
            g.setColor(Color.WHITE);
            g.drawLine(0 + dX, rangeHeight * idx, width + dX, rangeHeight * idx);
            double maxVisitCount = 0;
            for (Integer attrValue : range.getAttrValues()) {
                try {
                if (range.getVisitCount(attrValue) > maxVisitCount) {
                    maxVisitCount = range.getVisitCount(attrValue);
                }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
            int valueRange;
            if (range.getAttribute().getActualMax() == range.getAttribute().getActualMin()) {
                valueRange = 1;
            } else {
                valueRange = range.getAttribute().getActualMax() - range.getAttribute().getActualMin();
            }
            int barWidth = width / valueRange;
            int barIdx = 0;
            g.drawString(range.getAttribute().getName(), 30 + dX, rangeHeight * (idx + 1) - 15);
            final Color barColor = new Color(0 ,255, 0, 105);
            g.setColor(barColor);
            for (int attrValue = range.getAttribute().getActualMin(); attrValue <= range.getAttribute().getActualMax(); attrValue++) {
                Double visitCount = range.getVisitCount(attrValue);
                if (visitCount == null || visitCount == 0) {
                    continue;
                }
                int barHeight = (int) (rangeHeight * ((double) visitCount / maxVisitCount));
                if (attrValue == fireSituation.getAttributeValue(range.getAttribute())) {
                    g.setColor(Color.YELLOW);
                } else if (g.getColor() != barColor) {
                    g.setColor(barColor);
                }
                g.fillRect(barWidth * barIdx + dX, rangeHeight * idx, barWidth, barHeight - 1);
                /*g.setColor(Color.WHITE);
                g.drawRect(barWidth * barIdx, rangeHeight * idx, barWidth, barHeight - 1);*/
                barIdx++;
            }
            int avgHeight = (int) ((int) rangeHeight * idx + (rangeHeight * ((double) range.getAvgVisitCount() / maxVisitCount)));
            g.setColor(Color.BLUE);
            g.drawLine(dX, avgHeight, width + dX, avgHeight);
            idx++;
        }
        g.setFont(new Font("Arial", Font.BOLD, 25));
        g.setColor(Color.BLUE);
        g.drawString(Integer.toString(pattern.getFsCount()), 10 + dX, rangeHeight * 2 - 10);
    }

}
