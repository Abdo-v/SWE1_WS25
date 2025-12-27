package client.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Unit tests for {@link GameMode}.
 */
class GameModeTest {

    @ParameterizedTest
    @CsvSource({
            "TR,TR",
            "TRR,TRR",
            "ATTR,ATTR",
            " tr ,TR",
            " trr ,TRR",
            "unknown,UNKNOWN",
            "'',UNKNOWN"
    })
    void fromCLIValue_parsesKnownModesAndFallsBack(String input, GameMode expected) {
        assertEquals(expected, GameMode.fromCLIValue(input));
    }
}
