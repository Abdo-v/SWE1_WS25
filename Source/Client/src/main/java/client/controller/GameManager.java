package client.controller;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
import client.exception.GameCommunicationException;
import client.exception.GameStateException;
import client.exception.AIDecisionException;
import client.exception.MapProcessingException;
import client.model.*;
import client.model.ai.WayFinder;
import client.view.*;
import client.controller.network.service.NetworkCenter;
import client.model.mapper.GameMap;
import client.model.mapper.MapGenerator;
import client.model.mapper.MapValidator;
import client.model.mapper.PlayerHalfMap;
import client.model.common.Notification;
import messagesbase.UniquePlayerIdentifier;

public class GameManager {
    // private static final Logger logger = LoggerFactory.getLogger(GameManager.class);
    
    private client.model.GameState gameState;
    private NetworkCenter networkCenter;
    private String playerId;
    private GameMap gameMap;
    private WayFinder wayFinder = new WayFinder();
    private MapValidator mapValidator = new MapValidator();
    private CLIHandler cliHandler;
    private DynamicCLIGameView dynamicView = new DynamicCLIGameView();

    private static final String GREEN = "\u001B[32m";
    private static final String RESET = "\u001B[0m";

    public GameManager(client.model.GameState state, String serverBaseUrl, String gameMode){
        // // logger.debug("Creating GameManager with gameId: {}, server: {}, mode: {}", state.getGameStateID(), serverBaseUrl, gameMode);
        this.gameState = state;
        this.networkCenter = new NetworkCenter(serverBaseUrl, state.getGameStateID());
        this.gameMap = state.getMap();
        this.cliHandler = new CLIHandler(gameMode);
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
        // // logger.debug("Creating GameManager with gameId: {}, server: {}, playerId: {}", gameId, serverBaseUrl, playerId.getUniquePlayerID());
        this.networkCenter = new NetworkCenter(serverBaseUrl, gameId, playerId);
        this.gameState = new client.model.GameState(gameId);
    }

    /**
     * Constructs a new GameManager with the given game ID and server URL.
     * @param gameId The ID of the game.
     * @param serverBaseUrl The base URL of the server.
     */
    public GameManager(String gameId, String serverBaseUrl) {
        // // logger.debug("Creating GameManager with gameId: {}, server: {}", gameId, serverBaseUrl);
        this.networkCenter = new NetworkCenter(serverBaseUrl, gameId);
        this.gameState = new client.model.GameState(gameId);
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
        // // logger.debug("Generating and sending half map for player ID: {}", playerId);
        
        if (playerId == null) {
            throw new GameStateException(
                "Cannot generate half map: player ID is not set",
                gameState != null ? gameState.getGameStateID() : "unknown",
                "GENERATE_HALF_MAP",
                "no_player_id"
            );
        }
        
        try {
            int width = 10;
            int height = 5;
            PlayerHalfMap halfMapToSend = generateHalfMap(width, height, playerId);
            // // logger.trace("Generated half map: {}", halfMapToSend.toString());
            validateHalfMap(halfMapToSend);
            sendHalfMap(halfMapToSend);
            // // logger.info("Half map sent successfully for player ID: {}", playerId);
        } catch (GameCommunicationException e) {
            throw e; // Re-throw communication exceptions
        } catch (Exception e) {
            throw new GameStateException(
                "Failed to generate or send half map: " + e.getMessage(),
                e,
                gameState.getGameStateID(),
                "GENERATE_HALF_MAP",
                "error",
                null
            );
        }
    }

    private void validateHalfMap(PlayerHalfMap halfMap) {
        // // logger.debug("Validating half map");
        Notification validation = mapValidator.validate(halfMap);
        if (validation.hasErrors()) {
            // // logger.error("Map validation failed: {}", validation.getErrorMessages());
            System.err.println("❌ Map validation failed:");
            System.err.println(validation.getErrorMessages());
            throw new IllegalStateException("Generated map is invalid: " + validation.getErrorMessages());
        } else {
            // // logger.info("Map validation successful");
            // print in green all is ok
            System.out.println(GREEN + "✅ Map validation found no errors, sending map..." + RESET);
        }
    }

    /**
     * Sends the player's half map to the server.
     * @param halfMap The half map to send.
     * @throws GameCommunicationException If sending half map fails due to network issues.
     */
    private void sendHalfMap(PlayerHalfMap halfMap) throws GameCommunicationException {
        try {
            // // logger.debug("Sending half map to server");
            networkCenter.sendHalfMap(halfMap);
            // // logger.debug("Half map successfully transmitted to server");
        } catch (Exception e) {
            throw new GameCommunicationException(
                "Failed to send half map to server: " + e.getMessage(),
                e,
                networkCenter != null ? "unknown" : "no_network",
                "SEND_HALF_MAP",
                -1
            );
        }
    }

    /**
     * Generates a half map for the player with the specified dimensions.
     * @param width The width of the half map.
     * @param height The height of the half map.
     * @param playerId The ID of the player.
     * @return The generated PlayerHalfMap.
     */
    private PlayerHalfMap generateHalfMap(int width, int height, String playerId) {
        // // logger.debug("Generating half map with dimensions {}x{} for player: {}", width, height, playerId);
        MapGenerator generator = new MapGenerator();
        PlayerHalfMap halfMapToSend = generator.generateMap(width, height, playerId);
        cliHandler.printHalfMap(halfMapToSend, "own"); 
        // // logger.debug("Half map generation completed");
        return halfMapToSend;
    }

    /**
     * Checks if the full map is available from the server.
     * @return true if the full map is available, false otherwise.
     * @throws GameCommunicationException If polling fails due to network issues.
     */
    public boolean fullMapAvailable() throws GameCommunicationException {
        try {
            // // logger.trace("Checking if full map is available");
            messagesbase.messagesfromserver.GameState serverGameState = networkCenter.pollGameState();
            boolean available = serverGameState.getMap() != null && serverGameState.getMap().getMapNodes().size() == 100;
            // // logger.debug("Full map availability check: {}", available);
            return available;
        } catch (Exception e) {
            throw new GameCommunicationException(
                "Failed to check full map availability: " + e.getMessage(),
                e,
                networkCenter != null ? "unknown" : "no_network",
                "FULL_MAP_CHECK",
                -1
            );
        }
    }

    /**
     * Updates the game state with the latest data from the server.
     * @throws GameCommunicationException If polling fails due to network issues.
     * @throws MapProcessingException If the received data cannot be processed.
     */
    public void updateGameState() throws GameCommunicationException, MapProcessingException {
        try {
            // // logger.trace("Updating game state from server");
            long startPollGameState = System.nanoTime();
            messagesbase.messagesfromserver.GameState serverState = networkCenter.pollGameState();
            long polt = (System.nanoTime() - startPollGameState) / 1_000_000;
            
            // Validate server state before processing
            if (serverState == null) {
                throw new MapProcessingException(
                    "Received null game state from server",
                    "GameState",
                    "server_response_validation"
                );
            }
            
            GameState polledState = networkCenter.convertServerGamestate(serverState);
            
            // Validate converted state before updating
            if (polledState == null) {
                throw new MapProcessingException(
                    "Failed to convert server game state to client format",
                    "GameState",
                    "state_conversion"
                );
            }
            
            this.gameState.updateGameState(polledState);
            // // logger.trace("Game state update completed (poll: {}ms)", polt);
            
        } catch (GameCommunicationException | MapProcessingException e) {
            throw e; // Re-throw our custom exceptions
        } catch (Exception e) {
            // // logger.error("Unexpected error updating game state: {}", e.getMessage(), e);
            throw new MapProcessingException(
                "Unexpected error during game state update: " + e.getMessage(),
                e,
                "GameState",
                "update_process",
                -1,
                -1,
                null
            );
        }
    }

    /**
     * Polls the server for the current game state.
     * @return The current game state from the server.
     * @throws GameCommunicationException If polling fails due to network issues.
     */
    public messagesbase.messagesfromserver.GameState managerpollGameState() throws GameCommunicationException {
        try {
            // // logger.trace("Polling server for current game state");
            return networkCenter.pollGameState();
        } catch (Exception e) {
            throw new GameCommunicationException(
                "Failed to poll game state from server: " + e.getMessage(),
                e,
                networkCenter != null ? "unknown" : "no_network",
                "POLL_GAME_STATE",
                -1
            );
        }
    }

    /**
     * Polls the server for the current player's status.
     * @return The player's game state.
     * @throws GameCommunicationException If polling fails due to network issues.
     * @throws GameStateException If the player is not found in the game state.
     */
    public messagesbase.messagesfromserver.EPlayerGameState pollMyStatus() throws GameCommunicationException, GameStateException {
        // // logger.trace("Polling player status for player: {}", playerId);
        
        try {
            messagesbase.messagesfromserver.GameState serverGameState = managerpollGameState();
            
            for (messagesbase.messagesfromserver.PlayerState playerState : serverGameState.getPlayers()) {
                if (playerState.getUniquePlayerID().equals(playerId)) {
                    // // logger.trace("Player {} status: {}", playerId, playerState.getState());
                    return playerState.getState();
                }
            }
            
            // Player not found in server state
            throw new GameStateException(
                "Player not found in server game state",
                gameState.getGameStateID(),
                "POLL_PLAYER_STATUS",
                "player_not_found",
                "player_present"
            );
            
        } catch (GameCommunicationException e) {
            throw e; // Re-throw communication exceptions
        } catch (GameStateException e) {
            throw e; // Re-throw game state exceptions
        } catch (Exception e) {
            throw new GameStateException(
                "Unexpected error polling player status: " + e.getMessage(),
                e,
                gameState.getGameStateID(),
                "POLL_PLAYER_STATUS",
                "error",
                null
            );
        }
    }

    /**
     * Makes a move using the WayFinder and sends it to the server.
     * @throws GameCommunicationException If sending the move fails due to network issues.
     * @throws AIDecisionException If the AI fails to determine a valid move.
     * @throws GameStateException If the game state is invalid for making moves.
     */
    public void makeMove(String gameMode) throws GameCommunicationException, AIDecisionException, GameStateException {
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
                    
                    if (gameMode.equals("TRR")) {
                        // // logger.info("Player {} (TRR mode) moved to: {}", playerId, gameState.getCurrentPlayerState().getCurrentPosition().printCoordinates());
                        System.out.print("Move " + nextMoveDirection.name() + " sent to server, ");
                    }
                } catch (GameCommunicationException e) {
                    throw e; // Re-throw communication exceptions
                } catch (Exception e) {
                    // // logger.error("Error making move for player {}: {}", playerId, e.getMessage(), e);
                    throw new GameCommunicationException(
                        "Failed to send move to server: " + e.getMessage(),
                        e,
                        networkCenter != null ? "unknown" : "no_network",
                        "SEND_MOVE",
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
                "MAKE_MOVE",
                "error",
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
        boolean dynamicMode = "TR".equals(gameMode) || "ATTR".equals(gameMode);
        if (dynamicMode) {
            // // logger.info("Starting game with dynamic visualization (TR/ATTR mode)");
            System.out.println("\n🎮 Starting game with dynamic visualization...");
            try {
                Thread.sleep(1000);
                enableDynamicVisualization();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                // // logger.warn("Game loop initialization interrupted");
            }
        }
        
        // // logger.info("Entering main game loop with mode: {}", gameMode);
        int loops = 0;
        boolean gameIsRunning = true;
        client.model.PlayerStatus currentStatus;
        
        while (gameIsRunning) {
            try {
                boolean acted = false;
                updateGameState();
                currentStatus = getCurrentPlayerStatus();
                // // logger.trace("Game loop iteration: {}, Player status: {}", loops, currentStatus);
                
                switch (currentStatus) {
                    case MUST_WAIT:
                        // // logger.debug("Player {} must wait.", playerId);
                        break;
                    case MUST_ACT:
                        // // logger.debug("Player {} must act.", playerId);
                        try {
                            makeMove(gameMode);
                            if(gameMode.equals("TRR")) {
                                System.out.print(gameState.getCurrentPlayerState().getCurrentPosition().printCoordinates());
                            }
                            acted = true;
                        } catch (AIDecisionException e) {
                            // // logger.error("AI Decision Error for player {}: {}", playerId, e.getMessage());
                            System.err.println("🤖 AI Error: " + e.getMessage());
                            // Continue game loop - AI errors shouldn't terminate the game
                        } catch (GameCommunicationException e) {
                            // // logger.error("Communication error during move for player {}: {}", playerId, e.getMessage());
                            System.err.println("🌐 Network Error: " + e.getMessage());
                            if (!e.isRecoverable()) {
                                throw e; // Fatal communication error
                            }
                            // For recoverable errors, continue and try again next loop
                        }
                        break;
                    case WON:
                        disableDynamicVisualization();
                        // // logger.info("PLAYER {} WON THE GAME! Loops: {}", playerId, loops);
                        System.out.println("🎉🎉🎉======= = = YOU WON! = = =======🎉🎉🎉");
                        // // logger.info("Final player state: {}", gameState.getCurrentPlayerState().toString());
                        System.out.println(gameState.getCurrentPlayerState().toString());
                        if (dynamicMode) System.out.println(" loops: " + loops);
                        gameIsRunning = false;
                        break;
                    case LOST:
                        disableDynamicVisualization();
                        // // logger.warn("PLAYER {} LOST THE GAME. Loops: {}", playerId, loops);
                        System.out.println("💀💀💀======= = = YOU LOST! = = =======💀💀💀");
                        // // logger.info("Final player state: {}", gameState.getCurrentPlayerState().toString());
                        System.out.println(gameState.getCurrentPlayerState().toString());
                        if (dynamicMode) System.out.println(" loops: " + loops);
                        gameIsRunning = false;
                        break;
                    default:
                        disableDynamicVisualization();
                        // // logger.error("Unhandled player state: {}. Exiting game.", currentStatus);
                        System.err.println("Unhandled player state: " + currentStatus + ". Exiting game.");
                        gameIsRunning = false;
                        break;
                }
                
                loops++;
                if (acted) {
                    // // logger.trace("Loop {} completed with action", loops);
                } else {
                    // // logger.trace("Loop {} completed (no action taken)", loops);
                }
                if(gameMode.equals("TRR")) {
                    // // logger.debug("Loop {} completed (TRR mode).", loops);
                    System.out.println(" loops: " + loops);
                }
                
            } catch (MapProcessingException e) {
                // // logger.error("Map processing error in game loop: {}", e.getMessage(), e);
                System.err.println("🗺️ Map Error: " + e.getRecoveryMessage());
                if (!e.isRecoverable()) {
                    throw new GameStateException(
                        "Fatal map processing error: " + e.getMessage(),
                        e,
                        gameState.getGameStateID(),
                        "GAME_LOOP",
                        "map_error",
                        null
                    );
                }
                // For recoverable map errors, continue and try again
            } catch (GameCommunicationException | GameStateException e) {
                throw e; // Re-throw fatal exceptions
            } catch (Exception e) {
                // // logger.error("Unexpected error in game loop: {}", e.getMessage(), e);
                throw new GameStateException(
                    "Unexpected error in game loop: " + e.getMessage(),
                    e,
                    gameState.getGameStateID(),
                    "GAME_LOOP",
                    "unexpected_error",
                    null
                );
            }
        }
        
        disableDynamicVisualization();
        // // logger.info("Game loop completed after {} iterations", loops);
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
            // // logger.trace("Checking if server map is empty");
            messagesbase.messagesfromserver.GameState serverGameState = networkCenter.pollGameState();
            if (serverGameState.getMap() != null && serverGameState.getMap().getMapNodes().size() == 0) {
                // // logger.debug("Server map is empty");
                return true;
            }
        } catch (Exception e) {
            throw new GameCommunicationException(
                "Failed to check if server map is empty: " + e.getMessage(),
                e,
                networkCenter != null ? "unknown" : "no_network",
                "CHECK_SERVER_MAP_EMPTY",
                -1
            );
        }
        return false;
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
