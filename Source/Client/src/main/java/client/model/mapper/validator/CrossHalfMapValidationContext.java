package client.model.mapper.validator;

import client.model.ModelTextConfig;

/**
 * Derived values used by validations that compare a newly generated half-map
 * against an already existing half-map (the "second" client scenario).
 */
record CrossHalfMapValidationContext(int maxX, int maxY) {
    CrossHalfMapValidationContext {
        if (maxX < 0 || maxY < 0) {
            throw new IllegalArgumentException(ModelTextConfig.ERROR_MAX_XY_NON_NEGATIVE);
        }
    }
}
