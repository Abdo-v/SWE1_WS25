package client.model;

import client.model.mapper.MapNode;

import java.util.Objects;
import java.util.Optional;

/**
 * Class representing the state of a player in the game.
 */
public class PlayerState {

    private static final PlayerStatus DEFAULT_STATUS = PlayerStatus.MUST_WAIT;

    private String playerID;
    private String firstName;
    private String lastName;
    private String uAccount;
    private PlayerStatus status;
    private boolean collectedTreasure = false;
    private Optional<MapNode> currentPosition = Optional.empty();

    /**
     * Constructs a new PlayerState with the given parameters.
     * @param ID The player's unique ID.
     * @param firstName The player's first name.
     * @param lastName The player's last name.
     * @param acc The player's university account.
     */
    public PlayerState(String ID, String firstName, String lastName, String acc) {
        this(ID, firstName, lastName, acc, false, Optional.empty(), DEFAULT_STATUS);
    }

    public PlayerState(String uniquePlayerID, String firstName2, String lastName2, String uAccount2, boolean hasCollectedTreasure, MapNode playerMapNode, PlayerStatus status) {
        this(uniquePlayerID, firstName2, lastName2, uAccount2, hasCollectedTreasure, Optional.ofNullable(playerMapNode), status);
    }

    private PlayerState(String uniquePlayerID, String firstName2, String lastName2, String uAccount2, boolean hasCollectedTreasure, Optional<MapNode> playerMapNode, PlayerStatus status) {
        this.playerID = Objects.requireNonNullElse(uniquePlayerID, "");
        this.firstName = Objects.requireNonNullElse(firstName2, "");
        this.lastName = Objects.requireNonNullElse(lastName2, "");
        this.uAccount = Objects.requireNonNullElse(uAccount2, "");
        this.collectedTreasure = hasCollectedTreasure;
        this.currentPosition = Objects.requireNonNullElse(playerMapNode, Optional.empty());
        this.status = Objects.requireNonNullElse(status, DEFAULT_STATUS);
    }

    /**
     * Gets the current position of the player.
     * @return The current position of the player.
     */
    public Optional<MapNode> getCurrentPosition() {
        return currentPosition;
    }

    /**
     * Gets the player ID.
     * @return The player ID.
     */
    public String getPlayerID() {
        return playerID;
    }

    /**
     * Gets the first name of the player.
     * @return The first name of the player.
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Gets the last name of the player.
     * @return The last name of the player.
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Gets the university account of the player.
     * @return The university account of the player.
     */
    public String getUAccount() {
        return uAccount;
    }

    /**
     * Gets the status of the player.
     * @return The status of the player.
     */
    public PlayerStatus getStatus() {
        return status;
    }

    /**
     * Checks if the player has collected the treasure.
     * @return true if the player has collected the treasure, false otherwise.
     */
    public boolean hasCollectedTreasure() {
        return collectedTreasure;
    }

    /**
     * Converts the player state to a string representation.
     * @return The string representation of the player state.
     */
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
