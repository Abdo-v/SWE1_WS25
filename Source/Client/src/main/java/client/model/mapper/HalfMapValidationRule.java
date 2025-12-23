package client.model.mapper;

import client.model.common.Notification;

import java.util.Objects;

/**
 * A single half-map business rule.
 *
 * <p>Rules are designed to follow the Open-Closed Principle (OCP):
 * add a new rule by introducing a new implementation and registering it, without
 * modifying the {@link MapValidator} orchestration.
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
