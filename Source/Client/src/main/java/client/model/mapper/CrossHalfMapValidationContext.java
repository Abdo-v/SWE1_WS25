package client.model.mapper;

/**
 * Derived values used by validations that compare a newly generated half-map
 * against an already existing half-map (the "second" client scenario).
 */
record CrossHalfMapValidationContext(int maxX, int maxY) {
    CrossHalfMapValidationContext {
        if (maxX < 0 || maxY < 0) {
            throw new IllegalArgumentException("maxX/maxY must be non-negative");
        }
    }
}
