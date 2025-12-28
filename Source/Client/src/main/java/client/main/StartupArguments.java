package client.main;

import java.util.Objects;
import java.util.Optional;

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
