package client.model;
import client.model.mapper.MapNode;

/**
 * Class representing the state of a player in the game.
 */
public class PlayerState {

    private String playerID;
    private String firstName;
    private String lastName;
    private String uAccount;
    private PlayerStatus status;
    private boolean collectedTreasure = false;
    private MapNode currentPosition = null; //always assigned in converter

    /**
     * Constructs a new PlayerState with the given parameters.
     * @param ID The player's unique ID.
     * @param firstName The player's first name.
     * @param lastName The player's last name.
     * @param acc The player's university account.
     */
    public PlayerState(String ID, String firstName, String lastName, String acc) {
        this.playerID = ID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.uAccount = acc;
        this.status = PlayerStatus.MUST_WAIT;
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
        this.playerID = ID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.uAccount = acc;
        this.collectedTreasure = collectedTreasure;
        this.status = PlayerStatus.MUST_WAIT;
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
        this.playerID = uniquePlayerID;
        this.firstName = firstName2;
        this.lastName = lastName2;
        this.uAccount = uAccount2;
        this.collectedTreasure = hasCollectedTreasure;
        this.status = PlayerStatus.MUST_WAIT;
        this.currentPosition = playerMapNode;
    }

    public PlayerState(String uniquePlayerID, String firstName2, String lastName2, String uAccount2, boolean hasCollectedTreasure, MapNode playerMapNode, PlayerStatus status) {
        this.playerID = uniquePlayerID;
        this.firstName = firstName2;
        this.lastName = lastName2;
        this.uAccount = uAccount2;
        this.collectedTreasure = hasCollectedTreasure;
        this.currentPosition = playerMapNode;
        this.status = status;
    }

    /**
     * Updates this player state with data from another player state.
     * @param playerState The player state to update from.
     */
    public void updatePlayerState(PlayerState playerState) {
        if (playerState != null && this.playerID.equals(playerState.getPlayerID())) {
            this.firstName = playerState.getFirstName();
            this.lastName = playerState.getLastName();
            this.uAccount = playerState.getUAccount();
            this.status = playerState.getStatus();
            this.collectedTreasure = playerState.hasCollectedTreasure();
        }
    }

    /**
     * Gets the current position of the player.
     * @return The current position of the player.
     */
    public MapNode getCurrentPosition() {
        return currentPosition;
    }

    /**
     * Sets the current position of the player.
     * @param currentPosition The current position of the player.
     */
    public void setCurrentPosition(MapNode currentPosition) {
        this.currentPosition = currentPosition;
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
