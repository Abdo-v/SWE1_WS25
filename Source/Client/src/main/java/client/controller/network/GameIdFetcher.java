package client.controller.network;

import client.exception.GameCommunicationException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Responsible for fetching game IDs from the game server.
 * This class encapsulates the logic for retrieving unique game identifiers
 * from the server's API endpoint.
 */
public class GameIdFetcher {
    
    /**
     * Fetches the game ID from the server by making an HTTP GET request.
     * Parses the XML response to extract the uniqueGameID value.
     * 
     * @param serverBaseUrl The base URL of the game server
     * @return The unique game ID extracted from the server response
     * @throws GameCommunicationException if the request fails or game ID cannot be extracted
     */
    public static String fetchGameId(String serverBaseUrl) throws GameCommunicationException {
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        
        try {
            // Construct the URL for fetching game ID
            String urlString = serverBaseUrl;
            if (!urlString.endsWith("/")) {
                urlString += "/";
            }
            urlString += "games?enableDummyCompetition=true";
            
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            
            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                throw new GameCommunicationException(
                    "Failed to fetch game ID",
                    serverBaseUrl,
                    "fetchGameId",
                    responseCode
                );
            }
            
            // Read the response
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            
            // Parse the XML to extract uniqueGameID
            String responseText = response.toString();
            String startTag = "<uniqueGameID>";
            String endTag = "</uniqueGameID>";
            
            int startIndex = responseText.indexOf(startTag);
            int endIndex = responseText.indexOf(endTag);
            
            if (startIndex == -1 || endIndex == -1) {
                throw new GameCommunicationException(
                    "Could not find uniqueGameID in server response",
                    serverBaseUrl,
                    "fetchGameId",
                    -1
                );
            }
            
            String gameId = responseText.substring(startIndex + startTag.length(), endIndex).trim();
            
            if (gameId.isEmpty()) {
                throw new GameCommunicationException(
                    "Server returned empty game ID",
                    serverBaseUrl,
                    "fetchGameId",
                    -1
                );
            }
            
            return gameId;
            
        } catch (GameCommunicationException e) {
            throw e;
        } catch (Exception e) {
            throw new GameCommunicationException(
                "Error fetching game ID from server: " + e.getMessage(),
                e,
                serverBaseUrl,
                "fetchGameId",
                -1
            );
        } finally {
            // Clean up resources
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception ignored) {}
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
