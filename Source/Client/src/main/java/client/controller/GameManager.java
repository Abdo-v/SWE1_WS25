package client.controller;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
import client.exception.GameCommunicationException;
import client.exception.GameStateException;
import client.exception.FailureReason;
import client.exception.Operation;
import client.exception.AIDecisionException;
import client.exception.MapProcessingException;
import client.model.*;
import client.model.GameMode;
import client.model.ai.WayFinder;
import client.view.CLIHandler;
import client.view.DynamicCLIGameView;
import client.view.GameManagerView;
import client.view.GameOutput;
import client.view.MapVisualizationType;
import client.controller.network.service.NetworkCenter;
import client.model.mapper.GameMap;
import messagesbase.UniquePlayerIdentifier;

public class GameManager {
    // private static final Logger logger = LoggerFactory.getLogger(GameManager.class);
    
    private client.model.GameState gameState;
    private NetworkCenter networkCenter;
    private String playerId;
    private GameMap gameMap;
    private WayFinder wayFinder = new WayFinder();
    private CLIHandler cliHandler;
    private DynamicCLIGameView dynamicView = new DynamicCLIGameView();
    private final GameOutput output;
    private final HalfMapService halfMapService;
    private final GameStateSynchronizer gameStateSynchronizer;
    private final GameStateQueryService gameStateQueryService;
    private final GameLoopService gameLoopService;

    public GameManager(client.model.GameState state, String serverBaseUrl, String gameMode){
        this(state, serverBaseUrl, gameMode, new GameManagerView());
    }

    public GameManager(client.model.GameState state, String serverBaseUrl, String gameMode, GameOutput output){
        this(state, serverBaseUrl, GameMode.fromCliValue(gameMode), output);
    }

    public GameManager(client.model.GameState state, String serverBaseUrl, GameMode gameMode, GameOutput output){
        // // logger.debug("Creating GameManager with gameId: {}, server: {}, mode: {}", state.getGameStateID(), serverBaseUrl, gameMode);
        this.gameState = state;
        this.networkCenter = new NetworkCenter(serverBaseUrl, state.getGameStateID());
        this.gameMap = state.getMap();
        this.cliHandler = new CLIHandler(gameMode);
        this.output = output;
        this.halfMapService = new HalfMapService(this.networkCenter, this.cliHandler, this.output);
        this.gameStateSynchronizer = new GameStateSynchronizer(this.networkCenter);
        this.gameStateQueryService = new GameStateQueryService(this.networkCenter);
        this.gameLoopService = new GameLoopService(this, this.output);
        state.addObserver(cliHandler);
        state.addObserver(wayFinder);
        state.addObserver(dynamicView);
        wayFinder.setGameState(state);
        wayFinder.addSubObservers();
        // // logger.debug("GameManager initialization completed successfully");
    }

    /**
     * Constructs a new GameManager with the given game ID, server URL, and player ID.
     * @param gameId The ID of the game.
     * @param serverBaseUrl The base URL of the server.
     * @param playerId The unique player identifier.
     */
    public GameManager(String gameId, String serverBaseUrl, UniquePlayerIdentifier playerId) {
        this(gameId, serverBaseUrl, playerId, new GameManagerView());
    }

    public GameManager(String gameId, String serverBaseUrl, UniquePlayerIdentifier playerId, GameOutput output) {
        // // logger.debug("Creating GameManager with gameId: {}, server: {}, playerId: {}", gameId, serverBaseUrl, playerId.getUniquePlayerID());
        this.networkCenter = new NetworkCenter(serverBaseUrl, gameId, playerId);
        this.gameState = new client.model.GameState(gameId);
        this.output = output;
        this.halfMapService = new HalfMapService(this.networkCenter, this.cliHandler, this.output);
        this.gameStateSynchronizer = new GameStateSynchronizer(this.networkCenter);
        this.gameStateQueryService = new GameStateQueryService(this.networkCenter);
        this.gameLoopService = new GameLoopService(this, this.output);
    }

    /**
     * Constructs a new GameManager with the given game ID and server URL.
     * @param gameId The ID of the game.
     * @param serverBaseUrl The base URL of the server.
     */
    public GameManager(String gameId, String serverBaseUrl) {
        this(gameId, serverBaseUrl, new GameManagerView());
    }

    public GameManager(String gameId, String serverBaseUrl, GameOutput output) {
        // // logger.debug("Creating GameManager with gameId: {}, server: {}", gameId, serverBaseUrl);
        this.networkCenter = new NetworkCenter(serverBaseUrl, gameId);
        this.gameState = new client.model.GameState(gameId);
        this.output = output;
        this.halfMapService = new HalfMapService(this.networkCenter, this.cliHandler, this.output);
        this.gameStateSynchronizer = new GameStateSynchronizer(this.networkCenter);
        this.gameStateQueryService = new GameStateQueryService(this.networkCenter);
        this.gameLoopService = new GameLoopService(this, this.output);
    }

    /**
     * Registers a player with the server.
     * @param firstName Player's first name.
     * @param lastName Player's last name.
     * @param uAccount Player's university account.
     * @return The player's ID if registration is successful.
     * @throws GameCommunicationException If registration fails due to network or server issues.
     * @throws GameStateException If the game state is invalid for player registration.
     */
    public String registerPlayer(String firstName, String lastName, String uAccount) throws GameCommunicationException, GameStateException {
        // // logger.info("Attempting to register player: {} {}, uAccount: {}", firstName, lastName, uAccount);
        
        // Validate game state before registration
        if (gameState == null) {
            throw new GameStateException(
                "Cannot register player: game state is not initialized",
                null,
                "PLAYER_REGISTRATION",
                "uninitialized"
            );
        }
        
        try {
            UniquePlayerIdentifier playerIdentifier = networkCenter.registerPlayer(firstName, lastName, uAccount);
            this.playerId = playerIdentifier.getUniquePlayerID();
            client.model.PlayerState playerState = new client.model.PlayerState(playerId, firstName, lastName, uAccount);
            gameState.addPlayer(playerState);
            // // logger.info("Successfully registered player {} with ID: {}", playerState.getLastName(), playerId);
            return playerId;
        } catch (GameCommunicationException e) {
            // Re-throw communication exceptions as-is
            throw e;
        } catch (Exception e) {
            // Wrap unexpected exceptions
            throw new GameStateException(
                "Unexpected error during player registration: " + e.getMessage(),
                e,
                gameState.getGameStateID(),
                "PLAYER_REGISTRATION",
                "unknown",
                null
            );
        }
    }
    
    /**
     * Generates and sends the player's half map to the server.
     * @throws GameCommunicationException If sending half map fails due to network issues.
     * @throws GameStateException If the game state is invalid for map generation.
     */
    public void generateAndSendHalfMap() throws GameCommunicationException, GameStateException {
        halfMapService.generateAndSendHalfMap(playerId, gameState != null ? gameState.getGameStateID() : "unknown");
    }

    /**
     * Checks if the full map is available from the server.
     * @return true if the full map is available, false otherwise.
     * @throws GameCommunicationException If polling fails due to network issues.
     */
    public boolean fullMapAvailable() throws GameCommunicationException {
        return gameStateQueryService.isFullMapAvailable();
    }

    /**
     * Updates the game state with the latest data from the server.
     * @throws GameCommunicationException If polling fails due to network issues.
     * @throws MapProcessingException If the received data cannot be processed.
     */
    public void updateGameState() throws GameCommunicationException, MapProcessingException {
        gameStateSynchronizer.synchronize(this.gameState);
    }

    /**
     * Polls the server for the current game state.
     * @return The current game state from the server.
     * @throws GameCommunicationException If polling fails due to network issues.
     */
    public messagesbase.messagesfromserver.GameState managerpollGameState() throws GameCommunicationException {
        return gameStateQueryService.pollGameState();
    }

    /**
     * Polls the server for the current player's status.
     * @return The player's game state.
     * @throws GameCommunicationException If polling fails due to network issues.
     * @throws GameStateException If the player is not found in the game state.
     */
    public messagesbase.messagesfromserver.EPlayerGameState pollMyStatus() throws GameCommunicationException, GameStateException {
        return gameStateQueryService.pollPlayerStatus(playerId, gameState != null ? gameState.getGameStateID() : "unknown");
    }

    /**
     * Makes a move using the WayFinder and sends it to the server.
     * @throws GameCommunicationException If sending the move fails due to network issues.
     * @throws AIDecisionException If the AI fails to determine a valid move.
     * @throws GameStateException If the game state is invalid for making moves.
     */
    public void makeMove(String gameMode) throws GameCommunicationException, AIDecisionException, GameStateException {
        makeMove(GameMode.fromCliValue(gameMode));
    }

    public void makeMove(GameMode gameMode) throws GameCommunicationException, AIDecisionException, GameStateException {
        // // logger.debug("Making move for player: {}", playerId);
        
        // Validate game state before making move
        if (gameState == null || gameState.getCurrentPlayerState() == null) {
            throw new GameStateException(
                "Cannot make move: game state or player state is null",
                gameState != null ? gameState.getGameStateID() : "unknown",
                "MAKE_MOVE",
                "invalid_state"
            );
        }
        
        try {
            Direction nextMoveDirection = wayFinder.findNext();
            if (nextMoveDirection != null) {
                // // logger.debug("WayFinder suggested direction: {}", nextMoveDirection);
                try {
                    networkCenter.sendMove(nextMoveDirection);
                    // // logger.info("Move {} sent successfully for player {}", nextMoveDirection, playerId);
                    
                    if (gameMode != null && gameMode.isReduced()) {
                        // // logger.info("Player {} (TRR mode) moved to: {}", playerId, gameState.getCurrentPlayerState().getCurrentPosition().printCoordinates());
                        output.showMoveSent(nextMoveDirection);
                    }
                } catch (GameCommunicationException e) {
                    throw e; // Re-throw communication exceptions
                } catch (Exception e) {
                    // // logger.error("Error making move for player {}: {}", playerId, e.getMessage(), e);
                    throw new GameCommunicationException(
                        "Failed to send move to server: " + e.getMessage(),
                        e,
                        networkCenter != null ? FailureReason.UNKNOWN.code() : FailureReason.NO_NETWORK.code(),
                        Operation.SEND_MOVE,
                        -1
                    );
                }
            } else {
                // // logger.warn("WayFinder did not suggest a valid move for player {}. This may indicate AI decision failure.", playerId);
                throw new AIDecisionException(
                    "WayFinder failed to determine a valid move",
                    "WayFinder",
                    "findNext",
                    gameState.getCurrentPlayerState().getCurrentPosition()
                );
            }
        } catch (AIDecisionException | GameCommunicationException e) {
            throw e; // Re-throw our custom exceptions
        } catch (Exception e) {
            // // logger.error("Unexpected error during move making: {}", e.getMessage(), e);
            throw new GameStateException(
                "Unexpected error during move making: " + e.getMessage(),
                e,
                gameState.getGameStateID(),
                Operation.MAKE_MOVE,
                FailureReason.ERROR,
                null
            );
        }
    }

    /**
     * Gets the current player status with proper exception handling.
     * @return The current player status.
     * @throws GameStateException If the player is not found in the game state.
     */
    public PlayerStatus getCurrentPlayerStatus() throws GameStateException {
        if (gameState == null || gameState.getPlayers() == null) {
            throw new GameStateException(
                "Cannot get player status: game state or players list is null",
                gameState != null ? gameState.getGameStateID() : "unknown",
                "GET_PLAYER_STATUS",
                "null_state"
            );
        }
        
        for (client.model.PlayerState playerState : gameState.getPlayers()) {
            if (playerState.getPlayerID().equals(playerId)) {
                // // logger.trace("Current player {} status: {}", playerId, playerState.getStatus());
                return playerState.getStatus();
            }
        }
        
        throw new GameStateException(
            "Player ID not found in game state",
            gameState.getGameStateID(),
            "GET_PLAYER_STATUS",
            "player_not_found",
            "player_present"
        );
    }

    /**
     * Starts the main game loop with comprehensive exception handling.
     * @throws GameCommunicationException If network communication fails.
     * @throws GameStateException If game state becomes invalid.
     */
    public void startGameLoop(String gameMode) throws GameCommunicationException, GameStateException {
        gameLoopService.startGameLoop(gameMode);
    }

    public void startGameLoop(GameMode gameMode) throws GameCommunicationException, GameStateException {
        gameLoopService.startGameLoop(gameMode);
    }

    /**
     * Gets the current game state.
     * @return The current game state.
     */
    public client.model.GameState getGameState() {
        return gameState;
    }

    /**
     * Sets the current game state.
     * @param gameState The game state to set.
     */
    public void setGameState(client.model.GameState gameState) {
        this.gameState = gameState;
    }

    /**
     * Gets the full game map.
     * @return The full game map.
     */
    public GameMap getMap() {
        return gameMap;
    }

    /**
     * Gets the player ID.
     * @return The player ID.
     */
    public String getPlayerId() {
        return playerId;
    }

    /**
     * Sets the player ID.
     * @param playerId The player ID to set.
     */
    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    /**
     * Checks if the server map is empty.
     * @return true if the server map is empty, false otherwise.
     * @throws GameCommunicationException If polling fails due to network issues.
     */
    public boolean isServerMapEmpty() throws GameCommunicationException {
        try {
        } catch (Exception e) {
            throw new GameCommunicationException(
                "Failed to check if server map is empty: " + e.getMessage(),
                e,
                networkCenter != null ? "unknown" : "no_network",
                "CHECK_SERVER_MAP_EMPTY",
                -1
            );
        }
        return gameStateQueryService.isServerMapEmpty();
    }

    public boolean shouldAct() {
        boolean mustAct = this.gameState.getCurrentPlayerState().getStatus() == client.model.PlayerStatus.MUST_ACT;
        // // logger.trace("Should act check for player {}: {}", playerId, mustAct);
        return mustAct;
    }

    public boolean shouldWait() {
        boolean mustWait = this.gameState.getCurrentPlayerState().getStatus() == client.model.PlayerStatus.MUST_WAIT;
        // // logger.trace("Should wait check for player {}: {}", playerId, mustWait);
        return mustWait;
    }

    public void visualizeMap(String mapType) {
        // // logger.debug("Visualizing map type: {}", mapType);
        cliHandler.visualizeMap(mapType);
    }

    public void visualizeMap(MapVisualizationType mapType) {
        cliHandler.visualizeMap(mapType);
    }

    public void enableDynamicVisualization() {
        // // logger.debug("Enabling dynamic visualization");
        dynamicView.enableDynamicMode();
    }

    /**
     * Disable dynamic visualization
     */
    public void disableDynamicVisualization() {
        // // logger.debug("Disabling dynamic visualization");
        dynamicView.disableDynamicMode();
    }

}
