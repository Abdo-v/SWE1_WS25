package client.model.mapper.validator;

import client.model.ModelTextConfig;

/**
 * Derived values used by half-map validation rules.
 *
 * <p>This context is computed once per validation run to avoid duplicated dimension logic.
 */
record HalfMapValidationContext(int maxX, int maxY) {
    HalfMapValidationContext {
        if (maxX < 0 || maxY < 0) {
            throw new IllegalArgumentException(ModelTextConfig.ERROR_MAX_XY_NON_NEGATIVE);
        }
    }
}
