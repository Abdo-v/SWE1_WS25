package client.model.ai;

import client.exception.AIDecisionException;
import client.model.Direction;
import client.model.GameState;
import client.model.StaticColors;
import client.model.mapper.MapNode;
import client.view.CLIHandler;

/**
 * Handles the "fort found => path towards enemy fort" use case.
 *
 * Package-private: internal AI refactoring helper.
 */
final class FortTargetingUseCase {

    private final FortSeeker fortSeeker;
    private final StateHolder stateHolder;
    private final ShortestPathFinder shortestPathFinder;

    FortTargetingUseCase(FortSeeker fortSeeker, StateHolder stateHolder, ShortestPathFinder shortestPathFinder) {
        this.fortSeeker = fortSeeker;
        this.stateHolder = stateHolder;
        this.shortestPathFinder = shortestPathFinder;
    }

    Direction tryGetDirection(GameState gameState, MapNode currentMapNode, Objective objective) throws AIDecisionException {
        if (objective != Objective.FORT) {
            return null;
        }

        try {
            if (fortSeeker == null) {
                throw new AIDecisionException(
                        "Cannot target fort: FortSeeker is null",
                        "WayFinder",
                        "targetFortIfFOund",
                        currentMapNode
                );
            }

            MapNode fortNode = fortSeeker.getEnemyFortNodeIfFound();
            if (fortNode == null) {
                return null;
            }

            if (!stateHolder.isFortAlreadyFound()) {
                if (CLIHandler.isGameModeReduced()) {
                    System.out.println(StaticColors.PURPLE + "enemy fort found at: (" + fortNode.getX() + "," + fortNode.getY() + "), moving towards it." + StaticColors.RESET);
                }
                stateHolder.setFortAlreadyFound(true);
            }

            Direction nextDir = shortestPathFinder.findNextValidNodeToTarget(fortNode);
            MapNode nextNode = shortestPathFinder.getNodeInDirection(currentMapNode, nextDir);
            if (nextNode == null) {
                throw new AIDecisionException(
                        "Cannot find path to enemy fort at: " + fortNode.printCoordinates(),
                        "WayFinder",
                        "targetFortIfFOund",
                        currentMapNode
                );
            }

            Direction direction = shortestPathFinder.getDirectionToNeighbor(nextNode);
            if (direction == null) {
                throw new AIDecisionException(
                        "Cannot determine direction to enemy fort, nextNode: " + nextNode.printCoordinates(),
                        "WayFinder",
                        "targetFortIfFOund",
                        currentMapNode
                );
            }

            return direction;
        } catch (AIDecisionException e) {
            throw e;
        } catch (Exception e) {
            throw new AIDecisionException(
                    "Fort targeting failed: " + e.getMessage(),
                    e,
                    "WayFinder",
                    "targetFortIfFOund",
                    currentMapNode
            );
        }
    }
}
