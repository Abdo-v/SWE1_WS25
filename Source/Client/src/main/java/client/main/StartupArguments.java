package client.main;

import java.util.Objects;
import java.util.Optional;

/**
 * Immutable, validated startup configuration derived from CLI arguments.
 *
 * <p>Invariant: for {@code ATTR} mode, {@link #gameId()} is {@link Optional#empty()} because the
 * id is fetched from the server.
 */
record StartupArguments(String gameMode, String serverBaseUrl, Optional<String> gameId) {

    StartupArguments {
        Objects.requireNonNull(gameMode, MainTextConfig.REQUIRE_GAME_MODE);
        Objects.requireNonNull(serverBaseUrl, MainTextConfig.REQUIRE_SERVER_BASE_URL);
        Objects.requireNonNull(gameId, MainTextConfig.REQUIRE_GAME_ID);
    }

    boolean autoFetchGameId() {
        return "ATTR".equals(gameMode);
    }
}
