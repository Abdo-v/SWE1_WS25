package client.main;

import client.exception.ConfigurationException;

import java.util.Objects;
import java.util.Optional;

final class StartupArgumentsParser {

    private StartupArgumentsParser() {
    }

    static StartupArguments parse(String[] args) throws ConfigurationException {
        validateBasicArguments(args);

        String gameMode = args[0];
        String serverBaseUrl = args[1];

        String normalizedMode = validateAndNormalizeGameMode(gameMode);

        if ("ATTR".equals(normalizedMode)) {
            return new StartupArguments(normalizedMode, serverBaseUrl, Optional.empty());
        }

        if (args.length < 3) {
            throw new ConfigurationException(
                    "Game ID required for " + normalizedMode + " mode. Expected: <gameMode> <serverBaseUrl> <gameId>",
                    "arguments",
                    "count=" + args.length,
                    new String[]{"gameMode", "serverBaseUrl", "gameId"}
            );
        }

        return new StartupArguments(normalizedMode, serverBaseUrl, Optional.ofNullable(args[2]));
    }

    private static void validateBasicArguments(String[] args) throws ConfigurationException {
        if (args == null || args.length < 2) {
            int count = args == null ? 0 : args.length;
            throw new ConfigurationException(
                    "Insufficient arguments provided. Expected: <gameMode> <serverBaseUrl>",
                    "arguments",
                    "count=" + count,
                    new String[]{"gameMode", "serverBaseUrl", "[options...]"}
            );
        }
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
