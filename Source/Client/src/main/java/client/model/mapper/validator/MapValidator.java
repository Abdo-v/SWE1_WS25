package client.model.mapper.validator;

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

    /**
     * Creates a MapValidator with the default rule set.
     */
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
        this.halfMapRules = List.copyOf(Objects.requireNonNull(halfMapRules, "halfMapRules"));
        this.crossHalfMapRules = List.copyOf(Objects.requireNonNull(crossHalfMapRules, "crossHalfMapRules"));
    }

    /**
     * Validates a PlayerHalfMap according to game rules and structural requirements.
     * Performs comprehensive validation including:
     * - Structural integrity (presence checks, node count, coordinates)
     * - Map dimensions (WIDTHxHEIGHT or HEIGHTxWIDTH)
     * - Terrain distribution (minimum percentages for each terrain type)
     * - Fort placement and count
     * - Reachability of all walkable nodes
     * - Edge walkability requirements
     *
     * @param halfMap The PlayerHalfMap to validate
     * @return A Notification object containing any validation errors found
     */
    public Notification validate(PlayerHalfMap halfMap) {
        Notification notification = new Notification();
        if (Optional.ofNullable(halfMap).isEmpty()) {
            notification.addError("PlayerHalfMap must be provided.");
            return notification;
        }

        Optional<HalfMapBounds> boundsOpt = HalfMapStructureValidator.validateAndGetBounds(
                halfMap,
                notification,
                HALF_MAP_TOTAL_NODES);

        if (boundsOpt.isEmpty()) {
            return notification;
        }

        HalfMapBounds bounds = boundsOpt.get();
        HalfMapValidationContext context = new HalfMapValidationContext(bounds.maxX(), bounds.maxY());

        runHalfMapRules(HalfMapRulePhase.BASIC, halfMap, context, notification);

        // Only proceed with expensive validations if basic rules are okay.
        if (!notification.hasErrors()) {
            runHalfMapRules(HalfMapRulePhase.ADVANCED, halfMap, context, notification);
        }

        return notification;
    }

    private void runHalfMapRules(
            HalfMapRulePhase phase,
            PlayerHalfMap halfMap,
            HalfMapValidationContext context,
            Notification notification
    ) {
        Objects.requireNonNull(phase, "phase");
        for (HalfMapValidationRule rule : halfMapRules) {
            if (rule.phase() == phase) {
                rule.validate(halfMap, context, notification);
            }
        }
    }

    /**
     * Validates a PlayerHalfMap and also checks edge-crossing compatibility with an existing half-map.
     * This is intended for the client that generates the second half-map.
     *
     * Rule: For each edge of the new half-map, at least {@link MapRules#MIN_EDGE_CROSSABLE_RATIO} of edge fields must allow a successful
     * transition to the corresponding opposite edge of the existing half-map (walkable on both sides).
     *
     * Pairings checked:
     * - new LEFT  (x=0)      vs existing RIGHT (x=maxX)
     * - new RIGHT (x=maxX)   vs existing LEFT  (x=0)
     * - new TOP   (y=0)      vs existing BOTTOM(y=maxY)
     * - new BOTTOM(y=maxY)   vs existing TOP   (y=0)
     */
    public Notification validate(PlayerHalfMap newHalfMap, PlayerHalfMap existingHalfMap) {
        Notification notification = validate(newHalfMap);
        if (notification.hasErrors()) {
            return notification;
        }

        Optional<PlayerHalfMap> existingHalfMapOpt = Optional.ofNullable(existingHalfMap);
        if (existingHalfMapOpt.isEmpty()) {
            return notification;
        }

        PlayerHalfMap existing = existingHalfMapOpt.get();

        int[] newDims = HalfMapDimensionUtil.determineDimensions(newHalfMap);
        int[] existingDims = HalfMapDimensionUtil.determineDimensions(existing);

        if (newDims[0] != existingDims[0] || newDims[1] != existingDims[1]) {
            notification.addError(String.format(
                    "Half-map dimensions mismatch. New=%dx%d, Existing=%dx%d",
                    newDims[0], newDims[1], existingDims[0], existingDims[1]));
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
