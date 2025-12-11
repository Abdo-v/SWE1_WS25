package client.model.ai;

import client.model.GameState;
import client.model.mapper.MapNode;

/**
 * Holds state information for the AI decision-making process.
 * This class encapsulates flags and positions that track the progress of treasure and fort discovery.
 */
public class StateHolder implements client.observer.util.Observer {

    private GameState gameState;
    private boolean treasureAlreadyFound;
    private boolean treasureAlreadyCollected;
    private boolean fortAlreadyFound;
    private MapNode enemyFirstTruePosition;

    /**
     * Constructs a StateHolder with a given GameState.
     * @param gameState The current game state.
     */
    public StateHolder(GameState gameState) {
        this.gameState = gameState;
        this.treasureAlreadyFound = false;
        this.treasureAlreadyCollected = false;
        this.fortAlreadyFound = false;
        this.enemyFirstTruePosition = null;
    }

    /**
     * Default constructor for StateHolder.
     */
    public StateHolder() {
        this.gameState = null;
        this.treasureAlreadyFound = false;
        this.treasureAlreadyCollected = false;
        this.fortAlreadyFound = false;
        this.enemyFirstTruePosition = null;
    }

    @Override
    public void update(GameState gameState) {
        this.gameState = gameState;
        
        // Update treasure collected status from game state
        if (gameState != null && gameState.isTreasureCollected()) {
            this.treasureAlreadyCollected = true;
        }
    }

    // Getters and setters

    public boolean isTreasureAlreadyFound() {
        return treasureAlreadyFound;
    }

    public void setTreasureAlreadyFound(boolean treasureAlreadyFound) {
        this.treasureAlreadyFound = treasureAlreadyFound;
    }

    public boolean isTreasureAlreadyCollected() {
        return treasureAlreadyCollected;
    }

    public void setTreasureAlreadyCollected(boolean treasureAlreadyCollected) {
        this.treasureAlreadyCollected = treasureAlreadyCollected;
    }

    public boolean isFortAlreadyFound() {
        return fortAlreadyFound;
    }

    public void setFortAlreadyFound(boolean fortAlreadyFound) {
        this.fortAlreadyFound = fortAlreadyFound;
    }

    public MapNode getEnemyFirstTruePosition() {
        return enemyFirstTruePosition;
    }

    public void setEnemyFirstTruePosition(MapNode enemyFirstTruePosition) {
        this.enemyFirstTruePosition = enemyFirstTruePosition;
    }

    public GameState getGameState() {
        return gameState;
    }
}
