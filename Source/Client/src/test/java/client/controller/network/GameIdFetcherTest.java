package client.controller.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import client.exception.GameCommunicationException;

/**
 * Unit tests for {@link GameIdFetcher}.
 *
 * <p>Uses a local in-memory HTTP server to keep tests deterministic and avoid external dependencies.
 */
class GameIdFetcherTest {

    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void fetchGameId_whenResponseContainsUniqueGameId_returnsExtractedId() throws Exception {
        server = startServer(200, "<root><uniqueGameID>abc-123</uniqueGameID></root>");

        String baseUrlWithoutTrailingSlash = "http://localhost:" + server.getAddress().getPort();
        String id = GameIdFetcher.fetchGameId(baseUrlWithoutTrailingSlash);

        assertEquals("abc-123", id);
    }

    @Test
    void fetchGameId_whenUniqueGameIdMissing_throwsGameCommunicationException() throws Exception {
        server = startServer(200, "<root><nope>value</nope></root>");

        String baseUrl = "http://localhost:" + server.getAddress().getPort() + "/";
        GameCommunicationException ex = assertThrows(
                GameCommunicationException.class,
                () -> GameIdFetcher.fetchGameId(baseUrl)
        );
        assertTrue(ex.getMessage().toLowerCase().contains("uniquegameid"));
    }

    @Test
    void fetchGameId_whenUniqueGameIdEmpty_throwsGameCommunicationException() throws Exception {
        server = startServer(200, "<root><uniqueGameID>   </uniqueGameID></root>");

        String baseUrl = "http://localhost:" + server.getAddress().getPort();
        GameCommunicationException ex = assertThrows(
                GameCommunicationException.class,
                () -> GameIdFetcher.fetchGameId(baseUrl)
        );
        assertTrue(ex.getMessage().toLowerCase().contains("empty game id"));
    }

    @Test
    void fetchGameId_whenServerReturnsNon200_throwsGameCommunicationExceptionWithStatusCode() throws Exception {
        server = startServer(503, "service unavailable");

        String baseUrl = "http://localhost:" + server.getAddress().getPort();
        GameCommunicationException ex = assertThrows(
                GameCommunicationException.class,
                () -> GameIdFetcher.fetchGameId(baseUrl)
        );
        assertEquals(503, ex.getHttpStatusCode());
    }

    private static HttpServer startServer(int statusCode, String body) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/games", new FixedResponseHandler(statusCode, body));
        server.start();
        return server;
    }

    private static final class FixedResponseHandler implements HttpHandler {
        private final int statusCode;
        private final byte[] bytes;

        private FixedResponseHandler(int statusCode, String body) {
            this.statusCode = statusCode;
            this.bytes = body.getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Content-Type", "text/xml; charset=utf-8");
            exchange.sendResponseHeaders(statusCode, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }
}
