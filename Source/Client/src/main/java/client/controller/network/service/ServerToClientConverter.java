package client.controller.network.service;

import client.model.mapper.GameMap;
import client.model.mapper.MapNode;
import client.model.mapper.OwnToOppMapOrientation;
import messagesbase.UniquePlayerIdentifier;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Converts network (messagesbase) types to client/internal model types.
 */
public class ServerToClientConverter {

    public client.model.GameState convertServerGamestate(messagesbase.messagesfromserver.GameState serverGameState, UniquePlayerIdentifier playerId) {
        ArrayList<client.model.PlayerState> clientPlayers = new ArrayList<>();
        messagesbase.messagesfromserver.FullMap serverMap = serverGameState.getMap();

        if (serverGameState.getPlayers() != null && !serverGameState.getPlayers().isEmpty()) {
            String myPlayerUniqueId = null;
            if (playerId != null) {
                myPlayerUniqueId = playerId.getUniquePlayerID();
            }
            if (myPlayerUniqueId != null) {
                for (messagesbase.messagesfromserver.PlayerState serverPlayer : serverGameState.getPlayers()) {
                    if (serverPlayer.getUniquePlayerID().equals(myPlayerUniqueId)) {
                        MapNode playerMapNode = new MapNode();
                        for (messagesbase.messagesfromserver.FullMapNode node : serverMap.getMapNodes()) {
                            if (node.getPlayerPositionState() == messagesbase.messagesfromserver.EPlayerPositionState.MyPlayerPosition
                                    || node.getPlayerPositionState() == messagesbase.messagesfromserver.EPlayerPositionState.BothPlayerPosition) {
                                playerMapNode.setX(node.getX());
                                playerMapNode.setY(node.getY());
                                playerMapNode.setTerrain(convertServerTerrain(node.getTerrain()));
                                playerMapNode.setFortPresent(isFortOnServerNode(node));
                            }
                        }
                        clientPlayers.add(convertServerPlayerState(serverPlayer, playerMapNode));
                        break;
                    }
                }
            }
            for (messagesbase.messagesfromserver.PlayerState serverPlayer : serverGameState.getPlayers()) {
                if (myPlayerUniqueId == null || !serverPlayer.getUniquePlayerID().equals(myPlayerUniqueId)) {
                    MapNode playerMapNode = new MapNode();
                    for (messagesbase.messagesfromserver.FullMapNode node : serverMap.getMapNodes()) {
                        if (node.getPlayerPositionState() == messagesbase.messagesfromserver.EPlayerPositionState.EnemyPlayerPosition
                                || node.getPlayerPositionState() == messagesbase.messagesfromserver.EPlayerPositionState.BothPlayerPosition) {
                            playerMapNode.setX(node.getX());
                            playerMapNode.setY(node.getY());
                            playerMapNode.setTerrain(convertServerTerrain(node.getTerrain()));
                            playerMapNode.setFortPresent(isFortOnServerNode(node));
                        }
                    }
                    clientPlayers.add(convertServerPlayerState(serverPlayer, playerMapNode));
                }
            }
        }

        client.model.GameState gameState = new client.model.GameState(
                serverGameState.getGameStateId(),
                clientPlayers,
                convertServerMap(serverGameState.getMap())
        );

        gameState.setTreasureCollected(clientPlayers.get(0).hasCollectedTreasure());
        gameState.setOpponentFortFound(serverMapHasEnemyFort(serverGameState.getMap()));

        if (getTreasurePositionFromServerMap(serverGameState.getMap()) != null) {
            gameState.setTreasurePosition(getTreasurePositionFromServerMap(serverGameState.getMap()));
        } else {
            gameState.setTreasurePosition(null);
        }

        if (getEnemyFortMapNodeFromServerMap(serverGameState.getMap()) != null) {
            gameState.setOpponentFortPosition(getEnemyFortMapNodeFromServerMap(serverGameState.getMap()));
        } else {
            gameState.setOpponentFortPosition(null);
        }

        return gameState;
    }

    public client.model.PlayerState convertServerPlayerState(messagesbase.messagesfromserver.PlayerState serverPlayerState, MapNode playerMapNode) {
        client.model.PlayerState playerState = new client.model.PlayerState(
                serverPlayerState.getUniquePlayerID(),
                serverPlayerState.getFirstName(),
                serverPlayerState.getLastName(),
                serverPlayerState.getUAccount(),
                serverPlayerState.hasCollectedTreasure(),
                playerMapNode,
                convertServerStatus(serverPlayerState.getState())
        );
        return playerState;
    }

    public client.model.PlayerStatus convertServerStatus(messagesbase.messagesfromserver.EPlayerGameState serverStatus) {
        if (serverStatus == null) {
            throw new IllegalArgumentException("Server status cannot be null");
        }
        switch (serverStatus) {
            case MustAct:
                return client.model.PlayerStatus.MUST_ACT;
            case MustWait:
                return client.model.PlayerStatus.MUST_WAIT;
            case Lost:
                return client.model.PlayerStatus.LOST;
            case Won:
                return client.model.PlayerStatus.WON;
            default:
                throw new IllegalArgumentException("Unknown server status: " + serverStatus);
        }
    }

    public GameMap convertServerMap(messagesbase.messagesfromserver.FullMap serverMap) {
        if (serverMap.isEmpty()) {
            return new GameMap();
        }

        Collection<messagesbase.messagesfromserver.FullMapNode> serverMapNodes = serverMap.getMapNodes();
        OwnToOppMapOrientation orientation = null;
        boolean vertical = false;
        boolean horizontal = false;
        for (messagesbase.messagesfromserver.FullMapNode node : serverMapNodes) {
            if (node.getY() > 4) {
                vertical = true;
                break;
            }
            if (node.getX() > 9) {
                horizontal = true;
                break;
            }
        }

        ArrayList<MapNode> internalNodes = new ArrayList<>();
        for (messagesbase.messagesfromserver.FullMapNode node : serverMapNodes) {
            internalNodes.add(convertServerMapNode(node));
        }

        if (vertical) {
            for (messagesbase.messagesfromserver.FullMapNode node : serverMapNodes) {
                if (node.getFortState() == messagesbase.messagesfromserver.EFortState.MyFortPresent) {
                    if (node.getY() <= 4) {
                        orientation = OwnToOppMapOrientation.UP_DOWN;
                    } else {
                        orientation = OwnToOppMapOrientation.DOWN_UP;
                    }
                    break;
                }
            }
        } else if (horizontal) {
            for (messagesbase.messagesfromserver.FullMapNode node : serverMapNodes) {
                if (node.getFortState() == messagesbase.messagesfromserver.EFortState.MyFortPresent) {
                    if (node.getX() <= 9) {
                        orientation = OwnToOppMapOrientation.LEFT_RIGHT;
                    } else {
                        orientation = OwnToOppMapOrientation.RIGHT_LEFT;
                    }
                    break;
                }
            }
        }

        if (orientation == null) {
            return new GameMap();
        }

        int maxX;
        int maxY;
        if (orientation == OwnToOppMapOrientation.UP_DOWN || orientation == OwnToOppMapOrientation.DOWN_UP) {
            maxX = 9;
            maxY = 9;
        } else {
            maxX = 19;
            maxY = 4;
        }

        return new GameMap(internalNodes, orientation, maxX, maxY);
    }

    public MapNode convertServerMapNode(messagesbase.messagesfromserver.FullMapNode serverMapNode) {
        return new MapNode(
                serverMapNode.getX(),
                serverMapNode.getY(),
                convertServerTerrain(serverMapNode.getTerrain()),
                isFortOnServerNode(serverMapNode),
                isTreasureOnServerNode(serverMapNode)
        );
    }

    public boolean serverMapHasEnemyFort(messagesbase.messagesfromserver.FullMap serverMap) {
        for (messagesbase.messagesfromserver.FullMapNode node : serverMap.getMapNodes()) {
            if (node.getFortState() == messagesbase.messagesfromserver.EFortState.EnemyFortPresent) {
                return true;
            }
        }
        return false;
    }

    public client.model.mapper.Terrain convertServerTerrain(messagesbase.messagesfromclient.ETerrain serverTerrain) {
        if (serverTerrain == messagesbase.messagesfromclient.ETerrain.Mountain) {
            return client.model.mapper.Terrain.MOUNTAIN;
        } else if (serverTerrain == messagesbase.messagesfromclient.ETerrain.Water) {
            return client.model.mapper.Terrain.WATER;
        } else {
            return client.model.mapper.Terrain.GRASS;
        }
    }

    public boolean isFortOnServerNode(messagesbase.messagesfromserver.FullMapNode node) {
        return node.getFortState() == messagesbase.messagesfromserver.EFortState.MyFortPresent
                || node.getFortState() == messagesbase.messagesfromserver.EFortState.EnemyFortPresent;
    }

    public boolean isTreasureOnServerNode(messagesbase.messagesfromserver.FullMapNode node) {
        return node.getTreasureState() == messagesbase.messagesfromserver.ETreasureState.MyTreasureIsPresent;
    }

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
}
