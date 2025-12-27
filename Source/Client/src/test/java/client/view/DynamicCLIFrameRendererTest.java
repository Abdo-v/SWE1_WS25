package client.view;

import client.model.GameState;
import client.model.PlayerState;
import client.model.PlayerStatus;
import client.model.mapper.GameMap;
import client.model.mapper.MapNode;
import client.model.mapper.OwnToOppMapOrientation;
import client.model.mapper.Terrain;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class DynamicCLIFrameRendererTest {

    @Test
    void render_whenMapNotAvailable_rendersPlayerInfoAndMapNotAvailable() {
        GameState state = new GameState("id");

        var frame = new DynamicCLIFrameRenderer().render(state);

        assertNotNull(frame);
        assertTrue(frame.text().contains(CLITexts.GAME_STATE_HEADER));
        assertTrue(frame.text().contains("Your Player: Data N/A"));
        assertTrue(frame.text().contains("Opponent: Data N/A"));
        assertTrue(frame.text().contains(CLITexts.MAP_NOT_AVAILABLE));
        assertTrue(frame.lineCount() > 0);
    }

    @Test
    void render_whenMapDimensionsInvalid_returnsSingleLineErrorSection() {
        GameMap invalidMap = new GameMap(new ArrayList<>(), OwnToOppMapOrientation.UP_DOWN, -1, -1);
        GameState state = new GameState("id", new ArrayList<>(), invalidMap);

        var frame = new DynamicCLIFrameRenderer().render(state);

        assertTrue(frame.text().contains(CLITexts.MAP_DIMENSIONS_INVALID));
    }

    @Test
    void render_whenPlayersAndMapPresent_rendersIconsAndPositions() {
        // Create nodes (keep shared references for equality checks).
        MapNode myCell = new MapNode(0, 0, Terrain.GRASS, false, false);
        MapNode opponentCell = new MapNode(1, 0, Terrain.WATER, false, false);
        MapNode treasureCell = new MapNode(0, 1, Terrain.MOUNTAIN, false, false);
        MapNode fortCell = new MapNode(1, 1, Terrain.GRASS, true, false);

        ArrayList<MapNode> nodes = new ArrayList<>();
        nodes.add(myCell);
        nodes.add(opponentCell);
        nodes.add(treasureCell);
        nodes.add(fortCell);

        GameMap map = new GameMap(nodes, OwnToOppMapOrientation.UP_DOWN, 1, 1);

        PlayerState me = new PlayerState("p1", "A", "B", "u", true, myCell, PlayerStatus.MUST_WAIT);
        PlayerState opp = new PlayerState("p2", "C", "D", "u2", false, opponentCell, PlayerStatus.MUST_WAIT);

        ArrayList<PlayerState> players = new ArrayList<>();
        players.add(me);
        players.add(opp);

        GameState state = new GameState("id", players, map);
        state.setTreasurePosition(treasureCell);

        var frame = new DynamicCLIFrameRenderer().render(state);

        assertTrue(frame.text().contains(CLIIcons.PLAYER_WITH_TREASURE));
        assertTrue(frame.text().contains(CLIIcons.OPPONENT));
        // Treasure should NOT be shown when my treasure already collected.
        assertFalse(frame.text().contains(CLIIcons.TREASURE));
        assertTrue(frame.text().contains(CLIIcons.OWN_FORT));

        // Player info includes coordinates.
        assertTrue(frame.text().contains("Position: (0,0)"));
        assertTrue(frame.text().contains("Position: (1,0)"));
        assertTrue(frame.lineCount() >= 2);
    }
}
