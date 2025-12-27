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
        this.wayHelper = Objects.requireNonNull(wayHelper, "wayHelper is required");
        this.treasureSeeker = Objects.requireNonNull(treasureSeeker, "treasureSeeker is required");
        this.scorer = Objects.requireNonNull(scorer, "scorer is required");
    }

    MapNode selectBestNode(
        GameState gameState,
        MapNode currentMapNode,
        Objective objective,
        Map<MapNode, Integer> costMap
    ) throws AIDecisionException {
        try {
            Objects.requireNonNull(gameState, "gameState is required");
            Objects.requireNonNull(currentMapNode, "currentMapNode is required");
            Objects.requireNonNull(objective, "objective is required");
            Objects.requireNonNull(costMap, "costMap is required");

            // IMPORTANT: TraversalWayBuilders.toUnvisitedNodes(...) returns nodes where value != true.
            ArrayList<MapNode> unvisitedHalfMapNodes = new ArrayList<>();
            if (objective == Objective.TREASURE) {
                unvisitedHalfMapNodes.addAll(TraversalWayBuilders.toUnvisitedNodes(wayHelper.getHalfMapVisitedGrassFields()));
            } else {
                unvisitedHalfMapNodes.addAll(TraversalWayBuilders.toUnvisitedNodes(wayHelper.getOppHalfMapVisitedGrassFields()));
            }
            unvisitedHalfMapNodes.addAll(TraversalWayBuilders.toUnvisitedNodes(wayHelper.getAllMountainFieldsMap()));

            float[] bestValue = {0};
            int[] candidatesEvaluated = {0};
            Optional<MapNode> bestNode = Optional.empty();
            int[] bestCost = {Integer.MAX_VALUE};
            int[] bestDepth = {Integer.MIN_VALUE};
            boolean[] bestIsMountain = {false};

            if (objective == Objective.TREASURE) {
                bestNode = findBestNode(
                    treasureSeeker.getArrangedOwnHalfMap(currentMapNode).getMapNodes(),
                    unvisitedHalfMapNodes, gameState, currentMapNode, objective, costMap, scorer,
                    bestValue, bestCost, bestDepth, bestIsMountain, candidatesEvaluated
                );
            } else {
                // Fort seeking: DO NOT short-circuit after finding any positive-scoring grass.
                // Mountains can have a much higher benefit/cost (vision reveal), so grass and mountains
                // must compete in the same ranking.
                ArrayList<MapNode> fortCandidates = new ArrayList<>();
                fortCandidates.addAll(wayHelper.getOppHalfMapVisitedGrassFields().keySet());
                fortCandidates.addAll(
                    wayHelper.getAllMountainFieldsMap().keySet().stream()
                        .filter(tile -> gameState.getMap().map(map -> !map.isNodeInOwnHalf(tile)).orElse(false))
                        .toList()
                );

                bestNode = findBestNode(
                    fortCandidates,
                    unvisitedHalfMapNodes, gameState, currentMapNode, objective, costMap, scorer,
                    bestValue, bestCost, bestDepth, bestIsMountain, candidatesEvaluated
                );
            }

            if (bestNode.isEmpty()) {
                // Safety net: do not hard-fail the whole AI when scoring yields no positive candidate.
                // This can happen if everything looks "visited" or reveals nothing useful.
                ArrayList<MapNode> fallbackCandidates = new ArrayList<>();
                if (objective == Objective.TREASURE) {
                    fallbackCandidates.addAll(treasureSeeker.getArrangedOwnHalfMap(currentMapNode).getMapNodes());
                } else {
                    fallbackCandidates.addAll(wayHelper.getOppHalfMapVisitedGrassFields().keySet());
                    fallbackCandidates.addAll(
                        wayHelper.getAllMountainFieldsMap().keySet().stream()
                            .filter(tile -> gameState.getMap().map(map -> !map.isNodeInOwnHalf(tile)).orElse(false))
                            .toList()
                    );
                }

                Optional<MapNode> fallback = pickCheapestReachableUnvisitedFirst(
                        fallbackCandidates,
                        currentMapNode,
                        objective,
                        costMap
                );

                if (fallback.isPresent()) {
                    return fallback.orElseThrow();
                }

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
        ArrayList<MapNode> unvisitedHalfMapNodes,
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

        Optional<GameMap> mapForDepth = objective == Objective.TREASURE ? gameState.getMap() : Optional.empty();
        boolean computeDepth = mapForDepth.isPresent();
        for (MapNode tile : candidates) {
            if (tile.getTerrain() == Terrain.WATER) continue;
            // Evaluate only nodes that are still considered unvisited.
            if (unvisitedHalfMapNodes.contains(tile)) {
                candidatesEvaluated[0]++;

                float ratio = scorer.score(gameState, currentMapNode, tile, objective, costMap);
                if (ratio < 0) {
                    continue;
                }

                int cost = costMap.getOrDefault(tile, Integer.MAX_VALUE);
                boolean isMountain = tile.getTerrain() == Terrain.MOUNTAIN;
                int depth = computeDepth
                    ? mapForDepth.map(map -> distanceFromEnemyBorder(map, tile)).orElse(0)
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
                    if (computeDepth) {
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

    private Optional<MapNode> pickCheapestReachableUnvisitedFirst(
            Iterable<MapNode> candidates,
            MapNode currentMapNode,
            Objective objective,
            Map<MapNode, Integer> costMap
    ) {
        Optional<MapNode> bestUnvisited = Optional.empty();
        int bestUnvisitedCost = Integer.MAX_VALUE;

        Optional<MapNode> bestAny = Optional.empty();
        int bestAnyCost = Integer.MAX_VALUE;

        for (MapNode tile : candidates) {
            MapNode requiredTile = Objects.requireNonNull(tile, "candidate list contains a missing node");
            if (requiredTile.getTerrain() == Terrain.WATER) continue;
            if (requiredTile.equalsByCoordinates(currentMapNode)) continue;

            int cost = costMap.getOrDefault(requiredTile, Integer.MAX_VALUE);
            if (cost == Integer.MAX_VALUE || cost <= 0) continue;

            if (cost < bestAnyCost) {
                bestAnyCost = cost;
                bestAny = Optional.of(requiredTile);
            }

            if (!isAlreadyVisited(requiredTile, objective) && cost < bestUnvisitedCost) {
                bestUnvisitedCost = cost;
                bestUnvisited = Optional.of(requiredTile);
            }
        }

        return bestUnvisited.isPresent() ? bestUnvisited : bestAny;
    }

    private boolean isAlreadyVisited(MapNode node, Objective objective) {
        if (node.getTerrain() == Terrain.GRASS) {
            if (objective == Objective.TREASURE) {
                return Boolean.TRUE.equals(wayHelper.getHalfMapVisitedGrassFields().get(node));
            }

            // Fort phase: if it's not in the traversal key-set, it's considered irrelevant/visited.
            if (!wayHelper.getOppHalfMapVisitedGrassFields().containsKey(node)) {
                return true;
            }
            return Boolean.TRUE.equals(wayHelper.getOppHalfMapVisitedGrassFields().get(node));
        }

        if (node.getTerrain() == Terrain.MOUNTAIN) {
            return Boolean.TRUE.equals(wayHelper.getAllMountainFieldsMap().get(node));
        }

        return true;
    }

    private int distanceFromEnemyBorder(GameMap map, MapNode node) {
        Objects.requireNonNull(map, "map is required");
        Objects.requireNonNull(node, "node is required");

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
