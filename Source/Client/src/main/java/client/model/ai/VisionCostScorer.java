package client.model.ai;

import java.util.ArrayList;
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
    private final ShortestPathFinder shortestPathFinder;
    private final StrategyGuide strategyGuide;

    VisionCostScorer(WayHelper wayHelper, ShortestPathFinder shortestPathFinder, StrategyGuide strategyGuide) {
        this.wayHelper = wayHelper;
        this.shortestPathFinder = shortestPathFinder;
        this.strategyGuide = strategyGuide;
    }

    float score(GameState gameState, MapNode currentMapNode, MapNode node, Objective objective) {
        if (Objects.isNull(node) || Objects.isNull(gameState) || Objects.isNull(gameState.getMap())) return -1;

        ArrayList<MapNode> visitedGrassNodes = new ArrayList<>();
        if (objective == Objective.TREASURE) {
            for (MapNode grassNode : wayHelper.getHalfMapVisitedGrassFields().keySet()) {
                if (Boolean.TRUE.equals(wayHelper.getHalfMapVisitedGrassFields().get(grassNode))) {
                    visitedGrassNodes.add(grassNode);
                }
            }
        } else {
            for (MapNode grassNode : wayHelper.getOppHalfMapVisitedGrassFields().keySet()) {
                if (Boolean.TRUE.equals(wayHelper.getOppHalfMapVisitedGrassFields().get(grassNode))) {
                    visitedGrassNodes.add(grassNode);
                }
            }
        }

        MapNode previousTile = currentMapNode;
        float cost = 0;
        float grassVision = 0;

        ArrayList<MapNode> path = shortestPathFinder.findShortestPath(currentMapNode, node);
        for (MapNode tile : path) {
            if (tile.getTerrain() == Terrain.GRASS && !visitedGrassNodes.contains(tile)) {
                grassVision += 1;
            }
            cost += shortestPathFinder.getCostToReachNode(previousTile, tile);
            previousTile = tile;
        }

        if (node.getTerrain() == Terrain.MOUNTAIN) {
            ArrayList<MapNode> grassFromVision = strategyGuide.getGrassNodesFromExtendedVision(node);
            for (MapNode grassNode : grassFromVision) {
                if (!visitedGrassNodes.contains(grassNode)) {
                    grassVision += 1;
                }
            }
        }

        return grassVision > 0 ? grassVision / cost : -1;
    }
}
