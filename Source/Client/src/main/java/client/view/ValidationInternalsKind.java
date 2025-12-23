package client.view;

/**
 * Coarse error kinds for the technical-internals view during map generation.
 *
 * <p>This is intentionally a view-level concept: it exists to group validation
 * errors for human-friendly display on {@link System#err}.
 */
public enum ValidationInternalsKind {
    PRECONDITION("PRECONDITION"),
    STRUCTURE("STRUCTURE"),
    DIMENSIONS("DIMENSIONS"),
    TERRAIN_AND_FORT("TERRAIN_AND_FORT"),
    REACHABILITY("REACHABILITY"),
    EDGE_CONSTRAINTS("EDGE_CONSTRAINTS"),
    ORCHESTRATION("ORCHESTRATION"),
    VALIDATION("VALIDATION");

    private final String displayText;

    ValidationInternalsKind(String displayText) {
        this.displayText = displayText;
    }

    public String displayText() {
        return displayText;
    }

    public static ValidationInternalsKind from(java.util.Optional<StackTraceElement> referenceFrame, String message) {
        java.util.Optional<StackTraceElement> safeFrame = java.util.Optional.ofNullable(referenceFrame).orElse(java.util.Optional.empty());
        if (safeFrame.isPresent()) {
            String className = String.valueOf(safeFrame.orElseThrow().getClassName());
            if (className.contains("HalfMapStructureValidator")) {
                return STRUCTURE;
            }
            if (className.contains("HalfMapTerrainValidator")) {
                return TERRAIN_AND_FORT;
            }
            if (className.contains("HalfMapReachabilityValidator")) {
                return REACHABILITY;
            }
            if (className.contains("HalfMapEdgeValidator")) {
                return EDGE_CONSTRAINTS;
            }
            if (className.contains("MapValidator")) {
                return ORCHESTRATION;
            }
        }

        String lower = java.util.Objects.requireNonNullElse(message, "").toLowerCase();
        if (lower.contains("must be provided") || lower.contains("missing") || lower.contains("required")) {
            return PRECONDITION;
        }
        if (lower.contains("dimension") || lower.contains("width") || lower.contains("height")) {
            return DIMENSIONS;
        }

        return VALIDATION;
    }
}
