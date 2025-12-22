package client.model.ai;

import java.util.ArrayList;

import client.exception.AIDecisionException;
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
        this.shortestPathFinder = shortestPathFinder;
        this.strategyGuide = strategyGuide;

        this.nodeVisitTracker = new NodeVisitTracker(wayHelper);
        this.visionCostScorer = new VisionCostScorer(wayHelper, shortestPathFinder, strategyGuide);
        this.treasureTargeting = new TreasureTargetingUseCase(treasureSeeker, stateHolder, shortestPathFinder, nodeVisitTracker);
        this.fortTargeting = new FortTargetingUseCase(fortSeeker, stateHolder, shortestPathFinder);
        this.explorationTarget = new ExplorationTargetUseCase(wayHelper, treasureSeeker, visionCostScorer);
    }

    Direction moveBasedOnStrategy(GameState gameState, MapNode currentMapNode, Objective objective) throws AIDecisionException {
        try {
            if (currentMapNode == null) {
                throw new AIDecisionException(
                        "Cannot execute strategy: current map node is null",
                        "WayFinder",
                        "moveBasedOnStrategy",
                        null
                );
            }

            nodeVisitTracker.markVisited(currentMapNode, gameState.isPlayerInOwnHalfMap());

            if (currentMapNode.getTerrain() == Terrain.MOUNTAIN) {
                ArrayList<MapNode> extendedVisionNodes = strategyGuide.getGrassNodesFromExtendedVision(currentMapNode);
                for (MapNode node : extendedVisionNodes) {
                    nodeVisitTracker.markVisited(node, gameState.isPlayerInOwnHalfMap());
                }
            }

            Direction treasureDirection = treasureTargeting.tryGetDirection(gameState, currentMapNode, objective);
            if (treasureDirection != null) {
                return treasureDirection;
            }

            Direction fortDirection = fortTargeting.tryGetDirection(gameState, currentMapNode, objective);
            if (fortDirection != null) {
                return fortDirection;
            }

            MapNode bestNode = explorationTarget.selectBestNode(gameState, currentMapNode, objective);
            if (bestNode == null) {
                throw new AIDecisionException(
                        "No valid target node found for traversal",
                        "WayFinder",
                        "traverseHalfMap",
                        currentMapNode
                );
            }

            Direction nextDirToBest = shortestPathFinder.findNextValidNodeToTarget(bestNode);
            MapNode nextNode = shortestPathFinder.getNodeInDirection(currentMapNode, nextDirToBest);
            if (nextNode == null) {
                throw new AIDecisionException(
                        "Pathfinding failed: no valid next node found to target: " + bestNode.printCoordinates(),
                        "WayFinder",
                        "shortestPathFinder.getNodeInDirection",
                        currentMapNode
                );
            }

            Direction direction = shortestPathFinder.getDirectionToNeighbor(nextNode);
            if (direction == null) {
                throw new AIDecisionException(
                        "Direction calculation failed: cannot determine direction to neighbor: " + nextNode.printCoordinates(),
                        "WayFinder",
                        "shortestPathFinder.getDirectionToNeighbor",
                        currentMapNode
                );
            }

            return direction;
        } catch (AIDecisionException e) {
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

    float getCostToVisionRatio(GameState gameState, MapNode currentMapNode, MapNode node, Objective objective) {
        return visionCostScorer.score(gameState, currentMapNode, node, objective);
    }


}
