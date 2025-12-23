package client.model.mapper.validator;

import client.model.common.Notification;
import client.model.mapper.PlayerHalfMap;

import java.util.Objects;

/**
 * A single half-map business rule.
 */
interface HalfMapValidationRule {

    HalfMapRulePhase phase();

    void validate(PlayerHalfMap halfMap, HalfMapValidationContext context, Notification notification);

    default void requireArgs(PlayerHalfMap halfMap, HalfMapValidationContext context, Notification notification) {
        Objects.requireNonNull(halfMap, "halfMap");
        Objects.requireNonNull(context, "context");
        Objects.requireNonNull(notification, "notification");
    }
}
