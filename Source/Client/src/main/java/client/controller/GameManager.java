package client.controller;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
import client.exception.GameCommunicationException;
import client.exception.GameStateException;
import client.exception.AIDecisionException;
import client.exception.MapProcessingException;
import client.model.GameMode;
import client.model.PlayerStatus;
import client.model.ai.WayFinder;
import client.view.DynamicCLIGameView;
import client.view.GameOutput;
import client.view.MapVisualizationType;
import client.controller.network.service.NetworkCenter;
import client.model.mapper.GameMap;
import messagesbase.UniquePlayerIdentifier;

public class GameManager {
    // private static final Logger logger = LoggerFactory.getLogger(GameManager.class);
    
    private client.model.GameState gameState;
    private final NetworkCenter networkCenter;
    private String playerId;
    private final WayFinder wayFinder = new WayFinder();
    private final DynamicCLIGameView dynamicView = new DynamicCLIGameView();
    private final GameOutput output;
    private final HalfMapService halfMapService;
    private final GameStateSynchronizer gameStateSynchronizer;
    private final GameStateQueryService gameStateQueryService;
    private final GameLoopService gameLoopService;
    private final PlayerRegistrationService playerRegistrationService;
    private final MoveExecutionService moveExecutionService;
    private final GameVisualizationService visualizationService;

    public GameManager(client.model.GameState state, String serverBaseUrl, String gameMode){
        this(state, serverBaseUrl, gameMode, new client.view.GameManagerView());
    }

    public GameManager(client.model.GameState state, String serverBaseUrl, String gameMode, GameOutput output){
        this(state, serverBaseUrl, GameMode.fromCliValue(gameMode), output);
    }

    public GameManager(client.model.GameState state, String serverBaseUrl, GameMode gameMode, GameOutput output){
        this.gameState = state;
        this.networkCenter = new NetworkCenter(serverBaseUrl, state.getGameStateID());
        this.output = output;

        var cliHandler = GameManagerWiring.createCliHandler(gameMode);
        GameManagerWiring.wireObservers(state, cliHandler, wayFinder, dynamicView);

        this.halfMapService = new HalfMapService(this.networkCenter, cliHandler, this.output);
        this.gameStateSynchronizer = new GameStateSynchronizer(this.networkCenter);
        this.gameStateQueryService = new GameStateQueryService(this.networkCenter);
        this.gameLoopService = new GameLoopService(this, this.output);

        this.playerRegistrationService = new PlayerRegistrationService(this.networkCenter);
        this.moveExecutionService = new MoveExecutionService(this.networkCenter, this.wayFinder, this.output);
        this.visualizationService = new GameVisualizationService(cliHandler, this.dynamicView);
    }

    /**
     * Constructs a new GameManager with the given game ID, server URL, and player ID.
     * @param gameId The ID of the game.
     * @param serverBaseUrl The base URL of the server.
     * @param playerId The unique player identifier.
     */
    public GameManager(String gameId, String serverBaseUrl, UniquePlayerIdentifier playerId) {
        this(gameId, serverBaseUrl, playerId, new client.view.GameManagerView());
    }

    public GameManager(String gameId, String serverBaseUrl, UniquePlayerIdentifier playerId, GameOutput output) {
        this.networkCenter = new NetworkCenter(serverBaseUrl, gameId, playerId);
        this.gameState = new client.model.GameState(gameId);
        this.playerId = playerId != null ? playerId.getUniquePlayerID() : null;
        this.output = output;

        var cliHandler = GameManagerWiring.createCliHandler(GameMode.UNKNOWN);
        GameManagerWiring.wireObservers(this.gameState, cliHandler, wayFinder, dynamicView);

        this.halfMapService = new HalfMapService(this.networkCenter, cliHandler, this.output);
        this.gameStateSynchronizer = new GameStateSynchronizer(this.networkCenter);
        this.gameStateQueryService = new GameStateQueryService(this.networkCenter);
        this.gameLoopService = new GameLoopService(this, this.output);

        this.playerRegistrationService = new PlayerRegistrationService(this.networkCenter);
        this.moveExecutionService = new MoveExecutionService(this.networkCenter, this.wayFinder, this.output);
        this.visualizationService = new GameVisualizationService(cliHandler, this.dynamicView);
    }

    /**
     * Constructs a new GameManager with the given game ID and server URL.
     * @param gameId The ID of the game.
     * @param serverBaseUrl The base URL of the server.
     */
    public GameManager(String gameId, String serverBaseUrl) {
        this(gameId, serverBaseUrl, new client.view.GameManagerView());
    }

    public GameManager(String gameId, String serverBaseUrl, GameOutput output) {
        this.networkCenter = new NetworkCenter(serverBaseUrl, gameId);
        this.gameState = new client.model.GameState(gameId);
        this.output = output;

        var cliHandler = GameManagerWiring.createCliHandler(GameMode.UNKNOWN);
        GameManagerWiring.wireObservers(this.gameState, cliHandler, wayFinder, dynamicView);

        this.halfMapService = new HalfMapService(this.networkCenter, cliHandler, this.output);
        this.gameStateSynchronizer = new GameStateSynchronizer(this.networkCenter);
        this.gameStateQueryService = new GameStateQueryService(this.networkCenter);
        this.gameLoopService = new GameLoopService(this, this.output);

        this.playerRegistrationService = new PlayerRegistrationService(this.networkCenter);
        this.moveExecutionService = new MoveExecutionService(this.networkCenter, this.wayFinder, this.output);
        this.visualizationService = new GameVisualizationService(cliHandler, this.dynamicView);
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
        this.playerId = playerRegistrationService.registerPlayer(gameState, firstName, lastName, uAccount);
        return this.playerId;
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
        moveExecutionService.makeMove(gameState, playerId, gameMode);
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
        return gameState != null ? gameState.getMap() : null;
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
        return gameStateQueryService.isServerMapEmpty();
    }

    public boolean shouldAct() {
        return this.gameState.getCurrentPlayerState().getStatus() == client.model.PlayerStatus.MUST_ACT;
    }

    public boolean shouldWait() {
        return this.gameState.getCurrentPlayerState().getStatus() == client.model.PlayerStatus.MUST_WAIT;
    }

    public void visualizeMap(String mapType) {
        visualizationService.visualizeMap(mapType);
    }

    public void visualizeMap(MapVisualizationType mapType) {
        visualizationService.visualizeMap(mapType);
    }

    public void enableDynamicVisualization() {
        visualizationService.enableDynamicVisualization();
    }

    /**
     * Disable dynamic visualization
     */
    public void disableDynamicVisualization() {
        visualizationService.disableDynamicVisualization();
    }

}
