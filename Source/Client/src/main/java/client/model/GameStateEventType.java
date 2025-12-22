package client.model;

/**
 * Describes which part of the {@link GameState} has changed.
 *
 * <p>Used to support topic/event-based observer registration so that observers
 * can subscribe only to the changes they are interested in.</p>
 */
public enum GameStateEventType {

    /**
     * A broad update where multiple fields may have changed.
     * This is the default notification type when calling {@link GameState#notifyObservers()}.
     */
    BULK_UPDATE,

    MAP_CHANGED,
    PLAYERS_CHANGED,

    TREASURE_COLLECTED_CHANGED,
    TREASURE_POSITION_CHANGED,

    OPPONENT_FORT_FOUND_CHANGED,
    OPPONENT_FORT_POSITION_CHANGED
}
