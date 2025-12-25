package client.model.ai;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import client.exception.AIDecisionException;
import client.model.GameState;
import client.model.mapper.GameMap;
import client.model.mapper.HalfMapDimensions;
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

        MapNode selectBestNode(
            GameState gameState,
            MapNode currentMapNode,
            Objective objective,
            Map<MapNode, Integer> costMap
        ) throws AIDecisionException {
        try {
            Objects.requireNonNull(gameState, "gameState must not be null");
            Objects.requireNonNull(currentMapNode, "currentMapNode must not be null");
            Objects.requireNonNull(objective, "objective must not be null");
            Objects.requireNonNull(costMap, "costMap must not be null");

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
                int[] bestCost = {Integer.MAX_VALUE};
                int[] bestDepth = {Integer.MIN_VALUE};
                boolean[] bestIsMountain = {false};

            if (objective == Objective.TREASURE) {
                bestNode = findBestNode(
                    treasureSeeker.getArrangedOwnHalfMap(currentMapNode).getMapNodes(),
                    visitedHalfMapNodes, gameState, currentMapNode, objective, costMap, scorer,
                    bestValue, bestCost, bestDepth, bestIsMountain, candidatesEvaluated
                );
            } else {
                bestNode = findBestNode(
                    wayHelper.getOppHalfMapVisitedGrassFields().keySet(),
                    visitedHalfMapNodes, gameState, currentMapNode, objective, costMap, scorer,
                    bestValue, bestCost, bestDepth, bestIsMountain, candidatesEvaluated
                );
                bestNode = bestNode.or(() -> findBestNode(
                    wayHelper.getAllMountainFieldsMap().keySet().stream()
                        .filter(tile -> gameState.getMap().map(map -> !map.isNodeInOwnHalf(tile)).orElse(false))
                        .toList(),
                    visitedHalfMapNodes, gameState, currentMapNode, objective, costMap, scorer,
                    bestValue, bestCost, bestDepth, bestIsMountain, candidatesEvaluated
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
        Map<MapNode, Integer> costMap,
        VisionCostScorer scorer,
        float[] bestValue,
        int[] bestCost,
        int[] bestDepth,
        boolean[] bestIsMountain,
        int[] candidatesEvaluated
    ) {
        Optional<MapNode> bestNode = Optional.empty();
        final float EPS = 1e-6f;

        GameMap map = gameState.getMap().orElse(null);
        for (MapNode tile : candidates) {
            if (tile.getTerrain() == Terrain.WATER) continue;
            if (!visitedHalfMapNodes.contains(tile)) {
                candidatesEvaluated[0]++;

                float ratio = scorer.score(gameState, currentMapNode, tile, objective, costMap);
                if (ratio < 0) {
                    continue;
                }

                int cost = costMap.getOrDefault(tile, Integer.MAX_VALUE);
                boolean isMountain = tile.getTerrain() == Terrain.MOUNTAIN;
                int depth = (objective == Objective.TREASURE && map != null)
                        ? distanceFromEnemyBorder(map, tile)
                        : 0;

                if (ratio > bestValue[0] + EPS) {
                    bestValue[0] = ratio;
                    bestCost[0] = cost;
                    bestDepth[0] = depth;
                    bestIsMountain[0] = isMountain;
                    bestNode = Optional.of(tile);
                    continue;
                }

                if (Math.abs(ratio - bestValue[0]) <= EPS && bestNode.isPresent()) {
                    MapNode currentBest = bestNode.orElseThrow();

                    // Tie-breaker 1: Mountain over Grass
                    if (isMountain && !bestIsMountain[0]) {
                        bestCost[0] = cost;
                        bestDepth[0] = depth;
                        bestIsMountain[0] = true;
                        bestNode = Optional.of(tile);
                        continue;
                    }
                    if (!isMountain && bestIsMountain[0]) {
                        continue;
                    }

                    // Tie-breaker 2: Lower absolute action cost
                    if (cost < bestCost[0]) {
                        bestCost[0] = cost;
                        bestDepth[0] = depth;
                        bestIsMountain[0] = isMountain;
                        bestNode = Optional.of(tile);
                        continue;
                    }
                    if (cost > bestCost[0]) {
                        continue;
                    }

                    // Tie-breaker 3: Prefer deeper in own half (further away from enemy border)
                    if (objective == Objective.TREASURE && map != null) {
                        if (depth > bestDepth[0]) {
                            bestDepth[0] = depth;
                            bestIsMountain[0] = isMountain;
                            bestNode = Optional.of(tile);
                            continue;
                        }
                        if (depth < bestDepth[0]) {
                            continue;
                        }
                    }

                    // Tie-breaker 4: Randomization (anti-deterministic-loop)
                    if (!tile.equalsByCoordinates(currentBest) && ThreadLocalRandom.current().nextBoolean()) {
                        bestNode = Optional.of(tile);
                        bestIsMountain[0] = isMountain;
                        bestCost[0] = cost;
                        bestDepth[0] = depth;
                    }
                }
            }
        }
        return bestNode;
    }

    private int distanceFromEnemyBorder(GameMap map, MapNode node) {
        Objects.requireNonNull(map, "map must not be null");
        Objects.requireNonNull(node, "node must not be null");

        // The map orientation defines which half is "own".
        // We approximate "depth" as the number of steps to reach the first row/col of the enemy half.
        // Larger value => further away from the enemy border (preferred during treasure search).
        try {
            return switch (map.getOrientation()) {
                case UP_DOWN -> HalfMapDimensions.HEIGHT - node.getY();
                case DOWN_UP -> node.getY() - (HalfMapDimensions.HEIGHT - 1);
                case LEFT_RIGHT -> HalfMapDimensions.WIDTH - node.getX();
                case RIGHT_LEFT -> node.getX() - (HalfMapDimensions.WIDTH - 1);
            };
        } catch (IllegalStateException ignored) {
            return 0;
        }
    }
}
