package client.controller;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
import client.exception.GameCommunicationException;
import client.exception.GameStateException;
import client.exception.AIDecisionException;
import client.exception.MapProcessingException;
import client.model.GameMode;
import client.model.ai.WayFinder;
import client.view.DynamicCLIGameView;
import client.view.GameOutput;
import client.view.MapVisualizationType;
import client.controller.network.service.NetworkCenter;
import client.model.mapper.GameMap;
import messagesbase.UniquePlayerIdentifier;

import java.util.Optional;
import java.util.Objects;

public class GameManager {
    // private static final Logger logger = LoggerFactory.getLogger(GameManager.class);
    
    private client.model.GameState gameState;
    private final NetworkCenter networkCenter;
    private Optional<String> playerId;
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
    private final PlayerTurnService playerTurnService;

    private static final String UNKNOWN_GAME_STATE_ID = "unknown";

    public GameManager(client.model.GameState state, String serverBaseUrl, String gameMode){
        this(state, serverBaseUrl, gameMode, new client.view.GameManagerView());
    }

    public GameManager(client.model.GameState state, String serverBaseUrl, String gameMode, GameOutput output){
        this(state, serverBaseUrl, GameMode.fromCliValue(gameMode), output);
    }

    public GameManager(client.model.GameState state, String serverBaseUrl, GameMode gameMode, GameOutput output){
        this.gameState = Objects.requireNonNull(state, "state is required");
        this.networkCenter = new NetworkCenter(Objects.requireNonNull(serverBaseUrl, "serverBaseUrl is required"), this.gameState.getGameStateID());
        this.output = Optional.ofNullable(output).orElseGet(NoOpGameOutput::new);
        this.playerId = Optional.empty();

        var cliHandler = GameManagerWiring.createCliHandler(gameMode);
        GameManagerWiring.wireObservers(state, cliHandler, wayFinder, dynamicView);

        this.halfMapService = new HalfMapService(this.networkCenter, cliHandler, this.output);
        this.gameStateSynchronizer = new GameStateSynchronizer(this.networkCenter);
        this.gameStateQueryService = new GameStateQueryService(this.networkCenter);
        this.gameLoopService = new GameLoopService(this, this.output);

        this.playerRegistrationService = new PlayerRegistrationService(this.networkCenter);
        this.moveExecutionService = new MoveExecutionService(this.networkCenter, this.wayFinder, this.output);
        this.visualizationService = new GameVisualizationService(cliHandler, this.dynamicView);
        this.playerTurnService = new PlayerTurnService();
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
        this.networkCenter = new NetworkCenter(Objects.requireNonNull(serverBaseUrl, "serverBaseUrl is required"), Objects.requireNonNull(gameId, "gameId is required"), Objects.requireNonNull(playerId, "playerId is required"));
        this.gameState = new client.model.GameState(gameId);
        this.playerId = Optional.of(playerId.getUniquePlayerID());
        this.output = Optional.ofNullable(output).orElseGet(NoOpGameOutput::new);

        var cliHandler = GameManagerWiring.createCliHandler(GameMode.UNKNOWN);
        GameManagerWiring.wireObservers(this.gameState, cliHandler, wayFinder, dynamicView);

        this.halfMapService = new HalfMapService(this.networkCenter, cliHandler, this.output);
        this.gameStateSynchronizer = new GameStateSynchronizer(this.networkCenter);
        this.gameStateQueryService = new GameStateQueryService(this.networkCenter);
        this.gameLoopService = new GameLoopService(this, this.output);

        this.playerRegistrationService = new PlayerRegistrationService(this.networkCenter);
        this.moveExecutionService = new MoveExecutionService(this.networkCenter, this.wayFinder, this.output);
        this.visualizationService = new GameVisualizationService(cliHandler, this.dynamicView);
        this.playerTurnService = new PlayerTurnService();
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
        this.networkCenter = new NetworkCenter(Objects.requireNonNull(serverBaseUrl, "serverBaseUrl is required"), Objects.requireNonNull(gameId, "gameId is required"));
        this.gameState = new client.model.GameState(gameId);
        this.output = Optional.ofNullable(output).orElseGet(NoOpGameOutput::new);
        this.playerId = Optional.empty();

        var cliHandler = GameManagerWiring.createCliHandler(GameMode.UNKNOWN);
        GameManagerWiring.wireObservers(this.gameState, cliHandler, wayFinder, dynamicView);

        this.halfMapService = new HalfMapService(this.networkCenter, cliHandler, this.output);
        this.gameStateSynchronizer = new GameStateSynchronizer(this.networkCenter);
        this.gameStateQueryService = new GameStateQueryService(this.networkCenter);
        this.gameLoopService = new GameLoopService(this, this.output);

        this.playerRegistrationService = new PlayerRegistrationService(this.networkCenter);
        this.moveExecutionService = new MoveExecutionService(this.networkCenter, this.wayFinder, this.output);
        this.visualizationService = new GameVisualizationService(cliHandler, this.dynamicView);
        this.playerTurnService = new PlayerTurnService();
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
        String registeredPlayerId = playerRegistrationService.registerPlayer(gameState, firstName, lastName, uAccount);
        this.playerId = Optional.of(registeredPlayerId);
        return registeredPlayerId;
    }
    
    /**
     * Generates and sends the player's half map to the server.
     * @throws GameCommunicationException If sending half map fails due to network issues.
     * @throws GameStateException If the game state is invalid for map generation.
     */
    public void generateAndSendHalfMap() throws GameCommunicationException, GameStateException {
        halfMapService.generateAndSendHalfMap(requirePlayerId(), requireGameStateId());
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
        return gameStateQueryService.pollPlayerStatus(requirePlayerId(), requireGameStateId());
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
        moveExecutionService.makeMove(gameState, requirePlayerId(), gameMode);
    }

    /**
     * Gets the current player status with proper exception handling.
     * @return The current player status.
     * @throws GameStateException If the player is not found in the game state.
     */
    public client.model.PlayerStatus getCurrentPlayerStatus() throws GameStateException {
        return playerTurnService.getCurrentPlayerStatus(gameState, requirePlayerId());
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
        this.gameState = Objects.requireNonNull(gameState, "gameState is required");
    }

    /**
     * Gets the full game map.
     * @return The full game map.
     */
    public GameMap getMap() {
        return Optional.ofNullable(gameState.getMap()).orElseGet(GameMap::new);
    }

    /**
     * Gets the player ID.
     * @return The player ID.
     */
    public String getPlayerId() {
        return playerId.orElse("");
    }

    /**
     * Sets the player ID.
     * @param playerId The player ID to set.
     */
    public void setPlayerId(String playerId) {
        this.playerId = Optional.ofNullable(playerId)
                .filter(id -> !id.isBlank());
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
        return playerTurnService.shouldAct(this.gameState);
    }

    public boolean shouldWait() {
        return playerTurnService.shouldWait(this.gameState);
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

    private String requirePlayerId() {
        return playerId.filter(id -> !id.isBlank()).orElseThrow(() -> new GameStateException(
                "Player identifier is missing",
                requireGameStateId(),
                "PLAYER_ID",
                "missing"
        ));
    }

    private String requireGameStateId() {
        return Optional.ofNullable(gameState)
                .map(client.model.GameState::getGameStateID)
                .filter(id -> !id.isBlank())
                .orElse(UNKNOWN_GAME_STATE_ID);
    }

}

