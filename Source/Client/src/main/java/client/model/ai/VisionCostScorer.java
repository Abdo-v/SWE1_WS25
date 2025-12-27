package client.model.ai;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

import client.model.GameState;
import client.model.mapper.MapNode;
import client.model.mapper.Terrain;

/**
 * Computes the "vision per cost" score used by the exploration strategy.
 *
 * Package-private: internal AI refactoring helper.
 */
final class VisionCostScorer {

    private final WayHelper wayHelper;
    private final StrategyGuide strategyGuide;

    VisionCostScorer(WayHelper wayHelper, ShortestPathFinder shortestPathFinder, StrategyGuide strategyGuide) {
        this.wayHelper = wayHelper;
        this.strategyGuide = strategyGuide;
    }

    float score(GameState gameState, MapNode currentMapNode, MapNode node, Objective objective, Map<MapNode, Integer> costMap) {
        Objects.requireNonNull(gameState, "gameState is required");
        Objects.requireNonNull(currentMapNode, "currentMapNode is required");
        Objects.requireNonNull(node, "node is required");
        Objects.requireNonNull(objective, "objective is required");
        Objects.requireNonNull(costMap, "costMap is required");

        if (costMap.isEmpty()) {
            return -1;
        }

        int cost = costMap.getOrDefault(node, Integer.MAX_VALUE);
        if (cost == Integer.MAX_VALUE || cost <= 0) {
            return -1;
        }

        float benefit = computeBenefit(node, objective);
        return benefit > 0 ? (benefit / cost) : -1;
    }

    private float computeBenefit(MapNode node, Objective objective) {
        Objects.requireNonNull(node, "node is required");
        Objects.requireNonNull(objective, "objective is required");

        // Benefit model (simple + stable):
        // - Unvisited Grass: 1 (reveals itself)
        // - Mountain: number of not-yet-visited grass nodes in its extended vision
        // This matches the game mechanic where mountains reveal their 8 neighbors.

        if (node.getTerrain() == Terrain.GRASS) {
            boolean visited = isGrassVisited(node, objective);
            return visited ? 0 : 1;
        }

        if (node.getTerrain() == Terrain.MOUNTAIN) {
            boolean mountainVisited = Boolean.TRUE.equals(wayHelper.getAllMountainFieldsMap().get(node));
            if (mountainVisited) {
                return 0;
            }

            float revealCount = 0;
            ArrayList<MapNode> grassFromVision = strategyGuide.getGrassNodesFromExtendedVision(node);
            for (MapNode grassNode : grassFromVision) {
                if (!isGrassVisited(grassNode, objective)) {
                    revealCount += 1;
                }
            }
            return revealCount;
        }

        return 0;
    }

    private boolean isGrassVisited(MapNode grassNode, Objective objective) {
        if (grassNode.getTerrain() != Terrain.GRASS) {
            return false;
        }

        if (objective == Objective.TREASURE) {
            return Boolean.TRUE.equals(wayHelper.getHalfMapVisitedGrassFields().get(grassNode));
        }

        // Fort phase: only grass nodes inside the (potentially filtered) opponent traversal map are relevant.
        // If the node is not even part of that key-set, treat it as "already visited" (benefit = 0).
        if (!wayHelper.getOppHalfMapVisitedGrassFields().containsKey(grassNode)) {
            return true;
        }
        return Boolean.TRUE.equals(wayHelper.getOppHalfMapVisitedGrassFields().get(grassNode));
    }
}
