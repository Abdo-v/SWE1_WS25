package client.controller.network.service;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import client.controller.ControllerTextConfig;
import client.exception.GameCommunicationException;
import client.exception.MapProcessingException;
import client.model.Direction;
import client.model.mapper.PlayerHalfMap;
import messagesbase.ResponseEnvelope;
import messagesbase.UniquePlayerIdentifier;
import messagesbase.messagesfromclient.ERequestState;
import messagesbase.messagesfromclient.PlayerRegistration;
import messagesbase.messagesfromserver.GameState;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

public class NetworkCenter {
     // private static final Logger logger = LoggerFactory.getLogger(NetworkCenter.class);
    
    private final NetworkCenterHttpClient httpClient;
    private final String gameId;
    private final String serverBaseUrl;
    private final NetworkCenterConfig config;
    private Optional<UniquePlayerIdentifier> playerId = Optional.empty();
    private final ClientToServerConverter clientToServerConverter;
    private final ServerToClientConverter serverToClientConverter;

    /**
     * Constructs a NetworkCenter with the given server base URL and game ID.
     * @param serverBaseUrl The base URL of the server.
     * @param gameId The ID of the game.
     */
    public NetworkCenter(String serverBaseUrl, String gameId) {
        this(serverBaseUrl, gameId, NetworkCenterConfig.defaultConfig());
    }

    private NetworkCenter(String serverBaseUrl, String gameId, NetworkCenterConfig config) {
     // logger.debug("Creating NetworkCenter with server: {}, gameId: {}", serverBaseUrl, gameId);
        this.gameId = gameId;
        this.serverBaseUrl = serverBaseUrl;
        this.config = config;
        WebClient webClient = WebClient.builder()
            .baseUrl(serverBaseUrl + ControllerTextConfig.PATH_GAMES)
            .defaultHeader(ControllerTextConfig.HTTP_HEADER_CONTENT_TYPE, ControllerTextConfig.MEDIA_TYPE_APPLICATION_XML)
            .defaultHeader(ControllerTextConfig.HTTP_HEADER_ACCEPT, ControllerTextConfig.MEDIA_TYPE_APPLICATION_XML)
                .build();
        this.httpClient = new WebClientNetworkCenterHttpClient(webClient);
        this.clientToServerConverter = new ClientToServerConverter();
        this.serverToClientConverter = new ServerToClientConverter();
     // logger.debug("NetworkCenter initialized successfully for game: {}", gameId);
    }

    NetworkCenter(
            String serverBaseUrl,
            String gameId,
            NetworkCenterConfig config,
            NetworkCenterHttpClient httpClient,
            ClientToServerConverter clientToServerConverter,
            ServerToClientConverter serverToClientConverter
    ) {
        this.gameId = Objects.requireNonNull(gameId, "gameId is required");
        this.serverBaseUrl = Objects.requireNonNull(serverBaseUrl, ControllerTextConfig.REQUIRE_SERVER_BASE_URL);
        this.config = Objects.requireNonNull(config, "config is required");
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient is required");
        this.clientToServerConverter = Objects.requireNonNull(clientToServerConverter, "clientToServerConverter is required");
        this.serverToClientConverter = Objects.requireNonNull(serverToClientConverter, "serverToClientConverter is required");
    }

    /**
     * Registers a player with the server.
     * @param firstName Player's first name.
     * @param lastName Player's last name.
     * @param uAccount Player's university account.
     * @return The unique player ID assigned by the server.
     * @throws GameCommunicationException If registration fails due to network or server issues.
     */
    public UniquePlayerIdentifier registerPlayer(String firstName, String lastName, String uAccount) throws GameCommunicationException {
     // logger.info("Registering player: {} {}, uAccount: {} for game: {}", firstName, lastName, uAccount, gameId);
        PlayerRegistration playerReg = new PlayerRegistration(firstName, lastName, uAccount);
        
        try {
         // logger.debug("Sending player registration request to server");
            ResponseEnvelope<UniquePlayerIdentifier> resultReg = httpClient.post(
                "/" + gameId + ControllerTextConfig.PATH_PLAYERS,
                playerReg,
                new ParameterizedTypeReference<ResponseEnvelope<UniquePlayerIdentifier>>() {}
            );
            
            if (resultReg.getState() == ERequestState.Error) {
             // logger.error("Player registration failed for {} {}: {}", firstName, lastName, resultReg.getExceptionMessage());
                throw new GameCommunicationException(
                    formatServerRejection(ControllerTextConfig.OP_PLAYER_REGISTRATION, resultReg),
                    serverBaseUrl,
                    ControllerTextConfig.OP_PLAYER_REGISTRATION,
                    ControllerTextConfig.HTTP_STATUS_SERVER_REJECTED,
                    resultReg.getExceptionName(),
                    resultReg.getExceptionMessage()
                );
            }
            
                this.playerId = Optional.of(resultReg.getData().get());
         // logger.info("Player registration successful: {} {} assigned ID: {}", firstName, lastName, this.playerId.getUniquePlayerID());
                return this.playerId.get();
            
        } catch (WebClientResponseException e) {
         // logger.error("HTTP error during player registration: {}", e.getMessage(), e);
            throw new GameCommunicationException(
                "HTTP error during player registration: " + e.getMessage(),
                e,
                serverBaseUrl,
                ControllerTextConfig.OP_PLAYER_REGISTRATION,
                e.getStatusCode().value()
            );
        } catch (Exception e) {
            if (e instanceof GameCommunicationException) {
                throw e;
            }
         // logger.error("Network error during player registration: {}", e.getMessage(), e);
            throw new GameCommunicationException(
                "Network connection failed during player registration",
                e,
                serverBaseUrl,
                ControllerTextConfig.OP_PLAYER_REGISTRATION,
                -1
            );
        }
    }

    /**
     * Sends the player's half map to the server.
     * @param halfMap The player's half map.
     * @throws GameCommunicationException If sending the half map fails due to network or server issues.
     */
    public void sendHalfMap(PlayerHalfMap halfMap) throws GameCommunicationException {
        if (playerId.isEmpty()) {
         // logger.error("Attempted to send half map without player registration");
            throw new GameCommunicationException(
                "Player must be registered before sending a half map",
                serverBaseUrl,
                ControllerTextConfig.OP_SEND_HALF_MAP,
                -1
            );
        }
        
        try {
         // logger.info("Sending half map for player: {} in game: {}", playerId.getUniquePlayerID(), gameId);
         // logger.debug("Converting client half map to server format");
                messagesbase.messagesfromclient.PlayerHalfMap clientHalfMap =
                    clientToServerConverter.convertClientHalfMap(halfMap, this.playerId.get());
            
         // logger.debug("Transmitting half map to server");
            ResponseEnvelope<messagesbase.messagesfromserver.PlayerState> response = httpClient.post(
                "/" + gameId + ControllerTextConfig.PATH_HALFMAPS,
                clientHalfMap,
                new ParameterizedTypeReference<ResponseEnvelope<messagesbase.messagesfromserver.PlayerState>>() {}
            );
            
            if (response.getState() == ERequestState.Error) {
             // logger.error("Failed to send half map for player {}: {}", playerId.getUniquePlayerID(), response.getExceptionMessage());
                throw new GameCommunicationException(
                    formatServerRejection(ControllerTextConfig.OP_SEND_HALF_MAP, response),
                    serverBaseUrl,
                    ControllerTextConfig.OP_SEND_HALF_MAP,
                    ControllerTextConfig.HTTP_STATUS_SERVER_REJECTED,
                    response.getExceptionName(),
                    response.getExceptionMessage()
                );
            }
            
         // logger.info("Half map successfully sent for player: {}", playerId.getUniquePlayerID());
            
        } catch (WebClientResponseException e) {
         // logger.error("HTTP error during half map submission: {}", e.getMessage(), e);
            throw new GameCommunicationException(
                "HTTP error during half map submission: " + e.getMessage(),
                e,
                serverBaseUrl,
                ControllerTextConfig.OP_SEND_HALF_MAP,
                e.getStatusCode().value()
            );
        } catch (Exception e) {
            if (e instanceof GameCommunicationException) {
                throw e;
            }
         // logger.error("Network error during half map submission: {}", e.getMessage(), e);
            throw new GameCommunicationException(
                "Network connection failed during half map submission",
                e,
                serverBaseUrl,
                ControllerTextConfig.OP_SEND_HALF_MAP,
                -1
            );
        }
    }

    /**
     * Sends a player's move to the server.
     * @param direction The direction the player wants to move.
     * @throws GameCommunicationException If sending the move fails due to network or server issues.
     */
    public void sendMove(Direction direction) throws GameCommunicationException {
        if (playerId.isEmpty()) {
         // logger.error("Attempted to send move without player registration");
            throw new GameCommunicationException(
                "Player must be registered before sending a move",
                serverBaseUrl,
                ControllerTextConfig.OP_SEND_MOVE,
                -1
            );
        }

        Direction safeDirection = Objects.requireNonNull(direction, ControllerTextConfig.REQUIRE_DIRECTION);
        sendMoveInternal(safeDirection);
    }

    private void sendMoveInternal(Direction direction) throws GameCommunicationException {
        try {
            messagesbase.messagesfromclient.EMove networkMove = convertClientDirection(direction);
        
            messagesbase.messagesfromclient.PlayerMove playerMove = messagesbase.messagesfromclient.PlayerMove.of(this.playerId.get(), networkMove);
            
         // logger.trace("Transmitting move {} to server for player {}", networkMove, playerId.getUniquePlayerID());
            ResponseEnvelope<messagesbase.messagesfromserver.PlayerState> response = httpClient.post(
                "/" + gameId + ControllerTextConfig.PATH_MOVES,
                playerMove,
                new ParameterizedTypeReference<ResponseEnvelope<messagesbase.messagesfromserver.PlayerState>>() {}
            );
            
            if (response.getState() == ERequestState.Error) {
             // logger.error("Failed to send move {} for player {}: {}", direction, playerId.getUniquePlayerID(), response.getExceptionMessage());
                throw new GameCommunicationException(
                    formatServerRejection(ControllerTextConfig.OP_SEND_MOVE, response),
                    serverBaseUrl,
                    ControllerTextConfig.OP_SEND_MOVE,
                    ControllerTextConfig.HTTP_STATUS_SERVER_REJECTED,
                    response.getExceptionName(),
                    response.getExceptionMessage()
                );
            }
            
         // logger.debug("Move {} successfully sent for player: {}", direction, playerId.getUniquePlayerID());
            
        } catch (IllegalArgumentException e) {
            throw new GameCommunicationException(
                    "Invalid direction cannot be converted: " + direction,
                    e,
                    serverBaseUrl,
                    ControllerTextConfig.OP_SEND_MOVE,
                    -1
            );
        } catch (WebClientResponseException e) {
         // logger.error("HTTP error during move submission: {}", e.getMessage(), e);
            throw new GameCommunicationException(
                "HTTP error during move submission: " + e.getMessage(),
                e,
                serverBaseUrl,
                ControllerTextConfig.OP_SEND_MOVE,
                e.getStatusCode().value()
            );
        } catch (Exception e) {
            if (e instanceof GameCommunicationException) {
                throw e;
            }
         // logger.error("Network error during move submission: {}", e.getMessage(), e);
            throw new GameCommunicationException(
                "Network connection failed during move submission",
                e,
                serverBaseUrl,
                ControllerTextConfig.OP_SEND_MOVE,
                -1
            );
        }
    }

    /**
     * Retrieves the current game state from the server.
     * @return The current game state.
     * @throws GameCommunicationException If retrieving the game state fails due to network or server issues.
     * @throws MapProcessingException If the received game state cannot be processed.
     */
    public messagesbase.messagesfromserver.GameState pollGameState() throws GameCommunicationException, MapProcessingException {
    // logger.trace("Polling game state for player: {} in game: {}", playerId.map(UniquePlayerIdentifier::getUniquePlayerID).orElse("unknown"), gameId);
        
        if (playerId.isEmpty()) {
         // logger.error("Attempted to poll game state without player registration");
            throw new GameCommunicationException(
                ControllerTextConfig.ERROR_PLAYER_MUST_BE_REGISTERED_POLL_STATE,
                serverBaseUrl,
                ControllerTextConfig.OP_POLL_GAME_STATE,
                ControllerTextConfig.HTTP_STATUS_UNKNOWN_INT
            );
        }
        
        try {
            Thread.sleep(config.pollGameStateDelayMillis());
            
            ResponseEnvelope<GameState> response = httpClient.get(
                "/" + gameId + ControllerTextConfig.PATH_STATES_PREFIX + playerId.get().getUniquePlayerID(),
                new ParameterizedTypeReference<ResponseEnvelope<GameState>>() {}
            );
            
            if (response.getState() == ERequestState.Error) {
             // logger.error("Failed to poll game state for player {}: {}", playerId.getUniquePlayerID(), response.getExceptionMessage());
                throw new GameCommunicationException(
                    formatServerRejection(ControllerTextConfig.OP_POLL_GAME_STATE, response),
                    serverBaseUrl,
                    ControllerTextConfig.OP_POLL_GAME_STATE,
                    ControllerTextConfig.HTTP_STATUS_SERVER_REJECTED,
                    response.getExceptionName(),
                    response.getExceptionMessage()
                );
            }
            
            GameState gameState = response.getData().orElseThrow(() -> new MapProcessingException(
                    ControllerTextConfig.ERROR_RECEIVED_MISSING_GAME_STATE_FROM_SERVER_RESPONSE,
                    ControllerTextConfig.TYPE_GAME_STATE,
                    ControllerTextConfig.CONTEXT_SERVER_RESPONSE_VALIDATION
            ));

            int nodeCount;
            try {
                nodeCount = Objects.requireNonNull(gameState.getMap()).getMapNodes().size();
            } catch (NullPointerException e) {
                nodeCount = 0;
            }
         // logger.trace("Game state polled successfully for player: {}, map nodes: {}", playerId.getUniquePlayerID(), nodeCount);
            
            try {
                Collection<?> nodes = Objects.requireNonNull(Objects.requireNonNull(gameState.getMap()).getMapNodes());
                int expectedNodes = nodeCount == 50 ? 50 : (nodeCount == 100 ? 100 : -1);
                if (!nodes.isEmpty() && expectedNodes > 0 && nodeCount != expectedNodes && nodeCount != 0) {
                    // logger.warn("Unexpected map node count: expected {} or 0, got {}", expectedNodes, nodeCount);
                }
            } catch (NullPointerException ignored) {
                // keep existing behavior: treat missing map/nodes as nodeCount=0 and skip validation
            }
            
            return gameState;
            
        } catch (WebClientResponseException e) {
         // logger.error("HTTP error during game state polling: {}", e.getMessage(), e);
            throw new GameCommunicationException(
                ControllerTextConfig.ERROR_HTTP_ERROR_POLLING_GAME_STATE_PREFIX + e.getMessage(),
                e,
                serverBaseUrl,
                ControllerTextConfig.OP_POLL_GAME_STATE,
                e.getStatusCode().value()
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
         // logger.error("Polling interrupted: {}", e.getMessage(), e);
            throw new GameCommunicationException(
                ControllerTextConfig.ERROR_GAME_STATE_POLLING_INTERRUPTED,
                e,
                serverBaseUrl,
                ControllerTextConfig.OP_POLL_GAME_STATE,
                ControllerTextConfig.HTTP_STATUS_UNKNOWN_INT
            );
        } catch (Exception e) {
            if (e instanceof GameCommunicationException || e instanceof MapProcessingException) {
                throw e;
            }
         // logger.error("Network error during game state polling: {}", e.getMessage(), e);
            throw new GameCommunicationException(
                ControllerTextConfig.ERROR_NETWORK_CONNECTION_FAILED_POLLING_GAME_STATE,
                e,
                serverBaseUrl,
                ControllerTextConfig.OP_POLL_GAME_STATE,
                ControllerTextConfig.HTTP_STATUS_UNKNOWN_INT
            );
        }
    }

    /**
     * Converts the server game state to the internal representation.
     * @param serverGameState The server game state.
     * @return The internal game state.
     */
    public client.model.GameState convertServerGamestate(messagesbase.messagesfromserver.GameState serverGameState) {
    // logger.trace("Converting server game state to client format for player: {}", playerId.map(UniquePlayerIdentifier::getUniquePlayerID).orElse("unknown"));
        return serverToClientConverter.convertServerGamestate(
                serverGameState,
                playerId.orElseThrow(() -> new IllegalStateException(ControllerTextConfig.ERROR_PLAYER_MUST_BE_REGISTERED_CONVERT_STATE))
        );
    }

    /**
     * Converts the client direction to the network move.
     * @param d The client direction.
     * @return The network move.
     */
    private messagesbase.messagesfromclient.EMove convertClientDirection(Direction d){
     // logger.trace("Converting client direction {} to server move", d);
        return clientToServerConverter.convertClientDirection(d);
    }

    private static String formatServerRejection(String operation, ResponseEnvelope<?> envelope) {
        Objects.requireNonNull(envelope, ControllerTextConfig.REQUIRE_ENVELOPE);
        String safeOp = ControllerTextConfig.safeStringOrDefault(operation, ControllerTextConfig.FALLBACK_OPERATION_UNKNOWN);
        String exceptionName = ControllerTextConfig.safeStringOrDefault(envelope.getExceptionName(), ControllerTextConfig.FALLBACK_SERVER_ERROR_NAME).trim();
        String exceptionMessage = ControllerTextConfig.safeStringOrDefault(envelope.getExceptionMessage(), "").trim();
        if (!exceptionMessage.isBlank()) {
            return ControllerTextConfig.SERVER_REJECTED_PREFIX + safeOp
                    + ControllerTextConfig.SERVER_REJECTED_SEPARATOR_1 + exceptionName
                    + ControllerTextConfig.SERVER_REJECTED_SEPARATOR_2 + exceptionMessage;
        }
        return ControllerTextConfig.SERVER_REJECTED_PREFIX + safeOp
                + ControllerTextConfig.SERVER_REJECTED_SEPARATOR_1 + exceptionName;
    }

}
