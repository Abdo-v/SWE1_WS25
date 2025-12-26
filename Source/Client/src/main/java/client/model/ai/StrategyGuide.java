package client.model.ai;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.Optional;

import client.model.GameState;
import client.model.mapper.MapNode;
import client.model.mapper.Terrain;

class StrategyGuide implements client.observer.util.Observer {
    // private static final Logger logger = LoggerFactory.getLogger(StrategyGuide.class);

    private Optional<GameState> gameState;
    private final WayHelper wayHelper;

    /**
     * Default constructor for StrategyGuide.
     * Initializes the gameState to empty.
     */
    StrategyGuide(WayHelper wayHelper) {
        this.gameState = Optional.empty();
        this.wayHelper = Objects.requireNonNull(wayHelper);
    }

    @Override
    public void update(GameState gameState) {
        // logger.trace("StrategyGuide received GameState update");
        this.gameState = Optional.of(Objects.requireNonNull(gameState, "gameState must not be null"));
        wayHelper.update(gameState);

        Optional.of(gameState)
                .flatMap(GameState::getMap)
                .ifPresent(map -> {
                    int totalNodes = map.getGameMapNodes().size();
                    long mountainCount = map.getGameMapNodes().stream()
                            .filter(node -> node.getTerrain() == Terrain.MOUNTAIN)
                            .count();
                    // logger.trace("Updated StrategyGuide - total nodes: {}, mountains: {}", totalNodes, mountainCount);
                });
    }

    /**
     * Returns a list of grass nodes surrounding the given node.
     * @param currentNode The node from which to find surrounding grass nodes.
     * @return An ArrayList of MapNode objects representing grass nodes surrounding the current node.
     */
    ArrayList<MapNode> getGrassNodesFromExtendedVision(MapNode currentNode){
        if (Objects.isNull(currentNode)) return new ArrayList<>();
        
        ArrayList<MapNode> grassNodes = new ArrayList<>();
        ArrayList<MapNode> surroundingNodes = getSurroundingNodes(currentNode);
        
        for (MapNode node : surroundingNodes) {
            if (node.getTerrain() == Terrain.GRASS) {
                grassNodes.add(node);
            }
        }
        
        // logger.debug("Extended vision from {} found {} grass nodes out of {} surrounding nodes", 
        //             currentNode.printCoordinates(), grassNodes.size(), surroundingNodes.size());
        return grassNodes;
    }
    
    /**
     * Returns a list of surrounding nodes for the given position.
     * This includes diagonal neighbors and skips out-of-bounds coordinates.
     * @param position The MapNode position from which to find surrounding nodes.
     * @return An ArrayList of MapNode objects representing the surrounding nodes.
     */
    private ArrayList<MapNode> getSurroundingNodes(MapNode position) {
        if (Objects.isNull(position)) return new ArrayList<>();
        
        // logger.trace("Getting surrounding nodes for position: {}", position.printCoordinates());
        MapNode currentMapNode = position;
        //System.err.println("extended VISION: called, current: " + currentMapNode.toString());
        ArrayList<MapNode> nodes = new ArrayList<>();

        if (gameState.isEmpty()) return nodes;
        GameState state = gameState.orElseThrow();
        var mapOptional = state.getMap();
        if (mapOptional.isEmpty()) return nodes;
        var map = mapOptional.orElseThrow();
        
        // get neighbors (also diagonal)
        int currentX = currentMapNode.getX();
        int currentY = currentMapNode.getY();
        int outOfBoundsCount = 0;
        
        for(int x = currentX - 1; x <= currentX + 1; x++) {
            if (x < 0 || x > map.getMaxX()) {
                outOfBoundsCount++;
                continue; // Skip out of bounds X coordinates
            }
            for(int y = currentY - 1; y <= currentY + 1; y++) {
                if (y < 0 || y > map.getMaxY()) {
                    outOfBoundsCount++;
                    continue; // Skip out of bounds Y coordinates
                }
                try {
                    MapNode node = map.getNode(x, y);
                    if (!node.equalsByCoordinates(currentMapNode)) {
                        nodes.add(node);
                    }
                } catch (IllegalArgumentException e) {
                    // Out of bounds, skip this node
                    outOfBoundsCount++;
                    // logger.trace("Skipped out-of-bounds coordinate ({}, {})", x, y);
                }
            }
        }
        
        // logger.trace("Found {} surrounding nodes for {} (skipped {} out-of-bounds)", 
        //             nodes.size(), position.printCoordinates(), outOfBoundsCount);
        //System.out.println("extended VISION: found nodes: " + nodes.toString());
        return nodes;
    }
    
    /**
     * Returns a LinkedHashMap of all mountain fields in the game map.
     * The keys are MapNode objects representing the mountain nodes,
     * and the values are initialized to false (indicating unvisited).
     * @return A LinkedHashMap containing all mountain fields.
     */
    LinkedHashMap<MapNode,Boolean> getAllMountainFields(){
        return wayHelper.getAllMountainFields();
    }
}
