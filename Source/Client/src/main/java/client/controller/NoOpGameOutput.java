package client.controller;

import client.model.Direction;
import client.model.PlayerState;
import client.model.PlayerStatus;
import client.view.GameOutput;

/**
 * No-op implementation for {@link GameOutput}.
 * Used to keep controller code free of missing-value checks.
 */
final class NoOpGameOutput implements GameOutput {

    @Override
    public void showMapValidationFailed(String errorMessages) {
    }

    @Override
    public void showMapValidationOk() {
    }

    @Override
    public void showDynamicModeStarting() {
    }

    @Override
    public void showMoveSent(Direction direction) {
    }

    @Override
    public void showPosition(String positionText) {
    }

    @Override
    public void showAiError(String message) {
    }

    @Override
    public void showNetworkError(String message) {
    }

    @Override
    public void showWon(PlayerState playerState, int loops) {
    }

    @Override
    public void showLost(PlayerState playerState, int loops) {
    }

    @Override
    public void showUnhandledStatus(PlayerStatus status) {
    }

    @Override
    public void showLoops(int loops) {
    }

    @Override
    public void showMapError(String recoveryMessage) {
    }
}
