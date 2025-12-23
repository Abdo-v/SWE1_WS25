package client.model.ai;

import client.exception.AIDecisionException;
import client.model.Direction;
import client.model.GameState;
import client.model.mapper.MapNode;
import client.view.CLIHandler;

import java.util.Objects;
import java.util.Optional;

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
        this.treasureSeeker = Objects.requireNonNull(treasureSeeker, "treasureSeeker must not be null");
        this.stateHolder = Objects.requireNonNull(stateHolder, "stateHolder must not be null");
        this.shortestPathFinder = Objects.requireNonNull(shortestPathFinder, "shortestPathFinder must not be null");
        this.nodeVisitTracker = Objects.requireNonNull(nodeVisitTracker, "nodeVisitTracker must not be null");
    }

    Optional<Direction> tryGetDirection(GameState gameState, MapNode currentMapNode, Objective objective) throws AIDecisionException {
        Objects.requireNonNull(gameState, "gameState must not be null");
        Objects.requireNonNull(currentMapNode, "currentMapNode must not be null");
        Objects.requireNonNull(objective, "objective must not be null");

        if (objective != Objective.TREASURE) {
            return Optional.empty();
        }

        try {
            Optional<MapNode> treasureNodeOpt = treasureSeeker.getTreasureNodeIfFound();
            if (treasureNodeOpt.isEmpty()) {
                return Optional.empty();
            }

            MapNode treasureNode = treasureNodeOpt.orElseThrow();

            if (!stateHolder.isTreasureAlreadyFound()) {
                if (CLIHandler.isGameModeReduced()) {
                    System.out.println("treasure found at: (" + treasureNode.getX() + "," + treasureNode.getY() + "), moving towards it.");
                }
                stateHolder.setTreasureAlreadyFound(true);
            }

                Direction nextDir = shortestPathFinder.findNextValidNodeToTarget(treasureNode)
                    .orElseThrow(() -> new AIDecisionException(
                        "Cannot find path to treasure at: " + treasureNode.printCoordinates(),
                        "WayFinder",
                        "targetTreasureIfFound",
                        currentMapNode
                    ));

                MapNode nextNode = shortestPathFinder.getNodeInDirection(currentMapNode, nextDir);
            nodeVisitTracker.markVisited(currentMapNode, gameState.isPlayerInOwnHalfMap());

                Direction direction = shortestPathFinder.getDirectionToNeighbor(nextNode)
                    .orElseThrow(() -> new AIDecisionException(
                        "Cannot determine direction to treasure, nextNode: " + nextNode.printCoordinates(),
                        "WayFinder",
                        "targetTreasureIfFound",
                        currentMapNode
                    ));

                return Optional.of(direction);
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
