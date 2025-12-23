package client.model.mapper.validator;

/**
 * Validation phases used by {@link MapValidator} to keep fast/basic checks separate from
 * more expensive validations (e.g. flood-fill reachability).
 */
enum HalfMapRulePhase {
    BASIC,
    ADVANCED
}
