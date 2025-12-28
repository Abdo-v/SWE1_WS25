package client.model.ai;

import client.model.GameState;
import client.model.mapper.MapNode;

import java.util.Objects;
import java.util.Optional;

/**
 * State container for AI decision-making across turns.
 *
 * <p>Tracks derived flags (treasure/fort discovered), the enemy's first trustworthy position, and an
 * exploration target "lock" to prevent oscillation.
 */
class StateHolder implements client.observer.util.Observer {

    private Optional<GameState> gameState;
    private boolean treasureAlreadyFound;
    private boolean fortAlreadyFound;
    private Optional<MapNode> enemyFirstTruePosition;

    // "Path lock" for exploration: once an exploration target is chosen, keep it until reached
    // (or until the objective changes / treasure becomes known).
    private Optional<MapNode> lockedExplorationTarget;
    private Optional<Objective> lockedExplorationObjective;

    StateHolder() {
        this.gameState = Optional.empty();
        this.treasureAlreadyFound = false;
        this.fortAlreadyFound = false;
        this.enemyFirstTruePosition = Optional.empty();
        this.lockedExplorationTarget = Optional.empty();
        this.lockedExplorationObjective = Optional.empty();
    }

    @Override
    public void update(GameState gameState) {
        this.gameState = Optional.of(Objects.requireNonNull(gameState, MessageConfig.STATE_REQUIRED_MESSAGE));
        this.treasureAlreadyFound = gameState.getTreasurePosition().isEmpty();
        this.fortAlreadyFound = gameState.getOpponentFortPosition().isEmpty();
    }

    boolean isTreasureAlreadyFound() {
        return treasureAlreadyFound;
    }

    void setTreasureAlreadyFound(boolean treasureAlreadyFound) {
        this.treasureAlreadyFound = treasureAlreadyFound;
    }

    boolean isFortAlreadyFound() {
        return fortAlreadyFound;
    }

    void setFortAlreadyFound(boolean fortAlreadyFound) {
        this.fortAlreadyFound = fortAlreadyFound;
    }

    Optional<MapNode> getEnemyFirstTruePosition() {
        return enemyFirstTruePosition;
    }

    void setEnemyFirstTruePosition(Optional<MapNode> enemyFirstTruePosition) {
        this.enemyFirstTruePosition = Objects.requireNonNull(enemyFirstTruePosition, "enemyFirstTruePosition is required");
    }

    Optional<MapNode> getLockedExplorationTarget() {
        return lockedExplorationTarget;
    }

    Optional<Objective> getLockedExplorationObjective() {
        return lockedExplorationObjective;
    }

    void lockExplorationTarget(MapNode target, Objective objective) {
        this.lockedExplorationTarget = Optional.of(Objects.requireNonNull(target, "target is required"));
        this.lockedExplorationObjective = Optional.of(Objects.requireNonNull(objective, "objective is required"));
    }

    void clearLockedExplorationTarget() {
        this.lockedExplorationTarget = Optional.empty();
        this.lockedExplorationObjective = Optional.empty();
    }

    void clearLockedExplorationTargetIfObjectiveChanged(Objective objective) {
        Objects.requireNonNull(objective, "objective is required");
        if (lockedExplorationObjective.isPresent() && lockedExplorationObjective.orElseThrow() != objective) {
            clearLockedExplorationTarget();
        }
    }

}
