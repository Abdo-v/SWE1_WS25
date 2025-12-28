package client.controller.network.service;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;

import client.controller.ControllerTextConfig;
import client.exception.GameCommunicationException;
import client.exception.MapProcessingException;
import client.model.Direction;
import client.model.mapper.PlayerHalfMap;
import messagesbase.ResponseEnvelope;
import messagesbase.UniquePlayerIdentifier;
import messagesbase.messagesfromclient.PlayerRegistration;
import messagesbase.messagesfromserver.GameState;

import java.util.Objects;

public class NetworkCenter {

    private final String gameId;
    private final String serverBaseUrl;
    private final NetworkCenterConfig config;
    private final ClientToServerConverter clientToServerConverter;
    private final ServerToClientConverter serverToClientConverter;

    private final NetworkCenterSession session;
    private final NetworkCenterTransport transport;
    private final NetworkCenterEnvelopeValidator envelopeValidator;
    private final NetworkCenterGameStateSupport gameStateSupport;

    public NetworkCenter(String serverBaseUrl, String gameId) {
        this(serverBaseUrl, gameId, NetworkCenterConfig.defaultConfig());
    }

    private NetworkCenter(String serverBaseUrl, String gameId, NetworkCenterConfig config) {
        this.gameId = gameId;
        this.serverBaseUrl = serverBaseUrl;
        this.config = config;
        WebClient webClient = WebClient.builder()
                .baseUrl(serverBaseUrl + ControllerTextConfig.PATH_GAMES)
                .defaultHeader(ControllerTextConfig.HTTP_HEADER_CONTENT_TYPE, ControllerTextConfig.MEDIA_TYPE_APPLICATION_XML)
                .defaultHeader(ControllerTextConfig.HTTP_HEADER_ACCEPT, ControllerTextConfig.MEDIA_TYPE_APPLICATION_XML)
                .build();
        NetworkCenterHttpClient httpClient = new WebClientNetworkCenterHttpClient(webClient);
        this.clientToServerConverter = new ClientToServerConverter();
        this.serverToClientConverter = new ServerToClientConverter();
        this.session = new NetworkCenterSession();
        this.transport = new NetworkCenterTransport(serverBaseUrl, httpClient);
        this.envelopeValidator = new NetworkCenterEnvelopeValidator();
        this.gameStateSupport = new NetworkCenterGameStateSupport();
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
        NetworkCenterHttpClient safeHttpClient = Objects.requireNonNull(httpClient, "httpClient is required");
        this.clientToServerConverter = Objects.requireNonNull(clientToServerConverter, "clientToServerConverter is required");
        this.serverToClientConverter = Objects.requireNonNull(serverToClientConverter, "serverToClientConverter is required");
        this.session = new NetworkCenterSession();
        this.transport = new NetworkCenterTransport(this.serverBaseUrl, safeHttpClient);
        this.envelopeValidator = new NetworkCenterEnvelopeValidator();
        this.gameStateSupport = new NetworkCenterGameStateSupport();
    }

    public UniquePlayerIdentifier registerPlayer(String firstName, String lastName, String uAccount) throws GameCommunicationException {
        PlayerRegistration playerReg = new PlayerRegistration(firstName, lastName, uAccount);

        ResponseEnvelope<UniquePlayerIdentifier> resultReg = transport.post(
            ControllerTextConfig.OP_PLAYER_REGISTRATION,
            "HTTP error during player registration: ",
            "Network connection failed during player registration",
            "/" + gameId + ControllerTextConfig.PATH_PLAYERS,
            playerReg,
            new ParameterizedTypeReference<ResponseEnvelope<UniquePlayerIdentifier>>() {}
        );
        envelopeValidator.throwOnServerRejection(ControllerTextConfig.OP_PLAYER_REGISTRATION, resultReg, serverBaseUrl);

        UniquePlayerIdentifier playerId = resultReg.getData().orElseThrow();
        session.setPlayerId(playerId);
        return playerId;
    }

    public void sendHalfMap(PlayerHalfMap halfMap) throws GameCommunicationException {
        UniquePlayerIdentifier playerId = session.requirePlayerId(serverBaseUrl, ControllerTextConfig.OP_SEND_HALF_MAP,
            "Player must be registered before sending a half map");
        
        messagesbase.messagesfromclient.PlayerHalfMap clientHalfMap = clientToServerConverter.convertClientHalfMap(halfMap, playerId);

        ResponseEnvelope<messagesbase.messagesfromserver.PlayerState> response = transport.post(
            ControllerTextConfig.OP_SEND_HALF_MAP,
            "HTTP error during half map submission: ",
            "Network connection failed during half map submission",
            "/" + gameId + ControllerTextConfig.PATH_HALFMAPS,
            clientHalfMap,
            new ParameterizedTypeReference<ResponseEnvelope<messagesbase.messagesfromserver.PlayerState>>() {}
        );
        envelopeValidator.throwOnServerRejection(ControllerTextConfig.OP_SEND_HALF_MAP, response, serverBaseUrl);
    }

    public void sendMove(Direction direction) throws GameCommunicationException {
        UniquePlayerIdentifier playerId = session.requirePlayerId(serverBaseUrl, ControllerTextConfig.OP_SEND_MOVE,
            "Player must be registered before sending a move");

        Direction safeDirection = Objects.requireNonNull(direction, ControllerTextConfig.REQUIRE_DIRECTION);
        sendMoveInternal(playerId, safeDirection);
    }

    private void sendMoveInternal(UniquePlayerIdentifier playerId, Direction direction) throws GameCommunicationException {
        try {
            messagesbase.messagesfromclient.EMove networkMove = clientToServerConverter.convertClientDirection(direction);
            messagesbase.messagesfromclient.PlayerMove playerMove = messagesbase.messagesfromclient.PlayerMove.of(playerId, networkMove);

                ResponseEnvelope<messagesbase.messagesfromserver.PlayerState> response = transport.post(
                    ControllerTextConfig.OP_SEND_MOVE,
                    "HTTP error during move submission: ",
                    "Network connection failed during move submission",
                    "/" + gameId + ControllerTextConfig.PATH_MOVES,
                    playerMove,
                    new ParameterizedTypeReference<ResponseEnvelope<messagesbase.messagesfromserver.PlayerState>>() {}
                );
            envelopeValidator.throwOnServerRejection(ControllerTextConfig.OP_SEND_MOVE, response, serverBaseUrl);
        } catch (IllegalArgumentException e) {
            throw new GameCommunicationException(
                    "Invalid direction cannot be converted: " + direction,
                    e,
                    serverBaseUrl,
                    ControllerTextConfig.OP_SEND_MOVE,
                    -1
            );
        }
    }

    public messagesbase.messagesfromserver.GameState pollGameState() throws GameCommunicationException, MapProcessingException {
        UniquePlayerIdentifier playerId = session.requirePlayerId(serverBaseUrl, ControllerTextConfig.OP_POLL_GAME_STATE,
            ControllerTextConfig.ERROR_PLAYER_MUST_BE_REGISTERED_POLL_STATE, ControllerTextConfig.HTTP_STATUS_UNKNOWN_INT);
        
        try {
            Thread.sleep(config.pollGameStateDelayMillis());

            ResponseEnvelope<GameState> response = transport.get(
                ControllerTextConfig.OP_POLL_GAME_STATE,
                ControllerTextConfig.ERROR_HTTP_ERROR_POLLING_GAME_STATE_PREFIX,
                ControllerTextConfig.ERROR_NETWORK_CONNECTION_FAILED_POLLING_GAME_STATE,
                "/" + gameId + ControllerTextConfig.PATH_STATES_PREFIX + playerId.getUniquePlayerID(),
                new ParameterizedTypeReference<ResponseEnvelope<GameState>>() {}
            );
            envelopeValidator.throwOnServerRejection(ControllerTextConfig.OP_POLL_GAME_STATE, response, serverBaseUrl);

            GameState gameState = gameStateSupport.requireGameState(response);
            int nodeCount = gameStateSupport.safeMapNodeCount(gameState);
            gameStateSupport.validateMapNodeCountIfPresent(gameState, nodeCount);
            return gameState;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new GameCommunicationException(
                ControllerTextConfig.ERROR_GAME_STATE_POLLING_INTERRUPTED,
                e,
                serverBaseUrl,
                ControllerTextConfig.OP_POLL_GAME_STATE,
                ControllerTextConfig.HTTP_STATUS_UNKNOWN_INT
            );
        }
    }

    public client.model.GameState convertServerGamestate(messagesbase.messagesfromserver.GameState serverGameState) {
        return serverToClientConverter.convertServerGamestate(
                serverGameState,
                session.playerId().orElseThrow(() -> new IllegalStateException(ControllerTextConfig.ERROR_PLAYER_MUST_BE_REGISTERED_CONVERT_STATE))
        );
    }

}
