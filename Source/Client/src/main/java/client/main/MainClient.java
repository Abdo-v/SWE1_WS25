package client.main;
import client.controller.PollingDefaults;
import client.controller.GameManager;
import client.controller.network.GameIdFetcher;
import client.exception.ConfigurationException;
import client.exception.FullMapNotAvailableException;
import client.exception.GameCommunicationException;
import client.view.ClientStartupView;
import client.view.GameManagerView;
import client.view.GameOutput;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;


public class MainClient {
    // private static final Logger logger = LoggerFactory.getLogger(MainClient.class);

    public static void main(String[] args) {
        ClientStartupView view = new ClientStartupView();
        GameOutput gameOutput = new GameManagerView();
        try {
            // Set default logging configuration - file only, no console
            // System.setProperty("CONSOLE_LOGGING", "false");
            // System.setProperty("FILE_LOGGING", "true");
            
            // Parse command line arguments for logging configuration
            // parseLoggingArguments(args);
            
            // logger.info("Application starting...");
            
            StartupArguments startup = StartupArgumentsParser.parse(args);
            String gameMode = startup.gameMode();
            String serverBaseUrl = startup.serverBaseUrl();
            String gameId;

            // For ATTR mode, fetch game ID automatically; otherwise use provided game ID
            if (startup.autoFetchGameId()) {
                view.showAutoFetchGameIdStart();
                gameId = GameIdFetcher.fetchGameId(serverBaseUrl);
                view.showAutoFetchGameIdResult(gameId);
            } else {
                gameId = startup.gameId().orElseThrow(() -> new IllegalStateException(
                        "gameId must be present for mode " + gameMode
                ));
            }
            // logger.info("Game client configuration - Mode: {}, Server: {}, GameID: {}", gameMode, serverBaseUrl, gameId);

            // Inform user about logging configuration
            // System.out.println("📋 LOGGING INFO: File logging enabled by default → logs/game-client.log");
            // System.out.println("   Common options: -DCONSOLE_LOGGING=true (CLI only) | --no-file (no logging) | -DCONSOLE_LOGGING=true --file-log (CLI+file)");
            // System.out.println();
            
            view.showStartupBanner(serverBaseUrl, gameId, gameMode);

            // logger.debug("Creating shared game state with gameId: {}", gameId);
            client.model.GameState sharedGameState = new client.model.GameState(gameId);
            GameManager gameManager = new GameManager(sharedGameState, serverBaseUrl, gameMode, gameOutput);
            // logger.debug("GameManager initialized successfully");

            view.showRegisteringPlayer();
            // logger.info("Attempting to register player: Abdalrahman Mohammed (abdalrahmm77)");
            String playerId = gameManager.registerPlayer(ClientDefaults.PLAYER_FIRST_NAME, ClientDefaults.PLAYER_LAST_NAME, ClientDefaults.PLAYER_UACCOUNT);
            gameManager.setPlayerId(playerId);
            view.showPlayerRegistered(playerId);
            // logger.info("Player registration successful with ID: {}", playerId);

            // logger.debug("Updating initial game state after player registration");
            gameManager.updateGameState();

            // logger.debug("Waiting for game state to become ready (player status != MUST_WAIT)");
            while (gameManager.getCurrentPlayerStatus() == client.model.PlayerStatus.MUST_WAIT) {
                view.showWaitingForGameStateAfterRegister();
                // logger.trace("Player status is MUST_WAIT, continuing to poll...");
                gameManager.updateGameState();

                try {
                    Thread.sleep(PollingDefaults.MIN_POLL_INTERVAL.toMillis());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            // logger.info("Game state ready, player status: {}", gameManager.getCurrentPlayerStatus());

            view.showGeneratingAndSendingHalfMap();
            // logger.info("Starting half map generation and transmission");
            gameManager.generateAndSendHalfMap();
            // logger.info("Half map successfully generated and sent");


            view.showWaitingForFullMap();
            gameManager.waitForFullMap(PollingDefaults.FULL_MAP_WAIT_TIMEOUT, PollingDefaults.DEFAULT_POLL_INTERVAL);

			// logger.debug("Visualizing full map");
			gameManager.visualizeMap("full");

            view.showStartingMainGameLoop();
			// logger.info("Entering main game loop with mode: {}", gameMode);
			gameManager.startGameLoop(gameMode);
			// logger.info("Game loop completed");

        } catch (ConfigurationException e) {
            // Handle configuration errors with user-friendly messages
            view.showConfigurationError(e);
            // logger.error("Configuration error: {}", e.getMessage(), e);
            System.exit(1);
        } catch (FullMapNotAvailableException e) {
            view.showFullMapNotAvailable(e);
            System.exit(1);
        } catch (GameCommunicationException e) {
            // Handle communication errors with retry suggestions
            view.showCommunicationError(e);
            // logger.error("Communication error: {}", e.getMessage(), e);
            System.exit(1);
        } catch (Exception e) {
            view.showUnexpectedError(e);
            // logger.error("Critical error during game execution: {}", e.getMessage(), e);
            System.exit(1);
        }
        
        // logger.info("Application shutting down");
    }

}
