package client.controller.network.service;

import org.springframework.core.ParameterizedTypeReference;

import messagesbase.ResponseEnvelope;

/**
 * Small abstraction for network calls used by {@link NetworkCenter}.
 *
 * <p>
 * This interface exists to make {@link NetworkCenter} unit-testable without
 * performing real HTTP requests.
 */
interface NetworkCenterHttpClient {

    /**
     * Performs a POST request and returns the decoded response envelope.
     *
     * @param uri Relative URI (e.g., {@code /{gameId}/players}).
     * @param body Request body.
     * @param responseType Target response type.
     * @param <T> Response payload type.
     * @return The response envelope returned by the server.
     */
    <T> ResponseEnvelope<T> post(String uri, Object body, ParameterizedTypeReference<ResponseEnvelope<T>> responseType);

    /**
     * Performs a GET request and returns the decoded response envelope.
     *
     * @param uri Relative URI (e.g., {@code /{gameId}/states/{playerId}}).
     * @param responseType Target response type.
     * @param <T> Response payload type.
     * @return The response envelope returned by the server.
     */
    <T> ResponseEnvelope<T> get(String uri, ParameterizedTypeReference<ResponseEnvelope<T>> responseType);
}
