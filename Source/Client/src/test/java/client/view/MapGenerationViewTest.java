package client.view;

import client.model.mapper.MapNode;
import client.model.mapper.PlayerHalfMap;
import client.model.mapper.Terrain;
import client.view.testsupport.StdIoCapture;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MapGenerationViewTest {

    private static final String ANSI_ESCAPE_PREFIX = "\u001B[";

    @Test
    void printHalfMap_whenEmpty_printsNoDataMessageOnly() {
        try (StdIoCapture io = new StdIoCapture()) {
            new MapGenerationView().printHalfMap(new PlayerHalfMap("p1"), "HalfMap");

            String out = io.stdout();
            assertTrue(out.contains("HalfMap: (no half-map data)"));
            assertFalse(out.contains(CLILegends.halfMapLegend()), "Expected early return without legend");
            assertEquals("", io.stderr());
            assertFalse(out.contains(ANSI_ESCAPE_PREFIX));
        }
    }

    @Test
    void printHalfMap_whenHasData_printsLegendAndSeparator_andUsesStdoutOnly() {
        try (StdIoCapture io = new StdIoCapture()) {
            PlayerHalfMap halfMap = new PlayerHalfMap("p1");
            halfMap.addMapNode(new MapNode(0, 0, Terrain.GRASS, false, false));
            halfMap.addMapNode(new MapNode(1, 0, Terrain.MOUNTAIN, false, false));
            halfMap.addMapNode(new MapNode(0, 1, Terrain.WATER, false, false));
            halfMap.addMapNode(new MapNode(1, 1, Terrain.GRASS, true, false));

            new MapGenerationView().printHalfMap(halfMap, "Gen");

            String out = io.stdout();
            assertTrue(out.contains("Gen (Half Map):"));
            assertTrue(out.contains(CLILegends.halfMapLegend()));
            assertTrue(out.contains(CLITexts.SEPARATOR_HALF_MAP));
            assertTrue(out.contains("|"), "Expected grid rows to be enclosed by '|'");

            // Meaningful: fort icon has its own dedicated visualization.
            assertTrue(out.contains(CLIIcons.OWN_FORT));

            assertEquals("", io.stderr());
            assertFalse(out.contains(ANSI_ESCAPE_PREFIX));
        }
    }

    @Test
    void printHalfMap_fortHasPriorityOverTerrainIcon() {
        try (StdIoCapture io = new StdIoCapture()) {
            PlayerHalfMap halfMap = new PlayerHalfMap("p1");

            // Only a single node so we can make a clear assertion.
            MapNode fortOnWater = new MapNode(0, 0, Terrain.WATER, true, false);
            halfMap.addMapNode(fortOnWater);

            new MapGenerationView().printHalfMap(halfMap, "Gen");

            String out = io.stdout();
            assertTrue(out.contains(CLIIcons.OWN_FORT), "Expected fort icon");

            // The legend intentionally contains terrain icons (including water). What must hold is:
            // in the rendered grid, the fort cell uses the fort icon (not the underlying terrain icon).
            String gridLine = out.lines()
                    .filter(line -> line.startsWith("|") && line.endsWith("|"))
                    .findFirst()
                    .orElseThrow();

            assertEquals("|" + CLIIcons.OWN_FORT + "|", gridLine.trim());
            assertFalse(gridLine.contains(CLIIcons.WATER), "Fort grid cell must not render water icon");

            assertEquals("", io.stderr());
            assertFalse(out.contains(ANSI_ESCAPE_PREFIX));
        }
    }
}
