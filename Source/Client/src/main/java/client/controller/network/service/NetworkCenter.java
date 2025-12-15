package client.controller.network.service;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import client.exception.GameCommunicationException;
import client.exception.MapProcessingException;
import client.model.Direction;
import client.model.mapper.MapNode;
import client.model.mapper.PlayerHalfMap;
import messagesbase.ResponseEnvelope;
import messagesbase.UniquePlayerIdentifier;
import messagesbase.messagesfromclient.ERequestState;
import messagesbase.messagesfromclient.PlayerRegistration;
import messagesbase.messagesfromserver.GameState;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;

public class NetworkCenter {
     // private static final Logger logger = LoggerFactory.getLogger(NetworkCenter.class);
    
    private final WebClient webClient;
    private final String gameId;
    private final String serverBaseUrl;
    private final NetworkCenterConfig config;
    private UniquePlayerIdentifier playerId = null;
    private final ClientToServerConverter clientToServerConverter = new ClientToServerConverter();
    private final ServerToClientConverter serverToClientConverter = new ServerToClientConverter();

    /**
     * Constructs a NetworkCenter with the given server base URL, game ID, and player ID.
     * @param serverBaseUrl The base URL of the server.
     * @param gameId The ID of the game.
     * @param playerId The unique player identifier.
     */
    public NetworkCenter(String serverBaseUrl, String gameId, UniquePlayerIdentifier playerId) {
        this(serverBaseUrl, gameId, playerId, NetworkCenterConfig.defaultConfig());
    }

    public NetworkCenter(String serverBaseUrl, String gameId, UniquePlayerIdentifier playerId, NetworkCenterConfig config) {
     // logger.debug("Creating NetworkCenter with server: {}, gameId: {}, playerId: {}", serverBaseUrl, gameId, playerId.getUniquePlayerID());
        this.gameId = gameId;
        this.serverBaseUrl = serverBaseUrl;
        this.playerId = playerId;
        this.config = config;
        this.webClient = WebClient.builder()
                .baseUrl(serverBaseUrl + "/games")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE) 
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML_VALUE)
                .build();
     // logger.debug("NetworkCenter initialized successfully for game: {}", gameId);
    }

    /**
     * Constructs a NetworkCenter with the given server base URL and game ID.
     * @param serverBaseUrl The base URL of the server.
     * @param gameId The ID of the game.
     */
    public NetworkCenter(String serverBaseUrl, String gameId) {
        this(serverBaseUrl, gameId, NetworkCenterConfig.defaultConfig());
    }

    public NetworkCenter(String serverBaseUrl, String gameId, NetworkCenterConfig config) {
     // logger.debug("Creating NetworkCenter with server: {}, gameId: {}", serverBaseUrl, gameId);
        this.gameId = gameId;
        this.serverBaseUrl = serverBaseUrl;
        this.config = config;
        this.webClient = WebClient.builder()
                .baseUrl(serverBaseUrl + "/games")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE) 
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML_VALUE)
                .build();
     // logger.debug("NetworkCenter initialized successfully for game: {}", gameId);
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
            Mono<ResponseEnvelope<UniquePlayerIdentifier>> webAccess = webClient
                    .method(HttpMethod.POST)
                    .uri("/" + gameId + "/players")
                    .body(BodyInserters.fromValue(playerReg))
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ResponseEnvelope<UniquePlayerIdentifier>>() {});
            
            ResponseEnvelope<UniquePlayerIdentifier> resultReg = webAccess.block();
            
            if (resultReg.getState() == ERequestState.Error) {
             // logger.error("Player registration failed for {} {}: {}", firstName, lastName, resultReg.getExceptionMessage());
                throw new GameCommunicationException(
                    "Server rejected player registration: " + resultReg.getExceptionMessage(),
                    serverBaseUrl,
                    "PLAYER_REGISTRATION",
                    400
                );
            }
            
            this.playerId = resultReg.getData().get();
         // logger.info("Player registration successful: {} {} assigned ID: {}", firstName, lastName, this.playerId.getUniquePlayerID());
            return this.playerId;
            
        } catch (WebClientResponseException e) {
         // logger.error("HTTP error during player registration: {}", e.getMessage(), e);
            throw new GameCommunicationException(
                "HTTP error during player registration: " + e.getMessage(),
                e,
                serverBaseUrl,
                "PLAYER_REGISTRATION",
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
                "PLAYER_REGISTRATION",
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
        if (playerId == null) {
         // logger.error("Attempted to send half map without player registration");
            throw new GameCommunicationException(
                "Player must be registered before sending a half map",
                serverBaseUrl,
                "SEND_HALF_MAP",
                -1
            );
        }
        
        try {
         // logger.info("Sending half map for player: {} in game: {}", playerId.getUniquePlayerID(), gameId);
         // logger.debug("Converting client half map to server format");
                messagesbase.messagesfromclient.PlayerHalfMap clientHalfMap =
                    clientToServerConverter.convertClientHalfMap(halfMap, this.playerId);
            
         // logger.debug("Transmitting half map to server");
            Mono<ResponseEnvelope<messagesbase.messagesfromserver.PlayerState>> webAccess = webClient
                    .method(HttpMethod.POST)
                    .uri("/" + gameId + "/halfmaps")
                    .body(BodyInserters.fromValue(clientHalfMap))
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ResponseEnvelope<messagesbase.messagesfromserver.PlayerState>>() {});
            
            ResponseEnvelope<messagesbase.messagesfromserver.PlayerState> response = webAccess.block();
            
            if (response.getState() == ERequestState.Error) {
             // logger.error("Failed to send half map for player {}: {}", playerId.getUniquePlayerID(), response.getExceptionMessage());
                throw new GameCommunicationException(
                    "Server rejected half map: " + response.getExceptionMessage(),
                    serverBaseUrl,
                    "SEND_HALF_MAP",
                    400
                );
            }
            
         // logger.info("Half map successfully sent for player: {}", playerId.getUniquePlayerID());
            
        } catch (WebClientResponseException e) {
         // logger.error("HTTP error during half map submission: {}", e.getMessage(), e);
            throw new GameCommunicationException(
                "HTTP error during half map submission: " + e.getMessage(),
                e,
                serverBaseUrl,
                "SEND_HALF_MAP",
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
                "SEND_HALF_MAP",
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
        if (playerId == null) {
         // logger.error("Attempted to send move without player registration");
            throw new GameCommunicationException(
                "Player must be registered before sending a move",
                serverBaseUrl,
                "SEND_MOVE",
                -1
            );
        }
        if (direction == null) {
         // logger.warn("Attempted to send a null move for player {}. Skipping.", playerId.getUniquePlayerID());
            System.err.println("Attempted to send a null move. Skipping.");
            return;
        }
        
        try {
         // logger.debug("Sending move {} for player: {}", direction, playerId.getUniquePlayerID());
            messagesbase.messagesfromclient.EMove networkMove = convertClientDirection(direction);
            if (networkMove == null) {
             // logger.error("Invalid direction {} cannot be converted for player {}", direction, playerId.getUniquePlayerID());
                throw new GameCommunicationException(
                    "Invalid direction cannot be converted: " + direction,
                    serverBaseUrl,
                    "SEND_MOVE",
                    -1
                );
            }
            
            messagesbase.messagesfromclient.PlayerMove playerMove = messagesbase.messagesfromclient.PlayerMove.of(this.playerId, networkMove);
            
         // logger.trace("Transmitting move {} to server for player {}", networkMove, playerId.getUniquePlayerID());
            Mono<ResponseEnvelope<messagesbase.messagesfromserver.PlayerState>> webAccess = webClient
                    .method(HttpMethod.POST)
                    .uri("/" + gameId + "/moves")
                    .body(BodyInserters.fromValue(playerMove))
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ResponseEnvelope<messagesbase.messagesfromserver.PlayerState>>() {});
            
            ResponseEnvelope<messagesbase.messagesfromserver.PlayerState> response = webAccess.block();
            
            if (response.getState() == ERequestState.Error) {
             // logger.error("Failed to send move {} for player {}: {}", direction, playerId.getUniquePlayerID(), response.getExceptionMessage());
                throw new GameCommunicationException(
                    "Server rejected move: " + response.getExceptionMessage(),
                    serverBaseUrl,
                    "SEND_MOVE",
                    400
                );
            }
            
         // logger.debug("Move {} successfully sent for player: {}", direction, playerId.getUniquePlayerID());
            
        } catch (WebClientResponseException e) {
         // logger.error("HTTP error during move submission: {}", e.getMessage(), e);
            throw new GameCommunicationException(
                "HTTP error during move submission: " + e.getMessage(),
                e,
                serverBaseUrl,
                "SEND_MOVE",
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
                "SEND_MOVE",
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
     // logger.trace("Polling game state for player: {} in game: {}", playerId != null ? playerId.getUniquePlayerID() : "unknown", gameId);
        
        if (playerId == null) {
         // logger.error("Attempted to poll game state without player registration");
            throw new GameCommunicationException(
                "Player must be registered before polling game state",
                serverBaseUrl,
                "POLL_GAME_STATE",
                -1
            );
        }
        
        try {
            Thread.sleep(config.pollGameStateDelayMillis());
            
            Mono<ResponseEnvelope<GameState>> webAccess = webClient
                    .method(HttpMethod.GET)
                    .uri("/" + gameId + "/states/" + playerId.getUniquePlayerID())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ResponseEnvelope<GameState>>() {});
            
            ResponseEnvelope<GameState> response = webAccess.block();
            
            if (response.getState() == ERequestState.Error) {
             // logger.error("Failed to poll game state for player {}: {}", playerId.getUniquePlayerID(), response.getExceptionMessage());
                throw new GameCommunicationException(
                    "Server rejected game state request: " + response.getExceptionMessage(),
                    serverBaseUrl,
                    "POLL_GAME_STATE",
                    400
                );
            }
            
            GameState gameState = response.getData().get();
            
            if (gameState == null) {
                throw new MapProcessingException(
                    "Received null game state from server",
                    "GameState",
                    "server_response_validation"
                );
            }
            
            int nodeCount = gameState.getMap() != null ? gameState.getMap().getMapNodes().size() : 0;
         // logger.trace("Game state polled successfully for player: {}, map nodes: {}", playerId.getUniquePlayerID(), nodeCount);
            
            if (gameState.getMap() != null && gameState.getMap().getMapNodes() != null) {
                int expectedNodes = nodeCount == 50 ? 50 : (nodeCount == 100 ? 100 : -1);
                if (expectedNodes > 0 && nodeCount != expectedNodes && nodeCount != 0) {
                 // logger.warn("Unexpected map node count: expected {} or 0, got {}", expectedNodes, nodeCount);
                }
            }
            
            return gameState;
            
        } catch (WebClientResponseException e) {
         // logger.error("HTTP error during game state polling: {}", e.getMessage(), e);
            throw new GameCommunicationException(
                "HTTP error during game state polling: " + e.getMessage(),
                e,
                serverBaseUrl,
                "POLL_GAME_STATE",
                e.getStatusCode().value()
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
         // logger.error("Polling interrupted: {}", e.getMessage(), e);
            throw new GameCommunicationException(
                "Game state polling was interrupted",
                e,
                serverBaseUrl,
                "POLL_GAME_STATE",
                -1
            );
        } catch (Exception e) {
            if (e instanceof GameCommunicationException || e instanceof MapProcessingException) {
                throw e;
            }
         // logger.error("Network error during game state polling: {}", e.getMessage(), e);
            throw new GameCommunicationException(
                "Network connection failed during game state polling",
                e,
                serverBaseUrl,
                "POLL_GAME_STATE",
                -1
            );
        }
    }

    /**
     * Converts the internal half map to the network message format.
     * @param halfMap Our internal half map representation.
     * @return A half map in the format expected by the server.
     */
    public messagesbase.messagesfromclient.PlayerHalfMap convertClientHalfMap(PlayerHalfMap halfMap) {
     // logger.trace("Converting client half map to server format for player: {}", playerId != null ? playerId.getUniquePlayerID() : "unknown");
        return clientToServerConverter.convertClientHalfMap(halfMap, this.playerId);
    }

    /**
     * Converts a list of internal map nodes to network map nodes.
     * @param nodes List of internal map nodes.
     * @return Collection of network map nodes.
     */
    public Collection<messagesbase.messagesfromclient.PlayerHalfMapNode> convertToServerNodes(List<MapNode> nodes) {
        return clientToServerConverter.convertClientNodes(nodes);
    }

    /**
     * Converts client terrain to network terrain.
     * @param clientTerrain The client terrain.
     * @return The corresponding network terrain.
     */
    public messagesbase.messagesfromclient.ETerrain convertClientTerrain(client.model.mapper.Terrain clientTerrain) {
        return clientToServerConverter.convertClientTerrain(clientTerrain);
    }

    /**
     * Converts server terrain to client terrain.
     * @param serverTerrain The server terrain.
     * @return The corresponding client terrain.
     */
    public client.model.mapper.Terrain convertServerTerrain(messagesbase.messagesfromclient.ETerrain serverTerrain) {
        return serverToClientConverter.convertServerTerrain(serverTerrain);
    }

    /**
     * Converts the server game state to the internal representation.
     * @param serverGameState The server game state.
     * @return The internal game state.
     */
    public client.model.GameState convertServerGamestate(messagesbase.messagesfromserver.GameState serverGameState) {
     // logger.trace("Converting server game state to client format for player: {}", playerId != null ? playerId.getUniquePlayerID() : "unknown");
        return serverToClientConverter.convertServerGamestate(serverGameState, this.playerId);
    }

    /**
     * Checks if the server map has an enemy fort.
     * @param serverMap The server map.
     * @return True if enemy fort is present, false otherwise.
     */
    public boolean serverMapHasEnemyFort(messagesbase.messagesfromserver.FullMap serverMap) {
     // logger.trace("Checking for enemy fort in server map");
        return serverToClientConverter.serverMapHasEnemyFort(serverMap);
    }

    /**
     * Converts the server map to the internal game map.
     * @param serverMap The server map.
     * @return The internal game map.
     */
    public client.model.mapper.GameMap convertServerMap(messagesbase.messagesfromserver.FullMap serverMap) {
     // logger.debug("Converting server map to client format, nodes: {}", serverMap != null ? serverMap.getMapNodes().size() : 0);
        return serverToClientConverter.convertServerMap(serverMap);
    }

    /**
     * Converts the server player state to the internal representation.
     * @param serverPlayerState The server player state.
     * @param playerMapNode The player's map node.
     * @return The internal player state.
     */
    public client.model.PlayerState convertServerPlayerState(messagesbase.messagesfromserver.PlayerState serverPlayerState, MapNode playerMapNode) {
        return serverToClientConverter.convertServerPlayerState(serverPlayerState, playerMapNode);
    }

    public client.model.PlayerStatus convertServerStatus(messagesbase.messagesfromserver.EPlayerGameState serverStatus){
        return serverToClientConverter.convertServerStatus(serverStatus);
    }

    /**
     * Converts the server map node to the internal map node.
     * @param serverMapNode The server map node.
     * @return The internal map node.
     */
    public MapNode convertServerMapNode(messagesbase.messagesfromserver.FullMapNode serverMapNode) {
        return serverToClientConverter.convertServerMapNode(serverMapNode);
    }

    /**
     * Converts the client direction to the network move.
     * @param d The client direction.
     * @return The network move.
     */
    public messagesbase.messagesfromclient.EMove convertClientDirection(Direction d){
     // logger.trace("Converting client direction {} to server move", d);
        return clientToServerConverter.convertClientDirection(d);
    }

    /**
     * Gets the game ID.
     * @return The game ID.
     */
    public String getGameId() {
        return gameId;
    }

    /**
     * Gets the player ID.
     * @return The player ID.
     */
    public UniquePlayerIdentifier getPlayerId() {
        return playerId;
    }
}
