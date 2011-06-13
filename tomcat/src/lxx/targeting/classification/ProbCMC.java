package lxx.targeting.classification;

import lxx.model.TurnSnapshot;
import lxx.model.attributes.Attribute;
import lxx.segmentation_tree.EntryMatch;
import lxx.segmentation_tree.SegmentationTreeEntry;
import lxx.strategies.MovementDecision;

import java.util.List;
import java.util.Map;

import static java.lang.Math.round;

/**
 * User: jdev
 * Date: 12.06.11
 */
public class ProbCMC extends ComplexMovementClassifier {

    public ProbCMC(Attribute[] accelAttrs, Attribute[] turnAttrs, Map<Attribute, Integer> attrRanges) {
        super(accelAttrs, turnAttrs, attrRanges);
    }

    public MovementDecision classify(TurnSnapshot turnSnapshot) {
        final List<SegmentationTreeEntry<MovementDecision>> similarEntries = accelLog.getSimilarEntries(getLimits(turnSnapshot));

        final MovementDecision md;
        if (similarEntries.size() == 0) {
            md = accelLog.getClosestEntry(turnSnapshot);
        } else {
            md = similarEntries.get(0).result;
        }
        if (md == null) {
            return null;
        }

        int[] visits = new int[4];
        for (SegmentationTreeEntry<MovementDecision> e : similarEntries) {
            visits[((int) (round(e.result.getAcceleration()) + 2))]++;
        }

        double acceleration = 0;
        int maxVisits = 0;
        for (int i = 0; i < visits.length; i++) {
            if (visits[i] > maxVisits) {
                maxVisits = visits[i];
                acceleration = i - 2;

            }
        }
        final MovementDecision.MovementDirection dir = md.getMovementDirection();
        final double turnRate = turnLog.getClosestEntry(turnSnapshot).getTurnRateRadians();

        return new MovementDecision(acceleration, turnRate, dir);
    }
}
