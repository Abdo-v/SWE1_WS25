package client.model;

import client.model.mapper.GameMap;
import client.model.mapper.MapNode;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

/**
 * Mutable backing storage for {@link GameState}.
 *
 * <p>Separated to keep {@link GameState} focused on domain behavior and signaling.
 */
final class GameStateData {

    private final String gameStateID;
    private ArrayList<PlayerState> players;
    private Optional<GameMap> map;

    private boolean treasureCollected;
    private boolean opponentFortFound;
    private Optional<MapNode> treasurePosition;
    private Optional<MapNode> opponentFortPosition;

    GameStateData(String gameStateID, ArrayList<PlayerState> players, Optional<GameMap> map) {
        this.gameStateID = Objects.requireNonNull(gameStateID, "game state ID is required");
        this.players = new ArrayList<>(Objects.requireNonNull(players, "players are required"));
        this.map = Objects.requireNonNull(map, "map is required");

        this.treasureCollected = false;
        this.opponentFortFound = false;
        this.treasurePosition = Optional.empty();
        this.opponentFortPosition = Optional.empty();
    }

    String gameStateID() {
        return gameStateID;
    }

    ArrayList<PlayerState> players() {
        return players;
    }

    void players(ArrayList<PlayerState> players) {
        this.players = new ArrayList<>(Objects.requireNonNull(players, "players are required"));
    }

    Optional<GameMap> map() {
        return map;
    }

    void map(Optional<GameMap> map) {
        this.map = Objects.requireNonNull(map, "map is required");
    }

    boolean treasureCollected() {
        return treasureCollected;
    }

    void treasureCollected(boolean treasureCollected) {
        this.treasureCollected = treasureCollected;
    }

    boolean opponentFortFound() {
        return opponentFortFound;
    }

    void opponentFortFound(boolean opponentFortFound) {
        this.opponentFortFound = opponentFortFound;
    }

    Optional<MapNode> treasurePosition() {
        return treasurePosition;
    }

    void treasurePosition(Optional<MapNode> treasurePosition) {
        this.treasurePosition = Objects.requireNonNull(treasurePosition, "treasure position is required");
    }

    Optional<MapNode> opponentFortPosition() {
        return opponentFortPosition;
    }

    void opponentFortPosition(Optional<MapNode> opponentFortPosition) {
        this.opponentFortPosition = Objects.requireNonNull(opponentFortPosition, "opponent fort position is required");
    }
}
