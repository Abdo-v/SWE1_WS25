package client.model.mapper;

/**
 * Centralized rule constants shared across map generator and validator.
 *
 * <p>Keeping these values in one place prevents rule/config drift.
 */
public final class MapRules {

    private MapRules() {
    }

    /** Minimum ratio of walkable fields (non-water) required per edge. */
    public static final double MIN_EDGE_WALKABLE_RATIO = 0.40;

    /** Minimum ratio of non-walkable fields (water) required per edge. */
    public static final double MIN_EDGE_BLOCKED_RATIO = 0.20;

    /** Minimum ratio of crossable edge transitions for two compatible half-maps. */
    public static final double MIN_EDGE_CROSSABLE_RATIO = 0.40;
}
