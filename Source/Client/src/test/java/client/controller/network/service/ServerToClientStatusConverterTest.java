package client.controller.network.service;

import client.model.PlayerStatus;
import messagesbase.messagesfromserver.EPlayerGameState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link ServerToClientStatusConverter}.
 *
 * <p>Verifies mapping from network player states to internal {@link PlayerStatus}.
 */
class ServerToClientStatusConverterTest {

    /**
     * Ensures every {@link EPlayerGameState} maps to the expected internal status.
     *
     * @param serverState The server-side game state for a player.
     */
    @ParameterizedTest
    @EnumSource(EPlayerGameState.class)
    void convert_mapsAllKnownStates(EPlayerGameState serverState) {
        ServerToClientStatusConverter converter = new ServerToClientStatusConverter();

        PlayerStatus actual = converter.convert(serverState);

        PlayerStatus expected = switch (serverState) {
            case MustAct -> PlayerStatus.MUST_ACT;
            case MustWait -> PlayerStatus.MUST_WAIT;
            case Lost -> PlayerStatus.LOST;
            case Won -> PlayerStatus.WON;
        };
        assertEquals(expected, actual);
    }

    /**
     * Ensures null input is rejected with a clear exception.
     */
    @Test
    void convert_whenNull_throws() {
        ServerToClientStatusConverter converter = new ServerToClientStatusConverter();

        assertThrows(NullPointerException.class, () -> converter.convert(null));
    }
}
