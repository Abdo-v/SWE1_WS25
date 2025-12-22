package client.view;

import client.model.Direction;
import client.model.PlayerState;
import client.model.PlayerStatus;

public interface GameOutput {

    void showMapValidationFailed(String errorMessages);

    void showMapValidationOk();

    void showDynamicModeStarting();

    void showMoveSent(Direction direction);

    void showPosition(String positionText);

    void showAiError(String message);

    void showNetworkError(String message);

    void showWon(PlayerState playerState, int loops, boolean showLoops);

    void showLost(PlayerState playerState, int loops, boolean showLoops);

    void showUnhandledStatus(PlayerStatus status);

    void showLoops(int loops);

    void showMapError(String recoveryMessage);
}
