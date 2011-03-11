/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.targeting.tomcat_claws;

import lxx.model.BattleSnapshot;
import lxx.office.AttributesManager;
import robocode.Rules;

import java.util.*;

import static java.lang.Math.abs;
import static java.lang.Math.toRadians;

/**
 * User: jdev
 * Date: 21.02.11
 */
public class PatternTreeNode {

    public static int nodesCount = 0;
    private static final EnemyMovementDecision NO_ENEMY_DECISION = new EnemyMovementDecision(0, 0);

    private Map<EnemyMovementDecision, PatternTreeNode> children = new HashMap<EnemyMovementDecision, PatternTreeNode>();

    private final ArrayList<PredicateResult> predicateResults = new ArrayList<PredicateResult>();

    private final EnemyMovementDecision link;
    private final PatternTreeNode parent;
    private final int level;

    public int visitCount = 0;
    // public for optimization
    public int childrenCount = 0;

    public PatternTreeNode(EnemyMovementDecision link, PatternTreeNode parent, int level) {
        this.link = link;
        this.parent = parent;
        this.level = level;
        nodesCount++;
    }

    public PatternTreeNode getParent() {
        return parent;
    }

    public PatternTreeNode addChild(BattleSnapshot predicate, BattleSnapshot currentState) {
        final EnemyMovementDecision link = getEnemyMovementDecision(currentState);
        PatternTreeNode child = children.get(link);
        if (child == null) {
            child = new PatternTreeNode(link, this, level + 1);
            children.put(link, child);
            childrenCount++;
        }
        child.visitCount++;
        predicateResults.add(0, new PredicateResult(predicate, link));
        if (predicateResults.size() > 2000) {
            for (Iterator<PredicateResult> predicateResultIterator = predicateResults.iterator(); predicateResultIterator.hasNext();) {
                PredicateResult pr = predicateResultIterator.next();
                if (pr.enemyMovementDecision.equals(link)) {
                    predicateResultIterator.remove();
                    break;
                }
            }
        }

        return child;
    }

    public static EnemyMovementDecision getEnemyMovementDecision(BattleSnapshot predicate) {
        final double turnRateRadians = toRadians(predicate.getAttrValue(AttributesManager.enemyTurnRate));
        final double acceleration = predicate.getAttrValue(AttributesManager.enemyAcceleration);
        return new EnemyMovementDecision(acceleration, turnRateRadians);
    }

    public PatternTreeNode getChild(EnemyMovementDecision enemyMovementDecision) {
        return children.get(enemyMovementDecision);
    }

    public PatternTreeNodeSelectionData getChildBySnapshot(BattleSnapshot battleSnapshot, int[] indexes, double[] weights) {
        final PatternTreeNodeSelectionData selectionData = new PatternTreeNodeSelectionData();
        selectionData.decision = NO_ENEMY_DECISION;
        // optimization
        final PredicateResult[] prs = predicateResults.toArray(new PredicateResult[predicateResults.size()]);
        final int prsLength = prs.length;
        final double enemyVelocityModule = battleSnapshot.getEnemyVelocityModule();
        final double maxTurnRateRadians = Rules.getTurnRateRadians(enemyVelocityModule) + 0.01;
        for (int i = 0; i < prsLength; i++) {
            final PredicateResult pr = prs[i];
            if (!isValidEnemyDecision(pr, enemyVelocityModule, maxTurnRateRadians)) {
                continue;
            }
            final double dist = pr.predicate.quickDistance(indexes, battleSnapshot, weights);
            if (dist < selectionData.minDist) {
                selectionData.minDist = dist;
                selectionData.decision = pr.enemyMovementDecision;
            }
        }

        return selectionData;
    }

    private static boolean isValidEnemyDecision(PredicateResult pr, double enemyVelocity, final double maxTurnRateRadians) {
        final double acceleration = pr.enemyMovementDecision.acceleration;
        final double newVelocity = enemyVelocity + acceleration;
        return acceleration >= -Rules.DECELERATION &&
                acceleration <= Rules.ACCELERATION &&
                newVelocity <= Rules.MAX_VELOCITY &&
                newVelocity >= 0 &&
                abs(pr.enemyMovementDecision.turnRateRadians) <= maxTurnRateRadians;
    }

    public String getPath() {
        if (parent == null) {
            return "root";
        }

        return getParent().getPath() + "->" + link.key;
    }

    public String toString() {
        return "Path: " + getPath() + "\n" +
                "level: " + level + ", link = " + link + "\n" +
                "children: " + children.keySet();
    }

    public int getVisitCount() {
        return visitCount;
    }

    private class PredicateResult {

        private final BattleSnapshot predicate;
        private final EnemyMovementDecision enemyMovementDecision;

        private PredicateResult(BattleSnapshot predicate, EnemyMovementDecision enemyMovementDecision) {
            this.predicate = predicate;
            this.enemyMovementDecision = enemyMovementDecision;
        }

    }

    // todo(zhidkov): rename me
    public class PatternTreeNodeSelectionData {

        private EnemyMovementDecision decision;
        private double minDist = Integer.MAX_VALUE;

        public EnemyMovementDecision getDecision() {
            return decision;
        }
    }

}
