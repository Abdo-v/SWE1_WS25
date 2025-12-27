package client.view;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests {@link ValidationInternalsKind} classification based on stack frames and error messages.
 */
class ValidationInternalsKindTest {

    @ParameterizedTest
    @MethodSource("cases")
    void from_classNameOrMessage_classifiesKind(Optional<StackTraceElement> frame, String message, ValidationInternalsKind expected) {
        assertEquals(expected, ValidationInternalsKind.from(frame, message));
    }

    static Stream<Arguments> cases() {
        return Stream.of(
                Arguments.of(Optional.of(new StackTraceElement("client.model.mapper.validator.HalfMapStructureValidator", "validate", "x", 1)), "", ValidationInternalsKind.STRUCTURE),
                Arguments.of(Optional.of(new StackTraceElement("client.model.mapper.validator.HalfMapTerrainValidator", "validate", "x", 1)), "", ValidationInternalsKind.TERRAIN_AND_FORT),
                Arguments.of(Optional.of(new StackTraceElement("client.model.mapper.validator.HalfMapReachabilityValidator", "validate", "x", 1)), "", ValidationInternalsKind.REACHABILITY),
                Arguments.of(Optional.of(new StackTraceElement("client.model.mapper.validator.HalfMapEdgeValidator", "validate", "x", 1)), "", ValidationInternalsKind.EDGE_CONSTRAINTS),
                Arguments.of(Optional.of(new StackTraceElement("client.model.mapper.validator.MapValidator", "validate", "x", 1)), "", ValidationInternalsKind.ORCHESTRATION),

                Arguments.of(Optional.empty(), "PlayerHalfMap must be provided.", ValidationInternalsKind.PRECONDITION),
                Arguments.of(Optional.empty(), "Invalid map dimensions. Expected 10x5", ValidationInternalsKind.DIMENSIONS),
                Arguments.of(Optional.empty(), "Some unrelated validation error", ValidationInternalsKind.VALIDATION)
        );
    }
}