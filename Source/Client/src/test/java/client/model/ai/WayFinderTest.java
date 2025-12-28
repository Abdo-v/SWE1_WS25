package client.model.ai;

import client.exception.AIDecisionException;
import client.model.GameState;
import client.model.PlayerState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests {@link WayFinder} precondition checks and error signaling when state is missing or incomplete.
 */
class WayFinderTest {

    @Test
    void findNext_whenGameStateNotInitialized_throwsAIDecisionException() {
        WayFinder wayFinder = new WayFinder();

        AIDecisionException ex = assertThrows(AIDecisionException.class, wayFinder::findNext);
        assertTrue(ex.getMessage().contains("game state not initialized"));
    }

    @Test
    void findNext_whenCurrentPlayerMissing_throwsAIDecisionException() {
        WayFinder wayFinder = new WayFinder();
        GameState state = new GameState("id");

        wayFinder.update(state);

        AIDecisionException ex = assertThrows(AIDecisionException.class, wayFinder::findNext);
        assertTrue(ex.getMessage().contains("current player state is missing"));
    }

    @Test
    void findNext_whenCurrentPositionUnknown_throwsAIDecisionException() {
        WayFinder wayFinder = new WayFinder();
        GameState state = new GameState("id");
        state.addPlayer(new PlayerState("p1", "A", "B", "u"));

        wayFinder.update(state);

        AIDecisionException ex = assertThrows(AIDecisionException.class, wayFinder::findNext);
        assertTrue(ex.getMessage().contains("current position is unknown"));
    }

    @Test
    void addSubObservers_whenGameStateNotSet_throwsIllegalStateException() {
        WayFinder wayFinder = new WayFinder();

        assertThrows(IllegalStateException.class, wayFinder::addSubObservers);
    }
}
