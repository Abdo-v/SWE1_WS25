package client.model.ai;

import client.exception.AIDecisionException;
import client.model.Direction;
import client.model.GameState;
import client.model.StaticColors;
import client.model.mapper.MapNode;
import client.view.CLIHandler;

/**
 * Handles the "treasure found => path towards treasure" use case.
 *
 * Package-private: internal AI refactoring helper.
 */
final class TreasureTargetingUseCase {

    private final TreasureSeeker treasureSeeker;
    private final StateHolder stateHolder;
    private final ShortestPathFinder shortestPathFinder;
    private final NodeVisitTracker nodeVisitTracker;

    TreasureTargetingUseCase(
            TreasureSeeker treasureSeeker,
            StateHolder stateHolder,
            ShortestPathFinder shortestPathFinder,
            NodeVisitTracker nodeVisitTracker
    ) {
        this.treasureSeeker = treasureSeeker;
        this.stateHolder = stateHolder;
        this.shortestPathFinder = shortestPathFinder;
        this.nodeVisitTracker = nodeVisitTracker;
    }

    Direction tryGetDirection(GameState gameState, MapNode currentMapNode, Objective objective) throws AIDecisionException {
        if (objective != Objective.TREASURE) {
            return null;
        }

        try {
            if (treasureSeeker == null) {
                throw new AIDecisionException(
                        "Cannot target treasure: TreasureSeeker is null",
                        "WayFinder",
                        "targetTreasureIfFound",
                        currentMapNode
                );
            }

            MapNode treasureNode = treasureSeeker.getTreasureNodeIfFound();
            if (treasureNode == null) {
                return null;
            }

            if (!stateHolder.isTreasureAlreadyFound()) {
                if (CLIHandler.isGameModeReduced()) {
                    System.out.println(StaticColors.ORANGE + "treasure found at: (" + treasureNode.getX() + "," + treasureNode.getY() + "), moving towards it." + StaticColors.RESET);
                }
                stateHolder.setTreasureAlreadyFound(true);
            }

            Direction nextDir = shortestPathFinder.findNextValidNodeToTarget(treasureNode);
            MapNode nextNode = shortestPathFinder.getNodeInDirection(currentMapNode, nextDir);
            nodeVisitTracker.markVisited(currentMapNode, gameState.isPlayerInOwnHalfMap());

            if (nextNode == null) {
                throw new AIDecisionException(
                        "Cannot find path to treasure at: " + treasureNode.printCoordinates(),
                        "WayFinder",
                        "targetTreasureIfFound",
                        currentMapNode
                );
            }

            Direction direction = shortestPathFinder.getDirectionToNeighbor(nextNode);
            if (direction == null) {
                throw new AIDecisionException(
                        "Cannot determine direction to treasure, nextNode: " + nextNode.printCoordinates(),
                        "WayFinder",
                        "targetTreasureIfFound",
                        currentMapNode
                );
            }

            return direction;
        } catch (AIDecisionException e) {
            throw e;
        } catch (Exception e) {
            throw new AIDecisionException(
                    "Treasure targeting failed: " + e.getMessage(),
                    e,
                    "WayFinder",
                    "targetTreasureIfFound",
                    currentMapNode
            );
        }
    }
}
