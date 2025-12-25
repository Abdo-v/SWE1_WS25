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

            float[] bestValue = {0};
            int[] candidatesEvaluated = {0};
            Optional<MapNode> bestNode = Optional.empty();

            if (objective == Objective.TREASURE) {
                bestNode = findBestNode(
                    treasureSeeker.getArrangedOwnHalfMap(currentMapNode).getMapNodes(),
                    visitedHalfMapNodes, gameState, currentMapNode, objective, scorer, bestValue, candidatesEvaluated
                );
            } else {
                bestNode = findBestNode(
                    wayHelper.getOppHalfMapVisitedGrassFields().keySet(),
                    visitedHalfMapNodes, gameState, currentMapNode, objective, scorer, bestValue, candidatesEvaluated
                );
                bestNode = bestNode.or(() -> findBestNode(
                    wayHelper.getAllMountainFieldsMap().keySet().stream()
                        .filter(tile -> gameState.getMap().map(map -> !map.isNodeInOwnHalf(tile)).orElse(false))
                        .toList(),
                    visitedHalfMapNodes, gameState, currentMapNode, objective, scorer, bestValue, candidatesEvaluated
                ));
            }

            if (bestNode.isEmpty()) {
                String strategyName = objective == Objective.TREASURE ? "treasure hunting" : "fort seeking";
                throw new AIDecisionException(
                        String.format("No valid target found during %s (evaluated %d candidates, best value: %.2f)",
                                strategyName, candidatesEvaluated[0], bestValue[0]),
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

    private Optional<MapNode> findBestNode(
        Iterable<MapNode> candidates,
        ArrayList<MapNode> visitedHalfMapNodes,
        GameState gameState,
        MapNode currentMapNode,
        Objective objective,
        VisionCostScorer scorer,
        float[] bestValue,
        int[] candidatesEvaluated
    ) {
        Optional<MapNode> bestNode = Optional.empty();
        for (MapNode tile : candidates) {
            if (tile.getTerrain() == Terrain.WATER) continue;
            if (!visitedHalfMapNodes.contains(tile)) {
                candidatesEvaluated[0]++;
                float ratio = scorer.score(gameState, currentMapNode, tile, objective);
                if (ratio > bestValue[0]) {
                    bestValue[0] = ratio;
                    bestNode = Optional.of(tile);
                }
            }
        }
        return bestNode;
    }
}
