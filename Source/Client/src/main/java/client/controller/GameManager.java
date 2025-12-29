package client.controller;
import client.exception.GameCommunicationException;
import client.exception.GameStateException;
import client.exception.AIDecisionException;
import client.exception.FullMapNotAvailableException;
import client.exception.MapProcessingException;
import client.model.GameMode;
import client.model.ai.WayFinder;
import client.view.DynamicCLIGameView;
import client.view.GameOutput;
import client.controller.network.service.NetworkCenter;

import java.util.Optional;
import java.util.Objects;
import java.time.Duration;

/**
 * Top-level controller orchestrating client startup, polling, map submission, and turn execution.
 *
 * <p>This class wires together network access, shared {@link client.model.GameState} updates, and
 * view/AI observers. Most heavy lifting is delegated to dedicated services.
 */
public class GameManager {
    
    private final client.model.GameState gameState;
    private final NetworkCenter networkCenter;
    private Optional<String> playerId;
    private final WayFinder wayFinder = new WayFinder();
    private final DynamicCLIGameView dynamicView = new DynamicCLIGameView();
    private final GameOutput output;
    private final HalfMapService halfMapService;
    private final GameStateSynchronizer gameStateSynchronizer;
    private final GameStateQueryService gameStateQueryService;
    private final GameLoopService gameLoopService;
    private final FullMapWaitService fullMapWaitService;
    private final PlayerRegistrationService playerRegistrationService;
    private final MoveExecutionService moveExecutionService;
    private final GameVisualizationService visualizationService;
    private final PlayerTurnService playerTurnService;

    public GameManager(client.model.GameState state, String serverBaseUrl, String gameMode, GameOutput output){
        this(state, serverBaseUrl, GameMode.fromCLIValue(gameMode), output);
    }

    private GameManager(client.model.GameState state, String serverBaseUrl, GameMode gameMode, GameOutput output){
        this.gameState = Objects.requireNonNull(state, ControllerTextConfig.REQUIRE_STATE);
        this.networkCenter = new NetworkCenter(Objects.requireNonNull(serverBaseUrl, ControllerTextConfig.REQUIRE_SERVER_BASE_URL), this.gameState.getGameStateID());
        this.output = Objects.requireNonNull(output, ControllerTextConfig.REQUIRE_OUTPUT);
        this.playerId = Optional.empty();

        var cliHandler = GameManagerWiring.createCLIHandler(gameMode);
        GameManagerWiring.wireObservers(state, cliHandler, wayFinder, dynamicView);

        this.halfMapService = new HalfMapService(this.networkCenter, cliHandler, this.output);
        this.gameStateSynchronizer = new GameStateSynchronizer(this.networkCenter);
        this.gameStateQueryService = new GameStateQueryService(this.networkCenter);
        this.fullMapWaitService = new FullMapWaitService(this.gameStateSynchronizer, this.gameStateQueryService);
        this.gameLoopService = new GameLoopService(this, this.output);

        this.playerRegistrationService = new PlayerRegistrationService(this.networkCenter);
        this.moveExecutionService = new MoveExecutionService(this.networkCenter, this.wayFinder, this.output);
        this.visualizationService = new GameVisualizationService(cliHandler, this.dynamicView, this.gameState);
        this.playerTurnService = new PlayerTurnService();
    }

    /**
     * Registers the player on the server and records the returned player id locally.
     */
    public String registerPlayer(String firstName, String lastName, String uAccount) throws GameCommunicationException, GameStateException {
        String registeredPlayerId = playerRegistrationService.registerPlayer(gameState, firstName, lastName, uAccount);
        this.playerId = Optional.of(registeredPlayerId);
        return registeredPlayerId;
    }
    
    /**
     * Generates, validates, and sends the local half-map. Retries generation if validation fails.
     */
    public void generateAndSendHalfMap() throws GameCommunicationException, GameStateException {
        halfMapService.generateAndSendHalfMap(requirePlayerId(), requireGameStateId());
    }

    /**
     * Polls the server for the current snapshot and applies it to the shared game state.
     */
    public void updateGameState() throws GameCommunicationException, MapProcessingException {
        gameStateSynchronizer.synchronize(this.gameState);
    }

    /**
     * Waits until the full map is available, polling periodically.
     *
     * This method does NOT throw as part of normal control flow. It only throws
     * a checked exception when the timeout is exceeded.
     */
    public void waitForFullMap(Duration timeout, Duration pollInterval)
            throws GameCommunicationException, MapProcessingException, FullMapNotAvailableException {
        fullMapWaitService.waitForFullMap(this.gameState, timeout, pollInterval);
    }

    void makeMove(GameMode gameMode) throws GameCommunicationException, AIDecisionException, GameStateException {
        moveExecutionService.makeMove(gameState, gameMode);
    }

    public client.model.PlayerStatus getCurrentPlayerStatus() throws GameStateException {
        return playerTurnService.getCurrentPlayerStatus(gameState, requirePlayerId());
    }

    /**
     * Runs the main game loop until the game ends (won/lost/unhandled state).
     */
    public void startGameLoop(String gameMode) throws GameCommunicationException, GameStateException {
        gameLoopService.startGameLoop(gameMode);
    }

    public client.model.GameState getGameState() {
        return gameState;
    }

    public void setPlayerId(String playerId) {
        this.playerId = ControllerTextConfig.optionalNonBlank(playerId);
    }

    public void visualizeMap(String mapType) {
        visualizationService.visualizeMap(mapType);
    }

    void enableDynamicVisualization() {
        visualizationService.enableDynamicVisualization();
    }

    void disableDynamicVisualization() {
        visualizationService.disableDynamicVisualization();
    }

    private String requirePlayerId() {
        return playerId.filter(id -> !id.isBlank()).orElseThrow(() -> new GameStateException(
                ControllerTextConfig.ERROR_PLAYER_IDENTIFIER_MISSING,
                requireGameStateId(),
                ControllerTextConfig.PLAYER_ID_KEY,
                ControllerTextConfig.MISSING
        ));
    }

    private String requireGameStateId() {
        return ControllerTextConfig.nonBlankOrUnknown(gameState.getGameStateID());
    }

}

