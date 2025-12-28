package client.model.ai;

import client.model.GameState;
import client.model.mapper.GameMap;
import client.model.mapper.MapNode;
import client.model.mapper.OwnToOppMapOrientation;
import client.model.mapper.PlayerHalfMap;
import client.model.mapper.Terrain;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ExplorationTargetUseCase}.
 *
 * <p>Focuses on selection rules (equivalence classes + tie-breakers) and error handling.
 */
class ExplorationTargetUseCaseTest {

    /**
     * Ensures that when score ties, mountains win over grass (tie-breaker #1).
     */
    @Test
    void selectBestNode_whenScoreTies_prefersMountainOverGrass() {
        WayHelper wayHelper = mock(WayHelper.class);
        TreasureSeeker treasureSeeker = mock(TreasureSeeker.class);
        VisionCostScorer scorer = mock(VisionCostScorer.class);
        ExplorationTargetUseCase useCase = new ExplorationTargetUseCase(wayHelper, treasureSeeker, scorer);

        MapNode current = new MapNode(0, 0, Terrain.GRASS, false, false);
        MapNode grass = new MapNode(1, 0, Terrain.GRASS, false, false);
        MapNode mountain = new MapNode(2, 0, Terrain.MOUNTAIN, false, false);

        LinkedHashMap<MapNode, Boolean> ownVisited = new LinkedHashMap<>();
        ownVisited.put(grass, false);
        ownVisited.put(mountain, false);
        when(wayHelper.getHalfMapVisitedGrassFields()).thenReturn(ownVisited);

        LinkedHashMap<MapNode, Boolean> oppVisited = new LinkedHashMap<>();
        when(wayHelper.getOppHalfMapVisitedGrassFields()).thenReturn(oppVisited);

        LinkedHashMap<MapNode, Boolean> mountainVisited = new LinkedHashMap<>();
        mountainVisited.put(mountain, false);
        when(wayHelper.getAllMountainFieldsMap()).thenReturn(mountainVisited);

        PlayerHalfMap arranged = new PlayerHalfMap();
        arranged.addMapNode(grass);
        arranged.addMapNode(mountain);
        when(treasureSeeker.getArrangedOwnHalfMap(current)).thenReturn(arranged);

        when(scorer.score(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(1.0f);

        GameState gameState = new GameState("gs");
        Map<MapNode, Integer> costMap = Map.of(grass, 5, mountain, 5);

        MapNode chosen = useCase.selectBestNode(gameState, current, Objective.TREASURE, costMap);

        assertEquals(mountain, chosen);
    }

    /**
     * Ensures that when score ties and terrain ties, the lower cost wins (tie-breaker #2).
     */
    @Test
    void selectBestNode_whenScoreTies_prefersLowerCost() {
        WayHelper wayHelper = mock(WayHelper.class);
        TreasureSeeker treasureSeeker = mock(TreasureSeeker.class);
        VisionCostScorer scorer = mock(VisionCostScorer.class);
        ExplorationTargetUseCase useCase = new ExplorationTargetUseCase(wayHelper, treasureSeeker, scorer);

        MapNode current = new MapNode(0, 0, Terrain.GRASS, false, false);
        MapNode a = new MapNode(1, 0, Terrain.GRASS, false, false);
        MapNode b = new MapNode(2, 0, Terrain.GRASS, false, false);

        LinkedHashMap<MapNode, Boolean> ownVisited = new LinkedHashMap<>();
        ownVisited.put(a, false);
        ownVisited.put(b, false);
        when(wayHelper.getHalfMapVisitedGrassFields()).thenReturn(ownVisited);
        when(wayHelper.getOppHalfMapVisitedGrassFields()).thenReturn(new LinkedHashMap<>());
        when(wayHelper.getAllMountainFieldsMap()).thenReturn(new LinkedHashMap<>());

        PlayerHalfMap arranged = new PlayerHalfMap();
        arranged.addMapNode(a);
        arranged.addMapNode(b);
        when(treasureSeeker.getArrangedOwnHalfMap(current)).thenReturn(arranged);

        when(scorer.score(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(2.0f);

        GameState gameState = new GameState("gs");
        Map<MapNode, Integer> costMap = Map.of(a, 7, b, 3);

        MapNode chosen = useCase.selectBestNode(gameState, current, Objective.TREASURE, costMap);

        assertEquals(b, chosen);
    }

    /**
     * Ensures treasure search prefers nodes deeper in the own half when score+cost tie (tie-breaker #3).
     */
    @Test
    void selectBestNode_whenTreasureAndCostTie_prefersDeeperNode() {
        WayHelper wayHelper = mock(WayHelper.class);
        TreasureSeeker treasureSeeker = mock(TreasureSeeker.class);
        VisionCostScorer scorer = mock(VisionCostScorer.class);
        ExplorationTargetUseCase useCase = new ExplorationTargetUseCase(wayHelper, treasureSeeker, scorer);

        MapNode current = new MapNode(0, 0, Terrain.GRASS, false, false);
        MapNode shallow = new MapNode(1, 4, Terrain.GRASS, false, false);
        MapNode deep = new MapNode(1, 0, Terrain.GRASS, false, false);

        LinkedHashMap<MapNode, Boolean> ownVisited = new LinkedHashMap<>();
        ownVisited.put(shallow, false);
        ownVisited.put(deep, false);
        when(wayHelper.getHalfMapVisitedGrassFields()).thenReturn(ownVisited);
        when(wayHelper.getOppHalfMapVisitedGrassFields()).thenReturn(new LinkedHashMap<>());
        when(wayHelper.getAllMountainFieldsMap()).thenReturn(new LinkedHashMap<>());

        PlayerHalfMap arranged = new PlayerHalfMap();
        arranged.addMapNode(shallow);
        arranged.addMapNode(deep);
        when(treasureSeeker.getArrangedOwnHalfMap(current)).thenReturn(arranged);

        when(scorer.score(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(3.0f);

        ArrayList<MapNode> nodes = new ArrayList<>();
        nodes.add(current);
        nodes.add(shallow);
        nodes.add(deep);
        GameMap map = new GameMap(nodes, OwnToOppMapOrientation.UP_DOWN, 9, 9);
        GameState gameState = new GameState("gs", new ArrayList<>(), map);

        Map<MapNode, Integer> costMap = Map.of(shallow, 10, deep, 10);

        MapNode chosen = useCase.selectBestNode(gameState, current, Objective.TREASURE, costMap);

        assertEquals(deep, chosen);
    }
}
