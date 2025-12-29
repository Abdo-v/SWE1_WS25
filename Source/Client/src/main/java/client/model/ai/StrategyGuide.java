package client.model.ai;

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

    private Optional<GameState> gameState;
    private final WayHelper wayHelper;

    StrategyGuide(WayHelper wayHelper) {
        this.gameState = Optional.empty();
        this.wayHelper = Objects.requireNonNull(wayHelper, "wayHelper is required");
    }

    @Override
    public void update(GameState gameState) {
        this.gameState = Optional.of(Objects.requireNonNull(gameState, "gameState is required"));
        wayHelper.update(gameState);
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
        return grassNodes;
    }
    
    private ArrayList<MapNode> getSurroundingNodes(MapNode position) {
        Objects.requireNonNull(position, "position is required");
        
        MapNode currentMapNode = position;
        ArrayList<MapNode> nodes = new ArrayList<>();

        if (gameState.isEmpty()) return nodes;
        GameState state = gameState.orElseThrow();
        var mapOptional = state.getMap();
        if (mapOptional.isEmpty()) return nodes;
        var map = mapOptional.orElseThrow();
        
        int currentX = currentMapNode.getX();
        int currentY = currentMapNode.getY();
        
        for(int x = currentX - 1; x <= currentX + 1; x++) {
            if (x < 0 || x > map.getMaxX()) {
                continue; // Skip out of bounds X coordinates
            }
            for(int y = currentY - 1; y <= currentY + 1; y++) {
                if (y < 0 || y > map.getMaxY()) {
                    continue; // Skip out of bounds Y coordinates
                }
                try {
                    MapNode node = map.getNode(x, y);
                    if (!node.equalsByCoordinates(currentMapNode)) {
                        nodes.add(node);
                    }
                } catch (IllegalArgumentException e) {
                    // Intentionally ignored: the map may not contain a node for these coordinates
                    // (e.g., sparse/irregular maps), so we skip invalid positions.
                }
            }
        }
        return nodes;
    }
    
    LinkedHashMap<MapNode,Boolean> getAllMountainFields(){
        return wayHelper.getAllMountainFields();
    }
}
