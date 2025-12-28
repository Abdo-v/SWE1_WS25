package client.controller.network.service;

import client.model.mapper.GameMap;
import client.model.mapper.MapNode;
import client.model.mapper.OwnToOppMapOrientation;
import messagesbase.messagesfromserver.EFortState;
import messagesbase.messagesfromserver.FullMap;
import messagesbase.messagesfromserver.FullMapNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

/**
 * Converts a server full map to the client/internal game map.
 */
class ServerToClientMapConverter {

    private final ServerToClientMapNodeConverter mapNodeConverter;

    public ServerToClientMapConverter(ServerToClientMapNodeConverter mapNodeConverter) {
        this.mapNodeConverter = mapNodeConverter;
    }

    /**
     * Converts the server map and infers own-vs-opponent orientation from the fort position.
     *
     * <p>Returns an empty {@link GameMap} until orientation can be determined.
     */
    public GameMap convert(FullMap serverMap) {
        if (serverMap.isEmpty()) {
            return new GameMap();
        }

        Collection<FullMapNode> serverMapNodes = serverMap.getMapNodes();
        Optional<OwnToOppMapOrientation> orientation = Optional.empty();
        boolean vertical = false;
        boolean horizontal = false;
        for (FullMapNode node : serverMapNodes) {
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
        for (FullMapNode node : serverMapNodes) {
            internalNodes.add(mapNodeConverter.convert(node));
        }

        if (vertical) {
            for (FullMapNode node : serverMapNodes) {
                if (node.getFortState() == EFortState.MyFortPresent) {
                    if (node.getY() <= 4) {
                        orientation = Optional.of(OwnToOppMapOrientation.UP_DOWN);
                    } else {
                        orientation = Optional.of(OwnToOppMapOrientation.DOWN_UP);
                    }
                    break;
                }
            }
        } else if (horizontal) {
            for (FullMapNode node : serverMapNodes) {
                if (node.getFortState() == EFortState.MyFortPresent) {
                    if (node.getX() <= 9) {
                        orientation = Optional.of(OwnToOppMapOrientation.LEFT_RIGHT);
                    } else {
                        orientation = Optional.of(OwnToOppMapOrientation.RIGHT_LEFT);
                    }
                    break;
                }
            }
        }

        if (orientation.isEmpty()) {
            return new GameMap();
        }

        OwnToOppMapOrientation resolvedOrientation = orientation.get();

        int maxX;
        int maxY;
        if (resolvedOrientation == OwnToOppMapOrientation.UP_DOWN || resolvedOrientation == OwnToOppMapOrientation.DOWN_UP) {
            maxX = 9;
            maxY = 9;
        } else {
            maxX = 19;
            maxY = 4;
        }

        return new GameMap(internalNodes, resolvedOrientation, maxX, maxY);
    }
}
