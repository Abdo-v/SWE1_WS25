package client.main;

import java.util.Objects;
import java.util.Optional;

record StartupArguments(String gameMode, String serverBaseUrl, Optional<String> gameId) {

    StartupArguments {
        Objects.requireNonNull(gameMode, "gameMode is required");
        Objects.requireNonNull(serverBaseUrl, "serverBaseUrl is required");
        Objects.requireNonNull(gameId, "gameId is required");
    }

    boolean autoFetchGameId() {
        return "ATTR".equals(gameMode);
    }
}
