package client.model.mapper.generator;

import client.model.common.Notification;
import client.model.mapper.MapNode;
import client.model.mapper.MapRules;
import client.model.mapper.validator.MapValidator;
import client.model.mapper.PlayerHalfMap;
import client.model.mapper.Terrain;

import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;

public class MapGenerator {
    private final MapGenerationConfig config;
    private final Supplier<Random> randomSupplier;
    private final MapValidator validator;

    public MapGenerator() {
        this(MapGenerationConfig.defaultConfig(), Random::new, new MapValidator());
    }

    /**
     * Constructs a MapGenerator with custom generation parameters.
     *
     * @param config configuration object that contains generation values.
     */
    public MapGenerator(MapGenerationConfig config) {
        this(config, Random::new, new MapValidator());
    }

    /**
     * Constructs a MapGenerator with injected dependencies.
     *
     * <p>Inject a deterministic {@link Random} supplier (e.g. {@code () -> new Random(123)})
     * and a {@link MapValidator} to make generation predictable and easier to test.
     */
    public MapGenerator(MapGenerationConfig config, Supplier<Random> randomSupplier, MapValidator validator) {
        this.config = Objects.requireNonNull(config, "config");
        this.randomSupplier = Objects.requireNonNull(randomSupplier, "randomSupplier");
        this.validator = Objects.requireNonNull(validator, "validator");
    }

    /**
     * Generates a half map with the specified width, height, and player ID.
     *
     * @param width    The width of the map.
     * @param height   The height of the map.
     * @param playerID The ID of the player.
     * @return The generated PlayerHalfMap.
     * @throws IllegalArgumentException if width or height is not positive.
     */
    public PlayerHalfMap generateMap(int width, int height, String playerID) {
        return generateMap(width, height, playerID, Optional.empty());
    }

    /**
     * Generates a half map with the specified width, height, and player ID.
     *
     * <p>If an existing half map is provided (for the "second" client), generation also tries to ensure
     * that edge transitions are possible on at least {@link MapRules#MIN_EDGE_CROSSABLE_RATIO} of each edge
     * (walkable on both sides).
     */
    public PlayerHalfMap generateMap(int width, int height, String playerID, PlayerHalfMap existingHalfMap) {
        Objects.requireNonNull(existingHalfMap, "existingHalfMap is required");
        return generateMap(width, height, playerID, Optional.of(existingHalfMap));
    }

    /**
     * Generates a half map with the specified width, height, and player ID.
     *
     * <p>If an existing half map is present (for the "second" client), generation also tries to ensure
     * that edge transitions are possible on at least {@link MapRules#MIN_EDGE_CROSSABLE_RATIO} of each edge
     * (walkable on both sides).
     */
    public PlayerHalfMap generateMap(int width, int height, String playerID, Optional<PlayerHalfMap> existingHalfMap) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Width and height must be positive");
        }

        Objects.requireNonNull(playerID, "playerID");
        Objects.requireNonNull(existingHalfMap, "existingHalfMap");

        boolean validMap = false;
        Random random = randomSupplier.get();

        while (!validMap) {
            PlayerHalfMap halfMap = new PlayerHalfMap(playerID);

            Terrain[][] terrainGrid = new Terrain[width][height];
            boolean[][] fortGrid = new boolean[width][height];

            HalfMapTerrainSetup.initializeWithGrassAndNoFort(terrainGrid, fortGrid, width, height);

            int mountainCells = MapGenerationRandom.inInclusiveRange(random, config.minMountainTiles(), config.maxMountainTiles());
            int waterCells = MapGenerationRandom.inInclusiveRange(random, config.minWaterTiles(), config.maxWaterTiles());
            int fortCells = config.fortTiles();

            fortGrid = HalfMapTerrainSetup.possibleFortPositions(width, height, fortCells, random);

            TerrainGridPlacer.placeTerrain(terrainGrid, fortGrid, Terrain.MOUNTAIN, mountainCells, width, height, random);

            if (!HalfMapWaterPlacer.placeWaterWithConstraints(
                    terrainGrid,
                    fortGrid,
                    waterCells,
                    width,
                    height,
                    random,
                    existingHalfMap,
                    config)) {
                continue;
            }

            // Extra guard: ensure we never accept a map that violates the edge rules.
            // (This matches what the server validates for non-enterable border fields.)
            if (!TerrainBorderConstraints.checkBorderConstraints(terrainGrid, width, height)
                    || !TerrainGridConnectivity.checkConnectivity(terrainGrid, width, height)) {
                continue;
            }

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    MapNode node = new MapNode(x, y, terrainGrid[x][y], fortGrid[x][y], false);
                    halfMap.addMapNode(node);
                }
            }

            Notification result = existingHalfMap
                    .map(existing -> validator.validate(halfMap, existing))
                    .orElseGet(() -> validator.validate(halfMap));
            validMap = !result.hasErrors();
            if (validMap) {
                return halfMap;
            }
        }

        throw new IllegalStateException("Map generation failed unexpectedly");
    }
}
