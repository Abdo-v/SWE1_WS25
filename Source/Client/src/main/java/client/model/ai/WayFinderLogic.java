package client.model.ai;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import client.exception.AIDecisionException;
import client.exception.AIInvariantViolationException;
import client.exception.NoValidMoveAvailableException;
import client.model.Direction;
import client.model.GameState;
import client.model.mapper.MapNode;
import client.model.mapper.PlayerHalfMap;
import client.model.mapper.Terrain;

/**
 * Extracted decision logic from WayFinder to keep WayFinder focused on orchestration/state.
 *
 * Package-private on purpose: internal refactoring detail.
 */
final class WayFinderLogic {

    private final WayHelper wayHelper;
    private final StateHolder stateHolder;
    private final ShortestPathFinder shortestPathFinder;
    private final StrategyGuide strategyGuide;

    private final NodeVisitTracker nodeVisitTracker;
    private final VisionCostScorer visionCostScorer;
    private final TreasureTargetingUseCase treasureTargeting;
    private final FortTargetingUseCase fortTargeting;
    private final ExplorationTargetUseCase explorationTarget;

    WayFinderLogic(
            WayHelper wayHelper,
            StateHolder stateHolder,
            ShortestPathFinder shortestPathFinder,
            TreasureSeeker treasureSeeker,
            FortSeeker fortSeeker,
            StrategyGuide strategyGuide
    ) {
        this.wayHelper = Objects.requireNonNull(wayHelper, "wayHelper is required");
        this.stateHolder = Objects.requireNonNull(stateHolder, "stateHolder is required");
        this.shortestPathFinder = shortestPathFinder;
        this.strategyGuide = strategyGuide;

        this.nodeVisitTracker = new NodeVisitTracker(this.wayHelper);
        this.visionCostScorer = new VisionCostScorer(wayHelper, shortestPathFinder, strategyGuide);
        this.treasureTargeting = new TreasureTargetingUseCase(treasureSeeker, stateHolder, shortestPathFinder, nodeVisitTracker);
        this.fortTargeting = new FortTargetingUseCase(fortSeeker, stateHolder, shortestPathFinder);
        this.explorationTarget = new ExplorationTargetUseCase(wayHelper, treasureSeeker, visionCostScorer);
    }

    Direction moveBasedOnStrategy(GameState gameState, MapNode currentMapNode, Objective objective)
            throws AIDecisionException, NoValidMoveAvailableException {
        try {
            Objects.requireNonNull(gameState, "gameState is required");
            Objects.requireNonNull(currentMapNode, "currentMapNode is required");
            Objects.requireNonNull(objective, "objective is required");

            boolean nodeIsInOwnHalf = gameState.getMap()
                .map(map -> map.isNodeInOwnHalf(currentMapNode))
                .orElse(gameState.isPlayerInOwnHalfMap()); // fallback if map missing

            nodeVisitTracker.markVisited(currentMapNode, nodeIsInOwnHalf);

            if (currentMapNode.getTerrain() == Terrain.MOUNTAIN) {
                ArrayList<MapNode> extendedVisionNodes = strategyGuide.getGrassNodesFromExtendedVision(currentMapNode);
                for (MapNode node : extendedVisionNodes) {
                    nodeVisitTracker.markVisited(node, gameState.isPlayerInOwnHalfMap());
                }
            }

            // Fort phase: before the enemy true position is guaranteed (first 8 own moves), head to the enemy-half center.
            // This avoids chasing potentially random enemy positions and moves us into a good scanning position.
            if (objective == Objective.FORT && stateHolder.getEnemyFirstTruePosition().isEmpty()) {
                Optional<Direction> centerDir = tryGetDirectionToEnemyHalfCenter(gameState, currentMapNode);
                if (centerDir.isPresent()) {
                    return centerDir.orElseThrow();
                }
            }

            // Path lock is only about exploration targets. If the objective changes, discard the lock.
            stateHolder.clearLockedExplorationTargetIfObjectiveChanged(objective);

            Optional<Direction> treasureDirection = treasureTargeting.tryGetDirection(gameState, currentMapNode, objective);
            if (treasureDirection.isPresent()) {
                // Treasure known => exploration lock is irrelevant.
                stateHolder.clearLockedExplorationTarget();
                return treasureDirection.orElseThrow();
            }

            Optional<Direction> fortDirection = fortTargeting.tryGetDirection(gameState, currentMapNode, objective);
            if (fortDirection.isPresent()) {
                stateHolder.clearLockedExplorationTarget();
                return fortDirection.orElseThrow();
            }

            // "Loop killer": if we already committed to an exploration target, keep moving towards it
            // until we reach it or it becomes invalid/unreachable.
            Optional<MapNode> lockedTargetOpt = stateHolder.getLockedExplorationTarget();
            Optional<Objective> lockedObjectiveOpt = stateHolder.getLockedExplorationObjective();
            if (lockedTargetOpt.isPresent() && lockedObjectiveOpt.isPresent() && lockedObjectiveOpt.orElseThrow() == objective) {
                MapNode lockedTarget = lockedTargetOpt.orElseThrow();

                if (currentMapNode.equalsByCoordinates(lockedTarget) || isExplorationTargetAlreadyVisited(lockedTarget, objective)) {
                    stateHolder.clearLockedExplorationTarget();
                } else {
                    Optional<Direction> lockedDir = shortestPathFinder.findNextValidNodeToTarget(lockedTarget);
                    if (lockedDir.isPresent()) {
                        return lockedDir.orElseThrow();
                    }
                    stateHolder.clearLockedExplorationTarget();
                }
            }

            Map<MapNode, Integer> costMap = shortestPathFinder.computeCostMapFromCurrent(MovementCostProfile.SHORTEST_PATH);

            MapNode bestNode = explorationTarget.selectBestNode(gameState, currentMapNode, objective, costMap);
            stateHolder.lockExplorationTarget(bestNode, objective);

            Optional<Direction> nextDirToBestOpt = shortestPathFinder.findNextValidNodeToTarget(bestNode);
            if (nextDirToBestOpt.isEmpty()) {
                throw new NoValidMoveAvailableException(
                        "Pathfinding failed: no valid path to target: " + bestNode.printCoordinates(),
                        "WayFinderLogic",
                        "moveBasedOnStrategy",
                        suggestFallbackDirection(currentMapNode)
                );
            }

            Direction nextDirToBest = nextDirToBestOpt.orElseThrow();

            MapNode nextNode = shortestPathFinder.getNodeInDirection(currentMapNode, nextDirToBest);

            return shortestPathFinder.getDirectionToNeighbor(nextNode)
                    .orElseThrow(() -> new AIInvariantViolationException(
                            "Direction calculation failed: neighbor direction is missing",
                            "WayFinderLogic",
                            "neighbor direction must be present",
                            "current=" + currentMapNode.printCoordinates() + ", next=" + nextNode.printCoordinates()
                    ));
        } catch (AIDecisionException | NoValidMoveAvailableException | AIInvariantViolationException e) {
            throw e;
        } catch (Exception e) {
            throw new AIDecisionException(
                    "Strategy execution failed: " + e.getMessage(),
                    e,
                    "WayFinder",
                    "moveBasedOnStrategy",
                    currentMapNode
            );
        }
    }

    private Optional<Direction> suggestFallbackDirection(MapNode currentMapNode) {
        Objects.requireNonNull(currentMapNode, "currentMapNode is required");

        for (Direction direction : Direction.values()) {
            try {
                    MapNode candidate = shortestPathFinder.getNodeInDirection(currentMapNode, direction);
                if (candidate.getTerrain() != Terrain.WATER) {
                    return Optional.of(direction);
                }
            } catch (RuntimeException ignored) {
                // Out of bounds or state not initialized -> ignore.
            }
        }
        return Optional.empty();
    }

    private Optional<Direction> tryGetDirectionToEnemyHalfCenter(GameState gameState, MapNode currentMapNode) {
        Objects.requireNonNull(gameState, "gameState is required");
        Objects.requireNonNull(currentMapNode, "currentMapNode is required");

        return gameState.getMap().flatMap(map -> {
            PlayerHalfMap enemyHalf = map.getOpponentHalfMap();
            if (enemyHalf.getMapNodes().isEmpty()) {
                return Optional.empty();
            }

            int minX = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;
            int minY = Integer.MAX_VALUE;
            int maxY = Integer.MIN_VALUE;
            for (MapNode node : enemyHalf.getMapNodes()) {
                minX = Math.min(minX, node.getX());
                maxX = Math.max(maxX, node.getX());
                minY = Math.min(minY, node.getY());
                maxY = Math.max(maxY, node.getY());
            }

            // Half-map dimensions are 10x5 in this project: 2 center nodes across X, 1 center row in Y.
            int centerY = (minY + maxY) / 2;
            int centerX1 = (minX + maxX) / 2;
            int centerX2 = centerX1 + 1;

            MapNode center1;
            MapNode center2;
            try {
                center1 = map.getNode(centerX1, centerY);
                center2 = map.getNode(centerX2, centerY);
            } catch (IllegalArgumentException e) {
                return Optional.empty();
            }

            if (center1.getTerrain() == Terrain.WATER && center2.getTerrain() == Terrain.WATER) {
                return Optional.empty();
            }

            // Pick the reachable center candidate with the lower action cost.
            Map<MapNode, Integer> costMap = shortestPathFinder.computeCostMapFromCurrent(MovementCostProfile.SHORTEST_PATH);
            int c1 = costMap.getOrDefault(center1, Integer.MAX_VALUE);
            int c2 = costMap.getOrDefault(center2, Integer.MAX_VALUE);

            Optional<MapNode> chosenCenter = Optional.empty();
            if (center1.getTerrain() == Terrain.WATER) {
                chosenCenter = c2 == Integer.MAX_VALUE ? Optional.empty() : Optional.of(center2);
            } else if (center2.getTerrain() == Terrain.WATER) {
                chosenCenter = c1 == Integer.MAX_VALUE ? Optional.empty() : Optional.of(center1);
            } else if (c1 < c2) {
                chosenCenter = c1 == Integer.MAX_VALUE ? Optional.empty() : Optional.of(center1);
            } else if (c2 < c1) {
                chosenCenter = c2 == Integer.MAX_VALUE ? Optional.empty() : Optional.of(center2);
            } else {
                if (c1 != Integer.MAX_VALUE) {
                    chosenCenter = Optional.of(center1);
                }
            }

            if (chosenCenter.isEmpty()) {
                return Optional.empty();
            }

            MapNode target = chosenCenter.orElseThrow();
            if (currentMapNode.equalsByCoordinates(target)) {
                return Optional.empty();
            }

            Optional<Direction> next = shortestPathFinder.findNextValidNodeToTarget(target);
            if (next.isEmpty()) {
                return Optional.empty();
            }

            // Lock to center target to avoid oscillation.
            stateHolder.lockExplorationTarget(target, Objective.FORT);
            return next;
        });
    }

    private boolean isExplorationTargetAlreadyVisited(MapNode target, Objective objective) {
        Objects.requireNonNull(target, "target is required");
        Objects.requireNonNull(objective, "objective is required");

        if (target.getTerrain() == Terrain.GRASS) {
            if (objective == Objective.TREASURE) {
                return Boolean.TRUE.equals(wayHelper.getHalfMapVisitedGrassFields().get(target));
            }
            return Boolean.TRUE.equals(wayHelper.getOppHalfMapVisitedGrassFields().get(target));
        }

        if (target.getTerrain() == Terrain.MOUNTAIN) {
            return Boolean.TRUE.equals(wayHelper.getAllMountainFieldsMap().get(target));
        }

        return false;
    }


}
