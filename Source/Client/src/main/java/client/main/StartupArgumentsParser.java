package client.main;

import client.exception.ConfigurationException;

import java.util.Objects;
import java.util.Optional;

final class StartupArgumentsParser {

    private StartupArgumentsParser() {
    }

    static StartupArguments parse(String[] args) throws ConfigurationException {
        String[] safeArgs = validateBasicArguments(args);

        String gameMode = safeArgs[0];
        String serverBaseUrl = safeArgs[1];

        String normalizedMode = validateAndNormalizeGameMode(gameMode);

        if ("ATTR".equals(normalizedMode)) {
            return new StartupArguments(normalizedMode, serverBaseUrl, Optional.empty());
        }

        if (safeArgs.length < 3) {
            throw new ConfigurationException(
                    "Game ID required for " + normalizedMode + " mode. Expected: <gameMode> <serverBaseUrl> <gameId>",
                    "arguments",
                    "count=" + safeArgs.length,
                    new String[]{"gameMode", "serverBaseUrl", "gameId"}
            );
        }

        return new StartupArguments(normalizedMode, serverBaseUrl, Optional.ofNullable(safeArgs[2]));
    }

    private static String[] validateBasicArguments(String[] args) throws ConfigurationException {
        String[] safeArgs = Optional.ofNullable(args).orElseThrow(() -> new ConfigurationException(
                "Arguments are missing. Expected: <gameMode> <serverBaseUrl>",
                "arguments",
                "missing",
                new String[]{"gameMode", "serverBaseUrl", "[options...]"}
        ));

        if (safeArgs.length < 2) {
            throw new ConfigurationException(
                    "Insufficient arguments provided. Expected: <gameMode> <serverBaseUrl>",
                    "arguments",
                    "count=" + safeArgs.length,
                    new String[]{"gameMode", "serverBaseUrl", "[options...]"}
            );
        }

        return safeArgs;
    }

    private static String validateAndNormalizeGameMode(String gameMode) throws ConfigurationException {
        String normalizedMode = Objects.requireNonNullElse(gameMode, "").trim();
        if (normalizedMode.isEmpty()) {
            throw new ConfigurationException(
                    "Game mode is required",
                    "gameMode",
                    normalizedMode,
                    new String[]{"TR", "TRR", "ATTR"}
            );
        }

        String[] validModes = {"TR", "TRR", "ATTR"};
        for (String validMode : validModes) {
            if (validMode.equals(normalizedMode)) {
                return normalizedMode;
            }
        }

        throw new ConfigurationException(
                "Invalid game mode provided",
                "gameMode",
                normalizedMode,
                validModes
        );
    }
}
