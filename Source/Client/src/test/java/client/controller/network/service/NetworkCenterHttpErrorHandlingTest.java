package client.controller.network.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.nio.charset.StandardCharsets;

import client.exception.GameCommunicationException;
import client.exception.MapProcessingException;
import client.model.Direction;
import messagesbase.ResponseEnvelope;
import messagesbase.UniquePlayerIdentifier;
import messagesbase.messagesfromserver.GameState;

/**
 * Unit tests for {@link NetworkCenter} error handling.
 *
 * <p>
 * These tests avoid real HTTP requests by injecting a mocked {@link NetworkCenterHttpClient}.
 */
class NetworkCenterHttpErrorHandlingTest {

    /**
     * Ensures that server-side business rule violations during registration are mapped to a
     * {@link GameCommunicationException} that contains a readable rejection message.
     */
    @Test
    void registerPlayer_whenServerReturnsError_throwsGameCommunicationException() {
        NetworkCenterHttpClient httpClient = Mockito.mock(NetworkCenterHttpClient.class);
        ClientToServerConverter c2s = Mockito.mock(ClientToServerConverter.class);
        ServerToClientConverter s2c = Mockito.mock(ServerToClientConverter.class);

        when(httpClient.post(eq("/game-1/players"), any(), any()))
                .thenReturn(new ResponseEnvelope<>("BusinessRuleViolation", "Invalid registration"));

        NetworkCenter networkCenter = new NetworkCenter(
                "http://example.invalid",
                "game-1",
                NetworkCenterConfig.defaultConfig(),
                httpClient,
                c2s,
                s2c
        );

        GameCommunicationException ex = assertThrows(
                GameCommunicationException.class,
                () -> networkCenter.registerPlayer("A", "B", "u1")
        );

        assertTrue(ex.getMessage().contains("Server rejected PLAYER_REGISTRATION"));
        assertTrue(ex.getMessage().contains("BusinessRuleViolation"));
        assertTrue(ex.getMessage().contains("Invalid registration"));
    }

    /**
     * Ensures that an underlying HTTP error (e.g., 500) is wrapped into a {@link GameCommunicationException} and the
     * status code is preserved.
     */
    @Test
    void registerPlayer_whenHttpClientThrowsWebClientResponseException_wrapsStatusCode() {
        NetworkCenterHttpClient httpClient = Mockito.mock(NetworkCenterHttpClient.class);
        ClientToServerConverter c2s = Mockito.mock(ClientToServerConverter.class);
        ServerToClientConverter s2c = Mockito.mock(ServerToClientConverter.class);

        WebClientResponseException httpEx = WebClientResponseException.create(
                500,
                "Internal Server Error",
                HttpHeaders.EMPTY,
                new byte[0],
                        StandardCharsets.UTF_8
        );

        when(httpClient.post(eq("/game-1/players"), any(), any()))
                .thenThrow(httpEx);

        NetworkCenter networkCenter = new NetworkCenter(
                "http://example.invalid",
                "game-1",
                NetworkCenterConfig.defaultConfig(),
                httpClient,
                c2s,
                s2c
        );

        GameCommunicationException ex = assertThrows(
                GameCommunicationException.class,
                () -> networkCenter.registerPlayer("A", "B", "u1")
        );

        assertEquals(500, ex.getHttpStatusCode());
        assertTrue(ex.getMessage().contains("HTTP error during player registration"));
    }

    /**
     * Ensures that a server rejection during a move command is surfaced as a {@link GameCommunicationException}.
     */
    @Test
    void sendMove_whenServerReturnsError_throwsGameCommunicationException() throws GameCommunicationException {
        NetworkCenterHttpClient httpClient = Mockito.mock(NetworkCenterHttpClient.class);
                ClientToServerConverter c2s = new ClientToServerConverter();
        ServerToClientConverter s2c = Mockito.mock(ServerToClientConverter.class);

        when(httpClient.post(eq("/game-1/players"), any(), any()))
                .thenReturn(new ResponseEnvelope<>(UniquePlayerIdentifier.of("p1")));

        when(httpClient.post(eq("/game-1/moves"), any(), any()))
                .thenReturn(new ResponseEnvelope<>("MoveRejected", "Not your turn"));

        NetworkCenter networkCenter = new NetworkCenter(
                "http://example.invalid",
                "game-1",
                NetworkCenterConfig.defaultConfig(),
                httpClient,
                c2s,
                s2c
        );

        networkCenter.registerPlayer("A", "B", "u1");

        GameCommunicationException ex = assertThrows(
                GameCommunicationException.class,
                () -> networkCenter.sendMove(Direction.UP)
        );

        assertTrue(ex.getMessage().contains("Server rejected SEND_MOVE"));
        assertTrue(ex.getMessage().contains("MoveRejected"));
        assertTrue(ex.getMessage().contains("Not your turn"));
    }

    /**
     * Ensures that a missing GameState payload in an otherwise successful response leads to a
     * {@link MapProcessingException}.
     */
    @Test
    void pollGameState_whenResponseHasNoGameState_throwsMapProcessingException() throws GameCommunicationException {
        NetworkCenterHttpClient httpClient = Mockito.mock(NetworkCenterHttpClient.class);
        ClientToServerConverter c2s = Mockito.mock(ClientToServerConverter.class);
        ServerToClientConverter s2c = Mockito.mock(ServerToClientConverter.class);

        when(httpClient.post(eq("/game-1/players"), any(), any()))
                .thenReturn(new ResponseEnvelope<>(UniquePlayerIdentifier.of("p1")));

        when(httpClient.get(eq("/game-1/states/p1"), any()))
                .thenReturn(new ResponseEnvelope<>());

        NetworkCenter networkCenter = new NetworkCenter(
                "http://example.invalid",
                "game-1",
                new NetworkCenterConfig(0),
                httpClient,
                c2s,
                s2c
        );

        networkCenter.registerPlayer("A", "B", "u1");

        assertThrows(MapProcessingException.class, networkCenter::pollGameState);
    }

    /**
     * Ensures that server-side errors during polling are mapped to {@link GameCommunicationException}.
     */
    @Test
    void pollGameState_whenServerReturnsError_throwsGameCommunicationException() throws GameCommunicationException {
        NetworkCenterHttpClient httpClient = Mockito.mock(NetworkCenterHttpClient.class);
        ClientToServerConverter c2s = Mockito.mock(ClientToServerConverter.class);
        ServerToClientConverter s2c = Mockito.mock(ServerToClientConverter.class);

        when(httpClient.post(eq("/game-1/players"), any(), any()))
                .thenReturn(new ResponseEnvelope<>(UniquePlayerIdentifier.of("p1")));

        when(httpClient.get(eq("/game-1/states/p1"), any()))
                .thenReturn(new ResponseEnvelope<>("StateRejected", "Invalid player state request"));

        NetworkCenter networkCenter = new NetworkCenter(
                "http://example.invalid",
                "game-1",
                new NetworkCenterConfig(0),
                httpClient,
                c2s,
                s2c
        );

        networkCenter.registerPlayer("A", "B", "u1");

        GameCommunicationException ex = assertThrows(
                GameCommunicationException.class,
                networkCenter::pollGameState
        );

        assertTrue(ex.getMessage().contains("Server rejected POLL_GAME_STATE"));
        assertTrue(ex.getMessage().contains("StateRejected"));
    }

    /**
     * Ensures that an invalid direction conversion is wrapped into a {@link GameCommunicationException}.
     */
    @Test
    void sendMove_whenConverterRejectsDirection_wrapsIntoGameCommunicationException() throws GameCommunicationException {
        NetworkCenterHttpClient httpClient = Mockito.mock(NetworkCenterHttpClient.class);
        ClientToServerConverter c2s = Mockito.mock(ClientToServerConverter.class);
        ServerToClientConverter s2c = Mockito.mock(ServerToClientConverter.class);

        when(httpClient.post(eq("/game-1/players"), any(), any()))
                .thenReturn(new ResponseEnvelope<>(UniquePlayerIdentifier.of("p1")));

        when(c2s.convertClientDirection(Direction.UP))
                .thenThrow(new IllegalArgumentException("cannot convert"));

        NetworkCenter networkCenter = new NetworkCenter(
                "http://example.invalid",
                "game-1",
                NetworkCenterConfig.defaultConfig(),
                httpClient,
                c2s,
                s2c
        );

        networkCenter.registerPlayer("A", "B", "u1");

        GameCommunicationException ex = assertThrows(
                GameCommunicationException.class,
                () -> networkCenter.sendMove(Direction.UP)
        );

        assertTrue(ex.getMessage().contains("Invalid direction cannot be converted"));
    }

    /**
     * Ensures that converting a server game state without registration fails fast.
     */
    @Test
    void convertServerGamestate_withoutRegistration_throwsIllegalStateException() {
        NetworkCenterHttpClient httpClient = Mockito.mock(NetworkCenterHttpClient.class);
        ClientToServerConverter c2s = Mockito.mock(ClientToServerConverter.class);
        ServerToClientConverter s2c = Mockito.mock(ServerToClientConverter.class);

        NetworkCenter networkCenter = new NetworkCenter(
                "http://example.invalid",
                "game-1",
                NetworkCenterConfig.defaultConfig(),
                httpClient,
                c2s,
                s2c
        );

        assertThrows(IllegalStateException.class, () -> networkCenter.convertServerGamestate(new GameState()));
    }
}
