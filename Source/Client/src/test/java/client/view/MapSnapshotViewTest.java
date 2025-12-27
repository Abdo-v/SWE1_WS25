package client.view;

import client.model.GameState;
import client.model.PlayerState;
import client.model.PlayerStatus;
import client.model.mapper.GameMap;
import client.model.mapper.MapNode;
import client.model.mapper.OwnToOppMapOrientation;
import client.model.mapper.Terrain;
import client.view.testsupport.StdIoCapture;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests {@link MapSnapshotView} snapshot output for missing/incomplete maps and full-map rendering.
 */
class MapSnapshotViewTest {

    private static final String ANSI_ESCAPE_PREFIX = "\u001B[";

    @Test
    void visualize_whenMapMissing_printsNotAvailableToStdoutOnly() {
        try (StdIoCapture io = new StdIoCapture()) {
            GameState state = new GameState("gs-1");

            new MapSnapshotView().visualize(state, MapVisualizationType.FULL);

            assertTrue(io.stdout().contains("FULL map visual: map not available"));
            assertEquals("", io.stderr());
            assertFalse(io.stdout().contains(ANSI_ESCAPE_PREFIX));
        }
    }

    @Test
    void visualize_whenFullMapIncomplete_printsIncompleteMessage() {
        try (StdIoCapture io = new StdIoCapture()) {
            ArrayList<MapNode> nodes = new ArrayList<>();
            // Not 100 nodes -> should be treated as incomplete.
            for (int y = 0; y <= 4; y++) {
                for (int x = 0; x <= 9; x++) {
                    nodes.add(new MapNode(x, y, Terrain.GRASS, false, false));
                }
            }

            GameMap map = new GameMap(nodes, OwnToOppMapOrientation.UP_DOWN, 9, 4);
            GameState state = new GameState("gs-2", new ArrayList<>(), map);

            new MapSnapshotView().visualize(state, MapVisualizationType.FULL);

            assertTrue(io.stdout().contains("Full map visual: map incomplete"));
            assertEquals("", io.stderr());
            assertFalse(io.stdout().contains(ANSI_ESCAPE_PREFIX));
        }
    }

    @Test
    void visualize_whenFullMapAvailable_printsLegendSeparatorsAndIcons() {
        try (StdIoCapture io = new StdIoCapture()) {
            ArrayList<MapNode> nodes = new ArrayList<>();

            MapNode myCell = null;
            MapNode oppCell = null;
            MapNode treasureCell = null;
            MapNode fortCell = null;

            // FULL map in this client is 100 nodes. With LEFT_RIGHT orientation that is 20x5,
            // where the own half is x=0..9 (50 nodes) and opponent half is x=10..19 (50 nodes).
            for (int y = 0; y <= 4; y++) {
                for (int x = 0; x <= 19; x++) {
                    MapNode node = new MapNode(x, y, Terrain.GRASS, false, false);
                    nodes.add(node);

                    if (x == 0 && y == 0) {
                        myCell = node;
                    }
                    if (x == 1 && y == 0) {
                        oppCell = node;
                    }
                    if (x == 2 && y == 0) {
                        treasureCell = node;
                    }
                    if (x == 3 && y == 0) {
                        fortCell = node;
                    }
                }
            }

            assertNotNull(myCell);
            assertNotNull(oppCell);
            assertNotNull(treasureCell);
            assertNotNull(fortCell);

            fortCell.setFortPresent(true);

            GameMap map = new GameMap(nodes, OwnToOppMapOrientation.LEFT_RIGHT, 19, 4);

            PlayerState me = new PlayerState("p1", "A", "B", "u", true, myCell, PlayerStatus.MUST_WAIT);
            PlayerState opp = new PlayerState("p2", "C", "D", "u2", false, oppCell, PlayerStatus.MUST_WAIT);

            ArrayList<PlayerState> players = new ArrayList<>();
            players.add(me);
            players.add(opp);

            GameState state = new GameState("gs-3", players, map);
            state.setTreasurePosition(treasureCell);

            new MapSnapshotView().visualize(state, MapVisualizationType.FULL);

            String out = io.stdout();

            assertTrue(out.contains("Full Map Snapshot"));
            assertTrue(out.contains(CLILegends.fullMapLegend()));
            assertTrue(out.contains(CLITexts.SEPARATOR_SHORT));

            // Meaningful: icons representing the required game details appear in the snapshot.
            assertTrue(out.contains(CLIIcons.OWN_FORT));
            assertTrue(out.contains(CLIIcons.TREASURE));
            assertTrue(out.contains(CLIIcons.PLAYER));
            assertTrue(out.contains(CLIIcons.OPPONENT));

            assertEquals("", io.stderr());
            assertFalse(out.contains(ANSI_ESCAPE_PREFIX));
        }
    }
}
