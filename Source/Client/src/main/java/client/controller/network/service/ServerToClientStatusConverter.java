package client.controller.network.service;

import client.controller.ControllerTextConfig;
import client.model.PlayerStatus;
import messagesbase.messagesfromserver.EPlayerGameState;

import java.util.Objects;

/**
 * Converts network (messagesbase) player game state to client/internal player status.
 */
class ServerToClientStatusConverter {

    public PlayerStatus convert(EPlayerGameState serverStatus) {
        Objects.requireNonNull(serverStatus, ControllerTextConfig.ERROR_SERVER_STATUS_REQUIRED);
        switch (serverStatus) {
            case MustAct:
                return PlayerStatus.MUST_ACT;
            case MustWait:
                return PlayerStatus.MUST_WAIT;
            case Lost:
                return PlayerStatus.LOST;
            case Won:
                return PlayerStatus.WON;
            default:
                throw new IllegalArgumentException(ControllerTextConfig.ERROR_UNKNOWN_SERVER_STATUS_PREFIX + serverStatus);
        }
    }
}
