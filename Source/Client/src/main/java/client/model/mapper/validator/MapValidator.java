package client.model.mapper.validator;

import client.model.ModelTextConfig;
import client.model.common.Notification;
import client.model.mapper.HalfMapDimensions;
import client.model.mapper.MapRules;
import client.model.mapper.PlayerHalfMap;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Validates half-maps against map-related business rules.
 *
 * <p>The concrete business rules are implemented as rule objects and orchestrated in phases
 * (basic vs. advanced) to support the Open-Closed Principle.
 */
public class MapValidator {

    private static final int HALF_MAP_TOTAL_NODES = HalfMapDimensions.TOTAL_NODES;
    private static final double MIN_MOUNTAIN_PERCENTAGE = 0.10;
    private static final double MIN_GRASS_PERCENTAGE = 0.48;
    private static final double MIN_WATER_PERCENTAGE = 0.14;
    private static final double FORT_PERCENTAGE = 0.02; // Default: 1 fort (castle)

    private final List<HalfMapValidationRule> halfMapRules;
    private final List<CrossHalfMapValidationRule> crossHalfMapRules;

    public MapValidator() {
        this(
                MapValidationRuleSets.defaultHalfMapRules(
                        HALF_MAP_TOTAL_NODES,
                        MIN_MOUNTAIN_PERCENTAGE,
                        MIN_GRASS_PERCENTAGE,
                        MIN_WATER_PERCENTAGE,
                        FORT_PERCENTAGE),
                MapValidationRuleSets.defaultCrossHalfMapRules()
        );
    }

    /**
     * Creates a MapValidator with injected rule sets.
     *
     * <p>This constructor is intentionally package-private: rules are an internal extension point
     * of the validator module.
     */
    private MapValidator(List<HalfMapValidationRule> halfMapRules, List<CrossHalfMapValidationRule> crossHalfMapRules) {
        this.halfMapRules = List.copyOf(Objects.requireNonNull(halfMapRules, ModelTextConfig.REQUIRE_HALF_MAP_RULES));
        this.crossHalfMapRules = List.copyOf(Objects.requireNonNull(crossHalfMapRules, ModelTextConfig.REQUIRE_CROSS_HALF_MAP_RULES));
    }

    /**
     * Validates a single half-map.
     *
     * <p>Runs a cheap/basic phase first and only executes more expensive checks (e.g. reachability)
     * if the basic phase reports no errors.
     */
    public Notification validate(PlayerHalfMap halfMap) {
        Notification notification = new Notification();

        final PlayerHalfMap requiredHalfMap;
        try {
            requiredHalfMap = Objects.requireNonNull(halfMap);
        } catch (NullPointerException e) {
            notification.addError(ModelTextConfig.ERROR_PLAYER_HALF_MAP_REQUIRED);
            return notification;
        }

        Optional<HalfMapBounds> boundsOpt = HalfMapStructureValidator.validateAndGetBounds(
                requiredHalfMap,
                notification,
                HALF_MAP_TOTAL_NODES);

        if (boundsOpt.isEmpty()) {
            return notification;
        }

        HalfMapBounds bounds = boundsOpt.get();
        HalfMapValidationContext context = new HalfMapValidationContext(bounds.maxX(), bounds.maxY());

        runHalfMapRules(HalfMapRulePhase.BASIC, requiredHalfMap, context, notification);

        // Only proceed with expensive validations if basic rules are okay.
        if (!notification.hasErrors()) {
            runHalfMapRules(HalfMapRulePhase.ADVANCED, requiredHalfMap, context, notification);
        }

        return notification;
    }

    private void runHalfMapRules(
            HalfMapRulePhase phase,
            PlayerHalfMap halfMap,
            HalfMapValidationContext context,
            Notification notification
    ) {
        Objects.requireNonNull(phase, ModelTextConfig.REQUIRE_PHASE);
        for (HalfMapValidationRule rule : halfMapRules) {
            if (rule.phase() == phase) {
                rule.validate(halfMap, context, notification);
            }
        }
    }

    /**
     * Validates a half-map and (if valid) checks whether it is edge-compatible with an existing half-map.
     *
     * <p>Used when generating the "second" half-map: at least {@link MapRules#MIN_EDGE_CROSSABLE_RATIO}
     * of each edge must be crossable (walkable on both sides).
     */
    public Notification validate(PlayerHalfMap newHalfMap, PlayerHalfMap existingHalfMap) {
        Notification notification = validate(newHalfMap);
        if (notification.hasErrors()) {
            return notification;
        }

        final PlayerHalfMap existing;
        try {
            existing = Objects.requireNonNull(existingHalfMap);
        } catch (NullPointerException e) {
            return notification;
        }

        int[] newDims = HalfMapDimensionUtil.determineDimensions(newHalfMap);
        int[] existingDims = HalfMapDimensionUtil.determineDimensions(existing);

        if (newDims[0] != existingDims[0] || newDims[1] != existingDims[1]) {
            notification.addError(String.format(
                    "Half-map dimensions mismatch. New=%dx%d, Existing=%dx%d",
                    newDims[0],
                    newDims[1],
                    existingDims[0],
                    existingDims[1]
            ));
            return notification;
        }

        int maxX = newDims[0] - 1;
        int maxY = newDims[1] - 1;

        CrossHalfMapValidationContext context = new CrossHalfMapValidationContext(maxX, maxY);
        for (CrossHalfMapValidationRule rule : crossHalfMapRules) {
            rule.validate(newHalfMap, existing, context, notification);
        }
        return notification;
    }
}
