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
import java.time.Duration;

/**
 * Application entry point.
 *
 * <p>Bootstraps the client from CLI arguments, performs the initial handshake
 * (register player, send half-map, wait for full map), and then hands control to the game loop.
 *
 * <p>In {@code ATTR} mode, the game id is fetched from the server; in other modes it must be
 * provided as a third argument.
 */
public class MainClient {

    public static void main(String[] args) {
        ClientStartupView view = new ClientStartupView();
        GameOutput gameOutput = new GameManagerView();
        try {
            
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
                        MainTextConfig.ERROR_GAME_ID_MUST_BE_PRESENT_PREFIX + gameMode
                ));
            }
            
            view.showStartupBanner(serverBaseUrl, gameId, gameMode);

            client.model.GameState sharedGameState = new client.model.GameState(gameId);
            GameManager gameManager = new GameManager(sharedGameState, serverBaseUrl, gameMode, gameOutput);

            view.showRegisteringPlayer();
            String playerId = gameManager.registerPlayer(ClientDefaults.PLAYER_FIRST_NAME, ClientDefaults.PLAYER_LAST_NAME, ClientDefaults.PLAYER_UACCOUNT);
            gameManager.setPlayerId(playerId);
            view.showPlayerRegistered(playerId);

            gameManager.updateGameState();

            while (gameManager.getCurrentPlayerStatus() == client.model.PlayerStatus.MUST_WAIT) {
                view.showWaitingForGameStateAfterRegister();
                gameManager.updateGameState();

                if (!sleepFor(PollingDefaults.MIN_POLL_INTERVAL)) {
                    break;
                }
            }

            view.showGeneratingAndSendingHalfMap();
            gameManager.generateAndSendHalfMap();


            view.showWaitingForFullMap();
            gameManager.waitForFullMap(PollingDefaults.FULL_MAP_WAIT_TIMEOUT, PollingDefaults.DEFAULT_POLL_INTERVAL);

			gameManager.visualizeMap("full");

            view.showStartingMainGameLoop();

			gameManager.startGameLoop(gameMode);

        } catch (ConfigurationException e) {
            // Handle configuration errors with user-friendly messages
            view.showConfigurationError(e);
            System.exit(1);
        } catch (FullMapNotAvailableException e) {
            view.showFullMapNotAvailable(e);
            System.exit(1);
        } catch (GameCommunicationException e) {
            // Handle communication errors with retry suggestions
            view.showCommunicationError(e);
            System.exit(1);
        } catch (Exception e) {
            view.showUnexpectedError(e);
            System.exit(1);
        }
        
    }

    private static boolean sleepFor(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

}
