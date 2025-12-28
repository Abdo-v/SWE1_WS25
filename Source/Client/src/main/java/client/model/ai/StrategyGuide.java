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

/**
 * Helper that derives small tactical facts from the current map.
 *
 * <p>Consumes {@link GameState} updates and delegates shared bookkeeping to {@link WayHelper}.
 */
class StrategyGuide implements client.observer.util.Observer {
    // private static final Logger logger = LoggerFactory.getLogger(StrategyGuide.class);

    private Optional<GameState> gameState;
    private final WayHelper wayHelper;

    StrategyGuide(WayHelper wayHelper) {
        this.gameState = Optional.empty();
        this.wayHelper = Objects.requireNonNull(wayHelper, "wayHelper is required");
    }

    @Override
    public void update(GameState gameState) {
        // logger.trace("StrategyGuide received GameState update");
        this.gameState = Optional.of(Objects.requireNonNull(gameState, "gameState is required"));
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

    ArrayList<MapNode> getGrassNodesFromExtendedVision(MapNode currentNode){
        Objects.requireNonNull(currentNode, "currentNode is required");
        
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
    
    private ArrayList<MapNode> getSurroundingNodes(MapNode position) {
        Objects.requireNonNull(position, "position is required");
        
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
    
    LinkedHashMap<MapNode,Boolean> getAllMountainFields(){
        return wayHelper.getAllMountainFields();
    }
}
