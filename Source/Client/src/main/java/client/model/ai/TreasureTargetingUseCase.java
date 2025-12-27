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
        this.treasureSeeker = Objects.requireNonNull(treasureSeeker, "treasureSeeker is required");
        this.stateHolder = Objects.requireNonNull(stateHolder, "stateHolder is required");
        this.shortestPathFinder = Objects.requireNonNull(shortestPathFinder, "shortestPathFinder is required");
        this.nodeVisitTracker = Objects.requireNonNull(nodeVisitTracker, "nodeVisitTracker is required");
    }

    Optional<Direction> tryGetDirection(GameState gameState, MapNode currentMapNode, Objective objective) throws AIDecisionException {
        Objects.requireNonNull(gameState, "gameState is required");
        Objects.requireNonNull(currentMapNode, "currentMapNode is required");
        Objects.requireNonNull(objective, "objective is required");

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
