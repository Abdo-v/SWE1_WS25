package client.main;
import client.controller.GameManager;
import client.controller.network.GameIdFetcher;
import client.exception.ConfigurationException;
import client.exception.GameCommunicationException;
import client.view.ClientStartupView;
import client.view.GameManagerView;
import client.view.GameOutput;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;


// please note that most methodes are made
// public to allow easy testing with JUnit 5,
// this is not a good practice in production code

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
            
            // Validate basic arguments with custom exception
            validateBasicArguments(args);
            String gameMode = args[0];
            String serverBaseUrl = args[1];
            String gameId;
            
            // Validate game mode with custom exception
            validateGameMode(gameMode);
            
            // For ATTR mode, fetch game ID automatically; otherwise use provided game ID
            if ("ATTR".equals(gameMode)) {
                view.showAutoFetchGameIdStart();
                gameId = GameIdFetcher.fetchGameId(serverBaseUrl);
                view.showAutoFetchGameIdResult(gameId);
            } else {
                if (args.length < 3) {
                    throw new ConfigurationException(
                        "Game ID required for " + gameMode + " mode. Expected: <gameMode> <serverBaseUrl> <gameId>",
                        "arguments",
                        "count=" + args.length,
                        new String[]{"gameMode", "serverBaseUrl", "gameId"}
                    );
                }
                gameId = args[2];
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
            }
            // logger.info("Game state ready, player status: {}", gameManager.getCurrentPlayerStatus());

            view.showGeneratingAndSendingHalfMap();
            // logger.info("Starting half map generation and transmission");
            gameManager.generateAndSendHalfMap();
            // logger.info("Half map successfully generated and sent");

            // logger.debug("Updating game state after half map submission");
			gameManager.updateGameState();
			// logger.debug("Visualizing full map");
			gameManager.visualizeMap("full");

            view.showStartingMainGameLoop();
			// logger.info("Entering main game loop with mode: {}", gameMode);
			gameManager.startGameLoop(gameMode);
			// logger.info("Game loop completed");

        } catch (ConfigurationException e) {
            // Handle configuration errors with user-friendly messages
            view.showConfigurationError(e.getHelpMessage());
            // logger.error("Configuration error: {}", e.getMessage(), e);
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

    /**
     * Validates basic command line arguments.
     * 
     * @param args Command line arguments
     * @throws ConfigurationException if arguments are insufficient or invalid
     */
    public static void validateBasicArguments(String[] args) throws ConfigurationException {
        if (args.length < 2) {
            throw new ConfigurationException(
                "Insufficient arguments provided. Expected: <gameMode> <serverBaseUrl>",
                "arguments",
                "count=" + args.length,
                new String[]{"gameMode", "serverBaseUrl", "[options...]"}
            );
        }
    }



    /**
     * Validates the game mode parameter.
     * 
     * @param gameMode The game mode to validate
     * @throws ConfigurationException if the game mode is invalid
     */
    public static void validateGameMode(String gameMode) throws ConfigurationException {
        if (gameMode == null || gameMode.trim().isEmpty()) {
            throw new ConfigurationException(
                "Game mode cannot be null or empty",
                "gameMode",
                gameMode,
                new String[]{"TR", "TRR", "ATTR"}
            );
        }
        
        String[] validModes = {"TR", "TRR", "ATTR"};
        boolean isValid = false;
        for (String validMode : validModes) {
            if (validMode.equals(gameMode)) {
                isValid = true;
                break;
            }
        }
        
        if (!isValid) {
            throw new ConfigurationException(
                "Invalid game mode provided",
                "gameMode",
                gameMode,
                validModes
            );
        }
    }

    /**
     * Parses command line arguments for logging configuration and sets system properties
     * that will be used by logback.xml
     */
    /* LOGGING DISABLED
    public static void parseLoggingArguments(String[] args) throws ConfigurationException {
        for (String arg : args) {
            if (arg.startsWith("--log-level=")) {
                String level = arg.substring("--log-level=".length()).toUpperCase();
                validateLogLevel(level, "log-level");
                System.setProperty("ROOT_LOG_LEVEL", level);
                System.setProperty("CLIENT_LOG_LEVEL", level);
                System.out.println("🔧 Root log level set to: " + level);
            } else if (arg.startsWith("--ai-log=")) {
                String level = arg.substring("--ai-log=".length()).toUpperCase();
                validateLogLevel(level, "ai-log");
                System.setProperty("AI_LOG_LEVEL", level);
                System.out.println("🤖 AI log level set to: " + level);
            } else if (arg.startsWith("--network-log=")) {
                String level = arg.substring("--network-log=".length()).toUpperCase();
                validateLogLevel(level, "network-log");
                System.setProperty("NETWORK_LOG_LEVEL", level);
                System.out.println("📡 Network log level set to: " + level);
            } else if (arg.startsWith("--game-log=")) {
                String level = arg.substring("--game-log=".length()).toUpperCase();
                validateLogLevel(level, "game-log");
                System.setProperty("GAME_LOG_LEVEL", level);
                System.out.println("🎮 Game log level set to: " + level);
            } else if (arg.startsWith("--log-file=")) {
                String filePath = arg.substring("--log-file=".length());
                validateLogFilePath(filePath);
                System.setProperty("LOG_FILE", filePath);
                System.setProperty("FILE_LOGGING", "true");
                System.out.println("📁 Log file set to: " + filePath);
            } else if (arg.equals("--file-log")) {
                System.setProperty("FILE_LOGGING", "true");
                System.out.println("📁 File logging enabled (logs/game-client.log)");
            } else if (arg.equals("--no-console")) {
                System.setProperty("CONSOLE_LOGGING", "false");
                System.out.println("🔇 Console logging disabled");
            } else if (arg.equals("--no-file")) {
                System.setProperty("FILE_LOGGING", "false");
                System.out.println("📁 File logging disabled");
            } else if (arg.equals("--debug-ai")) {
                System.setProperty("AI_LOG_LEVEL", "DEBUG");
                System.setProperty("FILE_LOGGING", "true");
                System.out.println("🤖 AI debug mode enabled (DEBUG level + file logging)");
            } else if (arg.equals("--trace-ai")) {
                System.setProperty("AI_LOG_LEVEL", "TRACE");
                System.setProperty("FILE_LOGGING", "true");
                System.out.println("🤖 AI trace mode enabled (TRACE level + file logging)");
            } else if (arg.equals("--help-logging")) {
                printLoggingHelp();
                System.exit(0);
            }
        }
        
        // Create logs directory if file logging is enabled
        if ("true".equals(System.getProperty("FILE_LOGGING"))) {
            try {
                java.nio.file.Files.createDirectories(java.nio.file.Paths.get("logs"));
            } catch (Exception e) {
                throw new ConfigurationException(
                    "Could not create logs directory: " + e.getMessage(),
                    e,
                    "FILE_LOGGING",
                    "true",
                    new String[]{"Ensure write permissions", "Check disk space"}
                );
            }
        }
    }
    */

    /**
     * Validates log level values.
     * 
     * @param level The log level to validate
     * @param parameterName The name of the parameter for error reporting
     * @throws ConfigurationException if the log level is invalid
     */
    /* LOGGING DISABLED
    public static void validateLogLevel(String level, String parameterName) throws ConfigurationException {
        String[] validLevels = {"TRACE", "DEBUG", "INFO", "WARN", "ERROR"};
        for (String validLevel : validLevels) {
            if (validLevel.equals(level)) {
                return; // Valid level found
            }
        }
        
        throw new ConfigurationException(
            "Invalid log level provided",
            parameterName,
            level,
            validLevels
        );
    }
    */

    /**
     * Validates log file path.
     * 
     * @param filePath The file path to validate
     * @throws ConfigurationException if the file path is invalid
     */
    /* LOGGING DISABLED
    public static void validateLogFilePath(String filePath) throws ConfigurationException {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new ConfigurationException(
                "Log file path cannot be null or empty",
                "log-file",
                filePath,
                new String[]{"logs/my-game.log", "debug.log", "/path/to/game.log"}
            );
        }
        
        // Check if the parent directory can be created
        java.io.File file = new java.io.File(filePath);
        java.io.File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            try {
                if (!parentDir.mkdirs()) {
                    throw new ConfigurationException(
                        "Cannot create parent directories for log file",
                        "log-file",
                        filePath,
                        new String[]{"Use existing directory", "Check write permissions"}
                    );
                }
            } catch (SecurityException e) {
                throw new ConfigurationException(
                    "Permission denied creating log file directory: " + e.getMessage(),
                    e,
                    "log-file",
                    filePath,
                    new String[]{"Check file permissions", "Use different directory"}
                );
            }
        }
    }
    */

    /**
     * Prints detailed help for logging configuration options
     */
    /* LOGGING DISABLED
    public static void printLoggingHelp() {
        System.out.println("📋 LOGGING CONFIGURATION HELP");
        System.out.println("===============================");
        System.out.println();
        System.out.println("🎯 BASIC OPTIONS:");
        System.out.println("  --log-level=LEVEL     Set overall log level (TRACE, DEBUG, INFO, WARN, ERROR)");
        System.out.println("  --file-log            Enable logging to file (logs/game-client.log)");
        System.out.println("  --no-console          Disable console logging");
        System.out.println("  --log-file=PATH       Set custom log file path");
        System.out.println();
        System.out.println("🎯 COMPONENT-SPECIFIC LEVELS:");
        System.out.println("  --ai-log=LEVEL        Set AI components log level");
        System.out.println("  --network-log=LEVEL   Set network communication log level");
        System.out.println("  --game-log=LEVEL      Set game controller log level");
        System.out.println();
        System.out.println("🎯 QUICK PRESETS:");
        System.out.println("  --debug-ai            Enable AI debugging (DEBUG + file logging)");
        System.out.println("  --trace-ai            Enable AI tracing (TRACE + file logging)");
        System.out.println();
        System.out.println("🎯 EXAMPLES:");
        System.out.println("  # Basic file logging:");
        System.out.println("  java MainClient TR http://localhost:8080 Ew26i --file-log");
        System.out.println();
        System.out.println("  # Debug AI with custom log file:");
        System.out.println("  java MainClient TR http://localhost:8080 Ew26i --trace-ai --log-file=ai-debug.log");
        System.out.println();
        System.out.println("  # Only file logging, no console:");
        System.out.println("  java MainClient TR http://localhost:8080 Ew26i --file-log --no-console");
        System.out.println();
        System.out.println("  # Different levels for different components:");
        System.out.println("  java MainClient TR http://localhost:8080 Ew26i --ai-log=TRACE --network-log=DEBUG --game-log=INFO --file-log");
    }
    */
}
