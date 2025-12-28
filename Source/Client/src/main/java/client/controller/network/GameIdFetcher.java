package client.controller.network;

import client.controller.ControllerTextConfig;
import client.exception.GameCommunicationException;
import org.springframework.lang.NonNull;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

/** Fetches a fresh game id from the server's "create/list games" endpoint. */
public class GameIdFetcher {
    
    /**
     * Performs an HTTP GET and extracts {@code <uniqueGameID>...</uniqueGameID>} from the XML response.
     */
    public static String fetchGameId(String serverBaseUrl) throws GameCommunicationException {
        try {
            String urlString = Objects.requireNonNull(serverBaseUrl, ControllerTextConfig.REQUIRE_SERVER_BASE_URL);
            if (!urlString.endsWith("/")) {
                urlString += "/";
            }
            urlString += ControllerTextConfig.FETCH_GAME_ID_QUERY;
            
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            try {
                connection.setRequestMethod(ControllerTextConfig.HTTP_METHOD_GET);
                connection.setConnectTimeout(ControllerTextConfig.HTTP_TIMEOUT_MILLIS);
                connection.setReadTimeout(ControllerTextConfig.HTTP_TIMEOUT_MILLIS);

                int responseCode = connection.getResponseCode();
                if (responseCode != ControllerTextConfig.HTTP_STATUS_OK) {
                    throw new GameCommunicationException(
                        ControllerTextConfig.ERROR_FAILED_FETCH_GAME_ID,
                        serverBaseUrl,
                        ControllerTextConfig.OP_FETCH_GAME_ID,
                        responseCode
                    );
                }

                StringBuilder response = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    reader.lines().forEach(response::append);
                }
            
                return getString(serverBaseUrl, response);

            } finally {
                connection.disconnect();
            }
            
        } catch (GameCommunicationException e) {
            throw e;
        } catch (Exception e) {
            throw new GameCommunicationException(
                ControllerTextConfig.ERROR_FETCH_GAME_ID_PREFIX + e.getMessage(),
                e,
                serverBaseUrl,
                ControllerTextConfig.OP_FETCH_GAME_ID,
                ControllerTextConfig.HTTP_STATUS_UNKNOWN_INT
            );
        }
    }

    @NonNull
    private static String getString(String serverBaseUrl, StringBuilder response) throws GameCommunicationException {
        String responseText = response.toString();
        String startTag = ControllerTextConfig.XML_TAG_GAME_ID_START;
        String endTag = ControllerTextConfig.XML_TAG_GAME_ID_END;

        int startIndex = responseText.indexOf(startTag);
        int endIndex = responseText.indexOf(endTag);

        if (startIndex == -1 || endIndex == -1) {
            throw new GameCommunicationException(
                ControllerTextConfig.ERROR_CANNOT_FIND_GAME_ID_IN_RESPONSE,
                serverBaseUrl,
                ControllerTextConfig.OP_FETCH_GAME_ID,
                ControllerTextConfig.HTTP_STATUS_UNKNOWN_INT
            );
        }

        String gameId = responseText.substring(startIndex + startTag.length(), endIndex).trim();

        if (gameId.isEmpty()) {
            throw new GameCommunicationException(
                ControllerTextConfig.ERROR_SERVER_RETURNED_EMPTY_GAME_ID,
                serverBaseUrl,
                ControllerTextConfig.OP_FETCH_GAME_ID,
                ControllerTextConfig.HTTP_STATUS_UNKNOWN_INT
            );
        }
        return gameId;
    }
}
