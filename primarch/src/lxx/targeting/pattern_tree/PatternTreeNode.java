/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.targeting.pattern_tree;

import lxx.model.BattleSnapshot;
import lxx.office.AttributesManager;
import robocode.Rules;

import java.util.*;

import static java.lang.Math.*;

/**
 * User: jdev
 * Date: 21.02.11
 */
public class PatternTreeNode {

    public static int nodesCount = 0;

    private Map<EnemyMovementDecision, PatternTreeNode> children = new HashMap<EnemyMovementDecision, PatternTreeNode>();

    private final List<PredicateResult> predicateResults = new ArrayList<PredicateResult>();

    private final EnemyMovementDecision link;
    private final PatternTreeNode parent;
    private final int level;

    public int visitCount = 0;

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
        }
        child.visitCount++;
        predicateResults.add(new PredicateResult(predicate, child, link));
        if (predicateResults.size() > 2000) {
            for (int i = 0; i < predicateResults.size(); i++) {
                if (predicateResults.get(i).enemyMovementDecision.equals(link)) {
                    predicateResults.remove(i);
                    break;
                }
            }
        }

        return child;
    }

    public static EnemyMovementDecision getEnemyMovementDecision(BattleSnapshot predicate) {
        double turnRateRadians = toRadians(predicate.getAttrValue(AttributesManager.enemyTurnRate));
        double acceleration = predicate.getAttrValue(AttributesManager.enemyAcceleration);
        return new EnemyMovementDecision(acceleration, turnRateRadians);
    }

    public PatternTreeNode getChild(EnemyMovementDecision enemyMovementDecision) {
        return children.get(enemyMovementDecision);
    }

    public List<PatternTreeNodeSelectionData> getSortedNodes(BattleSnapshot battleSnapshot, int[] indexes, double[] weights) {
        final Map<PatternTreeNode, PatternTreeNodeSelectionData> nodesSelectionData = new HashMap<PatternTreeNode, PatternTreeNodeSelectionData>();
        for (PredicateResult pr : predicateResults) {
            if (pr.enemyMovementDecision.acceleration < -Rules.DECELERATION ||
                    pr.enemyMovementDecision.acceleration > Rules.ACCELERATION) {
                continue;
            }
            final double dist = pr.predicate.quickDistance(indexes, battleSnapshot, weights);
            PatternTreeNodeSelectionData sd = nodesSelectionData.get(pr.result);
            if (sd == null) {
                sd = new PatternTreeNodeSelectionData();
                nodesSelectionData.put(pr.result, sd);
            }
            if (dist < sd.minDist) {
                sd.battleSnapshot = pr.predicate;
                sd.minDist = dist;
                sd.decision = pr.enemyMovementDecision;
            }
        }

        final List<PatternTreeNodeSelectionData> sortedNodes = new ArrayList<PatternTreeNodeSelectionData>();
        double maxDist = 0;
        for (PatternTreeNodeSelectionData selectionData : nodesSelectionData.values()) {
            sortedNodes.add(selectionData);
            maxDist = max(maxDist, selectionData.minDist);
        }
        final double md = maxDist;
        Collections.sort(sortedNodes, new Comparator<PatternTreeNodeSelectionData>() {
            public int compare(PatternTreeNodeSelectionData o1, PatternTreeNodeSelectionData o2) {
                final Double o1quality = (1 - o1.minDist / md);
                final Double o2quality = (1 - o2.minDist / md);
                return (int) signum(o2quality - o1quality);
            }
        });

        return sortedNodes;
    }

    public PatternTreeNodeSelectionData getChildBySnapshot(BattleSnapshot battleSnapshot, int[] indexes, double[] weights) {
        PatternTreeNodeSelectionData selectionData = new PatternTreeNodeSelectionData();
        for (PredicateResult pr : predicateResults) {
            if (pr.enemyMovementDecision.acceleration < -Rules.DECELERATION ||
                    pr.enemyMovementDecision.acceleration > Rules.ACCELERATION) {
                continue;
            }
            final double dist = pr.predicate.quickDistance(indexes, battleSnapshot, weights);
            if (dist < selectionData.minDist) {
                selectionData.battleSnapshot = pr.predicate;
                selectionData.minDist = dist;
                selectionData.decision = pr.enemyMovementDecision;
            }
        }

        return selectionData;
    }

    public int getChildrenCount() {
        return children.size();
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

    public int getMinVisitCount() {
        int minVisitCount = Integer.MAX_VALUE;
        for (PatternTreeNode child : children.values()) {
            minVisitCount = min(minVisitCount, child.visitCount);
        }
        return minVisitCount;
    }

    public int getVisitCount() {
        return visitCount;
    }

    private class PredicateResult {

        private final BattleSnapshot predicate;
        private final PatternTreeNode result;
        private final EnemyMovementDecision enemyMovementDecision;

        private PredicateResult(BattleSnapshot predicate, PatternTreeNode result, EnemyMovementDecision enemyMovementDecision) {
            this.predicate = predicate;
            this.result = result;
            this.enemyMovementDecision = enemyMovementDecision;
        }

    }

    // todo(zhidkov): rename me
    public class PatternTreeNodeSelectionData {

        private EnemyMovementDecision decision;
        private BattleSnapshot battleSnapshot;
        private double minDist = Integer.MAX_VALUE;

        public EnemyMovementDecision getDecision() {
            return decision;
        }
    }

}
