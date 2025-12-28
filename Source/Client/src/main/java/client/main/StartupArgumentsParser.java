package client.main;

import client.exception.ConfigurationException;
import java.util.Objects;
import java.util.Optional;

final class StartupArgumentsParser {

    private static final String GAME_MODE = MainTextConfig.ARG_GAME_MODE;
    private static final String SERVER_BASE_URL = MainTextConfig.ARG_SERVER_BASE_URL;
    private static final String GAME_ID = MainTextConfig.ARG_GAME_ID;

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
                    MainTextConfig.ERROR_GAME_ID_REQUIRED_PREFIX + normalizedMode + MainTextConfig.ERROR_GAME_ID_REQUIRED_SUFFIX,
                    MainTextConfig.FIELD_ARGUMENTS,
                    MainTextConfig.DETAIL_COUNT_PREFIX + safeArgs.length,
                    new String[]{GAME_MODE, SERVER_BASE_URL, GAME_ID}
            );
        }

        String gameId = requireArg(safeArgs, 2, GAME_ID);
        return new StartupArguments(normalizedMode, serverBaseUrl, Optional.of(gameId));
    }

    private static String[] validateBasicArguments(String[] args) throws ConfigurationException {
        String[] safeArgs;
        try {
            safeArgs = Objects.requireNonNull(args, MainTextConfig.ERROR_ARGS_MISSING_EXPECTED_MODE_URL);
        } catch (NullPointerException e) {
            throw new ConfigurationException(
                    MainTextConfig.ERROR_ARGS_MISSING_EXPECTED_MODE_URL,
                    MainTextConfig.FIELD_ARGUMENTS,
                    MainTextConfig.DETAIL_MISSING,
                    new String[]{GAME_MODE, SERVER_BASE_URL, MainTextConfig.ARG_OPTIONS}
            );
        }

        if (safeArgs.length < 2) {
            throw new ConfigurationException(
                    MainTextConfig.ERROR_ARGS_INSUFFICIENT_EXPECTED_MODE_URL,
                    MainTextConfig.FIELD_ARGUMENTS,
                    MainTextConfig.DETAIL_COUNT_PREFIX + safeArgs.length,
                    new String[]{GAME_MODE, SERVER_BASE_URL, MainTextConfig.ARG_OPTIONS}
            );
        }

        return safeArgs;
    }

    private static String validateAndNormalizeGameMode(String gameMode) throws ConfigurationException {
        String normalizedMode;
        try {
            normalizedMode = Objects.requireNonNull(gameMode, MainTextConfig.ERROR_GAME_MODE_REQUIRED).trim();
        } catch (NullPointerException e) {
            normalizedMode = "";
        }
        if (normalizedMode.isEmpty()) {
            throw new ConfigurationException(
                    MainTextConfig.ERROR_GAME_MODE_REQUIRED,
                    GAME_MODE,
                    normalizedMode,
                    MainTextConfig.VALID_GAME_MODES.toArray(String[]::new)
            );
        }

        for (String validMode : MainTextConfig.VALID_GAME_MODES) {
            if (validMode.equals(normalizedMode)) {
                return normalizedMode;
            }
        }

        throw new ConfigurationException(
                MainTextConfig.ERROR_GAME_MODE_INVALID,
                GAME_MODE,
                normalizedMode,
                MainTextConfig.VALID_GAME_MODES.toArray(String[]::new)
        );
    }

    private static String requireArg(String[] args, int index, String argName) throws ConfigurationException {
        String requiredMessage = MainTextConfig.requiredArgumentMessage(argName);
        String value;
        try {
            value = Objects.requireNonNull(args[index], requiredMessage).trim();
        } catch (NullPointerException e) {
            throw new ConfigurationException(
                    requiredMessage,
                    argName,
                    MainTextConfig.DETAIL_MISSING,
                    new String[]{argName}
            );
        }

        if (value.isEmpty()) {
            throw new ConfigurationException(
                    requiredMessage,
                    argName,
                    MainTextConfig.DETAIL_MISSING,
                    new String[]{argName}
            );
        }
        return value;
    }
}
