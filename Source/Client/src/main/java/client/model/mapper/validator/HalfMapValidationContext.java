package client.model.mapper.validator;

/**
 * Derived values used by half-map validation rules.
 *
 * <p>This context is computed once per validation run to avoid duplicated dimension logic.
 */
record HalfMapValidationContext(int maxX, int maxY) {
    HalfMapValidationContext {
        if (maxX < 0 || maxY < 0) {
            throw new IllegalArgumentException("maxX/maxY must be non-negative");
        }
    }
}
