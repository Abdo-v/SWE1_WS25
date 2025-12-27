package client.model.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import client.exception.AIDecisionException;
import client.model.Direction;
import client.model.GameState;
import client.model.mapper.GameMap;
import client.model.mapper.MapNode;
import client.model.mapper.OwnToOppMapOrientation;
import client.model.mapper.Terrain;

/**
 * Unit tests for {@link WayFinderLogic} focusing on deterministic, branch-heavy behavior.
 *
 * <p>These tests avoid integration (no real map traversal / networking) and use mocks where appropriate.
 */
class WayFinderLogicTest {

    /**
     * Fort objective before the enemy true position is known should bias movement towards the opponent-half center.
     * This is a deterministic branch and also verifies the exploration lock behavior.
     */
    @Test
    void moveBasedOnStrategy_whenFortAndEnemyPositionUnknown_movesToEnemyHalfCenterAndLocksTarget() throws Exception {
        WayHelper wayHelper = mock(WayHelper.class);
        StateHolder stateHolder = new StateHolder();
        ShortestPathFinder shortestPathFinder = mock(ShortestPathFinder.class);
        TreasureSeeker treasureSeeker = mock(TreasureSeeker.class);
        FortSeeker fortSeeker = mock(FortSeeker.class);
        StrategyGuide strategyGuide = mock(StrategyGuide.class);

        LinkedHashMap<MapNode, Boolean> ownVisited = new LinkedHashMap<>();
        LinkedHashMap<MapNode, Boolean> oppVisited = new LinkedHashMap<>();
        LinkedHashMap<MapNode, Boolean> mountainsVisited = new LinkedHashMap<>();
        when(wayHelper.getHalfMapVisitedGrassFields()).thenReturn(ownVisited);
        when(wayHelper.getOppHalfMapVisitedGrassFields()).thenReturn(oppVisited);
        when(wayHelper.getAllMountainFieldsMap()).thenReturn(mountainsVisited);

        GameMap map = buildFullMap(OwnToOppMapOrientation.LEFT_RIGHT);
        MapNode current = map.getNode(0, 0);

        MapNode center1 = map.getNode(14, 2);
        MapNode center2 = map.getNode(15, 2);
        assertNotEquals(center1, center2);

        when(shortestPathFinder.computeCostMapFromCurrent(MovementCostProfile.SHORTEST_PATH))
                .thenReturn(Map.of(center1, 3, center2, 7));
        when(shortestPathFinder.findNextValidNodeToTarget(center1)).thenReturn(Optional.of(Direction.RIGHT));

        GameState gameState = mock(GameState.class);
        when(gameState.getMap()).thenReturn(Optional.of(map));
        when(gameState.isPlayerInOwnHalfMap()).thenReturn(true);

        WayFinderLogic logic = new WayFinderLogic(
                wayHelper,
                stateHolder,
                shortestPathFinder,
                treasureSeeker,
                fortSeeker,
                strategyGuide
        );

        Direction dir = logic.moveBasedOnStrategy(gameState, current, Objective.FORT);
        assertEquals(Direction.RIGHT, dir);
        assertEquals(Objective.FORT, stateHolder.getLockedExplorationObjective().orElseThrow());
        assertEquals(center1, stateHolder.getLockedExplorationTarget().orElseThrow());
    }

    /**
     * When an exploration target is locked for the current objective, the AI should keep moving towards it
     * (loop-killer behavior), provided it is still reachable.
     */
    @Test
    void moveBasedOnStrategy_whenLockedTargetExistsAndReachable_returnsDirectionToLockedTarget() throws Exception {
        WayHelper wayHelper = mock(WayHelper.class);
        StateHolder stateHolder = new StateHolder();
        ShortestPathFinder shortestPathFinder = mock(ShortestPathFinder.class);
        TreasureSeeker treasureSeeker = mock(TreasureSeeker.class);
        FortSeeker fortSeeker = mock(FortSeeker.class);
        StrategyGuide strategyGuide = mock(StrategyGuide.class);

        LinkedHashMap<MapNode, Boolean> ownVisited = new LinkedHashMap<>();
        LinkedHashMap<MapNode, Boolean> oppVisited = new LinkedHashMap<>();
        LinkedHashMap<MapNode, Boolean> mountainsVisited = new LinkedHashMap<>();
        when(wayHelper.getHalfMapVisitedGrassFields()).thenReturn(ownVisited);
        when(wayHelper.getOppHalfMapVisitedGrassFields()).thenReturn(oppVisited);
        when(wayHelper.getAllMountainFieldsMap()).thenReturn(mountainsVisited);

        MapNode lockedTarget = new MapNode(2, 2, Terrain.GRASS, false, false);
        stateHolder.lockExplorationTarget(lockedTarget, Objective.TREASURE);

        when(treasureSeeker.getTreasureNodeIfFound()).thenReturn(Optional.empty());
        when(shortestPathFinder.findNextValidNodeToTarget(lockedTarget)).thenReturn(Optional.of(Direction.UP));

        GameState gameState = mock(GameState.class);
        when(gameState.getMap()).thenReturn(Optional.empty());
        when(gameState.isPlayerInOwnHalfMap()).thenReturn(true);

        WayFinderLogic logic = new WayFinderLogic(
                wayHelper,
                stateHolder,
                shortestPathFinder,
                treasureSeeker,
                fortSeeker,
                strategyGuide
        );

        Direction dir = logic.moveBasedOnStrategy(gameState, new MapNode(0, 0, Terrain.GRASS, false, false), Objective.TREASURE);
        assertEquals(Direction.UP, dir);
        assertEquals(lockedTarget, stateHolder.getLockedExplorationTarget().orElseThrow());
    }

    /**
     * Unexpected exceptions inside the strategy execution should be wrapped into an {@link AIDecisionException}.
     */
    @Test
    void moveBasedOnStrategy_whenUnexpectedRuntimeExceptionOccurs_wrapsIntoAIDecisionException() {
        WayHelper wayHelper = mock(WayHelper.class);
        StateHolder stateHolder = new StateHolder();
        ShortestPathFinder shortestPathFinder = mock(ShortestPathFinder.class);
        TreasureSeeker treasureSeeker = mock(TreasureSeeker.class);
        FortSeeker fortSeeker = mock(FortSeeker.class);
        StrategyGuide strategyGuide = mock(StrategyGuide.class);

        LinkedHashMap<MapNode, Boolean> ownVisited = new LinkedHashMap<>();
        LinkedHashMap<MapNode, Boolean> oppVisited = new LinkedHashMap<>();
        LinkedHashMap<MapNode, Boolean> mountainsVisited = new LinkedHashMap<>();
        when(wayHelper.getHalfMapVisitedGrassFields()).thenReturn(ownVisited);
        when(wayHelper.getOppHalfMapVisitedGrassFields()).thenReturn(oppVisited);
        when(wayHelper.getAllMountainFieldsMap()).thenReturn(mountainsVisited);

        when(treasureSeeker.getTreasureNodeIfFound()).thenReturn(Optional.empty());
        when(shortestPathFinder.computeCostMapFromCurrent(any())).thenThrow(new RuntimeException("boom"));

        GameState gameState = mock(GameState.class);
        when(gameState.getMap()).thenReturn(Optional.empty());
        when(gameState.isPlayerInOwnHalfMap()).thenReturn(true);

        WayFinderLogic logic = new WayFinderLogic(
                wayHelper,
                stateHolder,
                shortestPathFinder,
                treasureSeeker,
                fortSeeker,
                strategyGuide
        );

        MapNode current = new MapNode(0, 0, Terrain.GRASS, false, false);
        AIDecisionException ex = assertThrows(
                AIDecisionException.class,
                () -> logic.moveBasedOnStrategy(gameState, current, Objective.TREASURE)
        );
        assertTrue(ex.getMessage().contains("Strategy execution failed"));
    }

    /**
     * When standing on a mountain, extended vision grass nodes should be marked visited before later failures.
     */
    @Test
    void moveBasedOnStrategy_whenOnMountain_marksExtendedVisionGrassVisited_beforeWrappingFailure() {
        WayHelper wayHelper = mock(WayHelper.class);
        StateHolder stateHolder = new StateHolder();
        ShortestPathFinder shortestPathFinder = mock(ShortestPathFinder.class);
        TreasureSeeker treasureSeeker = mock(TreasureSeeker.class);
        FortSeeker fortSeeker = mock(FortSeeker.class);
        StrategyGuide strategyGuide = mock(StrategyGuide.class);

        LinkedHashMap<MapNode, Boolean> ownVisited = new LinkedHashMap<>();
        LinkedHashMap<MapNode, Boolean> oppVisited = new LinkedHashMap<>();
        LinkedHashMap<MapNode, Boolean> mountainsVisited = new LinkedHashMap<>();
        when(wayHelper.getHalfMapVisitedGrassFields()).thenReturn(ownVisited);
        when(wayHelper.getOppHalfMapVisitedGrassFields()).thenReturn(oppVisited);
        when(wayHelper.getAllMountainFieldsMap()).thenReturn(mountainsVisited);

        MapNode current = new MapNode(0, 0, Terrain.MOUNTAIN, false, false);
        MapNode visible1 = new MapNode(0, 1, Terrain.GRASS, false, false);
        MapNode visible2 = new MapNode(1, 1, Terrain.GRASS, false, false);
        when(strategyGuide.getGrassNodesFromExtendedVision(current)).thenReturn(new ArrayList<>(List.of(visible1, visible2)));

        when(treasureSeeker.getTreasureNodeIfFound()).thenReturn(Optional.empty());
        when(shortestPathFinder.computeCostMapFromCurrent(any())).thenThrow(new RuntimeException("later failure"));

        GameState gameState = mock(GameState.class);
        when(gameState.getMap()).thenReturn(Optional.empty());
        when(gameState.isPlayerInOwnHalfMap()).thenReturn(true);

        WayFinderLogic logic = new WayFinderLogic(
                wayHelper,
                stateHolder,
                shortestPathFinder,
                treasureSeeker,
                fortSeeker,
                strategyGuide
        );

        assertThrows(AIDecisionException.class, () -> logic.moveBasedOnStrategy(gameState, current, Objective.TREASURE));

        assertEquals(Boolean.TRUE, mountainsVisited.get(current));
        assertEquals(Boolean.TRUE, ownVisited.get(visible1));
        assertEquals(Boolean.TRUE, ownVisited.get(visible2));
    }

    private static GameMap buildFullMap(OwnToOppMapOrientation orientation) {
        ArrayList<MapNode> nodes = new ArrayList<>();
        int maxX;
        int maxY;

        // Horizontal orientations produce a 20x5 full map.
        if (orientation == OwnToOppMapOrientation.LEFT_RIGHT || orientation == OwnToOppMapOrientation.RIGHT_LEFT) {
            maxX = 19;
            maxY = 4;
        } else {
            // Vertical orientations produce a 10x10 full map.
            maxX = 9;
            maxY = 9;
        }

        for (int y = 0; y <= maxY; y++) {
            for (int x = 0; x <= maxX; x++) {
                nodes.add(new MapNode(x, y, Terrain.GRASS, false, false));
            }
        }

        return new GameMap(nodes, orientation, maxX, maxY);
    }
}
