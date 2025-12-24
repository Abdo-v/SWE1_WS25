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

    /**
     * Constructs a new PlayerState with the given parameters, including treasure collection status.
     * @param ID The player's unique ID.
     * @param firstName The player's first name.
     * @param lastName The player's last name.
     * @param acc The player's university account.
     * @param collectedTreasure Whether the player has collected the treasure.
     */
    public PlayerState(String ID, String firstName, String lastName, String acc, boolean collectedTreasure) {
        this(ID, firstName, lastName, acc, collectedTreasure, Optional.empty(), DEFAULT_STATUS);
    }

    /**
     * Constructs a new PlayerState with the given parameters, including treasure collection status and map node.
     * @param uniquePlayerID The player's unique ID.
     * @param firstName2 The player's first name.
     * @param lastName2 The player's last name.
     * @param uAccount2 The player's university account.
     * @param hasCollectedTreasure Whether the player has collected the treasure.
     * @param playerMapNode The player's map node.
     */
    public PlayerState(String uniquePlayerID, String firstName2, String lastName2, String uAccount2, boolean hasCollectedTreasure, MapNode playerMapNode) {
        this(uniquePlayerID, firstName2, lastName2, uAccount2, hasCollectedTreasure, Optional.ofNullable(playerMapNode), DEFAULT_STATUS);
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
     * Updates this player state with data from another player state.
     * @param playerState The player state to update from.
     */
    public void updatePlayerState(PlayerState playerState) {
        PlayerState requiredState = Objects.requireNonNull(playerState, "player state is required");
        if (!this.playerID.equals(requiredState.getPlayerID())) {
            return;
        }

        this.firstName = requiredState.getFirstName();
        this.lastName = requiredState.getLastName();
        this.uAccount = requiredState.getUAccount();
        this.status = requiredState.getStatus();
        this.collectedTreasure = requiredState.hasCollectedTreasure();
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
     * Sets the first name of the player.
     * @param firstName The first name of the player.
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Gets the last name of the player.
     * @return The last name of the player.
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets the last name of the player.
     * @param lastName The last name of the player.
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Gets the university account of the player.
     * @return The university account of the player.
     */
    public String getUAccount() {
        return uAccount;
    }

    /**
     * Sets the university account of the player.
     * @param uAccount The university account of the player.
     */
    public void setUAccount(String uAccount) {
        this.uAccount = uAccount;
    }

    /**
     * Gets the status of the player.
     * @return The status of the player.
     */
    public PlayerStatus getStatus() {
        return status;
    }

    /**
     * Sets the status of the player.
     * @param status The status of the player.
     */
    public void setStatus(PlayerStatus status) {
        this.status = status;
    }

    /**
     * Checks if the player has collected the treasure.
     * @return true if the player has collected the treasure, false otherwise.
     */
    public boolean hasCollectedTreasure() {
        return collectedTreasure;
    }

    /**
     * Sets the treasure collected status of the player.
     * @param collectedTreasure Whether the player has collected the treasure.
     */
    public void setCollectedTreasure(boolean collectedTreasure) {
        this.collectedTreasure = collectedTreasure;
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
