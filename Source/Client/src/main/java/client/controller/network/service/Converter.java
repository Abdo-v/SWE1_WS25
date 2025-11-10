package client.controller.network.service;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import client.model.Direction;
import client.model.mapper.GameMap;
import client.model.mapper.MapNode;
import client.model.mapper.OwnToOppMapOrientation;
import client.model.mapper.PlayerHalfMap;
import messagesbase.UniquePlayerIdentifier;

public class Converter {
    // private static final Logger logger = LoggerFactory.getLogger(Converter.class);
    
    Converter(){}

    /**
     * Converts the server game state to the internal representation.
     * @param serverGameState The server game state.
     * @return The internal game state.
     */
    public client.model.GameState convertServerGamestate(messagesbase.messagesfromserver.GameState serverGameState, UniquePlayerIdentifier playerId) {
        // logger.debug("Converting server game state for player: {}", 
        //             playerId != null ? playerId.getUniquePlayerID() : "unknown");
        
        ArrayList<client.model.PlayerState> clientPlayers = new ArrayList<>();
        messagesbase.messagesfromserver.FullMap serverMap = serverGameState.getMap();
        
        if (serverGameState.getPlayers() != null && !serverGameState.getPlayers().isEmpty()) {
            // logger.trace("Processing {} players from server game state", serverGameState.getPlayers().size());
            String myPlayerUniqueId = null;
            if (playerId != null) {
                myPlayerUniqueId = playerId.getUniquePlayerID();
            }
            if (myPlayerUniqueId != null) {
                for (messagesbase.messagesfromserver.PlayerState serverPlayer : serverGameState.getPlayers()) {
                    if (serverPlayer.getUniquePlayerID().equals(myPlayerUniqueId)) {
                        // logger.trace("Processing own player state for: {}", myPlayerUniqueId);
                        MapNode playerMapNode = new MapNode();
                        for (messagesbase.messagesfromserver.FullMapNode node : serverMap.getMapNodes()) {
                            if (node.getPlayerPositionState() == messagesbase.messagesfromserver.EPlayerPositionState.MyPlayerPosition || node.getPlayerPositionState() == messagesbase.messagesfromserver.EPlayerPositionState.BothPlayerPosition) {
                                playerMapNode.setX(node.getX());
                                playerMapNode.setY(node.getY());
                                playerMapNode.setTerrain(convertServerTerrain(node.getTerrain()));
                                playerMapNode.setFortPresent(isFortOnServerNode(node));
                                // logger.trace("Found own player position at ({}, {})", node.getX(), node.getY());
                            }
                        }
                        clientPlayers.add(convertServerPlayerState(serverPlayer, playerMapNode));
                        break;
                    }
                }
            }
            for (messagesbase.messagesfromserver.PlayerState serverPlayer : serverGameState.getPlayers()) {
                if (myPlayerUniqueId == null || !serverPlayer.getUniquePlayerID().equals(myPlayerUniqueId)) {
                    // logger.trace("Processing opponent player state for: {}", serverPlayer.getUniquePlayerID());
                    MapNode playerMapNode = new MapNode();
                    for (messagesbase.messagesfromserver.FullMapNode node : serverMap.getMapNodes()) {
                        if (node.getPlayerPositionState() == messagesbase.messagesfromserver.EPlayerPositionState.EnemyPlayerPosition || node.getPlayerPositionState() == messagesbase.messagesfromserver.EPlayerPositionState.BothPlayerPosition) {
                            playerMapNode.setX(node.getX());
                            playerMapNode.setY(node.getY());
                            playerMapNode.setTerrain(convertServerTerrain(node.getTerrain()));
                            playerMapNode.setFortPresent(isFortOnServerNode(node));
                            // logger.trace("Found opponent position at ({}, {})", node.getX(), node.getY());
                        }
                    }
                    clientPlayers.add(convertServerPlayerState(serverPlayer, playerMapNode));
                }
            }
        }
        client.model.GameState gameState = new client.model.GameState(serverGameState.getGameStateId(), clientPlayers, convertServerMap(serverGameState.getMap()));
        gameState.setTreasureCollected(clientPlayers.get(0).hasCollectedTreasure());
        gameState.setOpponentFortFound(serverMapHasEnemyFort(serverGameState.getMap()));
        if(getTreasurePositionFromServerMap(serverGameState.getMap()) != null) {
            gameState.setTreasurePosition(getTreasurePositionFromServerMap(serverGameState.getMap()));
            // logger.debug("Treasure position set at: {}", getTreasurePositionFromServerMap(serverGameState.getMap()).printCoordinates());
        } else {
            gameState.setTreasurePosition(null);
            // logger.trace("No treasure position found in server map");
        }
        if(getEnemyFortMapNodeFromServerMap(serverGameState.getMap()) != null) {
            gameState.setOpponentFortPosition(getEnemyFortMapNodeFromServerMap(serverGameState.getMap()));
            // logger.debug("Enemy fort position set at: {}", getEnemyFortMapNodeFromServerMap(serverGameState.getMap()).printCoordinates());
        } else {
            gameState.setOpponentFortPosition(null);
            // logger.trace("No enemy fort position found in server map");
        }
        // logger.debug("Game state conversion completed successfully");
        return gameState;
    }

    /**
     * Converts the server player state to the internal representation.
     * @param serverPlayerState The server player state.
     * @param playerMapNode The player's map node.
     * @return The internal player state.
     */
    public client.model.PlayerState convertServerPlayerState(messagesbase.messagesfromserver.PlayerState serverPlayerState, MapNode playerMapNode) {
        // logger.trace("Converting server player state for: {} {}", serverPlayerState.getFirstName(), serverPlayerState.getLastName());
        client.model.PlayerState playerState = new client.model.PlayerState(serverPlayerState.getUniquePlayerID(), serverPlayerState.getFirstName(), serverPlayerState.getLastName(), serverPlayerState.getUAccount(), serverPlayerState.hasCollectedTreasure(), playerMapNode, convertServerStatus(serverPlayerState.getState()));
        return playerState;
    }

    public client.model.PlayerStatus convertServerStatus(messagesbase.messagesfromserver.EPlayerGameState serverStatus){
        // logger.trace("Converting server status: {}", serverStatus);
        if (serverStatus == null) {
            // logger.error("Server status is null");
            throw new IllegalArgumentException("Server status cannot be null");
        }
        switch(serverStatus){
            case MustAct:
                return client.model.PlayerStatus.MUST_ACT;
            case MustWait:
                return client.model.PlayerStatus.MUST_WAIT;
            case Lost:
                return client.model.PlayerStatus.LOST;
            case Won:
                return client.model.PlayerStatus.WON;
            default:
                // logger.error("Unknown server status encountered: {}", serverStatus);
                throw new IllegalArgumentException("Unknown server status: " + serverStatus);
        }
    }

    /**
     * Converts the server map to the internal game map.
     * @param serverMap The server map.
     * @return The internal game map.
     */
    public client.model.mapper.GameMap convertServerMap(messagesbase.messagesfromserver.FullMap serverMap) {
        if (serverMap.isEmpty()) {
            // logger.info("Server map is empty, returning an empty GameMap");
            return new client.model.mapper.GameMap();
        }
        
        // logger.debug("Converting server map with {} nodes", serverMap.getMapNodes().size());
        Collection<messagesbase.messagesfromserver.FullMapNode> serverMapNodes = serverMap.getMapNodes();
        OwnToOppMapOrientation orientation = null;
        boolean vertical = false;
        boolean horizontal = false;
        for(messagesbase.messagesfromserver.FullMapNode node : serverMapNodes) {
            if (node.getY() > 4){vertical = true;break;}
            if (node.getX() > 9){horizontal = true;break;}
        }
        
        // logger.trace("Map orientation analysis - vertical: {}, horizontal: {}", vertical, horizontal);
        
        ArrayList<MapNode> internalNodes = new ArrayList<>();
        for (messagesbase.messagesfromserver.FullMapNode node : serverMapNodes) {
            internalNodes.add(convertServerMapNode(node));
        }
        // determine orientation
        if(vertical){
            for (messagesbase.messagesfromserver.FullMapNode node : serverMapNodes) {
                if(node.getFortState() == messagesbase.messagesfromserver.EFortState.MyFortPresent){
                    if(node.getY() <= 4){
                        orientation = OwnToOppMapOrientation.UP_DOWN;
                        // logger.debug("Map orientation determined: UP_DOWN (fort at Y={})", node.getY());
                    } else {
                        orientation = OwnToOppMapOrientation.DOWN_UP;
                        // logger.debug("Map orientation determined: DOWN_UP (fort at Y={})", node.getY());
                    }
                    break;
                }
            }
        }
        else if(horizontal){
            for (messagesbase.messagesfromserver.FullMapNode node : serverMapNodes) {
                if(node.getFortState() == messagesbase.messagesfromserver.EFortState.MyFortPresent) {
                    if(node.getX() <= 9){
                        orientation = OwnToOppMapOrientation.LEFT_RIGHT;
                        // logger.debug("Map orientation determined: LEFT_RIGHT (fort at X={})", node.getX());
                    } else {
                        orientation = OwnToOppMapOrientation.RIGHT_LEFT;
                        // logger.debug("Map orientation determined: RIGHT_LEFT (fort at X={})", node.getX());
                    }
                    break;
                }
            }
        }
        if (orientation == null) {
            // logger.warn("Map orientation could not be determined, returning empty GameMap");
            return new client.model.mapper.GameMap();
        }
        // Determine maxX and maxY based on orientation
        int maxX = 0;
        int maxY = 0;
        if (orientation == OwnToOppMapOrientation.UP_DOWN || orientation == OwnToOppMapOrientation.DOWN_UP){
            maxX = 9; // Full map width is 10 fields (0-9)
            maxY = 9; // Full map height is 10 fields (0-9)
        }
        else if (orientation == OwnToOppMapOrientation.LEFT_RIGHT || orientation == OwnToOppMapOrientation.RIGHT_LEFT){
            maxX = 19; // Full map width is 20 fields (0-19)
            maxY = 4;  // Full map height is 5 fields (0-4)
        }
        GameMap gameMap = new GameMap(internalNodes, orientation, maxX, maxY);
        // logger.debug("Server map conversion completed - orientation: {}, dimensions: {}x{}", orientation, maxX+1, maxY+1);
        return gameMap;
    }

    /**
     * Converts the server map node to the internal map node.
     * @param serverMapNode The server map node.
     * @return The internal map node.
     */
    public MapNode convertServerMapNode(messagesbase.messagesfromserver.FullMapNode serverMapNode) {
        // logger.trace("Converting server map node at ({}, {})", serverMapNode.getX(), serverMapNode.getY());
        return new MapNode(serverMapNode.getX(), serverMapNode.getY(), convertServerTerrain(serverMapNode.getTerrain()), isFortOnServerNode(serverMapNode), isTreasureOnServerNode(serverMapNode));
    }

    /**
     * Checks if the server map has an enemy fort.
     * @param serverMap The server map.
     * @return True if enemy fort is present, false otherwise.
     */
    public boolean serverMapHasEnemyFort(messagesbase.messagesfromserver.FullMap serverMap) {
        // logger.trace("Checking server map for enemy fort");
        for (messagesbase.messagesfromserver.FullMapNode node : serverMap.getMapNodes()) {
            if (node.getFortState() == messagesbase.messagesfromserver.EFortState.EnemyFortPresent) {
                // logger.debug("Enemy fort found in server map at ({}, {})", node.getX(), node.getY());
                return true;
            }
        }
        // logger.trace("No enemy fort found in server map");
        return false;
    }

    /**
     * Gets the treasure position from the server map.
     * @param serverMap The server map.
     * @return The treasure position as a MapNode, or null if not found.
     */
    private MapNode getTreasurePositionFromServerMap(messagesbase.messagesfromserver.FullMap serverMap) {
        for (messagesbase.messagesfromserver.FullMapNode node : serverMap.getMapNodes()) {
            if (node.getTreasureState() == messagesbase.messagesfromserver.ETreasureState.MyTreasureIsPresent) {
                return new MapNode(node.getX(), node.getY(), convertServerTerrain(node.getTerrain()), isFortOnServerNode(node), true);
            }
        }
        return null;
    }

    private MapNode getEnemyFortMapNodeFromServerMap(messagesbase.messagesfromserver.FullMap serverMap) {
        for (messagesbase.messagesfromserver.FullMapNode node : serverMap.getMapNodes()) {
            if (node.getFortState() == messagesbase.messagesfromserver.EFortState.EnemyFortPresent) {
                return new MapNode(node.getX(), node.getY(), convertServerTerrain(node.getTerrain()), true, false);
            }
        }
        return null;
    }

    /**
     * Converts the internal half map to the Server message format.
     * @param halfMap Our internal half map representation.
     * @return A half map in the format expected by the server.
     */
    public messagesbase.messagesfromclient.PlayerHalfMap convertClientHalfMap(PlayerHalfMap halfMap, UniquePlayerIdentifier playerId) {
        // logger.debug("Converting client half map to server format for player: {}", playerId.getUniquePlayerID());
        // logger.trace("Client half map contains {} nodes", halfMap.getMapNodes().size());
        messagesbase.messagesfromclient.PlayerHalfMap ServerHalfMap = new messagesbase.messagesfromclient.PlayerHalfMap(playerId, convertClientNodes(halfMap.getMapNodes()));
        return ServerHalfMap;
    }

    /**
     * Converts a list of internal map nodes to Server map nodes.
     * @param nodes List of internal map nodes.
     * @return Collection of Server map nodes.
     */
    public Collection<messagesbase.messagesfromclient.PlayerHalfMapNode> convertClientNodes(List<MapNode> nodes) {
        // logger.trace("Converting {} client nodes to server format", nodes.size());
        HashSet<messagesbase.messagesfromclient.PlayerHalfMapNode> ServerNodes = new HashSet<messagesbase.messagesfromclient.PlayerHalfMapNode>();
        for (MapNode node : nodes) {
            ServerNodes.add(new messagesbase.messagesfromclient.PlayerHalfMapNode(node.getX(), node.getY(), node.isFortPresent(), convertClientTerrain(node.getTerrain())));
        }
        return ServerNodes;
    }

    /**
     * Converts client terrain to Server terrain.
     * @param clientTerrain The client terrain.
     * @return The corresponding Server terrain.
     */
    public messagesbase.messagesfromclient.ETerrain convertClientTerrain(client.model.mapper.Terrain clientTerrain) {
        // logger.trace("Converting client terrain: {}", clientTerrain);
        if (clientTerrain == client.model.mapper.Terrain.MOUNTAIN) {
            return messagesbase.messagesfromclient.ETerrain.Mountain;
        } else if (clientTerrain == client.model.mapper.Terrain.WATER) {
            return messagesbase.messagesfromclient.ETerrain.Water;
        } else {
            return messagesbase.messagesfromclient.ETerrain.Grass;
        }
    }

    /**
     * Converts server terrain to client terrain.
     * @param serverTerrain The server terrain.
     * @return The corresponding client terrain.
     */
    public client.model.mapper.Terrain convertServerTerrain(messagesbase.messagesfromclient.ETerrain serverTerrain) {
        // logger.trace("Converting server terrain: {}", serverTerrain);
        if (serverTerrain == messagesbase.messagesfromclient.ETerrain.Mountain) {
            return client.model.mapper.Terrain.MOUNTAIN;
        } else if (serverTerrain == messagesbase.messagesfromclient.ETerrain.Water) {
            return client.model.mapper.Terrain.WATER;
        } else {
            return client.model.mapper.Terrain.GRASS;
        }
    }

    /**
     * Converts the client direction to the Server move.
     * @param d The client direction.
     * @return The Server move.
     */
    public messagesbase.messagesfromclient.EMove convertClientDirection(Direction d){
        // logger.trace("Converting client direction: {}", d);
        if (d == Direction.UP) {
            return messagesbase.messagesfromclient.EMove.Up;
        } else if (d == Direction.DOWN) {
            return messagesbase.messagesfromclient.EMove.Down;
        } else if (d == Direction.LEFT) {
            return messagesbase.messagesfromclient.EMove.Left;
        } else if (d == Direction.RIGHT) {
            return messagesbase.messagesfromclient.EMove.Right;
        } else {
            // logger.warn("Unable to convert client direction: {}", d);
            return null;
        }
    }

    // helpers
    
    /**
     * Checks if a fort is present on the given Server node.
     * @param node The Server node.
     * @return True if a fort is present, false otherwise.
     */
    public boolean isFortOnServerNode(messagesbase.messagesfromserver.FullMapNode node) {
        return node.getFortState() == messagesbase.messagesfromserver.EFortState.MyFortPresent || node.getFortState() == messagesbase.messagesfromserver.EFortState.EnemyFortPresent;
    }

    public boolean isTreasureOnServerNode(messagesbase.messagesfromserver.FullMapNode node) {
        return node.getTreasureState() == messagesbase.messagesfromserver.ETreasureState.MyTreasureIsPresent;
    }


}
