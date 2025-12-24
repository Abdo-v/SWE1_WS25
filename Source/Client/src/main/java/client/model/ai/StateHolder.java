package client.model.ai;

import client.model.GameState;
import client.model.mapper.MapNode;

import java.util.Objects;
import java.util.Optional;

/**
 * Holds state information for the AI decision-making process.
 * This class encapsulates flags and positions that track the progress of treasure and fort discovery.
 */
public class StateHolder implements client.observer.util.Observer {

    private Optional<GameState> gameState;
    private boolean treasureAlreadyFound;
    private boolean treasureAlreadyCollected;
    private boolean fortAlreadyFound;
    private Optional<MapNode> enemyFirstTruePosition;

    /**
     * Constructs a StateHolder with a given GameState.
     * @param gameState The current game state.
     */
    public StateHolder(GameState gameState) {
        this.gameState = Optional.of(Objects.requireNonNull(gameState, "gameState must not be null"));
        this.treasureAlreadyFound = false;
        this.treasureAlreadyCollected = false;
        this.fortAlreadyFound = false;
        this.enemyFirstTruePosition = Optional.empty();
    }

    /**
     * Default constructor for StateHolder.
     */
    public StateHolder() {
        this.gameState = Optional.empty();
        this.treasureAlreadyFound = false;
        this.treasureAlreadyCollected = false;
        this.fortAlreadyFound = false;
        this.enemyFirstTruePosition = Optional.empty();
    }

    @Override
    public void update(GameState gameState) {
        this.gameState = Optional.of(Objects.requireNonNull(gameState, "gameState must not be null"));

        Optional.of(gameState)
                .filter(GameState::isTreasureCollected)
                .ifPresent(gs -> this.treasureAlreadyCollected = true);
    }

    // Getters and setters

    public boolean isTreasureAlreadyFound() {
        return treasureAlreadyFound;
    }

    public void setTreasureAlreadyFound(boolean treasureAlreadyFound) {
        this.treasureAlreadyFound = treasureAlreadyFound;
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

    public Optional<MapNode> getEnemyFirstTruePosition() {
        return enemyFirstTruePosition;
    }

    public void setEnemyFirstTruePosition(Optional<MapNode> enemyFirstTruePosition) {
        this.enemyFirstTruePosition = Objects.requireNonNull(enemyFirstTruePosition, "enemyFirstTruePosition must not be null");
    }

}
