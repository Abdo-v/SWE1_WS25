package client.view;

import client.model.GameState;
import client.model.PlayerState;
import client.model.PlayerStatus;
import client.model.mapper.GameMap;
import client.model.mapper.MapNode;
import client.model.mapper.OwnToOppMapOrientation;
import client.model.mapper.Terrain;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class MapCellRendererTest {

    @Test
    void renderCell_whenNodeIsEmpty_returnsUnknown() {
        GameState state = new GameState("id");

        String rendered = MapCellRenderer.renderCell(
                state,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                false,
                false
        );

        assertEquals(CLIIcons.UNKNOWN, rendered);
    }

    @Test
    void renderCell_whenBothPlayersOnSameCell_returnsClash() {
        GameState state = new GameState("id");
        MapNode cell = new MapNode(0, 0, Terrain.GRASS, false, false);

        String rendered = MapCellRenderer.renderCell(
                state,
                Optional.of(cell),
                Optional.of(cell),
                Optional.of(cell),
                false,
                false
        );

        assertEquals(CLIIcons.CLASH, rendered);
    }

    @Test
    void renderCell_whenMyPlayerOnCell_prioritizesMyPlayerIcon() {
        GameState state = new GameState("id");
        MapNode cell = new MapNode(0, 0, Terrain.WATER, false, false);

        String rendered = MapCellRenderer.renderCell(
                state,
                Optional.of(cell),
                Optional.of(cell),
                Optional.empty(),
                true,
                false
        );

        assertEquals(CLIIcons.PLAYER_WITH_TREASURE, rendered);
    }

    @Test
    void renderCell_whenOpponentOnCell_prioritizesOpponentIcon() {
        GameState state = new GameState("id");
        MapNode cell = new MapNode(0, 0, Terrain.GRASS, false, false);

        String rendered = MapCellRenderer.renderCell(
                state,
                Optional.of(cell),
                Optional.empty(),
                Optional.of(cell),
                false,
                true
        );

        assertEquals(CLIIcons.OPPONENT_WITH_TREASURE, rendered);
    }

    @Test
    void renderCell_whenTreasureAtCellAndNotCollected_showsTreasure() {
        GameState state = new GameState("id");
        MapNode treasureCell = new MapNode(0, 0, Terrain.GRASS, false, false);
        state.setTreasurePosition(treasureCell);

        String rendered = MapCellRenderer.renderCell(
                state,
                Optional.of(treasureCell),
                Optional.empty(),
                Optional.empty(),
                false,
                false
        );

        assertEquals(CLIIcons.TREASURE, rendered);
    }

    @Test
    void renderCell_whenFortIsInOwnHalf_showsOwnFort() {
        MapNode fortCell = new MapNode(0, 0, Terrain.GRASS, true, false);
        ArrayList<MapNode> nodes = new ArrayList<>();
        nodes.add(fortCell);

        GameMap map = new GameMap(nodes, OwnToOppMapOrientation.UP_DOWN, 0, 0);
        GameState state = new GameState("id", new ArrayList<>(), map);

        String rendered = MapCellRenderer.renderCell(
                state,
                Optional.of(fortCell),
                Optional.empty(),
                Optional.empty(),
                false,
                false
        );

        assertEquals(CLIIcons.OWN_FORT, rendered);
    }

    @Test
    void renderCell_whenFortIsNotInOwnHalf_showsOpponentFort() {
        MapNode fortCell = new MapNode(0, 6, Terrain.GRASS, true, false);
        ArrayList<MapNode> nodes = new ArrayList<>();
        nodes.add(fortCell);

        GameMap map = new GameMap(nodes, OwnToOppMapOrientation.UP_DOWN, 0, 6);
        GameState state = new GameState("id", new ArrayList<>(), map);

        String rendered = MapCellRenderer.renderCell(
                state,
                Optional.of(fortCell),
                Optional.empty(),
                Optional.empty(),
                false,
                false
        );

        assertEquals(CLIIcons.OPPONENT_FORT, rendered);
    }

    @ParameterizedTest
    @CsvSource({
            "GRASS,🟩",
            "MOUNTAIN,⬜",
            "WATER,🟦"
    })
    void renderCell_whenPlainTerrain_rendersTerrainIcon(Terrain terrain, String expectedIcon) {
        GameState state = new GameState("id");
        MapNode cell = new MapNode(0, 0, terrain, false, false);

        String rendered = MapCellRenderer.renderCell(
                state,
                Optional.of(cell),
                Optional.empty(),
                Optional.empty(),
                false,
                false
        );

        assertEquals(expectedIcon, rendered);
    }
}
