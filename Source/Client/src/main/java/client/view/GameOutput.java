package client.view;

import client.model.Direction;
import client.model.PlayerState;
import client.model.PlayerStatus;

/**
 * UI boundary for presenting game progress, errors, and end-of-game screens.
 *
 * <p>Controllers/services should depend on this interface rather than printing directly,
 * allowing different frontends (CLI, tests, no-op output).
 */
public interface GameOutput {

    void showMapValidationFailed(String errorMessages);

    void showMapValidationOk();

    void showDynamicModeStarting();

    void showMoveSent(Direction direction);

    void showPosition(String positionText);

    void showAiError(String message);

    void showNetworkError(String message);

    void showWon(PlayerState playerState, int loops);

    void showLost(PlayerState playerState, int loops);

    void showUnhandledStatus(PlayerStatus status);

    void showLoops(int loops);

    void showMapError(String recoveryMessage);
}
