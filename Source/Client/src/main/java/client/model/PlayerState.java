package client.model;

import client.model.mapper.MapNode;

import java.util.Optional;

/**
 * Snapshot of a player's state as known by the client.
 *
 * <p>This is treated as a value object in the client: it carries identity and status plus
 * optional position and treasure flag for visualization and AI decisions.
 */
public class PlayerState {

    private static final PlayerStatus DEFAULT_STATUS = PlayerStatus.MUST_WAIT;

    private final String playerID;
    private final String firstName;
    private final String lastName;
    private final String uAccount;
    private final PlayerStatus status;
    private boolean collectedTreasure = false;
    private Optional<MapNode> currentPosition = Optional.empty();

    public PlayerState(String ID, String firstName, String lastName, String acc) {
        this(ID, firstName, lastName, acc, false, Optional.empty(), DEFAULT_STATUS);
    }

    public PlayerState(String uniquePlayerID, String firstName2, String lastName2, String uAccount2, boolean hasCollectedTreasure, MapNode playerMapNode, PlayerStatus status) {
        this(uniquePlayerID, firstName2, lastName2, uAccount2, hasCollectedTreasure, ModelTextConfig.optionalIfPresent(playerMapNode), status);
    }

    private PlayerState(String uniquePlayerID, String firstName2, String lastName2, String uAccount2, boolean hasCollectedTreasure, Optional<MapNode> playerMapNode, PlayerStatus status) {
        this.playerID = ModelTextConfig.safeStringOrDefault(uniquePlayerID, "");
        this.firstName = ModelTextConfig.safeStringOrDefault(firstName2, "");
        this.lastName = ModelTextConfig.safeStringOrDefault(lastName2, "");
        this.uAccount = ModelTextConfig.safeStringOrDefault(uAccount2, "");
        this.collectedTreasure = hasCollectedTreasure;
        this.currentPosition = ModelTextConfig.defaultIfMissing(playerMapNode, Optional.empty());
        this.status = ModelTextConfig.defaultIfMissing(status, DEFAULT_STATUS);
    }

    public Optional<MapNode> getCurrentPosition() {
        return currentPosition;
    }

    public String getPlayerID() {
        return playerID;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getUAccount() {
        return uAccount;
    }

    public PlayerStatus getStatus() {
        return status;
    }

    public boolean hasCollectedTreasure() {
        return collectedTreasure;
    }

    @Override
    public String toString() {
        return "PlayerState{" +
                "playerID='" + playerID + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", uAccount='" + uAccount + '\'' +
                ", status=" + status +
                ", collectedTreasure=" + collectedTreasure +
                '}';
    }
}
