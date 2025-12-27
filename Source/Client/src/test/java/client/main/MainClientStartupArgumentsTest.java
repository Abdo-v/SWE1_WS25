package client.main;

import client.exception.ConfigurationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests {@link StartupArgumentsParser} validation and error reporting for CLI startup arguments.
 */
class StartupArgumentsParserTest {

    @Test
    void parseStartupArguments_rejectsTooFewArguments() {
        ConfigurationException ex = assertThrows(
                ConfigurationException.class,
            () -> StartupArgumentsParser.parse(new String[]{"TR"})
        );

        assertTrue(ex.getMessage().contains("Insufficient arguments"));
        assertEquals("arguments", ex.getConfigurationKey().orElseThrow());
        assertEquals("count=1", ex.getProvidedValue().orElseThrow());
        assertArrayEquals(new String[]{"gameMode", "serverBaseUrl", "[options...]"}, ex.getValidValues());
    }

    @Test
    void parseStartupArguments_rejectsBlankGameMode() {
        ConfigurationException ex = assertThrows(
                ConfigurationException.class,
            () -> StartupArgumentsParser.parse(new String[]{"   ", "http://localhost:8080", "id"})
        );

        assertTrue(ex.getMessage().contains("Game mode is required"));
        assertEquals("gameMode", ex.getConfigurationKey().orElseThrow());
        assertArrayEquals(new String[]{"TR", "TRR", "ATTR"}, ex.getValidValues());
    }

    @Test
    void parseStartupArguments_rejectsNullGameMode() {
        ConfigurationException ex = assertThrows(
                ConfigurationException.class,
            () -> StartupArgumentsParser.parse(new String[]{null, "http://localhost:8080", "id"})
        );

        assertEquals("gameMode", ex.getConfigurationKey().orElseThrow());
    }

    @Test
    void parseStartupArguments_rejectsInvalidGameMode() {
        ConfigurationException ex = assertThrows(
                ConfigurationException.class,
            () -> StartupArgumentsParser.parse(new String[]{"INVALID", "http://localhost:8080", "id"})
        );

        assertTrue(ex.getMessage().contains("Invalid game mode"));
        assertEquals("gameMode", ex.getConfigurationKey().orElseThrow());
        assertEquals("INVALID", ex.getProvidedValue().orElseThrow());
        assertArrayEquals(new String[]{"TR", "TRR", "ATTR"}, ex.getValidValues());
    }

    @Test
    void parseStartupArguments_trModeRequiresGameId() {
        ConfigurationException ex = assertThrows(
                ConfigurationException.class,
            () -> StartupArgumentsParser.parse(new String[]{"TR", "http://localhost:8080"})
        );

        assertTrue(ex.getMessage().contains("Game ID required for TR mode"));
        assertEquals("arguments", ex.getConfigurationKey().orElseThrow());
        assertEquals("count=2", ex.getProvidedValue().orElseThrow());
        assertArrayEquals(new String[]{"gameMode", "serverBaseUrl", "gameId"}, ex.getValidValues());
    }

    @Test
    void parseStartupArguments_attrModeDoesNotRequireGameId() {
        StartupArguments startup = StartupArgumentsParser.parse(
                new String[]{"ATTR", "http://localhost:8080"}
        );

        assertEquals("ATTR", startup.gameMode());
        assertEquals("http://localhost:8080", startup.serverBaseUrl());
        assertTrue(startup.autoFetchGameId());
        assertTrue(startup.gameId().isEmpty());
    }

    @Test
    void parseStartupArguments_trModeParsesGameId() {
        StartupArguments startup = StartupArgumentsParser.parse(
                new String[]{"TR", "http://localhost:8080", "Ew26i"}
        );

        assertEquals("TR", startup.gameMode());
        assertEquals("http://localhost:8080", startup.serverBaseUrl());
        assertFalse(startup.autoFetchGameId());
        assertEquals("Ew26i", startup.gameId().orElseThrow());
    }
}
