package client.controller.network.service;

import client.controller.ControllerTextConfig;
import client.model.Direction;
import messagesbase.messagesfromclient.EMove;

/**
 * Converts client/internal movement direction to network (messagesbase) move.
 */
class ClientToServerMoveConverter {

    public EMove convert(Direction direction) {
        if (direction == Direction.UP) {
            return EMove.Up;
        } else if (direction == Direction.DOWN) {
            return EMove.Down;
        } else if (direction == Direction.LEFT) {
            return EMove.Left;
        } else if (direction == Direction.RIGHT) {
            return EMove.Right;
        } else {
            throw new IllegalArgumentException(ControllerTextConfig.ERROR_UNSUPPORTED_DIRECTION_PREFIX + direction);
        }
    }
}
