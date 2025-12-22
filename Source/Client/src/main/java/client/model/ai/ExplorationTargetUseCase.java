package client.model.ai;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

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
        this.wayHelper = Objects.requireNonNull(wayHelper, "wayHelper must not be null");
        this.treasureSeeker = Objects.requireNonNull(treasureSeeker, "treasureSeeker must not be null");
        this.scorer = Objects.requireNonNull(scorer, "scorer must not be null");
    }

    MapNode selectBestNode(GameState gameState, MapNode currentMapNode, Objective objective) throws AIDecisionException {
        try {
            Objects.requireNonNull(gameState, "gameState must not be null");
            Objects.requireNonNull(currentMapNode, "currentMapNode must not be null");
            Objects.requireNonNull(objective, "objective must not be null");

            ArrayList<MapNode> visitedHalfMapNodes = new ArrayList<>();

            if (objective == Objective.TREASURE) {
                visitedHalfMapNodes.addAll(TraversalWayBuilders.toUnvisitedNodes(wayHelper.getHalfMapVisitedGrassFields()));
                visitedHalfMapNodes.addAll(TraversalWayBuilders.toUnvisitedNodes(wayHelper.getAllMountainFieldsMap()));
            } else {
                visitedHalfMapNodes.addAll(TraversalWayBuilders.toUnvisitedNodes(wayHelper.getOppHalfMapVisitedGrassFields()));
                visitedHalfMapNodes.addAll(TraversalWayBuilders.toUnvisitedNodes(wayHelper.getAllMountainFieldsMap()));
            }

            float bestValue = 0;
            Optional<MapNode> bestNode = Optional.empty();
            int candidatesEvaluated = 0;

            if (objective == Objective.TREASURE) {
                for (MapNode tile : treasureSeeker.getArrangedOwnHalfMap(currentMapNode).getMapNodes()) {
                    if (tile.getTerrain() == Terrain.WATER) continue;
                    if (!visitedHalfMapNodes.contains(tile)) {
                        candidatesEvaluated++;
                        float ratio = scorer.score(gameState, currentMapNode, tile, objective);
                        if (ratio > bestValue) {
                            bestValue = ratio;
                            bestNode = Optional.of(tile);
                        }
                    }
                }
            } else {
                for (MapNode tile : wayHelper.getOppHalfMapVisitedGrassFields().keySet()) {
                    if (tile.getTerrain() == Terrain.WATER) continue;
                    if (!visitedHalfMapNodes.contains(tile)) {
                        candidatesEvaluated++;
                        float ratio = scorer.score(gameState, currentMapNode, tile, objective);
                        if (ratio > bestValue) {
                            bestValue = ratio;
                            bestNode = Optional.of(tile);
                        }
                    }
                }
                for (MapNode tile : wayHelper.getAllMountainFieldsMap().keySet()) {
                    boolean isInOpponentHalf = gameState.getMap()
                            .map(map -> !map.isNodeInOwnHalf(tile))
                            .orElse(false);

                    if (!visitedHalfMapNodes.contains(tile) && isInOpponentHalf) {
                        candidatesEvaluated++;
                        float ratio = scorer.score(gameState, currentMapNode, tile, objective);
                        if (ratio > bestValue) {
                            bestValue = ratio;
                            bestNode = Optional.of(tile);
                        }
                    }
                }
            }

            if (bestNode.isEmpty()) {
                String strategyName = objective == Objective.TREASURE ? "treasure hunting" : "fort seeking";
                throw new AIDecisionException(
                        String.format("No valid target found during %s (evaluated %d candidates, best value: %.2f)",
                                strategyName, candidatesEvaluated, bestValue),
                        "WayFinder",
                        "traverseHalfMap",
                        currentMapNode
                );
            }

            return bestNode.orElseThrow();
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
