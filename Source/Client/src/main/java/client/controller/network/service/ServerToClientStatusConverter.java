package client.controller.network.service;

import client.model.PlayerStatus;
import messagesbase.messagesfromserver.EPlayerGameState;

import java.util.Objects;

/**
 * Converts network (messagesbase) player game state to client/internal player status.
 */
public class ServerToClientStatusConverter {

    public PlayerStatus convert(EPlayerGameState serverStatus) {
        Objects.requireNonNull(serverStatus, "Server status must be provided");
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
                throw new IllegalArgumentException("Unknown server status: " + serverStatus);
        }
    }
}
