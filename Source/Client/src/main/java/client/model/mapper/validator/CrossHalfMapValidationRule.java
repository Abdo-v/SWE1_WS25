package client.model.mapper.validator;

import client.model.common.Notification;
import client.model.mapper.PlayerHalfMap;

import java.util.Objects;

/**
 * A single rule that validates compatibility between a newly generated half-map
 * and an already existing half-map.
 */
interface CrossHalfMapValidationRule {

    void validate(PlayerHalfMap newHalfMap,
                  PlayerHalfMap existingHalfMap,
                  CrossHalfMapValidationContext context,
                  Notification notification);

    default void requireArgs(PlayerHalfMap newHalfMap,
                            PlayerHalfMap existingHalfMap,
                            CrossHalfMapValidationContext context,
                            Notification notification) {
        Objects.requireNonNull(newHalfMap, "newHalfMap");
        Objects.requireNonNull(existingHalfMap, "existingHalfMap");
        Objects.requireNonNull(context, "context");
        Objects.requireNonNull(notification, "notification");
    }
}
