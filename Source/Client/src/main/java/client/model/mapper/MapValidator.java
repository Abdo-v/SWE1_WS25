package client.model.mapper;

import client.model.common.Notification;

import java.util.Optional;

public class MapValidator {

    private static final int HALF_MAP_TOTAL_NODES = HalfMapDimensions.TOTAL_NODES;
    private static final double MIN_MOUNTAIN_PERCENTAGE = 0.10;
    private static final double MIN_GRASS_PERCENTAGE = 0.48;
    private static final double MIN_WATER_PERCENTAGE = 0.14;
    private static final double FORT_PERCENTAGE = 0.02; // Default: 1 fort (castle)

    /**
     * Validates a PlayerHalfMap according to game rules and structural requirements.
     * Performs comprehensive validation including:
     * - Structural integrity (null checks, node count, coordinates)
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
        if (halfMap == null) {
            notification.addError("PlayerHalfMap cannot be null.");
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
        int maxX = bounds.maxX();
        int maxY = bounds.maxY();

        HalfMapTerrainValidator.validateTerrainAndFort(
                halfMap,
                notification,
                HALF_MAP_TOTAL_NODES,
                MIN_MOUNTAIN_PERCENTAGE,
                MIN_GRASS_PERCENTAGE,
                MIN_WATER_PERCENTAGE,
                FORT_PERCENTAGE);
        
        // Only proceed with complex validations if basic structure and terrain are okay
        if (!notification.hasErrors()) {
            HalfMapReachabilityValidator.validateReachability(
                    halfMap,
                    notification,
                    maxX,
                    maxY,
                    MIN_GRASS_PERCENTAGE,
                    MIN_MOUNTAIN_PERCENTAGE);
            HalfMapEdgeValidator.validateEdgeConstraints(halfMap, notification, maxX, maxY);
        }

        return notification;
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
        if (existingHalfMap == null) {
            return notification;
        }

        int[] newDims = HalfMapDimensionUtil.determineDimensions(newHalfMap);
        int[] existingDims = HalfMapDimensionUtil.determineDimensions(existingHalfMap);

        if (newDims[0] != existingDims[0] || newDims[1] != existingDims[1]) {
            notification.addError(String.format(
                    "Half-map dimensions mismatch. New=%dx%d, Existing=%dx%d",
                    newDims[0], newDims[1], existingDims[0], existingDims[1]));
            return notification;
        }

        int maxX = newDims[0] - 1;
        int maxY = newDims[1] - 1;
        HalfMapEdgeValidator.validateEdgeCrossingCompatibility(newHalfMap, existingHalfMap, notification, maxX, maxY);
        return notification;
    }
}