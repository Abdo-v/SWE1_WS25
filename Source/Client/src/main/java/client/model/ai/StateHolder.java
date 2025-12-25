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

    // "Path lock" for exploration: once an exploration target is chosen, keep it until reached
    // (or until the objective changes / treasure becomes known).
    private Optional<MapNode> lockedExplorationTarget;
    private Optional<Objective> lockedExplorationObjective;

    /**
     * Default constructor for StateHolder.
     */
    public StateHolder() {
        this.gameState = Optional.empty();
        this.treasureAlreadyFound = false;
        this.treasureAlreadyCollected = false;
        this.fortAlreadyFound = false;
        this.enemyFirstTruePosition = Optional.empty();
        this.lockedExplorationTarget = Optional.empty();
        this.lockedExplorationObjective = Optional.empty();
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

    public Optional<MapNode> getLockedExplorationTarget() {
        return lockedExplorationTarget;
    }

    public Optional<Objective> getLockedExplorationObjective() {
        return lockedExplorationObjective;
    }

    public void lockExplorationTarget(MapNode target, Objective objective) {
        this.lockedExplorationTarget = Optional.of(Objects.requireNonNull(target, "target must not be null"));
        this.lockedExplorationObjective = Optional.of(Objects.requireNonNull(objective, "objective must not be null"));
    }

    public void clearLockedExplorationTarget() {
        this.lockedExplorationTarget = Optional.empty();
        this.lockedExplorationObjective = Optional.empty();
    }

    public void clearLockedExplorationTargetIfObjectiveChanged(Objective objective) {
        Objects.requireNonNull(objective, "objective must not be null");
        if (lockedExplorationObjective.isPresent() && lockedExplorationObjective.orElseThrow() != objective) {
            clearLockedExplorationTarget();
        }
    }

}
