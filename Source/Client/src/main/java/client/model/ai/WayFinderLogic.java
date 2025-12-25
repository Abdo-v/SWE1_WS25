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
        this.wayHelper = Objects.requireNonNull(wayHelper, "wayHelper must not be null");
        this.stateHolder = Objects.requireNonNull(stateHolder, "stateHolder must not be null");
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
            Objects.requireNonNull(gameState, "gameState must not be null");
            Objects.requireNonNull(currentMapNode, "currentMapNode must not be null");
            Objects.requireNonNull(objective, "objective must not be null");

            nodeVisitTracker.markVisited(currentMapNode, gameState.isPlayerInOwnHalfMap());

            if (currentMapNode.getTerrain() == Terrain.MOUNTAIN) {
                ArrayList<MapNode> extendedVisionNodes = strategyGuide.getGrassNodesFromExtendedVision(currentMapNode);
                for (MapNode node : extendedVisionNodes) {
                    nodeVisitTracker.markVisited(node, gameState.isPlayerInOwnHalfMap());
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
        Objects.requireNonNull(currentMapNode, "currentMapNode must not be null");

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

    private boolean isExplorationTargetAlreadyVisited(MapNode target, Objective objective) {
        Objects.requireNonNull(target, "target must not be null");
        Objects.requireNonNull(objective, "objective must not be null");

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
