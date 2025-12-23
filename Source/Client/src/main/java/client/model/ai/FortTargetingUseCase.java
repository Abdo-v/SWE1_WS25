package client.model.ai;

import client.exception.AIDecisionException;
import client.model.Direction;
import client.model.GameState;
import client.model.mapper.MapNode;
import client.view.CLIHandler;

import java.util.Objects;
import java.util.Optional;

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
        this.fortSeeker = Objects.requireNonNull(fortSeeker, "fortSeeker must not be null");
        this.stateHolder = Objects.requireNonNull(stateHolder, "stateHolder must not be null");
        this.shortestPathFinder = Objects.requireNonNull(shortestPathFinder, "shortestPathFinder must not be null");
    }

    Optional<Direction> tryGetDirection(GameState gameState, MapNode currentMapNode, Objective objective) throws AIDecisionException {
        Objects.requireNonNull(gameState, "gameState must not be null");
        Objects.requireNonNull(currentMapNode, "currentMapNode must not be null");
        Objects.requireNonNull(objective, "objective must not be null");

        if (objective != Objective.FORT) {
            return Optional.empty();
        }

        try {
            Optional<MapNode> fortNodeOpt = fortSeeker.getEnemyFortNodeIfFound();
            if (fortNodeOpt.isEmpty()) {
                return Optional.empty();
            }

            MapNode fortNode = fortNodeOpt.orElseThrow();

            if (!stateHolder.isFortAlreadyFound()) {
                if (CLIHandler.isGameModeReduced()) {
                    System.out.println("enemy fort found at: (" + fortNode.getX() + "," + fortNode.getY() + "), moving towards it.");
                }
                stateHolder.setFortAlreadyFound(true);
            }

                Direction nextDir = shortestPathFinder.findNextValidNodeToTarget(fortNode)
                    .orElseThrow(() -> new AIDecisionException(
                        "Cannot find path to enemy fort at: " + fortNode.printCoordinates(),
                        "WayFinder",
                        "targetFortIfFOund",
                        currentMapNode
                    ));

                MapNode nextNode = shortestPathFinder.getNodeInDirection(currentMapNode, nextDir);

                Direction direction = shortestPathFinder.getDirectionToNeighbor(nextNode)
                    .orElseThrow(() -> new AIDecisionException(
                        "Cannot determine direction to enemy fort, nextNode: " + nextNode.printCoordinates(),
                        "WayFinder",
                        "targetFortIfFOund",
                        currentMapNode
                    ));

                return Optional.of(direction);
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
