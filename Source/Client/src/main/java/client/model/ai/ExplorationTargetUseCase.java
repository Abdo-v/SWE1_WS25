package client.model.ai;

import java.util.ArrayList;

import client.exception.AIDecisionException;
import client.model.GameState;
import client.model.mapper.MapNode;
import client.model.mapper.Terrain;

/**
 * Handles choosing the next exploration target (best node) when there is no immediate treasure/fort target.
 *
 * Package-private: internal AI refactoring helper.
 */
final class ExplorationTargetUseCase {

    private final WayHelper wayHelper;
    private final TreasureSeeker treasureSeeker;
    private final VisionCostScorer scorer;

    ExplorationTargetUseCase(WayHelper wayHelper, TreasureSeeker treasureSeeker, VisionCostScorer scorer) {
        this.wayHelper = wayHelper;
        this.treasureSeeker = treasureSeeker;
        this.scorer = scorer;
    }

    MapNode selectBestNode(GameState gameState, MapNode currentMapNode, boolean treasureHunting) throws AIDecisionException {
        try {
            ArrayList<MapNode> visitedHalfMapNodes = new ArrayList<>();

            if (treasureHunting && (wayHelper.getHalfMapVisitedGrassFields() == null || wayHelper.getAllMountainFieldsMap() == null)) {
                throw new AIDecisionException(
                        "Cannot traverse half map: required data structures not initialized for treasure hunting",
                        "WayFinder",
                        "traverseHalfMap",
                        currentMapNode
                );
            }

            if (!treasureHunting && (wayHelper.getOppHalfMapVisitedGrassFields() == null || wayHelper.getAllMountainFieldsMap() == null)) {
                throw new AIDecisionException(
                        "Cannot traverse half map: required data structures not initialized for fort seeking",
                        "WayFinder",
                        "traverseHalfMap",
                        currentMapNode
                );
            }

            if (treasureHunting) {
                visitedHalfMapNodes.addAll(TraversalWayBuilders.toUnvisitedNodes(wayHelper.getHalfMapVisitedGrassFields()));
                visitedHalfMapNodes.addAll(TraversalWayBuilders.toUnvisitedNodes(wayHelper.getAllMountainFieldsMap()));
            } else {
                visitedHalfMapNodes.addAll(TraversalWayBuilders.toUnvisitedNodes(wayHelper.getOppHalfMapVisitedGrassFields()));
                visitedHalfMapNodes.addAll(TraversalWayBuilders.toUnvisitedNodes(wayHelper.getAllMountainFieldsMap()));
            }

            float bestValue = 0;
            MapNode bestNode = null;
            int candidatesEvaluated = 0;

            if (treasureHunting) {
                if (treasureSeeker == null) {
                    throw new AIDecisionException(
                            "Cannot execute treasure hunting strategy: TreasureSeeker is null",
                            "WayFinder",
                            "traverseHalfMap",
                            currentMapNode
                    );
                }

                for (MapNode tile : treasureSeeker.getArrangedOwnHalfMap(currentMapNode).getMapNodes()) {
                    if (tile.getTerrain() == Terrain.WATER) continue;
                    if (!visitedHalfMapNodes.contains(tile)) {
                        candidatesEvaluated++;
                        float ratio = scorer.score(gameState, currentMapNode, tile, true);
                        if (ratio > bestValue) {
                            bestValue = ratio;
                            bestNode = tile;
                        }
                    }
                }
            } else {
                for (MapNode tile : wayHelper.getOppHalfMapVisitedGrassFields().keySet()) {
                    if (tile.getTerrain() == Terrain.WATER) continue;
                    if (!visitedHalfMapNodes.contains(tile)) {
                        candidatesEvaluated++;
                        float ratio = scorer.score(gameState, currentMapNode, tile, false);
                        if (ratio > bestValue) {
                            bestValue = ratio;
                            bestNode = tile;
                        }
                    }
                }
                for (MapNode tile : wayHelper.getAllMountainFieldsMap().keySet()) {
                    if (!visitedHalfMapNodes.contains(tile) && !gameState.getMap().isNodeInOwnHalf(tile)) {
                        candidatesEvaluated++;
                        float ratio = scorer.score(gameState, currentMapNode, tile, false);
                        if (ratio > bestValue) {
                            bestValue = ratio;
                            bestNode = tile;
                        }
                    }
                }
            }

            if (bestNode == null) {
                String strategyName = treasureHunting ? "treasure hunting" : "fort seeking";
                throw new AIDecisionException(
                        String.format("No valid target found during %s (evaluated %d candidates, best value: %.2f)",
                                strategyName, candidatesEvaluated, bestValue),
                        "WayFinder",
                        "traverseHalfMap",
                        currentMapNode
                );
            }

            return bestNode;
        } catch (AIDecisionException e) {
            throw e;
        } catch (Exception e) {
            throw new AIDecisionException(
                    "Half map traversal failed: " + e.getMessage(),
                    e,
                    "WayFinder",
                    "traverseHalfMap",
                    currentMapNode
            );
        }
    }
}
